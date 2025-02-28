package com.javacodegeeks.quartz;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzSchedulerCronTriggerExample
        implements
        ILatch
{

    private final CountDownLatch latch = new CountDownLatch(1);

    public static void main(
            String[] args)
            throws Exception
    {
        final QuartzSchedulerCronTriggerExample quartzSchedulerExample = new QuartzSchedulerCronTriggerExample();
        quartzSchedulerExample.fireJob();
    }

    public void fireJob()
            throws SchedulerException,
            InterruptedException
    {
        final SchedulerFactory schedFact = new StdSchedulerFactory();
        final Scheduler        scheduler = schedFact.getScheduler();
        scheduler.start();

        // define the job and tie it to our HelloJob class
        final JobBuilder jobBuilder = JobBuilder.newJob(MyJob.class);
        final JobDataMap data       = new JobDataMap();
        data.put("latch", this);

        final JobDetail jobDetail = jobBuilder.usingJobData("example", "com.javacodegeeks.quartz.QuartzSchedulerListenerExample").usingJobData(data).withIdentity("myJob", "group1").build();

        final Calendar  rightNow  = Calendar.getInstance();
        final int       hour      = rightNow.get(Calendar.HOUR_OF_DAY);
        final int       min       = rightNow.get(Calendar.MINUTE);

        System.out.println("Current time: " + new Date());

        // Tell quartz to schedule the job using our trigger
        // Fire at current time + 1 min every day
        scheduler.scheduleJob(jobDetail, CronExpressionsExample.fireEvery5Seconds());
        latch.await();
        System.out.println("All triggers executed. Shutdown scheduler");
        // scheduler.shutdown();
    }

    @Override
    public void countDown()
    {
        latch.countDown();
    }

}
