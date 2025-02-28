package com.itextos.beacon.commonlib.scheduler.job;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;

import com.itextos.beacon.commonlib.scheduler.logging.JobData;
import com.itextos.beacon.commonlib.scheduler.logging.JobExecutedData;
import com.itextos.beacon.commonlib.scheduler.logging.LoggingFactory;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class ItextosJobListener
        implements
        JobListener
{

    private static final Log log = LogFactory.getLog(ItextosJobListener.class);

    @Override
    public String getName()
    {
        return "ItextosJobListener";
    }

    @Override
    public void jobToBeExecuted(
            JobExecutionContext aContext)
    {

        try
        {
            final JobData lJobExecute = jobExecute(aContext);

            if (lJobExecute.isLogRequired())
                LoggingFactory.getInstance().addJobTobeExecuted(lJobExecute);
        }
        catch (final Exception e)
        {
            log.error("Exception while executing the Job Tobe Executed", e);
        }
    }

    @Override
    public void jobExecutionVetoed(
            JobExecutionContext aContext)
    {}

    @Override
    public void jobWasExecuted(
            JobExecutionContext aContext,
            JobExecutionException aJobException)
    {

        try
        {
            final JobExecutedData lJobExecuted = jobExecuted(aContext, aJobException);

            if (lJobExecuted.isLogRequired())
                LoggingFactory.getInstance().addJobWasExecuted(lJobExecuted);
        }
        catch (final Exception e)
        {
            log.error("Exception while executing the Job was Executed", e);
        }
    }

    private static JobData jobExecute(
            JobExecutionContext aContext)
    {
        final JobDetail lJobDetail        = aContext.getJobDetail();
        final JobKey    lKey              = lJobDetail.getKey();
        final String    scheduleId        = lKey.getName();
        final String    scheduleGroupId   = lKey.getGroup();
        final String    scheduleName      = lJobDetail.getDescription();
        final Date      scheduledFireTime = aContext.getScheduledFireTime();
        final Date      lFireTime         = aContext.getFireTime();

        return new JobData(scheduleId, scheduleGroupId, scheduleName, scheduledFireTime, lFireTime);
    }

    private static JobExecutedData jobExecuted(
            JobExecutionContext aContext,
            JobExecutionException aJobException)
    {
        final JobDetail lJobDetail        = aContext.getJobDetail();
        final JobKey    lKey              = lJobDetail.getKey();
        final String    scheduleId        = lKey.getName();
        final String    scheduleGroupId   = lKey.getGroup();
        final String    scheduleName      = lJobDetail.getDescription();
        final Date      lFireTime         = aContext.getFireTime();
        final Date      scheduledFireTime = aContext.getScheduledFireTime();
        final Date      lNextFireTime     = aContext.getNextFireTime();

        final long      duration          = aContext.getJobRunTime();
        String          errorString       = null;
        if (aJobException != null)
            errorString = CommonUtility.getStackTrace(aJobException);

        return new JobExecutedData(scheduleId, scheduleGroupId, scheduleName, scheduledFireTime, lFireTime, duration, lNextFireTime, errorString, false);
    }

}