package com.winnovature.groupsprocessor.consumers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.groupsprocessor.daos.UpdateGroupMasterCompletedDAO;
import com.winnovature.groupsprocessor.singletons.GroupsProcessorPropertiesTon;
import com.winnovature.groupsprocessor.utils.Constants;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.Utility;

public class PollerGroupMasterCompleted extends Thread {

	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);

	PropertiesConfiguration prop = null;

	private static final String className = "PollerGroupMasterCompleted";
	String methodName = null;

	private String instanceId = "";

	Map<String, String> configParamsTon = null;

	private long sleepTime = 1000;

	public PollerGroupMasterCompleted() throws Exception {
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

	public void updateGroupMasterStatus() throws Exception {
		String methodName = " [updateGroupMasterStatus] ";

		UpdateGroupMasterCompletedDAO dao = new UpdateGroupMasterCompletedDAO();
		try {
			// get group_master inprocess records
			List<Map<String, String>> groupMasterList = dao.getInprocessRecords();

			if (groupMasterList == null || groupMasterList.size() == 0) {
				// No data found let consumer sleep for some time
				consumerSleep(sleepTime);
				return;
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " group_master inprocess count :" + groupMasterList.size());
			}

			for (int i = 0; i < groupMasterList.size(); i++) {
				Map<String, String> groupMaster = groupMasterList.get(i);
				String id = groupMaster.get("id");
				//String g_type = groupMaster.get("g_type");
				Map<String, String> groupFilesStatus = dao.getGroupFiles(id);
				
				if (log.isDebugEnabled())
					log.debug(className + methodName + " group_master record id:" + id + " groupFilesStatus:"
							+ groupFilesStatus);

				dao.updateCampaignMasterCompleted(id, groupFilesStatus);
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

				updateGroupMasterStatus();
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
