package com.winnovature.campaignfinisher.consumers;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.campaignfinisher.singletons.CampaignFinisherPropertiesTon;
import com.winnovature.campaignfinisher.singletons.RedisConnectionFactory;
import com.winnovature.campaignfinisher.utils.Constants;
import com.winnovature.utils.dtos.QueueType;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.singletons.DeliveryEngineQueueTon;
import com.winnovature.utils.utils.HeartBeatMonitoring;

import redis.clients.jedis.Jedis;

public class DQRedisCleaner extends Thread {

	static Log log = LogFactory.getLog(Constants.CampaignFinisherLogger);
	private static String className = "DQRedisCleaner";
	public static final String EXCLUDE = "_exclude";
	private String instanceId = "";

	public DQRedisCleaner() {
	}

	@Override
	public void run() {
		String methodName = " [run] ";
		int threadSleepTime = 1000;
		while (true) {
			try {
				// HeartBeat changes Start
				threadSleepTime = com.winnovature.utils.utils.Utility.getThreadSleepTime();
				String timeStampAsString = com.winnovature.utils.utils.Utility.getTimestampAsString();
				instanceId = CampaignFinisherPropertiesTon.getInstance()
						.getPropertiesConfiguration()
						.getString(Constants.MONITORING_INSTANCE_ID);
				
				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-CampaignFinisher", className, instanceId,
						this.getName(), timeStampAsString);

				process();
				
			}// end of try
			catch (Exception ex) {
				log.error("DQRedisCleaner exception in run", ex);
				log.error(className + methodName + " :"+this.getName()+" will sleep for " + threadSleepTime+" milli seconds then restarts."); 
				consumerSleep(threadSleepTime);
			}// end of catch
		} // end of while
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
	
	
	public void process() throws Exception {
		String methodName = className + " - [process] - ";
		
		Jedis conn = null;
		Jedis conn1 = null;
		long idleThreadSleepTime = 1000;
		try {
			idleThreadSleepTime = com.winnovature.utils.utils.Utility.getIdleThreadSleepTime();
			String rid = CampaignFinisherPropertiesTon.getInstance()
			.getPropertiesConfiguration()
			.getString(Constants.REDIS_RID_INFO);
			
			conn = RedisConnectionFactory.getInstance().getConnection(rid);
			
			String campIdWithCount = conn.rpop(CampaignFinisherPropertiesTon.getInstance()
					.getPropertiesConfiguration()
					.getString(Constants.DELETE_CAMPID_QUEUE_NAME));
			if(conn != null) {
				conn.close();
			}
			
			Map<String, RedisServerDetailsBean> configurationFromconfigParams = RedisConnectionFactory
					.getInstance().getConfigurationFromconfigParams();
			long count = 0;
			if(campIdWithCount!=null && campIdWithCount.trim().length() > 0) {
				String data[] = campIdWithCount.split(":");
				String campId = data[0]; 
				String total = data[1];
				long masterTotal = Long.parseLong(total);
				QueueType queueNameType = DeliveryEngineQueueTon
						.getInstance()
						.getQueueNameBasedOnCount(masterTotal);
				String queueName = queueNameType.getQueueName();
				
				for (RedisServerDetailsBean bean : configurationFromconfigParams.values()) {
					conn1 = RedisConnectionFactory.getInstance().getConnection(bean.getRid());
					if(conn1!=null) {
						if(StringUtils.isNotBlank(queueName) && StringUtils.isNotBlank(campId)) {
							count = conn1.lrem(queueName, 0, campId);
							if(log.isDebugEnabled())
								log.debug(methodName + "campid removal status : "+count);
							
							// delete campid from excludeQ if exist
							conn1.lrem(queueName + EXCLUDE, 0, campId + EXCLUDE);
						}else {
							if(log.isDebugEnabled())
								log.debug(methodName + "Could not remove from DQ/ExcludeQ since either queueName ["+queueName+"]  or campId ["+campId+"] is null/empty.");
						}
					}
					if(conn1 != null) {
						conn1.close();
					}
				}
			}else {
				// no data found, thread will sleep for some time
				consumerSleep(idleThreadSleepTime);
			}
		} catch (Exception e) {
			log.error(className + methodName + " Exception:", e);
			throw e;
		}

	}
}
