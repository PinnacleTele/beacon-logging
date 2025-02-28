package com.winnovature.downloadhandler.consumers;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.downloadhandler.singletons.DownloadHandlerPropertiesTon;
import com.winnovature.downloadhandler.singletons.RedisConnectionTon;
import com.winnovature.downloadhandler.utils.Constants;
import com.winnovature.downloadhandler.utils.CsvToExcelConvertor;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.JsonUtility;
import com.winnovature.utils.utils.Utility;

import redis.clients.jedis.Jedis;

public class CsvToExcelConvertionRequestConsumer extends Thread {
	static Log log = LogFactory.getLog(Constants.DownloadHandlerLogger);
	PropertiesConfiguration prop = null;

	String className = "[CsvToExcelConvertor]";
	private String instanceId = "";
	private long sleepTime = 1000;

	public CsvToExcelConvertionRequestConsumer() throws Exception {
		prop = DownloadHandlerPropertiesTon.getInstance().getPropertiesConfiguration();
		this.sleepTime = Utility.getConsumersSleepTime();
	}

	@Override
	public void run() {
		String queueName = null;
		int threadSleepTime = Utility.getThreadSleepTime();
		CsvToExcelConvertor convertor = new CsvToExcelConvertor();
		while (true) {
			try {
				// HeartBeat changes Start
				String timeStampAsString = Utility.getTimestampAsString();

				new HeartBeatMonitoring().pushConsumersHeartBeat("FP-DownloadHandler", "CsvToExcelConvertor",
						instanceId, this.getName(), timeStampAsString);
				// End of HeartBeat changes

				queueName = prop.getString("csv.excel.convertion.queue.name");
				Jedis con = null;

				try {

					con = RedisConnectionTon.getInstance().getJedisConnectionAsRoundRobin();

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
						} else {
							Map<String, String> map = new JsonUtility().convertJsonStringToMap(jsonRequest);

							if (log.isDebugEnabled()) {
								log.debug(Thread.currentThread().getName() + "[CsvToExcelConvertor]"
										+ " Request consumed:" + map + " and will be removed from Q[" + queueName
										+ "] and processing business logic");
							}
							
							map.put("excel_rows_limit", prop.getString("excel.rows.limit", "1000000"));
							try {
								convertor.process(map);
							}catch(Exception e) {
								// push back to redis
								int retryLimit = prop.getInt("retry.max.limit", 5);
								convertor.pushBackToRedis(map, con, queueName, retryLimit);
							}
							
							// Jedis con close should be the last statement
							if (con != null) {
								con.close();
							}
						}
					} // end of redis connection not null

				} catch (Exception e) {
					log.error(className + " [run] : " + this.getName() + " exception", e);
					try {
						if (con != null) {
							con.close();
						}
					} catch (Exception e1) {
						log.error(className + " [run] : " + this.getName() + " Exception closing Jedis con", e1);
					}
					throw e;
				}
			} catch (Exception e) {
				log.error("Exception connecting to Q[" + queueName + "]", e);
				log.error(className + " [run] : " + this.getName() + "  will sleep for " + threadSleepTime
						+ " milli seconds then restarts.");
				consumerSleep(threadSleepTime);
			} catch (Throwable t) {
				log.error(className + " Throwable connecting to Q[" + queueName + "]", t);
				log.error(className + " [run] : " + this.getName() + "  will sleep for " + threadSleepTime
						+ " milli seconds then restarts.");
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
