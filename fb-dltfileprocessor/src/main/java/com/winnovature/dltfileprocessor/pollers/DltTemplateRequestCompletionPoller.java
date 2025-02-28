package com.winnovature.dltfileprocessor.pollers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.dltfileprocessor.daos.DltTemplateRequestDAO;
import com.winnovature.dltfileprocessor.singletons.DltFileProcessorPropertiesTon;
import com.winnovature.dltfileprocessor.utils.Constants;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.Utility;

public class DltTemplateRequestCompletionPoller extends Thread {

	static Log log = LogFactory.getLog(Constants.FileUploadLogger);

	PropertiesConfiguration prop = null;

	private static final String className = "DltTemplateRequestCompletionPoller";
	String methodName = null;

	private String instanceId = "";

	Map<String, String> configParamsTon = null;

	private long sleepTime = 1000;

	public DltTemplateRequestCompletionPoller() throws Exception {
		try {
			prop = DltFileProcessorPropertiesTon.getInstance().getPropertiesConfiguration();
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

	public void updateDltTemplateRequestStatus() throws Exception {
		String methodName = " [updateDltTemplateRequestStatus] ";

		DltTemplateRequestDAO dao = new DltTemplateRequestDAO();
		try {
			// get dlt_template_request inprocess records
			List<Map<String, String>> dltRequestList = dao.getInprocessRecords();

			if (dltRequestList == null || dltRequestList.size() == 0) {
				// No data found let consumer sleep for some time
				consumerSleep(sleepTime);
				return;
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " dlt_template_request inprocess count :" + dltRequestList.size());
			}

			for (int i = 0; i < dltRequestList.size(); i++) {
				Map<String, String> dltReq = dltRequestList.get(i);
				String id = dltReq.get("id");
				Map<String, String> dltFilesStatus = dao.getDltTemplateFiles(id);
				
				if (log.isDebugEnabled())
					log.debug(className + methodName + " dlt_template_files record id:" + id + " dltFilesStatus:"
							+ dltFilesStatus);

				dao.updateDltTemplateRequestCompleted(id, dltFilesStatus);
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
				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-DltFileProcessor", className, instanceId,
						this.getName(), timeStampAsString);

				updateDltTemplateRequestStatus();
			} catch (Exception e) {
				log.error(className + methodName + " RUNTIME EXCEPTION Update group_files status Thread  ****\n", e);
				int threadSleepTime = Utility.getThreadSleepTime();
				log.error(className + methodName + " Update dlt_template_request status Thread will sleep for "
						+ threadSleepTime + " milli seconds then restarts.");
				consumerSleep(threadSleepTime);
			}
		} // end of while
	}

}
