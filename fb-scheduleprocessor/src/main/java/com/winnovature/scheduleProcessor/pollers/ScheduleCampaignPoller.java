package com.winnovature.scheduleProcessor.pollers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.scheduleProcessor.utils.Constants;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.scheduleProcessor.daos.CampaignScheduleMasterDAO;
import com.winnovature.scheduleProcessor.singletons.ScheduleProcessorPropertiesTon;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.Utility;

public class ScheduleCampaignPoller extends Thread {

	static Log log = LogFactory.getLog(Constants.ScheduleProcessorLogger);
	private static final String className = "[ScheduleCampaignPoller]";
	private String threadName = null;

	public ScheduleCampaignPoller(String threadName) throws Exception {
		this.threadName = threadName;
	}

	@Override
	public void run() {
		String methodName = " [run] ";
		while (true) {
			try {
				String instanceId = ScheduleProcessorPropertiesTon.getInstance().getPropertiesConfiguration().getString(Constants.MONITORING_INSTANCE_ID);
				String timeStampAsString = Utility.getTimestampAsString();
				
				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-ScheduleProcessor", "ScheduleCampaignPoller", instanceId, this.getName(), timeStampAsString);
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
		
		String maxRetryCount = null;
		try {
			
			configMap = (HashMap<String, String>) ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
			
			maxRetryCount = configMap.get(com.winnovature.utils.utils.Constants.MAX_RETRY_COUNT);
		
		} catch (Exception e) {
			throw e;
		}
		
		if(StringUtils.isBlank(maxRetryCount)) {
			maxRetryCount = "5";
		}
		try {
			
			// get campaigns data and send to FileSplitQ/GroupQ
			new CampaignScheduleMasterDAO().pollcampScheduleMaster(maxRetryCount);
			
		} catch (Exception e) {
			throw e;
		}	
	}
}


