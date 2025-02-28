package com.winnovature.cronjobs.consumers;

import java.util.Collection;
import java.util.Properties;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.winnovature.cronjobs.services.UnwantedFilesCleaner;
import com.winnovature.cronjobs.singletons.CronJobsPropertiesTon;
import com.winnovature.cronjobs.utils.Constants;

public class UnwantedFilesRemoval extends Thread {

	static Log log = LogFactory.getLog(Constants.CronJobLogger);
	private static final String className = "[UnwantedFilesRemoval]";

	@Override
	public void run() {
		PropertiesConfiguration cronJobProperties = null;
		String cronExpression = null;
		try {
			cronJobProperties = CronJobsPropertiesTon.getInstance().getPropertiesConfiguration();
			cronExpression = cronJobProperties.getString("abandoned.files.removal.runtime.expression");
			
			Properties props = new Properties();
			props.setProperty("org.quartz.scheduler.instanceName", "CleaningQuartzInstance");
			props.setProperty("org.quartz.scheduler.schedulerName", "CleaningQuartzScheduler");
			props.setProperty("org.quartz.threadPool.threadCount", "5");

			SchedulerFactory sf = new StdSchedulerFactory(props);
			
			Collection<Scheduler> allSchedulers = sf.getAllSchedulers();
			for (Scheduler scheduler : allSchedulers) {
				log.debug(scheduler.getContext() + " "+scheduler.getSchedulerName() + " "+scheduler.getSchedulerInstanceId());
			}
			
			//Scheduler sched = sf.getScheduler("CleaningQuartzScheduler");
			//if(sched == null) {
				//sched = sf.getScheduler();
			//}
			
			Scheduler sched = sf.getScheduler();

			JobDetail job = JobBuilder.newJob(UnwantedFilesCleaner.class)
					.withIdentity("abandoned_files_removal_job", "UnwantedFilesCleaner").build();

			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity("abandoned_files_removal_trigger", "UnwantedFilesCleaner")
					.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();
			
			// start before scheduling jobs
			sched.start();
			
			if (sched.checkExists(job.getKey())){
				sched.deleteJob(job.getKey());
			}

			sched.scheduleJob(job, trigger);

			if (log.isDebugEnabled()) {
				log.debug(className + " [run] "
						+ " Cron job name : abandoned_files_removal_job, Group :UnusedFilesCleaner scheduled to execute at "
						+ trigger.getNextFireTime() + " on daily basis.");
			}

		} catch (Exception e) {
			log.error("Exception", e);
			log.error(className + " RESTART FP-CronJobs MODULE ");
		}
	}
}
