package com.winnovature.groupsprocessor.consumers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.groupsprocessor.daos.CampaignsDAO;
import com.winnovature.groupsprocessor.handlers.GroupsCampaignFileGenerator;
import com.winnovature.groupsprocessor.utils.Constants;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.singletons.RedisConnectionFactory;
import com.winnovature.utils.singletons.RedisConnectionTon;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.JsonUtility;
import com.winnovature.utils.utils.Utility;

import redis.clients.jedis.Jedis;

public class GroupsCampaignQConsumer extends Thread {
	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);
	PropertiesConfiguration prop = null;

	private RedisServerDetailsBean bean = null;
	String className = "[GroupsCampaignQConsumer]";
	private String instanceId = "";
	private long sleepTime = 1000;
	Map<String, String> map = null;
	String queueName = null;
	Map<String, String> configMap = null;

	public GroupsCampaignQConsumer(RedisServerDetailsBean bean, String instanceId) {
		this.bean = bean;
		this.instanceId = instanceId;
		this.sleepTime = Utility.getConsumersSleepTime();
	}

	@Override
	public void run() {
		int threadSleepTime = Utility.getThreadSleepTime();
		while (true) {
			try {
				// HeartBeat changes Start
				String timeStampAsString = Utility.getTimestampAsString();

				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-GroupsProcessor", "GroupsCampaignQConsumer",
						instanceId, this.getName(), timeStampAsString);
				// End of HeartBeat changes

				configMap = (HashMap<String, String>) ConfigParamsTon.getInstance().getConfigurationFromconfigParams();

				queueName = configMap.get(com.winnovature.utils.utils.Constants.GROUP_CAMPAIGN_QUEUE_NAME);
				Jedis con = null;

				String fileStoreLocation = configMap
						.get(com.winnovature.utils.utils.Constants.CAMPAIGNS_FILE_STORE_PATH);

				try {

					con = RedisConnectionFactory.getInstance().getConnection(bean.getRid());

					if (con != null) {

						String groupCampDetailsJSON = con.rpop(queueName);

						// need to skip this time, need to continue on next time.
						if (groupCampDetailsJSON == null) {
							// close Jedis con since thread is going to sleep.
							if (con != null) {
								con.close();
							}

							// No data found let consumer rest for some time
							consumerSleep(sleepTime);
						} else {
							map = new JsonUtility().convertJsonStringToMap(groupCampDetailsJSON);
							fileStoreLocation = fileStoreLocation + map.get("username").toLowerCase() + File.separator;

							if (log.isDebugEnabled()) {
								log.debug(Thread.currentThread().getName() + "[GroupsCampaignQConsumer]"
										+ " Request consumed:" + map + " and will be removed from GroupsCampaignQ["
										+ queueName + "] and preparing file with group contacts.");
							}
							if (con != null) {
								con.close();
							}
							new GroupsCampaignFileGenerator().generate(map, queueName, fileStoreLocation);
						}
					} // end of redis connection not null

				} catch (Exception e) {
					log.error(className + " [run] : " + this.getName() + " GroupsCampaignQConsumer exception", e);
					try {
						if (con != null) {
							con.close();
						}
					} catch (Exception e1) {
						log.error(className + " [run] : " + this.getName() + " Exception closing Jedis con", e1);
					}
					pushBackToGroupsCampaignQ();
					throw e;
				}
			} catch (Exception e) {
				log.error("Exception connecting to GroupsCampaignQ[" + queueName + "]", e);
				log.error(className + " [run] : " + this.getName() + "  will sleep for " + threadSleepTime
						+ " milli seconds then restarts.");
				consumerSleep(threadSleepTime);
			} catch (Throwable t) {
				log.error(className + " Throwable connecting to GroupsCampaignQ[" + queueName + "]", t);
				log.error(className + " [run] : " + this.getName() + "  will sleep for " + threadSleepTime
						+ " milli seconds then restarts.");
				consumerSleep(threadSleepTime);
			}
		}
	}

	private void pushBackToGroupsCampaignQ() {
		String methodName = " [pushBackToGroupsCampaignQ] ";
		methodName += com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + map.get("cg_id") + " ";

		Jedis con = null;
		CampaignsDAO dao = new CampaignsDAO();
		try {

			// Incrementing retry count since failed
			String retryCount = map.get("retry_count");
			String maxRetryCount = configMap.get(com.winnovature.utils.utils.Constants.MAX_RETRY_COUNT);

			if (StringUtils.isBlank(retryCount)) {
				retryCount = "0";
			}

			// if max retry crossed stop adding to GroupsCampaignQ and update directly in DB
			if (Integer.parseInt(retryCount) <= Integer.parseInt(maxRetryCount)) {
				int retryTime = 0;

				if (retryCount != null) {
					retryTime = Integer.parseInt(retryCount);
				}
				retryTime = retryTime + 1;
				map.put("retry_count", String.valueOf(retryTime));

				con = RedisConnectionTon.getInstance().getJedisConnectionAsRoundRobin();

				String json = new JsonUtility().convertMapToJSON(map);
				con.lpush(queueName, json);
				if (log.isDebugEnabled())
					log.debug(className + methodName + " FileSender-request sent-" + json);
			} else {
				dao.updateCampaignStatus(map.get("cg_id"), "Max retry reached", Constants.PROCESS_STATUS_FAILED,
						maxRetryCount);
			}
		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			try {
				dao.updateCampaignStatus(map.get("cg_id"), "GroupsCampaignQ HO failed in FP-GroupsProcessor",
						Constants.PROCESS_STATUS_FAILED, "1");
			} catch (Exception e2) {
				// if update status failed in DB , log in a separate logger
				log.error(className + methodName
						+ "GroupsCampaignQ HO failed in FP-GroupsProcessor and DB back update status [id:"
						+ map.get("cm_id") + " total:" + map.get("total") + " ] Exception:", e2);
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
