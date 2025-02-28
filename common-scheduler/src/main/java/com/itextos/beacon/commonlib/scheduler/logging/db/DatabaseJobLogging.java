package com.itextos.beacon.commonlib.scheduler.logging.db;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.scheduler.logging.AbstractJobLogging;
import com.itextos.beacon.commonlib.scheduler.logging.JobData;
import com.itextos.beacon.commonlib.scheduler.logging.JobExecutedData;
import com.itextos.beacon.commonlib.scheduler.logging.LoggingFactory;
import com.itextos.beacon.commonlib.scheduler.util.DatabaseOperation;

public class DatabaseJobLogging
        extends
        AbstractJobLogging
{

    private static final Log log = LogFactory.getLog(DatabaseJobLogging.class);

    @Override
    protected void storeTobeExecuteInDb(
            List<JobData> aJobTobeExecuted)
    {
        // Don't do anything here.
    }

    @Override
    protected void storeExecutedInDb(
            List<JobExecutedData> aJobTobeExecuted)
    {
        if (LoggingFactory.getInstance().isDatabaseLoggingEnabled())
            insertIntoDb(aJobTobeExecuted);
    }

    private static void insertIntoDb(
            List<JobExecutedData> aJobTobeExecuted)
    {
        if (log.isDebugEnabled())
            log.debug("Inserting records into DB " + (aJobTobeExecuted == null ? "null" : aJobTobeExecuted.size() + ""));

        if ((aJobTobeExecuted == null) || aJobTobeExecuted.isEmpty())
            return;

        DatabaseOperation.insertJobWasExecuted(aJobTobeExecuted);
    }

}