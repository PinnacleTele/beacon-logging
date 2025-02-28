package com.winnovature.exclude.consumers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.exclude.daos.CampaignDAO;
import com.winnovature.exclude.processors.FileDataExtractor;
import com.winnovature.exclude.singletons.RedisConnectionFactory;
import com.winnovature.exclude.singletons.RedisConnectionTon;
import com.winnovature.exclude.utils.Constants;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.JsonUtility;
import com.winnovature.utils.utils.Utility;

import redis.clients.jedis.Jedis;

public class ExcludeConsumer extends Thread {
	static Log logger = LogFactory.getLog(Constants.ExcludeLogger);
	static Log htLogger = LogFactory.getLog(Constants.ExcludeHeartBeat);
	
	private static final String className = "[ExcludeConsumer]";
	private String queueName = "";
	private RedisServerDetailsBean bean;
	PropertiesConfiguration prop = null;
	public static final String EXCLUDE = "_exclude";
	private String instanceId = "";
	private long sleepTime = 1000;
	long nextRequestPickDelay = 1000l;
	Map<String, String> configMap = null;

	public ExcludeConsumer(String queueName,
			RedisServerDetailsBean bean, String instanceId) {
		this.bean = bean;
		this.queueName = queueName;
		this.instanceId = instanceId;
		this.sleepTime = Utility.getConsumersSleepTime();
	}

	@Override
	public void run() {
		String methodName = "[run()]";

		while (true) {
			try {
				String timeStampAsString = Utility.getTimestampAsString();
				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-ExcludeProcessor", className, instanceId,
						this.getName(), timeStampAsString);

				process();

			}// end of try
			catch (Exception ex) {
				logger.error("ExcludeConsumer exception in run", ex);
				int threadSleepTime = Utility.getThreadSleepTime();
				logger.error(className + methodName + " : "+this.getName()+" will sleep for " + threadSleepTime+" milli seconds then restarts."); 
				consumerSleep(threadSleepTime);
			}// end of catch
		} // end of while
	}
	
	private void consumerSleep(long sleepTime) {
		try {
			TimeUnit.MILLISECONDS.sleep(sleepTime);
		} catch (InterruptedException e) {
			logger.error("*** " + className, e);
		} catch (Exception e) {
			logger.error("*** " + className, e);
		}
	}

	public void process() throws Exception {
		String methodName = " [process] ";
		Map<String, String> map = null;
		String splitFileMetaData = null;
		Jedis conn = null;
		String campIdQueue = null;

		try {
			if (htLogger.isDebugEnabled()) {
				htLogger.debug(className + " Looking up ExcludeQ "+queueName+" in redis " + bean.getIpAddress()+":"+bean.getPort());
			}
			
			try {
				configMap = (HashMap<String, String>) ConfigParamsTon.getInstance()
						.getConfigurationFromconfigParams();
				String nextRequestPickDelayAsString = configMap.get(Constants.EE_NEXT_REQUEST_POP_DELAY);
				nextRequestPickDelay = Long.parseLong(nextRequestPickDelayAsString);
			}catch(Exception e) {
				logger.error(className + methodName + " Exception in parsing "+ e.getMessage()+" taking default 1 sec");
				nextRequestPickDelay = 1000l;
			}

			conn = RedisConnectionFactory.getInstance().getConnection(bean.getRid());

			campIdQueue = conn.rpoplpush(queueName, queueName);

			if (campIdQueue != null) {
				
				splitFileMetaData = conn.rpop(campIdQueue);
				
				if(conn != null){
					conn.close();
				}
				
				if (splitFileMetaData != null) {
					
					if (logger.isDebugEnabled()) {
						logger.debug(className + methodName + " Found campaign id :"+campIdQueue+" and its metadata: "+splitFileMetaData);
					}
					
					map = new JsonUtility().convertJsonStringToMap(splitFileMetaData);

					handoverToProcess(map, splitFileMetaData, campIdQueue);

					if (logger.isDebugEnabled()) {
						logger.debug(className
								+ " Request completed. Removing from queueName queue:"
								+ map);
					}
				} // end of if splitFileMetaData != null
				else {
					if (htLogger.isDebugEnabled()) {
						htLogger.debug(className + " Metadata not found for campaign id="	+ campIdQueue);
					}
				}
				// request processing done, wait for some time before looking up for other requests.
				consumerSleep(nextRequestPickDelay);
			}else{
				if (conn != null) {
					conn.close();
				}
				// No data found let consumer rest for some time
				consumerSleep(sleepTime);
			}

		} catch (Exception e) {
			logger.error("ExcludeConsumer exception", e);
			throw e;
		}
	}


	public void updateExcludeFileStatus(String id, String file_loc,String fileName,
			String status, String reason,int excludeCount, int fileNooftimesProcess, int allrecordExcude) {
		String methodName = " [updateExcludeFileStatus] ";
		methodName += com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id+" ";
		try {
			String sql = new CampaignDAO().updateFileFailedRequestToQueuedSql(id,
					file_loc, status, reason,excludeCount,fileNooftimesProcess,allrecordExcude);
			sendToUpdateSQLQueue(sql);
		} catch (Exception e1) {
			logger.error(
					className
							+ methodName
							+ " Exception while preparing campaign_file_splits update query for exclude file status or sending to Update Queue "
							+ " id[" + id + "] error:", e1);

			try {
				new CampaignDAO().updateFailedRequestToQueued(id, file_loc,
						status, reason,excludeCount,fileNooftimesProcess,allrecordExcude);
			} catch (Exception ex2) {
				logger.error(
						className
								+ methodName
								+ " Exception while updating exclude file status in campaign_file_splits "
								+ " id[" + id + "] error:", ex2);
			}

		}
	}

	public void handoverToProcess(Map<String, String> splitFileDetails,
			String splitFileMetaData, String campIdQueue) throws Exception {
		String methodName = " [ExcludeConsumer] [handoverToProcess()] ";
		methodName += com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + splitFileDetails.get("c_f_s_id")+" ";
		
		if (logger.isDebugEnabled()) {
			logger.debug(methodName+" Starting Core Logic \n - "
				+ splitFileDetails);
		}
		
		Map<String, String> configMap = ConfigParamsTon.getInstance()
				.getConfigurationFromconfigParams();
		String productName = configMap.get(Constants.PRODUCT_NAME);
		String fileName = splitFileDetails.get("fileloc");
		String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
		String exc_groupIds = splitFileDetails.get("exclude_group_ids");
		String type = splitFileDetails.get("c_type");
		String delimiter = splitFileDetails.get("delimiter") == null ? ""
				: splitFileDetails.get("delimiter").toString();
		String c_f_s_id = splitFileDetails.get("c_f_s_id");
		String clientId = splitFileDetails.get("cli_id");
		String fileId = splitFileDetails.get("fileid");
		File file = new File(fileName);
		
		int noOfProcess = Integer.parseInt(splitFileDetails.get("retry_count") == null ? "0"
				: splitFileDetails.get("retry_count").toString());
	
		String json = "";
		int excludeCnt = 0;
		int fileNooftimesProcess = 0;
		//ArrayList<UnprocessRow> lstUnProcessRow = null;
		try {
			fileNooftimesProcess = Integer.parseInt(configMap
					.get(com.winnovature.utils.utils.Constants.MAX_RETRY_COUNT));

			// totalMap(total) = total
			// totalMap(file_loc) = file_loc (non-exclude file)
			// totalMap(excludeCnt) = excludeCnt
			// totalMap(exc_file_loc) = exc_file_loc (exclude file)
			
			if(splitFileDetails.get("EXCLUDE_FILE_LOC") == null || splitFileDetails.get("EXCLUDE_FILE_LOC").isEmpty()) {
				Map<String, String> totalMap = new FileDataExtractor().process(fileType, file,
						 exc_groupIds, delimiter, clientId,fileId,c_f_s_id,type, splitFileDetails.get("username"), splitFileDetails.get("cm_id"));
				int fileTotalCnt = Integer.parseInt(totalMap.get("total")
						.toString());
				String file_loc = totalMap.get("file_loc");
				String exclude_file_loc = totalMap.get("exc_file_loc");
				excludeCnt = Integer.parseInt(totalMap.get("excludeCnt"));
				
				if(logger.isDebugEnabled()) {
					logger.debug(methodName + " Exclude statistics ::: "+totalMap);
				}
				
				if (fileTotalCnt == 0) {
					updateExcludeFileStatus(c_f_s_id, file_loc,fileName,
							Constants.STATUS_COMPLETED, Constants.FAILURE_REASON,excludeCnt,-1,1);
					splitFileDetails.put("DQHADOVER","FALSE");
					Map<String,String> excludeNumber = new HashMap<String,String>();
					excludeNumber.put("c_f_s_id", c_f_s_id);
					excludeNumber.put("EXCLUDE_FILE", exclude_file_loc);
					excludeNumber.put("cli_id", splitFileDetails.get("cli_id"));
					excludeNumber.put("fileid", splitFileDetails.get("fileid"));

					//pushToExcludeNumberQueue(excludeNumber);
				} else {	
					updateExcludeFileStatus(c_f_s_id, file_loc,fileName,
							null,"",0,-1,0);
					// HO non-exclude numbers file to DQ.
					splitFileDetails.put("fileloc", file_loc);
					splitFileDetails.put("total", String.valueOf(fileTotalCnt));
					splitFileDetails.put("exclude", String.valueOf(excludeCnt));
					splitFileDetails.put("EXCLUDE_FILE_LOC", exclude_file_loc);
					splitFileDetails.put("DQHADOVER","TRUE");
				}
			}
			boolean queueHO = Boolean.parseBoolean(splitFileDetails.get("DQHADOVER"));
			if(queueHO) {
				json = new JsonUtility().convertMapToJSON(splitFileDetails);
				boolean dqHoSuccess = pushToDeliveryQueue(productName,
						campIdQueue, json, c_f_s_id, 0);

				if (dqHoSuccess) {
					// ExcludeFileQ Handover
					if(splitFileDetails.get("EXCLUDE_FILE_LOC") != null && !splitFileDetails.get("EXCLUDE_FILE_LOC").isEmpty())
					{
						Map<String,String> excludeNumber = new HashMap<String,String>();
						excludeNumber.put("c_f_s_id", c_f_s_id);
						excludeNumber.put("EXCLUDE_FILE", splitFileDetails.get("EXCLUDE_FILE_LOC"));
						excludeNumber.put("cli_id", splitFileDetails.get("cli_id"));
						excludeNumber.put("fileid", splitFileDetails.get("fileid"));

						//pushToExcludeNumberQueue(excludeNumber);
					}
					
					//adding queue name to splitFileDetails and passing it to fileStatsCollector
					// remove _exclude from the Q name to send it to DQ
					//String DQName = queueName.replace(EXCLUDE, "");
					//Map<String,String> ssrMetaData = new HashMap<String,String>(splitFileDetails);
					//ssrMetaData.put(MonitoringStatusConstants.STATSTYPE, MonitoringStatusConstants.FILE_PROCESSING_QUEUE);
					//ssrMetaData.put(MonitoringStatusConstants.FILE_PROCESSING_QUEUE, DQName);
					//new FileProcessStatsCollector().fileStatsCollector(ssrMetaData);
				}else {
					repushToExcludeQueue(campIdQueue, splitFileDetails, noOfProcess,excludeCnt);
				}
			}

		} catch (Exception e) {
			logger.error("[ExcludeConsumer.handoverToProcess()] Exception while extracting exclude numbers from file,\n"
					+ e);
			if (e instanceof FileNotFoundException) {
				updateExcludeFileStatus(c_f_s_id, null,fileName, Constants.STATUS_FAILED,
						"FileNotFoundException",0,fileNooftimesProcess,0);
			} else {
				logger.error("[ExcludeConsumer.handoverToProcess()] Hence pushing back to Exclude Queue \n"
						+ e);
				repushToExcludeQueue(campIdQueue, splitFileDetails, noOfProcess,excludeCnt);
			}
			throw e;
		}
	}

	public boolean pushToExcludeNumberQueue(Map<String, String> excludeNumber) {
		String methodName = "pushToExcludeNumberQueue";
		methodName += com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER +excludeNumber.get("fileid")+" ";
		Jedis redisConnection = null;
		String excludenumberQ=null,excludenumber_payload=null;
		try {
			Map<String, String> configMap = ConfigParamsTon.getInstance()
					.getConfigurationFromconfigParams();
			excludenumberQ = configMap.get(Constants.EXCLUDE_NUMBER_FILE_QUEUE_NAME);
			excludenumber_payload = new JsonUtility().convertMapToJSON(excludeNumber);
			redisConnection = RedisConnectionTon.getInstance().getJedisConnectionAsRoundRobin();
			redisConnection.lpush(excludenumberQ, excludenumber_payload);
			return true;
		}catch(Exception e) {
			logger.error(className + methodName + "Error while sending to excludenumberQ[" + excludenumberQ
					+ "] fileId:" + excludeNumber.get("fileid") + " activitiesQ_payload=:" + excludenumber_payload + "\n",
					e);
		}finally {
			if (redisConnection != null)
				redisConnection.close();
		}
		return false;
	}

	public boolean pushToDeliveryQueue(String productName, String tagidQueue,
			String json, String ssd_id, int retryCount) throws Exception {

		String methodName = "pushToDeliveryQueue";
		methodName += com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER +ssd_id+" ";
		boolean dqHoSuccess = false;
		Jedis resource = null;
		try {
			resource = RedisConnectionTon.getInstance()
					.getJedisConnectionAsRoundRobin();
			// remove _exclude from the Q name to send it to DQ
			String DQName = queueName.replace(EXCLUDE, "");
			String campIdQ = tagidQueue.replace(EXCLUDE, "");
			resource.lpush(campIdQ, json);
			resource.lpush(DQName, campIdQ);
			dqHoSuccess = true;
			if (logger.isDebugEnabled())
				logger.debug(className + methodName
						+ " pushed to Delivery Q successfuly");

		} catch (Exception e) {
			logger.error("ExcludeConsumer.pushToDeliveryQueue()  Exception "
					+ e);
			throw e;
		} finally {
			if (resource != null) {
				resource.close();
			}
		}
		return dqHoSuccess;
	}

	public void repushToExcludeQueue(String tagidQueue,
			Map<String, String> splitFileDetails, int retryCount1, int excludeCnt) throws Exception {

		String methodName = "repushToExcludeQueue";
		methodName += com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + splitFileDetails.get("ID")+" ";
		Jedis resource = null;
		//Map<String, String> configMap = null;
		String attempts = "5";
		CampaignDAO campaignDAO = new CampaignDAO();
		try {

			//configMap = (HashMap<String, String>) ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
			//attempts = configMap.get(Constants.DQ_HO_INFINITE_ATTEMPTS_LOG_LIMIT);
			// Incrementing retry count since failed
				String retryCount = splitFileDetails.get("retry_count");
				
				if (retryCount == null || retryCount.trim().length() == 0) {
					retryCount = "0";
				}

				if (logger.isDebugEnabled()) {
					logger.debug(className + methodName + " retryCount" + retryCount);
				}
				if (Integer.parseInt(retryCount) <= Integer.parseInt(attempts)) {
					int retryTime = 0;

					if (retryCount != null) {
						retryTime = Integer.parseInt(retryCount);
					}

					retryTime = retryTime + 1;
					splitFileDetails.put("retry_count",
							String.valueOf(retryTime));
					
					resource = RedisConnectionFactory.getInstance().getConnection(
							bean.getRid());
					Long lrem = resource.lrem(queueName, 0, tagidQueue);

					if (logger.isDebugEnabled()) {
						logger.debug(className + methodName
								+ " Request repushing. Removing campid:" + tagidQueue
								+ " from queue:" + queueName + " lrem:" + lrem);
					}
					String json = new JsonUtility().convertMapToJSON(splitFileDetails);
					resource.lpush(tagidQueue, json);
					resource.lpush(queueName, tagidQueue);
					if (logger.isDebugEnabled())
						logger.debug(className + methodName
								+ " pushed back to Exclude Q successfuly");

				}else {
					// send failure notification on max retry reached
					campaignDAO.updateFailedRequestToQueued(splitFileDetails.get("c_f_s_id"),
							splitFileDetails.get("fileloc"),
							Constants.STATUS_FAILED,"DeliveryQ HO failed in ExcludeEngine",excludeCnt,retryCount1,0);
				}
		} catch (Exception e) {
			// push back to Exclude Queue also fails, then log it.
			logger.error("ExcludeConsumer.repushToExcludeQueue()  Exception "
					+ e);
			try {
				campaignDAO.updateFailedRequestToQueued(splitFileDetails.get("c_f_s_id"),
						splitFileDetails.get("fileloc"),
						Constants.STATUS_FAILED,"DeliveryQ HO failed in ExcludeEngine",excludeCnt,retryCount1,0);
			} catch (Exception e2) {
				// if update status failed in DB , log in a separate logger
				logger
						.error(className
								+ methodName
								+ "DeliveryQ HO failed in ExcludeEngine and Db back update status [id:"
								+ splitFileDetails.get("c_f_s_id") + " File_loc:"
								+ splitFileDetails.get("fileloc") + " ] Exception:", e2);
			}
			
		} finally {
			if (resource != null) {
				resource.close();
			}
		}

	}

	private void sendToUpdateSQLQueue(String sql) throws Exception {

		String methodName = "[sendToUpdateSQLQueue]";
		Map<String, String> configParamsTon = ConfigParamsTon.getInstance()
				.getConfigurationFromconfigParams();

		String statsUpdateStatusQueryQueueName = configParamsTon
				.get(com.winnovature.utils.utils.Constants.STATS_UPDATE_STATUS_QUERY_QUEUE_NAME)
				.toString();
		Jedis redis = null;
		try {
			redis = RedisConnectionFactory.getInstance().getConnection(
					bean.getRid());
			redis.lpush(statsUpdateStatusQueryQueueName, sql);
		} catch (Exception e) {
			logger.error(className + methodName
					+ "Exception while sending to redis update queue", e);
			throw e;
		} finally {
			if (redis != null) {
				redis.close();
			}
		}
	}


}
