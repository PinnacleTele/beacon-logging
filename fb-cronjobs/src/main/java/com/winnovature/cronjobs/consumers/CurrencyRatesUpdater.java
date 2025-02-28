package com.winnovature.cronjobs.consumers;

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

import com.winnovature.cronjobs.services.CurrencyRatesGetter;
import com.winnovature.cronjobs.singletons.CronJobsPropertiesTon;
import com.winnovature.cronjobs.utils.Constants;

public class CurrencyRatesUpdater extends Thread {

	static Log log = LogFactory.getLog(Constants.CronJobLogger);
	private static final String className = "[CurrencyRatesUpdater]";

	@Override
	public void run() {
		PropertiesConfiguration cronJobProperties = null;
		String cronExpression = null;
		try {
			cronJobProperties = CronJobsPropertiesTon.getInstance().getPropertiesConfiguration();
			cronExpression = cronJobProperties.getString("exchange.api.runtime.expression");
			
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler sched = sf.getScheduler();

			JobDetail job = JobBuilder.newJob(CurrencyRatesGetter.class)
					.withIdentity("latest_rates", "CurrencyRatesUpdate").build();

			Trigger trigger = TriggerBuilder.newTrigger().withIdentity("latest_rates_trigger", "CurrencyRatesUpdate")
					.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();

			sched.scheduleJob(job, trigger);

			sched.start();

			if (log.isDebugEnabled()) {
				log.debug(className + " [run] "
						+ " Cron job name : latest_rates, Group :CurrencyRatesUpdate scheduled to execute at "
						+ trigger.getNextFireTime() + " on daily basis.");
			}
			
			/*
			while (sched.isStarted() && !sched.isShutdown()) {}
			*/

		} catch (Exception e) {
			log.error("Exception", e);
			log.error(className + " RESTART FP-CronJobs MODULE ");
		}
	}
}
