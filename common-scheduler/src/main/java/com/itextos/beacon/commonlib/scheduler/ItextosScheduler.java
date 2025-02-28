package com.itextos.beacon.commonlib.scheduler;

import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.daemonprocess.ShutdownHandler;
import com.itextos.beacon.commonlib.daemonprocess.ShutdownHook;
import com.itextos.beacon.commonlib.scheduler.impl.ScheduleDataLoader;
import com.itextos.beacon.commonlib.scheduler.job.ItextosJobListener;
import com.itextos.beacon.commonlib.scheduler.logging.db.DatabaseJobLogging;
import com.itextos.beacon.commonlib.scheduler.util.SchedulerUtility;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class ItextosScheduler
        implements
        ShutdownHook
{

    private static final Log    log                  = LogFactory.getLog(ItextosScheduler.class);
    private static final String FOR_EVERY_30_SECONDS = "0/30 * * ? * * *";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final ItextosScheduler INSTANCE = new ItextosScheduler();

    }

    public static ItextosScheduler getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private Scheduler scheduler = null;

    private ItextosScheduler()
    {

        try
        {
            final StdSchedulerFactory sf                  = new StdSchedulerFactory();
            final Properties          schedulerProperties = SchedulerUtility.getProperties();

            if (schedulerProperties != null)
                sf.initialize(schedulerProperties);
            else
                sf.initialize();

            scheduler = sf.getScheduler();

            scheduler.getListenerManager().addJobListener(new ItextosJobListener());
            // scheduler.getListenerManager().addSchedulerListener(new
            // ItextosSchedulerListener());
            // scheduler.getListenerManager().addTriggerListener(new
            // ItextosTriggerListener());

            scheduler.standby();
            scheduler.start();

            startSchedulerDataJob();

            startListenerReapers();

            if (log.isInfoEnabled())
                log.info("Itextos Scheduler started successfully....");
        }
        catch (final Exception e)
        {
            final String s = "Exception while starting the Itextos Scheduler";
            log.error(s, e);
        //    throw new ItextosRuntimeException(s, e);
        }
        finally
        {
            ShutdownHandler.getInstance().addHook("Itextos Scheduler", this);
        }
    }

    private void startListenerReapers()
    {
        new DatabaseJobLogging();
    }

    private void startSchedulerDataJob()
    {

        try
        {
            final JobDetail   jobDetail    = JobBuilder.newJob(ScheduleDataLoader.class).withIdentity("ScheduleDataLoader", "Mandatory").withDescription("To Load the schedule data from database")
                    .build();

            final CronTrigger cronTrigger  = TriggerBuilder.newTrigger().withIdentity("TrgScheduleDataLoader", "TrgMandatory").withSchedule(CronScheduleBuilder.cronSchedule(FOR_EVERY_30_SECONDS))
                    .build();

            final Date        lScheduleJob = scheduler.scheduleJob(jobDetail, cronTrigger);

            if (log.isInfoEnabled())
                log.info("ScheduleDataLoader : Scheduling of schedule data loading started. Its execution is at '" + lScheduleJob + "'");
        }
        catch (final SchedulerException e)
        {
            final String s = "";
            log.error(s, e);
    //        throw new ItextosRuntimeException(s, e);
        }
    }

    public Date addJob(
            JobDetail aJobDetail,
            Trigger aJobCronTrigger)
            throws SchedulerException
    {
        if (log.isDebugEnabled())
            log.debug("Scheduling job for '" + aJobDetail.getKey() + "' with trigger key '" + aJobCronTrigger.getKey() + "'");

        return scheduler.scheduleJob(aJobDetail, aJobCronTrigger);
    }

    public boolean unscheduleJob(
            JobKey aJobKey)
            throws SchedulerException
    {
        if (log.isDebugEnabled())
            log.debug("Unscheduling Job '" + aJobKey + "'");

        if (aJobKey != null)
            return scheduler.deleteJob(aJobKey);

        return false;
    }

    @Override
    public void shutdown()
    {

        try
        {
            if (log.isInfoEnabled())
                log.info("Itextos Scheduler shutdown invoked");

            if (scheduler != null)
            {
                scheduler.shutdown(true);

                while (!scheduler.isShutdown())
                {
                    log.fatal("Waiting for the scheduler to shutdown.");

                    SchedulerUtility.printRunningJobs(scheduler);

                    CommonUtility.sleepForAWhile();
                }
            }
        }
        catch (final SchedulerException e)
        {
            log.error("Exception while stopping the Itextos Scheduler.", e);
        }
    }

}