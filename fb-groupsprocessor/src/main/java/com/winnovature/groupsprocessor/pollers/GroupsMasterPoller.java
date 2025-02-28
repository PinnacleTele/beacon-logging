package com.winnovature.groupsprocessor.pollers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.groupsprocessor.daos.GroupsMasterDAO;
import com.winnovature.groupsprocessor.singletons.GroupsProcessorPropertiesTon;
import com.winnovature.groupsprocessor.utils.Constants;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.Utility;

public class GroupsMasterPoller extends Thread {

	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);
	private static final String className = "[GroupsMasterPoller]";
	private String threadName = null;

	public GroupsMasterPoller(String threadName) throws Exception {
		this.threadName = threadName;
	}

	@Override
	public void run() {
		String methodName = " [run] ";
		while (true) {
			try {
				String instanceId = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration()
						.getString(Constants.MONITORING_INSTANCE_ID);
				String timeStampAsString = Utility.getTimestampAsString();
				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-GroupsProcessor", "GroupsMasterPoller", instanceId,
						this.getName(), timeStampAsString);
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
		String groupQueueName = null;
		String maxRetryCount = null;
		try {
			configMap = (HashMap<String, String>) ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
			groupQueueName = configMap.get(com.winnovature.utils.utils.Constants.GROUP_QUEUE_NAME);
			maxRetryCount = configMap.get(com.winnovature.utils.utils.Constants.MAX_RETRY_COUNT);
			if (StringUtils.isBlank(groupQueueName)) {
				throw new Exception(" GroupQ name not found ");
			}
		} catch (Exception e) {
			throw e;
		}

		if (StringUtils.isBlank(maxRetryCount)) {
			maxRetryCount = "5";
		}

		try {
			// get groups data and send to GroupQ
			new GroupsMasterDAO().pollGroups(maxRetryCount, groupQueueName);
		} catch (Exception e) {
			throw e;
		}
	}
}
