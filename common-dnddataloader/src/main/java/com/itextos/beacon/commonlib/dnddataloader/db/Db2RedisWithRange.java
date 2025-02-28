package com.itextos.beacon.commonlib.dnddataloader.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.dnddataloader.common.DndInfo;
import com.itextos.beacon.commonlib.dnddataloader.common.InMemoryDataHolder;
import com.itextos.beacon.commonlib.dnddataloader.enums.DndAction;

public class Db2RedisWithRange
        implements
        Runnable
{

    private static final Log log                    = LogFactory.getLog(Db2RedisWithRange.class);

    private final long       startNumber;
    private final long       endNumber;
    private boolean          hasAllThreadsCompleted = false;
    private final String     name;

    /**
     * @param aStartNumber
     * @param aEndNumber
     */
    public Db2RedisWithRange(
            long aStartNumber,
            long aEndNumber)
    {
        startNumber = aStartNumber;
        endNumber   = aEndNumber;
        name        = startNumber + "^" + endNumber;
    }

    /**
     *
     */
    @Override
    public void run()
    {
        if (log.isDebugEnabled())
            log.debug("Started processing records for " + startNumber + " and " + endNumber);

        try (
                Connection con = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfo(Db2RedisThreadBased.DND_JNDI_INFO));
                PreparedStatement pstmtSelect = con.prepareStatement(Db2RedisThreadBased.SQL_TO_SELECT, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);)
        {
            final int recordsPerBatch   = 10000;
            long      numberGreaterThan = startNumber;

            while (true)
            {
                ResultSet resultSet = null;

                try
                {
                    pstmtSelect.setLong(1, numberGreaterThan);
                    pstmtSelect.setLong(2, endNumber);

                    resultSet = pstmtSelect.executeQuery();
                    boolean recordsExist = false;

                    while (resultSet.next())
                    {
                        recordsExist      = true;
                        numberGreaterThan = processRecords(resultSet);
                    }

                    if (log.isDebugEnabled())
                        log.debug("recordsExist : " + recordsExist);

                    if (!recordsExist)
                        break;
                }
                catch (final Exception e)
                {
                    log.error("Exception in reading from db", e);
                    break;
                }
                finally
                {
                    closeResultSet(resultSet);
                }
            }

            hasAllThreadsCompleted = true;
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            log.error("Exception while getting records from DB", e);
        }
    }

    private static void closeResultSet(
            ResultSet aResultSet)
    {

        try
        {
            if (aResultSet != null)
                aResultSet.close();
        }
        catch (final SQLException e)
        {}
    }

    private long processRecords(
            ResultSet aResultSet)
            throws Exception
    {

        try
        {
            final long    dest        = aResultSet.getLong(Db2RedisThreadBased.COLUMN_NAME_DEST);
            final String  preferences = aResultSet.getString(Db2RedisThreadBased.COLUMN_NAME_PREFERENCES);

            final DndInfo dndInfo     = new DndInfo(dest + "", preferences, Db2RedisThreadBased.ACTION_ADD_OR_UPDATE);

            if ((DndAction.INVALID == dndInfo.getDndAction()) || (DndAction.INVALID_NUMBER == dndInfo.getDndAction()))
                log.error(name + " : " + " : Invalid Info " + dndInfo);

            InMemoryDataHolder.getInstance().addData(dndInfo);
            return dest + 1;
        }
        catch (final Exception e)
        {
            log.error("Exception in reading from db. Continuing with next record.", e);
            throw e;
        }
    }

    public boolean isCompleted()
    {
        return hasAllThreadsCompleted;
    }

    @Override
    public String toString()
    {
        return "[" + startNumber + "-" + endNumber + "]";
    }

}