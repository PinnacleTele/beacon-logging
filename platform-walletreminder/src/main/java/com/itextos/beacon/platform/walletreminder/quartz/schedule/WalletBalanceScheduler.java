package com.itextos.beacon.platform.walletreminder.quartz.schedule;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.walletreminder.quartz.jobs.ClientBalanceJob;
import com.itextos.beacon.platform.walletreminder.quartz.jobs.DailyBalanceJob;
import com.itextos.beacon.platform.walletreminder.quartz.jobs.OverallBalanceJob;
import com.itextos.beacon.platform.walletreminder.quartz.jobs.WalletDataBackup;
import com.itextos.beacon.platform.walletreminder.utils.WalletReminderProperties;

public class WalletBalanceScheduler
{

    private static final Log    log                = LogFactory.getLog(WalletBalanceScheduler.class);
    private static final String JOB_GROUP_NAME     = "WalletBalanceGroup";
    private static final String TRIGGER_GROUP_NAME = "TriggerWalletBalanceGroup";

    private Scheduler           mQuartzScheduler   = null;

    public void start()
            throws SchedulerException
    {
        if (log.isDebugEnabled())
            log.debug("Starting Wallet Balance Scheduler");

        final SchedulerFactory schedFact = new StdSchedulerFactory();
        mQuartzScheduler = schedFact.getScheduler();
        mQuartzScheduler.start();

        if (log.isDebugEnabled())
            log.debug("Started Wallet Balance Scheduler");

        if (log.isDebugEnabled())
            log.debug("Wallet Balance Schedule Triggers");

        startOverAllBalanceJob();
        startDailyBalanceJob();
        startClientBalanceJob();
        startWalletBackupJob();
    }

    private void startWalletBackupJob()
            throws SchedulerException
    {
        final JobBuilder jobBuilder   = JobBuilder.newJob(WalletDataBackup.class);
        final JobDetail  jobDetail    = jobBuilder.withIdentity("WalletBackupJob", JOB_GROUP_NAME).build();
        final Date       lScheduleJob = mQuartzScheduler.scheduleJob(jobDetail, getWalletBackupTrigger());

        log.fatal("Next schedule for the Wallet Backuup job is  " + DateTimeUtility.getFormattedDateTime(lScheduleJob, DateTimeFormat.DEFAULT));
    }

    private static Trigger getWalletBackupTrigger()
    {
        final String cronExpression = WalletReminderProperties.getInstance().getWalletBackupTrigger();

        if (log.isInfoEnabled())
            log.info("Starting Wallet Backup trigger with the cron expression  '" + cronExpression + "'");

        return TriggerBuilder.newTrigger().withIdentity("WalletBackupTrigger", TRIGGER_GROUP_NAME).withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();
    }

    private void startClientBalanceJob()
            throws SchedulerException
    {
        final JobBuilder jobBuilder   = JobBuilder.newJob(ClientBalanceJob.class);
        final JobDetail  jobDetail    = jobBuilder.withIdentity("ClientWalletBalanceJob", JOB_GROUP_NAME).build();
        final Date       lScheduleJob = mQuartzScheduler.scheduleJob(jobDetail, getClientTrigger());

        log.fatal("Next schedule for the Client Balance job is  " + DateTimeUtility.getFormattedDateTime(lScheduleJob, DateTimeFormat.DEFAULT));
    }

    private static Trigger getClientTrigger()
    {
        final String cronExpression = WalletReminderProperties.getInstance().getClientTrigger();

        if (log.isInfoEnabled())
            log.info("Starting client trigger with the cron expression  '" + cronExpression + "'");

        return TriggerBuilder.newTrigger().withIdentity("ClientWalletBalanceTrigger", TRIGGER_GROUP_NAME).withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();
    }

    private void startDailyBalanceJob()
            throws SchedulerException
    {
        final JobBuilder jobBuilder   = JobBuilder.newJob(DailyBalanceJob.class);
        final JobDetail  jobDetail    = jobBuilder.withIdentity("DailyWalletBalanceJob", JOB_GROUP_NAME).build();
        final Date       lScheduleJob = mQuartzScheduler.scheduleJob(jobDetail, getDailyTrigger());

        log.fatal("Next schedule for the Daily Balance job is   " + DateTimeUtility.getFormattedDateTime(lScheduleJob, DateTimeFormat.DEFAULT));
    }

    private static Trigger getDailyTrigger()
    {
        final String cronExpression = WalletReminderProperties.getInstance().getDailyTrigger();

        if (log.isInfoEnabled())
            log.info("Starting Daily trigger with the cron expression   '" + cronExpression + "'");

        return TriggerBuilder.newTrigger().withIdentity("DailyWalletBalanceTrigger", TRIGGER_GROUP_NAME).withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();
    }

    private void startOverAllBalanceJob()
            throws SchedulerException
    {
        final JobBuilder jobBuilder   = JobBuilder.newJob(OverallBalanceJob.class);
        final JobDetail  jobDetail    = jobBuilder.withIdentity("OverallWalletBalanceJob", JOB_GROUP_NAME).build();
        final Date       lScheduleJob = mQuartzScheduler.scheduleJob(jobDetail, getOverallTrigger());

        log.fatal("Next schedule for the Overall Balance job is " + DateTimeUtility.getFormattedDateTime(lScheduleJob, DateTimeFormat.DEFAULT));
    }

    private static Trigger getOverallTrigger()
    {
        final String cronExpression = WalletReminderProperties.getInstance().getOverallTrigger();

        if (log.isInfoEnabled())
            log.info("Starting Overall trigger with the cron expression '" + cronExpression + "'");

        return TriggerBuilder.newTrigger().withIdentity("OverallWalletBalanceTrigger", TRIGGER_GROUP_NAME).withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();
    }

    public void shutdown()
    {
        log.fatal("Shutting down the Quartz Scheduler. Will wait for the jobs to be completed (if any).");

        try
        {

            if (mQuartzScheduler != null)
            {
                mQuartzScheduler.shutdown(true);

                log.fatal("Quartz Scheduler shutting down initiated...");

                printWorkingJobs();
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while shutting down the mQuartzScheduler.", e);
        }
    }

    private void printWorkingJobs()
            throws SchedulerException
    {
        final List<JobExecutionContext> lCurrentlyExecutingJobs = mQuartzScheduler.getCurrentlyExecutingJobs();

        if (lCurrentlyExecutingJobs != null)
            for (final JobExecutionContext jce : lCurrentlyExecutingJobs)
            {
                final JobDetail lJobDetail = jce.getJobDetail();
                log.fatal(lJobDetail.getKey() + " - " + lJobDetail.getDescription() + " is running. Started at : '" + jce.getFireTime() + "'");
            }
    }

}
