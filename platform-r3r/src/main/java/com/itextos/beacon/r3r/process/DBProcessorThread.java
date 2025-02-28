package com.itextos.beacon.r3r.process;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.r3r.data.R3RObject;
import com.itextos.beacon.r3r.dbo.DBHandler;
import com.itextos.beacon.r3r.inmemory.RequestDetailsInmemoryQueue;

public class DBProcessorThread
        extends
        Thread
{

    private static final Log log          = LogFactory.getLog(DBProcessorThread.class);
    private boolean          mCanContinue = true;

    @Override
    public void run()
    {

        while (mCanContinue)
        {
            consumeInmemoryData();
            CommonUtility.sleepForAWhile(2000);
        }
    }

    private static void consumeInmemoryData()
    {

        try
        {
            final List<R3RObject> aR3RObjectList = RequestDetailsInmemoryQueue.getInstance().getData();

            if ((aR3RObjectList != null) && (aR3RObjectList.size() > 0))
            {
                if (log.isDebugEnabled())
                    log.debug("Data consumed from the RequestDetailsInmemoryQueue is : " + aR3RObjectList.size());
                DBHandler.insertRecords(aR3RObjectList);
            }
        }
        catch (final Exception e)
        {
            log.error("Error while process the Request Data ", e);
        }
    }

    public void stopMe()
    {
        consumeInmemoryData();
        mCanContinue = false;
        log.fatal("Stopping the DB Processor Thread");
    }

}