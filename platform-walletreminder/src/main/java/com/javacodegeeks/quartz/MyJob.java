package com.javacodegeeks.quartz;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MyJob
        implements
        Job
{

    private static int count;

    @Override
    public void execute(
            JobExecutionContext jobContext)
            throws JobExecutionException
    {
        System.out.println("--------------------------------------------------------------------");
        System.out.println("MyJob start: " + jobContext.getFireTime());
        final JobDetail jobDetail = jobContext.getJobDetail();
        System.out.println("Example name is: " + jobDetail.getJobDataMap().getString("example"));
        System.out.println("MyJob end: " + jobContext.getJobRunTime() + ", key: " + jobDetail.getKey());
        System.out.println("MyJob next scheduled time: " + jobContext.getNextFireTime());
        System.out.println("--------------------------------------------------------------------");

        final ILatch latch = (ILatch) jobDetail.getJobDataMap().get("latch");
        if (latch != null)
            latch.countDown();
        count++;
        System.out.println("Job count " + count);
    }

}
