package com.itextos.beacon.commonlib.scheduler.job;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

public class ItextosSchedulerListener
        implements
        SchedulerListener
{

    @Override
    public void jobScheduled(
            Trigger aTrigger)
    {}

    @Override
    public void jobUnscheduled(
            TriggerKey aTriggerKey)
    {}

    @Override
    public void triggerFinalized(
            Trigger aTrigger)
    {}

    @Override
    public void triggerPaused(
            TriggerKey aTriggerKey)
    {}

    @Override
    public void triggersPaused(
            String aTriggerGroup)
    {}

    @Override
    public void triggerResumed(
            TriggerKey aTriggerKey)
    {}

    @Override
    public void triggersResumed(
            String aTriggerGroup)
    {}

    @Override
    public void jobAdded(
            JobDetail aJobDetail)
    {}

    @Override
    public void jobDeleted(
            JobKey aJobKey)
    {}

    @Override
    public void jobPaused(
            JobKey aJobKey)
    {}

    @Override
    public void jobsPaused(
            String aJobGroup)
    {}

    @Override
    public void jobResumed(
            JobKey aJobKey)
    {}

    @Override
    public void jobsResumed(
            String aJobGroup)
    {}

    @Override
    public void schedulerError(
            String aMsg,
            SchedulerException aCause)
    {}

    @Override
    public void schedulerInStandbyMode()
    {}

    @Override
    public void schedulerStarted()
    {}

    @Override
    public void schedulerStarting()
    {}

    @Override
    public void schedulerShutdown()
    {}

    @Override
    public void schedulerShuttingdown()
    {}

    @Override
    public void schedulingDataCleared()
    {}

}