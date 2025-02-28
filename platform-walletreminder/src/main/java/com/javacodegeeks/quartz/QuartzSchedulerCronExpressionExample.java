package com.javacodegeeks.quartz;

import java.util.Date;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;

public class QuartzSchedulerCronExpressionExample
{

    public static void main(
            String[] args)
            throws Exception
    {
        final QuartzSchedulerCronExpressionExample quartzSchedulerExample = new QuartzSchedulerCronExpressionExample();
        quartzSchedulerExample.fireJob();
    }

    public void fireJob()
            throws SchedulerException,
            InterruptedException
    {
        final SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
        final Scheduler        scheduler = schedFact.getScheduler();
        scheduler.start();

        // define the job and tie it to our HelloJob class
        final JobBuilder jobBuilder = JobBuilder.newJob(MyJob.class);

        final JobDetail  jobDetail  = jobBuilder.usingJobData("example", "com.javacodegeeks.quartz.QuartzSchedulerListenerExample").withIdentity("myJob", "group1").build();

        System.out.println("Current time: " + new Date());

        // Tell quartz to schedule the job using our trigger
        // Fire at current time + 1 min every day
        scheduler.scheduleJob(jobDetail, CronExpressionsExample.fireAfterTwoMinFrom7_46To7_58());
    }

}
