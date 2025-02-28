package com.winnovature.dltfileprocessor.pollers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.dltfileprocessor.daos.DltTemplateRequestDAO;
import com.winnovature.dltfileprocessor.singletons.DltFileProcessorPropertiesTon;
import com.winnovature.dltfileprocessor.utils.Constants;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.Utility;

public class DltTemplateRequestPoller extends Thread {

	static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	private static final String className = "[dltTemplateRequestPoller]";
	private String threadName = null;

	public DltTemplateRequestPoller(String threadName) throws Exception {
		this.threadName = threadName;
	}

	@Override
	public void run() {
		String methodName = " [run] ";
		while (true) {
			try {
				String instanceId = DltFileProcessorPropertiesTon.getInstance().getPropertiesConfiguration().getString(Constants.MONITORING_INSTANCE_ID);
				String timeStampAsString = Utility.getTimestampAsString();
				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-DltFileProcessor", "dltTemplateRequestPoller", instanceId, this.getName(), timeStampAsString);
				poll();
			} catch (Exception e) {
				log.error(className + methodName + " RUNTIME EXCEPTION dlt_template_request Fetch Thread  \n", e);
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
		String queueName = null;
		try {
			configMap = (HashMap<String, String>) ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
			queueName = configMap.get(Constants.DLT_FILE_QUEUE_NAME);
			if (StringUtils.isBlank(queueName)) {
				throw new Exception(" DltFileQ name not found ");
			}
		} catch (Exception e) {
			throw e;
		}
		
		try {
			// get DLT Template requests and send to DltFileQ
			new DltTemplateRequestDAO().pollDltTemplateRequest(queueName);
		} catch (Exception e) {
			throw e;
		}
	}
}
