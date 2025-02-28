package com.winnovature.initialstate.pollers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.initialstate.daos.CampaignMasterDAO;
import com.winnovature.initialstate.singletons.InitialStagePropertiesTon;
import com.winnovature.initialstate.utils.Constants;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.Utility;

public class CampaignMasterPoller extends Thread {

	static Log log = LogFactory.getLog(Constants.InitialStageLogger);
	private static final String className = "[CampaignMasterPoller]";
	private String threadName = null;

	public CampaignMasterPoller(String threadName) throws Exception {
		this.threadName = threadName;
	}

	@Override
	public void run() {
		String methodName = " [run] ";
		while (true) {
			try {
				String instanceId = InitialStagePropertiesTon.getInstance().getPropertiesConfiguration().getString(Constants.MONITORING_INSTANCE_ID);
				String timeStampAsString = Utility.getTimestampAsString();
				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-InitialStage", "CampaignMasterPoller", instanceId, this.getName(), timeStampAsString);
				poll();
			} catch (Exception e) {
				log.error(className + methodName + " RUNTIME EXCEPTION campaign_master Fetch Thread  \n", e);
				int threadSleepTime = Utility.getThreadSleepTime();
				log.error(className + methodName + " : " + threadName + "  will sleep for " + threadSleepTime
						+ " milli seconds then restarts.");
				consumerSleep(threadSleepTime);
			}
		} // end of while
	}

	private void consumerSleep(long sleepTime) {
		try {
			TimeUnit.MILLISECONDS.sleep(sleepTime);
		} catch (Exception e) {
			log.error(className, e);
		}
	}

	private void poll() throws Exception {
		Map<String, String> configMap = null;
		String fileSplitQueueName = null;
		String maxRetryCount = null;
		try {
			configMap = (HashMap<String, String>) ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
			fileSplitQueueName = configMap.get(com.winnovature.utils.utils.Constants.FILE_SPLIT_QUEUE_NAME);
			maxRetryCount = configMap.get(com.winnovature.utils.utils.Constants.MAX_RETRY_COUNT);
			if (StringUtils.isBlank(fileSplitQueueName)) {
				throw new Exception(" FileSplitQ name not found ");
			}
		} catch (Exception e) {
			throw e;
		}
		
		if(StringUtils.isBlank(maxRetryCount)) {
			maxRetryCount = "5";
		}

		try {
			// get campaigns data and send to FileSplitQ
			new CampaignMasterDAO().pollCampaigns(maxRetryCount, fileSplitQueueName);
		} catch (Exception e) {
			throw e;
		}
	}
}
