package com.itextos.beacon.commonlib.scheduler.logging.redis;

import java.util.List;

import com.itextos.beacon.commonlib.scheduler.logging.AbstractJobLogging;
import com.itextos.beacon.commonlib.scheduler.logging.JobData;
import com.itextos.beacon.commonlib.scheduler.logging.JobExecutedData;

public class RedisJobLogging
        extends
        AbstractJobLogging
{

    @Override
    protected void storeTobeExecuteInDb(
            List<JobData> aJobTobeExecuted)
    {
        updateJobToExecuteInRedis(aJobTobeExecuted);
    }

    private void updateJobToExecuteInRedis(
            List<JobData> aJobTobeExecuted)
    {}

    @Override
    protected void storeExecutedInDb(
            List<JobExecutedData> aJobTobeExecuted)
    {
        updateExecutedInRedis(aJobTobeExecuted);
    }

    private void updateExecutedInRedis(
            List<JobExecutedData> aJobTobeExecuted)
    {}

}