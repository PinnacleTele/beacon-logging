package com.itextos.beacon.commonlib.scheduler.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.itextos.beacon.commonlib.scheduler.job.AbstractScheduleJob;

public class SampleJob
        extends
        AbstractScheduleJob
{

    private static final Log log = LogFactory.getLog(SampleJob.class);

    @Override
    public void execute(
            JobExecutionContext aContext)
            throws JobExecutionException
    {
        log.info("Job name " + aContext.getJobDetail().getKey() + " -- " + aContext.getJobDetail().getJobDataMap() + " - " + aContext.getNextFireTime());
    }

}