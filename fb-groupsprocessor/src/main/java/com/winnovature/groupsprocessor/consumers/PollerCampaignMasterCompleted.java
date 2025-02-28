package com.winnovature.groupsprocessor.consumers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.groupsprocessor.daos.CampaignsDAO;
import com.winnovature.groupsprocessor.singletons.GroupsProcessorPropertiesTon;
import com.winnovature.groupsprocessor.utils.Constants;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.Utility;

public class PollerCampaignMasterCompleted extends Thread {

	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);

	PropertiesConfiguration prop = null;

	private static final String className = "PollerCampaignMasterCompleted";
	String methodName = null;

	private String instanceId = "";

	Map<String, String> configParamsTon = null;

	private long sleepTime = 1000;

	public PollerCampaignMasterCompleted() throws Exception {
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

	public void updateCampaignMasterStatus() throws Exception {
		String methodName = " [updateCampaignMasterStatus] ";

		CampaignsDAO dao = new CampaignsDAO();
		try {
			// get group_master inprocess records
			List<Map<String, String>> campaignMasterList = dao.getGroupCampaignInprocessRecords();

			if (campaignMasterList == null || campaignMasterList.size() == 0) {
				// No data found let consumer sleep for some time
				consumerSleep(sleepTime);
				return;
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " campaign_master Group inprocess count :" + campaignMasterList.size());
			}

			for (int i = 0; i < campaignMasterList.size(); i++) {
				Map<String, String> campaignMaster = campaignMasterList.get(i);
				String id = campaignMaster.get("id");
				Map<String, String> campaignGroupStatus = dao.getCampaignGroups(id);
				
				if (log.isDebugEnabled())
					log.debug(className + methodName + " campaign_groups:" + campaignGroupStatus);

				dao.updateCampaignMaster(id, campaignGroupStatus);
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

				updateCampaignMasterStatus();
			} catch (Exception e) {
				log.error(className + methodName + " RUNTIME EXCEPTION Update campaign_master status Thread  ****\n", e);
				int threadSleepTime = Utility.getThreadSleepTime();
				log.error(className + methodName + " Update campaign_master status Thread will sleep for "
						+ threadSleepTime + " milli seconds then restarts.");
				consumerSleep(threadSleepTime);
			}
		} // end of while
	}

}
