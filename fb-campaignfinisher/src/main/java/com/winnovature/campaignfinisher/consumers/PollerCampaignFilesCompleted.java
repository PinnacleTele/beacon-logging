package com.winnovature.campaignfinisher.consumers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.campaignfinisher.daos.UpdateCampaignFilesCompletedDAO;
import com.winnovature.campaignfinisher.singletons.CampaignFinisherPropertiesTon;
import com.winnovature.campaignfinisher.utils.Constants;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.Utility;

public class PollerCampaignFilesCompleted extends Thread {

	static Log log = LogFactory.getLog(Constants.CampaignFinisherLogger);

	PropertiesConfiguration prop = null;

	private static final String className = "PollerCampaignFilesCompleted";
	String methodName = null;

	private String instanceId = "";

	Map<String, String> configParamsTon = null;

	private long sleepTime = 1000;

	public PollerCampaignFilesCompleted() throws Exception {
		try {
			prop = CampaignFinisherPropertiesTon.getInstance().getPropertiesConfiguration();
			this.instanceId = prop.getString(Constants.MONITORING_INSTANCE_ID);
		} catch (Exception e) {
			log.error(className + "PropertiesLoadException: ", e);
		}
		this.sleepTime = com.winnovature.utils.utils.Utility.getIdleThreadSleepTime();
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

	public void updateCampaignFilesStatus() throws Exception {
		methodName = "[updateCampaignFilesStatus] ";

		UpdateCampaignFilesCompletedDAO dao = new UpdateCampaignFilesCompletedDAO();
		try {
			// get campaign_files inprocess records
			List<Map<String, String>> campaignFilesList = dao.getInprocessRecords();

			if (campaignFilesList == null || campaignFilesList.size() == 0) {
				// No data found let consumer sleep for some time
				consumerSleep(sleepTime);
				return;
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " campaigns_files inprocess count :" + campaignFilesList.size());
			}

			for (int i = 0; i < campaignFilesList.size(); i++) {
				Map<String, String> campaignFiles = campaignFilesList.get(i);

				String id = campaignFiles.get("id");

				Map<String, String> campaignFileSplitsList = dao.getCampaignFileSplits(id);

				if (log.isDebugEnabled())
					log.debug(className + methodName + " campaign_files record id:" + id + " campaignFileSplitsList:"
							+ campaignFileSplitsList.size());

				dao.updateMasterRecordCompleted(id, campaignFileSplitsList);

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
				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-CampaignFinisher", className, instanceId,
						this.getName(), timeStampAsString);

				updateCampaignFilesStatus();
			} catch (Exception e) {
				log.error(className + methodName + " RUNTIME EXCEPTION Update campaign_files status Thread  ****\n", e);
				int threadSleepTime = Utility.getThreadSleepTime();
				log.error(className + methodName + " Update campaign_files status Thread will sleep for "
						+ threadSleepTime + " milli seconds then restarts.");
				consumerSleep(threadSleepTime);
			}
		} // end of while
	}

}
