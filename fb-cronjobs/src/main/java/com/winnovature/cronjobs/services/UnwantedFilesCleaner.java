package com.winnovature.cronjobs.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.winnovature.cronjobs.dao.GenericDAO;
import com.winnovature.cronjobs.singletons.CronJobsPropertiesTon;
import com.winnovature.cronjobs.utils.Constants;
import com.winnovature.cronjobs.utils.Utility;
import com.winnovature.utils.utils.DuplicateCheckDataCleaner;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.UploadedFilesTrackingUtility;

public class UnwantedFilesCleaner implements Job {

	static Log log = LogFactory.getLog(Constants.CronJobLogger);
	private static final String className = "[UnwantedFilesCleaner]";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String logname = className + " [execute] ";
		PropertiesConfiguration cronJobProperties = null;

		if (log.isDebugEnabled()) {
			log.debug(logname + "Executing cronjob[" + context.getJobDetail().getKey() + "], scheduled date & time ="
					+ context.getFireTime());
		}

		try {
			cronJobProperties = CronJobsPropertiesTon.getInstance().getPropertiesConfiguration();
			String instanceId = cronJobProperties.getString("instance.monitoring.id");

			// HeartBeat
			String timeStampAsString = com.winnovature.utils.utils.Utility.getTimestampAsString();
			new HeartBeatMonitoring().pushConsumersHeartBeat("FP-CronJobs", "UnwantedFilesCleaner", instanceId,
					"UnwantedFilesCleaner", timeStampAsString);

			int limit = cronJobProperties.getInt("abandoned.files.fetch.limit", 10000);
			int days = cronJobProperties.getInt("abandoned.files.fetch.for.days.ago", 1);
			String campStatusToDeleteFiles = cronJobProperties.getString("campaigns.status.for.files.deletion",
					"completed");
			int nthDayToDeleteFiles = cronJobProperties.getInt("oldfiles.deletion.days.count", 7);
			String campStatusToDeleteDupcheckRedisData = cronJobProperties
					.getString("campaigns.status.for.dupcheck.data.deletion", "completed");
			int nthDayToDeleteDupcheckRedisData = cronJobProperties.getInt("dupcheck.redis.data.deletion.days.count",
					2);
			String dupcheckRedisKeyPrefix = cronJobProperties.getString("dupcheck.redis.key.prefix",
					"campaigndupcheck");

			removeAbandonedFiles(limit, days);

			removeNdaysOldFiles(nthDayToDeleteFiles, campStatusToDeleteFiles);

			clearDuplicateCheckData(nthDayToDeleteDupcheckRedisData, campStatusToDeleteDupcheckRedisData,
					dupcheckRedisKeyPrefix);

		} catch (Exception e) {
			log.error(logname + "Exception ", e);
			// TODO - Need to discuss on how to handle error cases
		}

		if (log.isDebugEnabled()) {
			log.debug(logname + "Cronjob[" + context.getJobDetail().getKey() + "] next scheduled date & time ="
					+ context.getNextFireTime());
		}

	}

	private void removeAbandonedFiles(int limit, int days) {
		String logname = className + " [removeAbandonedFiles] ";
		List<String> campaignsFilesList = null;
		List<String> templatesFilesList = null;
		List<String> groupsFilesList = null;
		List<String> dltFilesList = null;
		List<String> filesToBeRemoved = new ArrayList<String>();
		List<String> filesList = null;

		try {
			filesList = UploadedFilesTrackingUtility.getUploadedFilesInfo(limit, days);
			if (filesList != null && filesList.size() > 0) {
				campaignsFilesList = new ArrayList<String>();
				templatesFilesList = new ArrayList<String>();
				groupsFilesList = new ArrayList<String>();
				dltFilesList = new ArrayList<String>();
				for (String data : filesList) {
					if (data.trim().length() > 0) {
						String[] splits = StringUtils.splitByWholeSeparatorPreserveAllTokens(data.trim(),
								com.winnovature.utils.utils.Constants.ABANDONED_FILES_DELIMITER);

						if (splits[0].equalsIgnoreCase(com.winnovature.utils.utils.Constants.CAMPAIGN)) {
							campaignsFilesList.add(splits[2].trim());
						} else if (splits[0].equalsIgnoreCase(com.winnovature.utils.utils.Constants.TEMPLATE)) {
							templatesFilesList.add(splits[2].trim());
						} else if (splits[0].equalsIgnoreCase(com.winnovature.utils.utils.Constants.GROUP)) {
							groupsFilesList.add(splits[2].trim());
						} else if (splits[0].equalsIgnoreCase(com.winnovature.utils.utils.Constants.DLT)) {
							dltFilesList.add(splits[2].trim());
						}
					}
				}

				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, -days);
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String date = dateFormat.format(calendar.getTime());
				List<String> filesFromDB = null;

				if (campaignsFilesList.size() > 0 || groupsFilesList.size() > 0 || dltFilesList.size() > 0) {
					filesFromDB = new GenericDAO().getFilesForDate(date);
				}

				if (filesFromDB != null && filesFromDB.size() > 0 && campaignsFilesList.size() > 0) {
					campaignsFilesList.removeAll(filesFromDB);
				}

				if (filesFromDB != null && filesFromDB.size() > 0 && groupsFilesList.size() > 0) {
					groupsFilesList.removeAll(filesFromDB);
				}

				if (filesFromDB != null && filesFromDB.size() > 0 && dltFilesList.size() > 0) {
					dltFilesList.removeAll(filesFromDB);
				}

				if (campaignsFilesList.size() > 0) {
					filesToBeRemoved.addAll(campaignsFilesList);
				}

				if (groupsFilesList.size() > 0) {
					filesToBeRemoved.addAll(groupsFilesList);
				}

				if (dltFilesList.size() > 0) {
					filesToBeRemoved.addAll(dltFilesList);
				}

				if (templatesFilesList.size() > 0) {
					filesToBeRemoved.addAll(templatesFilesList);
				}

				if (filesToBeRemoved.size() > 0) {
					new Utility().deleteFiles(filesToBeRemoved);
					if (log.isDebugEnabled()) {
						log.debug(logname + " "+filesToBeRemoved.size()+" files removed. \n "+filesToBeRemoved);
					}
				}
			}
		} catch (Exception e) {
			log.error(logname + "Exception ", e);
		}
	}

	private void removeNdaysOldFiles(int days, String status) {
		String logname = className + " [removeNdaysOldFiles] ";
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -days);
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String date = dateFormat.format(calendar.getTime());

			List<String> filesToBeRemoved = new GenericDAO().getProcessedFilesForDate(date, status);
			if (filesToBeRemoved != null && filesToBeRemoved.size() > 0) {
				new Utility().deleteFiles(filesToBeRemoved);
				if (log.isDebugEnabled()) {
					log.debug(logname + days + " days older campaigns files removed. \n "+filesToBeRemoved);
				}
			}

		} catch (Exception e) {
			log.error(logname + "Exception ", e);
		}

	}

	private void clearDuplicateCheckData(int days, String status, String dupcheckRedisKeyPrefix) {
		String logname = className + " [clearDuplicateCheckData] ";
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -days);
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String date = dateFormat.format(calendar.getTime());

			List<Map<String, String>> campaigns = new GenericDAO().getProcessedCampaignsForDate(date, status);
			if (campaigns != null && campaigns.size() > 0) {
				new DuplicateCheckDataCleaner().clearDuplicateCheckData(campaigns, dupcheckRedisKeyPrefix);
			}

			if (log.isDebugEnabled()) {
				log.debug(logname + " duplicate redis data cleared.");
			}

		} catch (Exception e) {
			log.error(logname + "Exception ", e);
		}
	}
}
