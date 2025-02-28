package com.winnovature.dltfileprocessor.consumers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.dltfileprocessor.services.MasterFileSplitHandler;
import com.winnovature.dltfileprocessor.singletons.RedisConnectionFactory;
import com.winnovature.dltfileprocessor.utils.Constants;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.JsonUtility;
import com.winnovature.utils.utils.Utility;

import redis.clients.jedis.Jedis;

public class DltFileQConsumer extends Thread {
	static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	PropertiesConfiguration prop = null;

	private RedisServerDetailsBean bean = null;
	String className = "[DltFileQConsumer]";
	private String instanceId = "";
	private long sleepTime = 1000;

	public DltFileQConsumer(RedisServerDetailsBean bean, String instanceId) {
		this.bean = bean;
		this.instanceId = instanceId;
		this.sleepTime = Utility.getConsumersSleepTime();
	}

	@Override
	public void run() {
		String queueName = null;
		int threadSleepTime = Utility.getThreadSleepTime();
		while (true) {
			try {
				// HeartBeat changes Start
				String timeStampAsString = Utility.getTimestampAsString();

				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-DltFileProcessor", "DltFileQConsumer", instanceId, this.getName(), timeStampAsString);
				// End of HeartBeat changes

				Map<String, String> configMap = (HashMap<String, String>) ConfigParamsTon.getInstance()
						.getConfigurationFromconfigParams();

				queueName = configMap.get(Constants.DLT_FILE_QUEUE_NAME);
				Jedis con = null;

				try {

					con = RedisConnectionFactory.getInstance().getConnection(bean.getRid());

					if (con != null) {

						String jsonRequest = con.rpop(queueName);

						// need to skip this time, need to continue on next time.
						if (jsonRequest == null) {
							// close Jedis con since thread is going to sleep.
							if (con != null) {
								con.close();
							}
								  
							// No data found let consumer rest for some time
							consumerSleep(sleepTime);
						}else {
							Map<String, String> map = new JsonUtility().convertJsonStringToMap(jsonRequest);

							if (log.isDebugEnabled()) {
								log.debug(Thread.currentThread().getName() + "[DltFileQConsumer]" + " Request consumed:" + map 
								+ " and will be removed from DltFileQ[" + queueName + "] and processing business logic");
							}

							new MasterFileSplitHandler(map, queueName).handleMasterRecords();
							// Jedis con close should be the last statement
							if (con != null) {
								con.close();
							}
						}
					} // end of redis connection not null
						
				} catch (Exception e) {
					log.error(className  + " [run] : "+this.getName() + " DltFileQConsumer exception", e);
					try {
						if (con != null) {
							con.close();
						}
					}catch(Exception e1) {
						log.error(className  + " [run] : "+this.getName() + " Exception closing Jedis con", e1);
					}
					throw e;
				}
			} catch (Exception e) {
				log.error("Exception connecting to DltFileQ[" + queueName + "]", e);
				log.error(className  + " [run] : "+this.getName() + "  will sleep for " + threadSleepTime+" milli seconds then restarts."); 
				consumerSleep(threadSleepTime);
			} catch (Throwable t) {
				log.error(className + " Throwable connecting to DltFileQ[" + queueName + "]", t);
				log.error(className  + " [run] : "+this.getName() + "  will sleep for " + threadSleepTime+" milli seconds then restarts."); 
				consumerSleep(threadSleepTime);
			}
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
