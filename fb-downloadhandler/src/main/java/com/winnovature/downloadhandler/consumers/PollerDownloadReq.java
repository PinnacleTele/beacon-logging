package com.winnovature.downloadhandler.consumers;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.downloadhandler.daos.DownloadReqDAO;
//import com.winnovature.campaignfinisher.daos.UpdateCampaignFilesCompletedDAO;
import com.winnovature.downloadhandler.singletons.DownloadHandlerPropertiesTon;
import com.winnovature.downloadhandler.utils.Constants;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.Utility;

public class PollerDownloadReq extends Thread {

	static Log log = LogFactory.getLog(Constants.DownloadHandlerLogger);

	PropertiesConfiguration prop = null;

	private static final String className = "PollerDownloadReq";
	String methodName = null;

	private String instanceId = "";

	Map<String, String> configParamsTon = null;

	public PollerDownloadReq() throws Exception {
		try {
			prop = DownloadHandlerPropertiesTon.getInstance().getPropertiesConfiguration();
			this.instanceId = prop.getString(Constants.MONITORING_INSTANCE_ID);
		} catch (Exception e) {
			log.error(className + "PropertiesLoadException: ", e);
		}
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

	@Override
	public void run() {
		methodName = " [run] ";
		DownloadReqDAO dao = new DownloadReqDAO();
		String csvCompleteStatus = prop.getString("csv.file.complete.status");
		String redisQueueName = prop.getString("csv.excel.convertion.queue.name");
		while (true) {
			try {
				// HeartBeat
				String timeStampAsString = Utility.getTimestampAsString();
				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-DownloadHandler", className, instanceId,
						this.getName(), timeStampAsString);

				dao.pollDownloadReq(csvCompleteStatus, redisQueueName);
			} catch (Exception e) {
				log.error(className + methodName + " RUNTIME EXCEPTION polling download_req table Thread  ****\n", e);
				int threadSleepTime = Utility.getThreadSleepTime();
				log.error(className + methodName + " polling download_req table, Thread will sleep for "
						+ threadSleepTime + " milli seconds then restarts.");
				consumerSleep(threadSleepTime);
			}
		} // end of while
	}

}
