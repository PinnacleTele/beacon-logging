package com.winnovature.groupsprocessor.consumers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.groupsprocessor.daos.UpdateGroupFilesCompletedDAO;
import com.winnovature.groupsprocessor.singletons.GroupsProcessorPropertiesTon;
import com.winnovature.groupsprocessor.utils.Constants;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.Utility;

public class PollerGroupFilesCompleted extends Thread {

	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);

	PropertiesConfiguration prop = null;

	private static final String className = "PollerCampaignFilesCompleted";
	String methodName = null;

	private String instanceId = "";

	Map<String, String> configParamsTon = null;

	private long sleepTime = 1000;

	public PollerGroupFilesCompleted() throws Exception {
		try {
			prop = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration();
			this.instanceId = prop.getString(Constants.MONITORING_INSTANCE_ID);
		} catch (Exception e) {
			log.error(className + "PropertiesLoadException: ", e);
		}
		this.sleepTime = com.winnovature.utils.utils.Utility.getConsumersSleepTime();
	}

	private void consumerSleep(long sleepTime) {
		try {
			TimeUnit.MILLISECONDS.sleep(sleepTime);
		} catch (InterruptedException e) {
			log.error("*** " + className, e);
		} catch (Exception e) {
			log.error("*** " + className, e);
		}
	}

	public void updateGroupFilesStatus() throws Exception {
		methodName = "[updateGroupFilesStatus] ";

		UpdateGroupFilesCompletedDAO dao = new UpdateGroupFilesCompletedDAO();
		try {
			// get group_files inprocess records
			List<Map<String, String>> groupFilesList = dao.getInprocessRecords();

			if (groupFilesList == null || groupFilesList.size() == 0) {
				// No data found let consumer sleep for some time
				consumerSleep(sleepTime);
				return;
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " group_files inprocess count :" + groupFilesList.size());
			}

			for (int i = 0; i < groupFilesList.size(); i++) {
				Map<String, String> groupFiles = groupFilesList.get(i);

				String id = groupFiles.get("id");

				Map<String, String> groupFileSplitsList = dao.getGroupFileSplits(id);

				if (log.isDebugEnabled())
					log.debug(className + methodName + " campaign_files record id:" + id + " campaignFileSplitsList:"
							+ groupFileSplitsList.size());

				dao.updateGroupFilesRecordCompleted(id, groupFileSplitsList);

			} // End of for Loop
		} catch (Exception e) {
			log.error(className + methodName + " Exception:", e);
			throw e;
		}

	}

	@Override
	public void run() {
		methodName = " [run] ";
		while (true) {
			try {
				// HeartBeat
				String timeStampAsString = Utility.getTimestampAsString();
				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-GroupsProcessor", className, instanceId,
						this.getName(), timeStampAsString);

				updateGroupFilesStatus();
			} catch (Exception e) {
				log.error(className + methodName + " RUNTIME EXCEPTION Update group_files status Thread  ****\n", e);
				int threadSleepTime = Utility.getThreadSleepTime();
				log.error(className + methodName + " Update group_files status Thread will sleep for "
						+ threadSleepTime + " milli seconds then restarts.");
				consumerSleep(threadSleepTime);
			}
		} // end of while
	}

}
