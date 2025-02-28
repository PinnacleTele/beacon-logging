package com.itextos.beacon.commonlib.scheduler.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.scheduler.ItextosScheduler;
import com.itextos.beacon.commonlib.scheduler.config.MisfireInstruction;
import com.itextos.beacon.commonlib.scheduler.config.ParamInfo;
import com.itextos.beacon.commonlib.scheduler.config.ScheduleInfo;
import com.itextos.beacon.commonlib.scheduler.config.ScheduleState;
import com.itextos.beacon.commonlib.scheduler.job.AbstractScheduleJob;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

abstract class ScheduleDataLoaderHelper
        extends
        AbstractScheduleJob
{

    private static final Log log = LogFactory.getLog(ScheduleDataLoaderHelper.class);

    protected static Map<String, Date> scheduleData(
            Map<String, ScheduleInfo> aScheduleMap)
    {
        final Map<String, Date> returnValue = new HashMap<>();

        if (log.isInfoEnabled())
            log.info("Scheduling the data...");

        for (final Entry<String, ScheduleInfo> entry : aScheduleMap.entrySet())
        {
            final String       scheduleId   = entry.getKey();
            final ScheduleInfo scheduleInfo = entry.getValue();

            if (scheduleInfo.getScheduleState() == ScheduleState.RESCHEDULED)
            {
                final boolean uncheduled = unscheduleData(scheduleInfo);
                if (!uncheduled)
                    log.error("Unable to unschedule the job for " + scheduleInfo.getScheduleId() + " " + scheduleInfo.getScheduleGroupId());
            }

            final Date lScheduleTime = scheduleIt(scheduleInfo);

            if (log.isDebugEnabled())
                log.debug("Schedule Time " + lScheduleTime + " Scheduling the data for " + scheduleId + " with " + scheduleInfo);

            returnValue.put(scheduleId, lScheduleTime);
        }

        return returnValue;
    }

    private static boolean unscheduleData(
            ScheduleInfo aScheduleInfo)
    {
        boolean returnValue = false;

        try
        {
            final JobKey jobKey = JobKey.jobKey(aScheduleInfo.getScheduleId(), aScheduleInfo.getScheduleGroupId());
            returnValue = ItextosScheduler.getInstance().unscheduleJob(jobKey);
        }
        catch (final Exception e)
        {
            log.error("Exception while unscheduling the data for " + aScheduleInfo, e);
        }
        return returnValue;
    }

    protected static Map<String, Boolean> unscheduleData(
            Map<String, JobKey> aUnscheduleData)
    {
        final Map<String, Boolean> unscheduleResult = new HashMap<>();

        for (final Entry<String, JobKey> entryKey : aUnscheduleData.entrySet())
        {
            final String scheduleId = entryKey.getKey();
            final JobKey jobKey     = entryKey.getValue();
            boolean      result     = false;

            try
            {
                result = ItextosScheduler.getInstance().unscheduleJob(jobKey);

                if (log.isDebugEnabled())
                    log.debug("Unschedule status '" + result + "'");
            }
            catch (final SchedulerException e)
            {
                log.error("Exception while unscheduling the job for '" + scheduleId + "' Trigger Key '" + jobKey + "'", e);
            }

            unscheduleResult.put(scheduleId, result);
        }
        return unscheduleResult;
    }

    private static Date scheduleIt(
            ScheduleInfo aScheduleInfo)
    {
        Date lScheduleTime = null;

        try
        {
            final JobDetail   jobDetail   = getJobDetail(aScheduleInfo);
            final CronTrigger cronTrigger = getTrigger(aScheduleInfo);
            lScheduleTime = ItextosScheduler.getInstance().addJob(jobDetail, cronTrigger);

            if (log.isInfoEnabled())
                log.info("Scheduling of schedule data loading started. Its execution is at '" + lScheduleTime + "'");
        }
        catch (final
                SchedulerException
                | ClassNotFoundException e)
        {
            final String s = "";
            log.error(s, e);
        //    throw new ItextosRuntimeException(s, e);
        }
        return lScheduleTime;
    }

    private static CronTrigger getTrigger(
            ScheduleInfo aScheduleInfo)
    {
        final CronScheduleBuilder lCronScheduleBuilder = getCronBuilder(aScheduleInfo);
        return TriggerBuilder.newTrigger().withIdentity("Trg" + aScheduleInfo.getScheduleId(), "Trg" + aScheduleInfo.getScheduleGroupId()).withSchedule(lCronScheduleBuilder).build();
    }

    private static JobDetail getJobDetail(
            ScheduleInfo aScheduleInfo)
            throws ClassNotFoundException
    {
        @SuppressWarnings("unchecked")
        final Class<Job> cls        = (Class<Job>) Class.forName(aScheduleInfo.getScheduleJobClassName());

        final JobDataMap jobDataMap = getJobDataMap(aScheduleInfo);
        return JobBuilder.newJob(cls).withIdentity(aScheduleInfo.getScheduleId(), aScheduleInfo.getScheduleGroupId()).withDescription(aScheduleInfo.getScheduleName()).setJobData(jobDataMap).build();
    }

    private static CronScheduleBuilder getCronBuilder(
            ScheduleInfo aScheduleInfo)
    {
        CronScheduleBuilder lCronScheduleBuilder = null;
        if (aScheduleInfo.getMisfireInstruction() == MisfireInstruction.DONT_DO_ANYTHING_ON_MISFIRE)
            lCronScheduleBuilder = CronScheduleBuilder.cronSchedule(aScheduleInfo.getCronExpression()).withMisfireHandlingInstructionDoNothing();
        else
            if (aScheduleInfo.getMisfireInstruction() == MisfireInstruction.FIRE_ONCE_ON_MISFIRE)
                lCronScheduleBuilder = CronScheduleBuilder.cronSchedule(aScheduleInfo.getCronExpression()).withMisfireHandlingInstructionFireAndProceed();
            else
                lCronScheduleBuilder = CronScheduleBuilder.cronSchedule(aScheduleInfo.getCronExpression());
        return lCronScheduleBuilder;
    }

    private static JobDataMap getJobDataMap(
            ScheduleInfo aScheduleInfo)
    {
        final JobDataMap      lJobDataMap    = new JobDataMap();

        final List<ParamInfo> lParamInfoList = aScheduleInfo.getParamInfoList();

        for (final ParamInfo paramInfo : lParamInfoList)
        {
            if (log.isDebugEnabled())
                log.debug("Setting the paramvalue '" + paramInfo + "'");

            switch (paramInfo.getDataType())
            {
                case BOOLEAN:
                    lJobDataMap.put(paramInfo.getParamName(), Boolean.parseBoolean(paramInfo.getParamValue()));
                    break;

                case DATE:
                case DATE_AND_TIME:
                case TIME:
                    lJobDataMap.put(paramInfo.getParamName(), DateTimeUtility.getDateFromString(paramInfo.getParamValue(), paramInfo.getDateTimeFormat()));
                    break;

                case FLOAT_OR_DOUBLE:
                    lJobDataMap.put(paramInfo.getParamName(), CommonUtility.getDouble(paramInfo.getParamValue()));
                    break;

                case INT_OR_LONG:
                    lJobDataMap.put(paramInfo.getParamName(), CommonUtility.getInteger(paramInfo.getParamValue()));
                    break;

                case STRING:
                default:
                    lJobDataMap.put(paramInfo.getParamName(), paramInfo.getParamValue());
                    break;
            }
        }
        return lJobDataMap;
    }

}
