package com.winnovature.groupsprocessor.consumers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.groupsprocessor.daos.GroupsMasterDAO;
import com.winnovature.groupsprocessor.handlers.SplitFileProcessor;
import com.winnovature.groupsprocessor.singletons.GroupsProcessorPropertiesTon;
import com.winnovature.groupsprocessor.singletons.RedisConnectionFactory;
import com.winnovature.groupsprocessor.singletons.RedisConnectionTon;
import com.winnovature.groupsprocessor.utils.Constants;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.JsonUtility;
import com.winnovature.utils.utils.Utility;

import redis.clients.jedis.Jedis;

public class GroupsFileSplitQConsumer extends Thread {
	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);
	PropertiesConfiguration prop = null;

	private RedisServerDetailsBean bean = null;
	String className = "[GroupsFileSplitQConsumer]";
	private String instanceId = "";
	private long sleepTime = 1000;
	private String batchSize = null;
	private Map<String, String> requestMap = null;
	PropertiesConfiguration groupsProperties = null;

	public GroupsFileSplitQConsumer(RedisServerDetailsBean bean, String instanceId, String batchSize) {
		this.bean = bean;
		this.instanceId = instanceId;
		this.sleepTime = Utility.getConsumersSleepTime();
		this.batchSize = batchSize;
	}

	@Override
	public void run() {
		String queueName = null;
		int threadSleepTime = Utility.getThreadSleepTime();
		while (true) {
			try {
				// HeartBeat changes Start
				String timeStampAsString = Utility.getTimestampAsString();

				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-GroupsProcessor", "GroupsFileSplitQConsumer", instanceId, this.getName(), timeStampAsString);
				// End of HeartBeat changes

				groupsProperties = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration();

				queueName = groupsProperties.getString(Constants.GROUP_SPLIT_FILES_QUEUE);
				Jedis con = null;

				try {

					con = RedisConnectionFactory.getInstance().getNormalRedisConnection(bean.getRid());

					if (con != null) {

						String groupDetailsJSON = con.rpop(queueName);

						// need to skip this time, need to continue on next time.
						if (groupDetailsJSON == null) {
							// close Jedis con since thread is going to sleep.
							if (con != null) {
								con.close();
							}
								  
							// No data found let consumer rest for some time
							consumerSleep(sleepTime);
						}else {
							requestMap = new JsonUtility().convertJsonStringToMap(groupDetailsJSON);

							if (log.isDebugEnabled()) {
								log.debug(Thread.currentThread().getName() + "[GroupsFileSplitQConsumer]" + " Request consumed:" + requestMap 
								+ " and will be removed from GroupQ[" + queueName + "] and processing business logic");
							}
							
							requestMap.put(Constants.REDIS_PUSH_BATCH_SIZE, batchSize);
							requestMap.put("instance_id", instanceId);

							new SplitFileProcessor().process(requestMap);
							// Jedis con close should be the last statement
							if (con != null) {
								con.close();
							}
						}
					} // end of redis connection not null
						
				} catch (Exception e) {
					log.error(className  + " [run] : "+this.getName() + " GroupsFileSplitQConsumer exception", e);
					try {
						if (con != null) {
							con.close();
						}
					}catch(Exception e1) {
						log.error(className  + " [run] : "+this.getName() + " Exception closing Jedis con", e1);
					}
					pushBackToGroupSplitFileQ();
					throw e;
				}
			} catch (Exception e) {
				log.error("Exception connecting to GroupQ[" + queueName + "]", e);
				log.error(className  + " [run] : "+this.getName() + "  will sleep for " + threadSleepTime+" milli seconds then restarts."); 
				consumerSleep(threadSleepTime);
			} catch (Throwable t) {
				log.error(className + " Throwable connecting to GroupQ[" + queueName + "]", t);
				log.error(className  + " [run] : "+this.getName() + "  will sleep for " + threadSleepTime+" milli seconds then restarts."); 
				consumerSleep(threadSleepTime);
			}
		}
	}
	
	private void pushBackToGroupSplitFileQ() {
		String methodName = " [pushBackToGroupSplitFileQ] ";
		Jedis con = null;
		GroupsMasterDAO dao = new GroupsMasterDAO();
		String maxRetryCount = "";
		try {
			Map<String, String> configMap = (HashMap<String, String>) ConfigParamsTon.getInstance()
					.getConfigurationFromconfigParams();
			String queueName = groupsProperties.getString(Constants.GROUP_SPLIT_FILES_QUEUE);
			// Incrementing retry count since failed
			String retryCount = requestMap.get("retry_count");
			maxRetryCount = configMap.get(com.winnovature.utils.utils.Constants.MAX_RETRY_COUNT);

			if (StringUtils.isBlank(retryCount)) {
				retryCount = "0";
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " retryCount" + retryCount);
			}

			// if max retry crossed stop adding to GroupQ and update directly in DB
			if (Integer.parseInt(retryCount) <= Integer.parseInt(maxRetryCount)) {
				int retryTime = 0;

				if (retryCount != null) {
					retryTime = Integer.parseInt(retryCount);
				}

				retryTime = retryTime + 1;
				requestMap.put("retry_count", String.valueOf(retryTime));

				con = RedisConnectionTon.getInstance().getJedisConnectionAsRoundRobin();

				String json = new JsonUtility().convertMapToJSON(requestMap);
				con.lpush(queueName, json);
			} else {
				dao.updateCampaignStatus(requestMap.get("gm_id"),requestMap.get("gf_id"),Constants.PROCESS_STATUS_FAILED, "Repushing to GroupsFileSplitQ failed in GroupsProcessor", maxRetryCount);
			}
		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			// if push to GroupQ failed, update status as failed in groups table
			try {
				dao.updateCampaignStatus(requestMap.get("gm_id"),requestMap.get("gf_id"),Constants.PROCESS_STATUS_FAILED, "Repushing to GroupsFileSplitQ failed in GroupsProcessor", maxRetryCount);
			} catch (Exception e2) {
				// if update status failed in DB , log in a separate logger
				log.error(className + methodName + " Failed Repushing to GroupsFileSplitQ and failed updating DB in GroupsProcessor [id:"
						+ requestMap.get("gm_id") + " total:" + requestMap.get("total") + " ] Exception:", e2);
			}
		} finally {
			if (con != null)
				con.close();
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
	
}
