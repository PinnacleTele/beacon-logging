package com.itextos.beacon.inmemory.loader;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfo;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.IInmemoryProcess;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class InmemoryLoaderCollection
        implements
        ITimedProcess
{

    private static final Log    log                              = LogFactory.getLog(InmemoryLoaderCollection.class);

    private static final int    COL_INDEX_INMEMORY_ID            = 1;
    private static final int    COL_INDEX_DESCRIPTION            = 2;
    private static final int    COL_INDEX_JNDI_INFO_ID           = 3;
    private static final int    COL_INDEX_SQL                    = 4;
    private static final int    COL_INDEX_AUTO_REFRESH_REQ       = 5;
    private static final int    COL_INDEX_SLEEP_TIME_IN_SEC      = 6;
    private static final int    COL_INDEX_INMEMORY_PROCESS_CLASS = 7;

    private static final String JNDI_INDI_SQL                    = "select * from inmemory_loader_config";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final InmemoryLoaderCollection INSTANCE = new InmemoryLoaderCollection();

    }

    public static InmemoryLoaderCollection getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private boolean                                 mCanContinue               = true;
    private Map<InmemoryId, InmemoryInput>          mInMemoryInputCollection   = new EnumMap<>(InmemoryId.class);
    private final Map<InmemoryId, IInmemoryProcess> mInMemoryProcessCollection = new EnumMap<>(InmemoryId.class);

    private InmemoryLoaderCollection()
    {

        try
        {
            loadInmemoryInfoFromDB();
        }
        catch (final Exception e)
        {
            final String s = "Exception while loading inmemory input information from DB";
            log.error(s, e);
            //throw new ItextosRuntimeException(s, e);
        }
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {

        try
        {
            loadInmemoryInfoFromDB();
        }
        catch (final Exception e)
        {
            log.error("IGNORABLE Exception while reloading inmemory input information from DB.", e);
        }
        return false;
    }

    private void loadInmemoryInfoFromDB()
            throws Exception
    {
        final Map<InmemoryId, InmemoryInput> tempMemoryInputCollection = new EnumMap<>(InmemoryId.class);

        try (
                Connection con = DBDataSourceFactory.getConnection(JndiInfo.CONFIGURARION_DB);
                PreparedStatement pstmt = con.prepareStatement(JNDI_INDI_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = pstmt.executeQuery();)
        {

            while (resultSet.next())
            {
                final InmemoryInput inmemoryInput = new InmemoryInput(resultSet.getString(COL_INDEX_INMEMORY_ID), resultSet.getString(COL_INDEX_DESCRIPTION), resultSet.getInt(COL_INDEX_JNDI_INFO_ID),
                        resultSet.getString(COL_INDEX_SQL), CommonUtility.isEnabled(CommonUtility.nullCheck(resultSet.getString(COL_INDEX_AUTO_REFRESH_REQ), true)),
                        resultSet.getInt(COL_INDEX_SLEEP_TIME_IN_SEC), resultSet.getString(COL_INDEX_INMEMORY_PROCESS_CLASS));

                if (inmemoryInput.getInmemoryId() == null)
                {
                    // Dont try to add this entry, as it will spoil the other process.
                    log.error("Unable to get the Inmemory id for '" + resultSet.getString(COL_INDEX_INMEMORY_ID) + "'. Skipping it.");
                    continue;
                }

                tempMemoryInputCollection.put(inmemoryInput.getInmemoryId(), inmemoryInput);
            }

            final Set<InmemoryId> oldKeys = mInMemoryInputCollection.keySet();
            final Set<InmemoryId> newKeys = tempMemoryInputCollection.keySet();

            oldKeys.removeAll(newKeys);

            if (!oldKeys.isEmpty())
                removeTheExistingInmemoryCollection(oldKeys);

            mInMemoryInputCollection = tempMemoryInputCollection;
        }
        catch (final Exception e)
        {
            throw e;
        }
    }

    private void removeTheExistingInmemoryCollection(
            Set<InmemoryId> aOldKeys)
    {
        for (final InmemoryId inmemoryId : aOldKeys)
            try
            {
                final IInmemoryProcess lIInmemoryProcess = mInMemoryProcessCollection.remove(inmemoryId);

                if (lIInmemoryProcess instanceof AbstractAutoRefreshInMemoryProcessor)
                    ((ITimedProcess) lIInmemoryProcess).stopMe();
            }
            catch (final Exception e)
            {
                log.error("Exception while stopping the inmemory collection " + inmemoryId, e);
            }
    }

    public IInmemoryProcess getInmemoryCollection(
            InmemoryId aInmemoryId)
    {
        IInmemoryProcess lIInmemoryProcess = mInMemoryProcessCollection.get(aInmemoryId);

        if (lIInmemoryProcess != null)
            return lIInmemoryProcess;

        // The Inmemory object is not available.
        // Try to create a new object.
        // Let the other threads wait for the object to be created.
        synchronized (mInMemoryProcessCollection)
        {
            // Once the lock is released, try to get the inmemory object again.
            // If it is still not available then it will be some serious issue.
            lIInmemoryProcess = mInMemoryProcessCollection.get(aInmemoryId);
            if (lIInmemoryProcess != null)
                return lIInmemoryProcess;

            final InmemoryInput inmemoryInput = mInMemoryInputCollection.get(aInmemoryId);

            if (inmemoryInput == null)
            {
                log.error("No inmemory input configuration information available for the inmemory id " + aInmemoryId);
                return null;
            }

            try
            {
                final long             start            = System.currentTimeMillis();
                final Class<?>         classObj         = Class.forName(inmemoryInput.getInmemoryProcessClassName());
                final Constructor<?>   constructor      = classObj.getConstructor(InmemoryInput.class);
                final IInmemoryProcess iInmemoryProcess = (IInmemoryProcess) constructor.newInstance(inmemoryInput);
                iInmemoryProcess.getDataFromDB();
                mInMemoryProcessCollection.put(aInmemoryId, iInmemoryProcess);
                final long end = System.currentTimeMillis();

                if (log.isDebugEnabled())
                    log.debug("Time taken to load initial data for " + aInmemoryId + " class " + inmemoryInput.getInmemoryProcessClassName() + " is " + (end - start) + " millis");

                return iInmemoryProcess;
            }
            catch (final Throwable e)
            {
                log.error("Exception while loading the Inmemory processs class with input values " + inmemoryInput, e);
            }
        }
        return null;
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
        removeTheExistingInmemoryCollection(mInMemoryProcessCollection.keySet());
    }

}