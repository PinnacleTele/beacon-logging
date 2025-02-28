package com.winnovature.handoverstage.consumers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.messageidentifier.MessageIdentifier;
import com.winnovature.handoverstage.daos.GenericDAO;
import com.winnovature.handoverstage.processors.ProcessMTM;
import com.winnovature.handoverstage.processors.ProcessOTM;
import com.winnovature.handoverstage.processors.ProcessTEM;
import com.winnovature.handoverstage.singletons.RedisConnectionFactory;
import com.winnovature.handoverstage.utils.Constants;
import com.winnovature.handoverstage.utils.Utility;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.dtos.Templates;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.JsonUtility;

import redis.clients.jedis.Jedis;

public class SplitFileConsumer extends Thread {
	static Log logger = LogFactory.getLog(Constants.HandoverStageLogger);
	private static final String className = "[SplitFileConsumer]";

	private RedisServerDetailsBean bean;
	private String instanceId = "";
	private String deliveryQ = "";
	private long sleepTime = 1000;
	long nextRequestPickDelay = 1000l;
	
	public SplitFileConsumer(String queue, RedisServerDetailsBean bean,
			String instanceId) {
		this.bean = bean;
		this.deliveryQ = queue;
		this.instanceId = instanceId;
		this.sleepTime = com.winnovature.utils.utils.Utility
				.getConsumersSleepTime();
	}

	@Override
	public void run() {
		String methodName = " [run()] ";

		while (true) {
			try {
				String timeStampAsString = com.winnovature.utils.utils.Utility.getTimestampAsString();

				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-HandoverStage",
						deliveryQ, instanceId, this.getName(),
						timeStampAsString);

				process();

			} // end of try
			catch (Exception ex) {
				logger.error(className + methodName
						+ " exception in run ", ex);
				int threadSleepTime = com.winnovature.utils.utils.Utility
						.getThreadSleepTime();
				logger.error(className + methodName + " : " + this.getName()
						+ " will sleep for " + threadSleepTime
						+ " milli seconds then restarts.");
				consumerSleep(threadSleepTime);
			}// end of catch

		} // end of while
	} // end of run

	private void consumerSleep(long sleepTime) {
		try {
			TimeUnit.MILLISECONDS.sleep(sleepTime);
		} catch (Exception e) {
			logger.error(className + " [consumerSleep] Exception:",
					e);
		}
	}

	private void process() throws Exception {

		String methodName = " [process()] ";
		Map<String, String> map = null;
		String splitFileMetaData = null;
		Jedis jedisConnection = null;
		String campIdQueue = null;
		Map<String, String> configMap = null;
		String productName = null;
		Utility utility = new Utility();

		try {

			if (logger.isDebugEnabled()) {
				logger.debug(className + " Looking up DQ " + deliveryQ
						+ " in redis " + bean.getIpAddress() + ":"
						+ bean.getPort() + " ");
			}

			configMap = (HashMap<String, String>) ConfigParamsTon.getInstance()
					.getConfigurationFromconfigParams();
			productName = configMap.get(Constants.PRODUCT_NAME);

			try {
				String nextRequestPickDelayAsString = configMap
						.get(Constants.DE_NEXT_REQUEST_POP_DELAY);
				nextRequestPickDelay = Long
						.parseLong(nextRequestPickDelayAsString);
			} catch (Exception e) {
				logger.error(className + methodName + " Exception in parsing "
						+ e.getMessage() + " taking default 1 sec");
				nextRequestPickDelay = 1000l;
			}

			jedisConnection = RedisConnectionFactory.getInstance()
					.getConnection(bean.getRid());

			campIdQueue = jedisConnection.rpoplpush(deliveryQ,
					deliveryQ);

			if (campIdQueue != null) {

				splitFileMetaData = jedisConnection.rpoplpush(campIdQueue,
						productName + ":processing:" + campIdQueue);

				if (splitFileMetaData != null) {

					if (logger.isDebugEnabled()) {
						logger.debug(className + methodName
								+ " Found cm_id :" + campIdQueue
								+ " and its metadata: " + splitFileMetaData);
					}

					map = new JsonUtility().convertJsonStringToMap(splitFileMetaData);

					if (logger.isDebugEnabled()) {
						logger.debug(className + " Request consumed:" + map);
						logger.debug(className
								+ " Request will be removed from queue and processing business logic");
					}

					//String id = map.get("ID").toString();
					map.put("DQQUEUENAME", deliveryQ);
					handoverToProcess(map, splitFileMetaData, campIdQueue);

					if (logger.isDebugEnabled()) {
						logger.debug(className
								+ " Request completed. Removing from deliveryQ queue:"
								+ map);
					}

				}

				// close Jedis con 1 req processed and thread is going to sleep.
				if (jedisConnection != null) {
					jedisConnection.close();
				}

				// request processing done, wait for some time before looking up
				// for other requests.
				consumerSleep(nextRequestPickDelay);
			} else {
				// close Jedis con since thread is going to sleep.
				if (jedisConnection != null) {
					jedisConnection.close();
				}
				// No data found let consumer rest for some time
				consumerSleep(sleepTime);
			}

		} catch (Exception e) {

			if (jedisConnection != null) {
				jedisConnection.close();
			}

			logger.error(className + methodName
					+ " Exception", e);

			if (map == null) {
				logger.error(className + methodName
						+ " Parse exception in HandoverStage module for cm_id:"
						+ campIdQueue);
			} else {
				if (campIdQueue != null && splitFileMetaData != null) {

					String noOfTimesRetry = configMap
							.get(com.winnovature.utils.utils.Constants.MAX_RETRY_COUNT);
					String id = map.get("c_f_s_id") == null ? "0" : map.get("c_f_s_id");
					long retryCount = utility.getRetryCountFromRedis(productName, id, bean.getRid());

					String status = "FAILED";

					if (retryCount <= Integer.parseInt(noOfTimesRetry)) {
						try {
							utility.repushSplitRecord(productName, campIdQueue,
									splitFileMetaData, deliveryQ, bean.getRid());
						} catch (Exception e1) {
							logger.error(className + methodName
									+ " Exception while repushing back to deliveryQ:"
									+ bean.getRid() + " error:", e);
							try {
								// second level Redis.
								utility.repushToRedisQueueInRoundRobin(productName,
										campIdQueue, splitFileMetaData, 1,
										Integer.parseInt(noOfTimesRetry), id,
										status, deliveryQ, bean.getRid());
							} catch (Exception ex) {
								logger.error(className
												+ methodName
												+ " Exception while pushing back to secondry redis :"
												+ " cm_id[" + map.get("cm_id")
												+ " SplitfileData["
												+ splitFileMetaData + "]"
												+ " error:", ex);
							}
						}
					} else {
						try {
							String sql = new GenericDAO()
									.updateFailedRequestToQueuedSql(
											id,
											status,
											"Maximum retried in HandoverStage module",
											Integer.parseInt(noOfTimesRetry) + 1);

							// Update to DB as failed
							utility.sendToUpdateSQLQueue(sql, bean.getRid());

							// remove from tempQ
							utility.removeFromProcessingQueue(productName, campIdQueue,
									splitFileMetaData, deliveryQ, id);

						} catch (Exception e1) {
							logger.error(className + methodName
									+ " Exception while updating as failed"
									+ " cm_id[" + map.get("cm_id") + " for cf_id:"
									+ id + "] error:", e1);

							try {
								new GenericDAO().updateFailedRequestToQueued(id,
										status,
										"Maximum retried in HandoverStage module",
										Integer.parseInt(noOfTimesRetry) + 1);

								// remove from tempQ
								utility.removeFromProcessingQueue(productName,
										campIdQueue, splitFileMetaData,
										deliveryQ, id);

							} catch (Exception ex2) {
								logger.error(className
												+ methodName
												+ " Exception while DB update as FAILED :"
												+ " cm_id[" + map.get("cm_id")
												+ " for cf_id:" + id
												+ " SplitfileData["
												+ splitFileMetaData + "]"
												+ " error:", ex2);

								// remove from tempQ
								utility.removeFromProcessingQueue(productName,
										campIdQueue, splitFileMetaData,
										deliveryQ, id);
							}
						}
					}
				} // end of campIdQueue and metadata Q not null
			}
			throw e;
		}
	}

	public void handoverToProcess(Map<String, String> splitFileDetails,
			String splitFileMetaData, String campIdQueue) throws Exception {
		
		String methodName = " [handoverToProcess] ";

		if (logger.isDebugEnabled())
			logger.debug(className + methodName + " Starting Core Logic \n - "
					+ splitFileDetails);
		
		Map<String, String> configMap = ConfigParamsTon.getInstance()
				.getConfigurationFromconfigParams();
		String productName = configMap.get(Constants.PRODUCT_NAME);
		
		Utility utility = new Utility();
		try {
			String platformAppInstanceId = MessageIdentifier.getInstance().getAppInstanceId();
			String type = splitFileDetails.get("c_type");
			String clientId = splitFileDetails.get("cli_id");
			// String to lookup for linebreak replacement.
			String newLineChar = configMap
					.get(com.winnovature.utils.utils.Constants.LINE_BREAK_REPLACER);
			splitFileDetails.put(Constants.NEW_LINE_CHAR, newLineChar);
			splitFileDetails.put("delimiter", configMap.get(com.winnovature.utils.utils.Constants.SPLIT_FILE_DELIMITER));
			splitFileDetails.put("APP_INSTANCE_ID", platformAppInstanceId);
			if (logger.isDebugEnabled())
				logger.debug(className
								+ methodName
								+ " Starting Core Logic \n - "
								+ com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER
								+ splitFileDetails.get("c_f_s_id") + " type: " + type
								+ " clientId:" + clientId);

			if (type.equalsIgnoreCase(com.winnovature.utils.utils.Constants.OTM_CAMP)
					|| type.equalsIgnoreCase(com.winnovature.utils.utils.Constants.GROUP_CAMP)
					|| type.equalsIgnoreCase(com.winnovature.utils.utils.Constants.QUICK_CAMP)) {
				new ProcessOTM().processData(splitFileDetails);
			} else if (type.equalsIgnoreCase(com.winnovature.utils.utils.Constants.MTM_CAMP)) {
				new ProcessMTM().processData(splitFileDetails);
			} else if (type.equalsIgnoreCase(com.winnovature.utils.utils.Constants.TEM_CAMP)) {
				// if file type is template - get template related details
				String jsonTemplateDetails = splitFileDetails
						.get("TEMPLATE_BEAN");
				Templates tmplDetails = new Templates();
				tmplDetails = new com.winnovature.utils.utils.JsonUtility().convertJsonToTemplate(
						jsonTemplateDetails, tmplDetails);

				new ProcessTEM().processData(splitFileDetails, tmplDetails);
			}

			// remove from processing queue
			utility.removeFromProcessingQueue(productName, campIdQueue,
					splitFileMetaData, deliveryQ,
					splitFileDetails.get("c_f_s_id"));

			if (logger.isDebugEnabled()) {
				logger.debug(className + " removed from processing queue id:"
						+ splitFileDetails.get("c_f_s_id"));
				logger.debug(className + methodName
						+ " Duplicate Queue insertion Starting..");
			}

			// Insert into Dupcheck queue for tracking of completion
			// utility.insertIntoDupQueue(productName, campIdQueue, splitFileMetaData);

		} catch (Exception ex) {
			logger.error(className + methodName
					+ "FileConsumer.handoverToProcess()  Exception "
					+ com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + splitFileDetails.get("c_f_s_id")
					+ ex);
			throw ex;
		}

		if (logger.isDebugEnabled()) {
			logger.debug(className + methodName + " Core Logic Completed.");
		}

	}
	
}
