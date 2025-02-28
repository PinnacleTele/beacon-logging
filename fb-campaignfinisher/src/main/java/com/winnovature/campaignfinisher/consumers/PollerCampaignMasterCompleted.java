package com.winnovature.campaignfinisher.consumers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.campaignfinisher.daos.UpdateCampaignMasterCompletedDAO;
import com.winnovature.campaignfinisher.singletons.CampaignFinisherPropertiesTon;
import com.winnovature.campaignfinisher.utils.Constants;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.Utility;

public class PollerCampaignMasterCompleted extends Thread {

	static Log log = LogFactory.getLog(Constants.CampaignFinisherLogger);

	PropertiesConfiguration prop = null;

	private static final String className = "PollerCampaignMasterCompleted";
	String methodName = null;

	private String instanceId = "";

	Map<String, String> configParamsTon = null;

	private long sleepTime = 1000;

	public PollerCampaignMasterCompleted() throws Exception {
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

	public void updateCampaignMasterStatus() throws Exception {
		String methodName = " [updateCampaignMasterStatus] ";

		UpdateCampaignMasterCompletedDAO dao = new UpdateCampaignMasterCompletedDAO();
		com.winnovature.campaignfinisher.utils.Utility utils = new com.winnovature.campaignfinisher.utils.Utility();
		try {
			// get campaign_master inprocess records
			List<Map<String, String>> campaignMasterList = dao.getInprocessRecords();

			if (campaignMasterList == null || campaignMasterList.size() == 0) {
				// No data found let consumer sleep for some time
				consumerSleep(sleepTime);
				return;
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " campaign_master inprocess count :" + campaignMasterList.size());
			}

			for (int i = 0; i < campaignMasterList.size(); i++) {
				Map<String, String> campaignMaster = campaignMasterList.get(i);
				String id = campaignMaster.get("id");
				Map<String, String> campaignFilesStatus = dao.getCampaignFiles(id);

				String total = campaignFilesStatus.get("total");

				if (log.isDebugEnabled())
					log.debug(className + methodName + " campaign_master record id:" + id + " campaignFilesStatus:"
							+ campaignFilesStatus);

				// removal using updateSuccess flag.
				boolean updateSuccess = dao.updateCampaignMasterCompleted(id, campaignFilesStatus);
				if (updateSuccess) { 
					// Add tag ids to DeleteQ 
					boolean status = utils.handoverCampIdstoDeleteQ(id + ":" + total);
					if (!status)
						log.debug(className + methodName + Constants.DELETE_CAMPID_QUEUE_NAME
								+ " Queue HO Failed - campaign id:" + id + " status:" + status + " of count:" + total);
				}
			} // End of for Loop
		} catch (Exception e) {
			log.error(className + methodName + " Exception:", e);
			throw e;
		}

	}

	/*
	 * private static String convertToJSON(Map<String, String> jsonString) throws
	 * Exception { ObjectMapper mapper = new ObjectMapper(); String json = null;
	 * 
	 * json = mapper.writeValueAsString(jsonString);
	 * 
	 * return json; }
	 */

	@Override
	public void run() {
		methodName = " [run] ";
		while (true) {
			try {
				// HeartBeat
				String timeStampAsString = Utility.getTimestampAsString();
				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-CampaignFinisher", className, instanceId,
						this.getName(), timeStampAsString);

				updateCampaignMasterStatus();
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
