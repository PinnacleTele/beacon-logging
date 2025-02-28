package com.winnovature.utils.singletons;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.utils.daos.GenericDao;
import com.winnovature.utils.dtos.QueueType;
import com.winnovature.utils.utils.Constants;

public class DeliveryEngineQueueTon {

	private static String className = "[DeliveryEngineQueueTon]";
	static Log log = LogFactory.getLog(Constants.UtilsLogger);

	private static DeliveryEngineQueueTon singleton = new DeliveryEngineQueueTon();
	private static Map<Long, QueueType> countVsQueue = null;

	private DeliveryEngineQueueTon() {

	}

	public static DeliveryEngineQueueTon getInstance() {
		return singleton;
	}

	public QueueType getQueueNameBasedOnCount(long total) {
		
		String logName = className + "[getQueueNameBasedOnCount]";

		if (log.isDebugEnabled())
			log.debug(className
					+ " [DeliveryEngineQueueTon].getQueueNameBasedOnCount()");

		if (countVsQueue == null || countVsQueue.size() <= 0) {
			initializeQueueNameMappingMap();
		}

		QueueType queueType = new QueueType();
		String queue = null;

		try {

			queue = UtilsPropertiesTon.getInstance()
					.getPropertiesConfiguration()
					.getString(Constants.DEFAULT_HIGH_VOL_QUEUE_NAME);
			queueType.setQueueName(queue);
			
			if (total > 0) {
				for (Long totalKey : countVsQueue.keySet()) {
					if (totalKey > total) {
						queueType = countVsQueue.get(totalKey);
					}
				}
			}
		} catch (Exception e) {
			log.error(logName + "Exception: ",e);
			countVsQueue = null;
		}
		return queueType;
	}

	private void initializeQueueNameMappingMap() {
		String logName = className + "[initializeQueueNameMappingMap]";
		if (log.isDebugEnabled())
			log.debug(logName);
		try {
			reloadFromDB();
		} catch (Exception e) {
			log.error(logName + "Exception: ",e);
			countVsQueue = null;
		}
	}

	public void reloadFromDB() throws Exception {

		String logName = className + "[reloadFromDB]";
		try {
			countVsQueue = new GenericDao().getQueueDetails();

			if (log.isDebugEnabled())
				log.debug(className
						+ " [DeliveryEngineQueueTon].reloadFromDB() countVsQueue="
						+ countVsQueue);

		} catch (Exception e) {
			log.error(logName + "Exception: ",e);
			countVsQueue = null;
			throw e;
		}
	}
}