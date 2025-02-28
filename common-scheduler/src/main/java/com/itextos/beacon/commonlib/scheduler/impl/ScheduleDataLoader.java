package com.itextos.beacon.commonlib.scheduler.impl;

import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.scheduler.config.ScheduleInfo;
import com.itextos.beacon.commonlib.scheduler.util.DatabaseOperation;

/**
 * This class will be used to read the schedule data from database and schedule
 * them.
 *
 * @author KS
 */
public class ScheduleDataLoader
        extends
        ScheduleDataLoaderHelper
{

    private static final Log log = LogFactory.getLog(ScheduleDataLoader.class);

    @Override
    public void execute(
            JobExecutionContext aContext)
            throws JobExecutionException
    {
        if (log.isInfoEnabled())
            log.info("Loading data ....");

        loadData(false);
        loadUnscheduleData();

        final Date lNextFireTime = aContext.getNextFireTime();

        if (log.isInfoEnabled())
            log.info("Next fire time for '" + aContext.getJobDetail().getKey() + "' is '" + lNextFireTime + "'");
    }

    public static void loadData(
            boolean aInitialLoad)
    {

        try
        {
            final Map<String, ScheduleInfo> scheduleMap = DatabaseOperation.getScheduleInfo(aInitialLoad);

            if (!scheduleMap.isEmpty())
            {
                final Map<String, Date> lScheduleResult = scheduleData(scheduleMap);
                DatabaseOperation.updateSchedule(lScheduleResult);
            }
        }
        catch (final Exception e)
        {
            final String s = "Exception on loading the schedule data from db";
            log.error(s, e);
        //    throw new ItextosRuntimeException(s, e);
        }
    }

    private static void loadUnscheduleData()
    {

        try
        {
            final Map<String, JobKey> unscheduleData = DatabaseOperation.getUnScheduleInfo();

            if (!unscheduleData.isEmpty())
            {
                final Map<String, Boolean> lUnscheduleResult = unscheduleData(unscheduleData);
                DatabaseOperation.updateUnscheduleRecords(lUnscheduleResult);
            }
        }
        catch (final Exception e)
        {
            final String s = "Exception on loading the schedule data from db";
            log.error(s, e);
         //   throw new ItextosRuntimeException(s, e);
        }
    }

}