package com.itextos.beacon.commonlib.scheduler.util;

import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class SchedulerUtility
{

    private static final Log log        = LogFactory.getLog(SchedulerUtility.class);
    private static boolean   mFirstLoad = true;

    private SchedulerUtility()
    {}

    public static Properties getProperties()
    {
        Properties schedulerProperties = null;

        try
        {
            final String schedulerPropertiesFileName = CommonUtility.nullCheck(System.getProperty("scheduler.properties.path"), true);

            if (log.isDebugEnabled())
                log.debug("Properties Filename from runtime argument '" + schedulerPropertiesFileName + "'");

            if ("".equals(schedulerPropertiesFileName))
                schedulerProperties = PropertyLoader.getInstance().getProperties(PropertiesPath.SCHEDULER_PROPERTIES, false);
            else
                schedulerProperties = PropertyLoader.getInstance().getPropertiesByFileName(schedulerPropertiesFileName);

            if (mFirstLoad)
            {
                mFirstLoad = false;
                if (log.isDebugEnabled() && (schedulerProperties != null))
                    for (final Entry<Object, Object> entry : schedulerProperties.entrySet())
                        log.debug("Key '" + entry.getKey() + "' : Value '" + entry.getValue() + "'");
            }
        }
        catch (final Exception e)
        {
            log.error("********************** Unable to get the schedule properties.");
        }
        return schedulerProperties;
    }

    public static void printRunningJobs(
            Scheduler aScheduler)
    {

        if (log.isInfoEnabled())
        {
            log.info("Currently running Jobs ...");

            try
            {
                final List<JobExecutionContext> lCurrentlyExecutingJobs = aScheduler.getCurrentlyExecutingJobs();

                if (lCurrentlyExecutingJobs.isEmpty())
                {
                    log.info("No Jobs running currently...");
                    return;
                }

                for (final JobExecutionContext jec : lCurrentlyExecutingJobs)
                    try
                    {
                        log.info(jec.getJobDetail() + " Fired at " + jec.getFireTime());
                    }
                    catch (final Exception e)
                    {
                        log.error("Exception while listing the running jobs", e);
                    }
            }
            catch (final SchedulerException e)
            {
                log.error("Exception while listing the running jobs", e);
            }
        }
    }

}
