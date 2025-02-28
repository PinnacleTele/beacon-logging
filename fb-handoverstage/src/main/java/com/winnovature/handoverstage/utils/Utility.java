package com.winnovature.handoverstage.utils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.winnovature.handoverstage.daos.GenericDAO;
import com.winnovature.handoverstage.singletons.RedisConnectionFactory;
import com.winnovature.handoverstage.singletons.RedisConnectionTon;
import com.winnovature.utils.singletons.ConfigParamsTon;

import redis.clients.jedis.Jedis;

public class Utility {
	private static final String className = "[Utility]";
	static Log logger = LogFactory.getLog(Constants.HandoverStageLogger);

	public long getRetryCountFromRedis(String productName, String id, String rid) {
		String methodName = " [getRetryCountFromRedis] ";
		long retryCount = 0;
		Jedis resource = null;
		try {
			resource = RedisConnectionFactory.getInstance().getConnection(rid);
			retryCount = resource.hincrBy(productName + ":retry", id, 1);
		} catch (Exception e) {
			logger.error(className + methodName + " Exception while getting retry count from redis with rid:" + rid, e);
		} finally {
			if (resource != null) {
				resource.close();
			}
		}
		return retryCount;
	}

	public void repushSplitRecord(String productName, String campIdQueue, String json, String queue, String rid)
			throws Exception {

		String methodName = " [repushSplitRecord] ";

		Jedis resource = null;
		try {
			resource = RedisConnectionFactory.getInstance().getConnection(rid);
			Long lrem = resource.lrem(queue, 0, campIdQueue);

			if (logger.isDebugEnabled()) {
				logger.debug(className + methodName + " Request repushing. Removing cm_id:" + campIdQueue
						+ " from queue:" + queue + " lrem:" + lrem);
			}
			resource.lpush(campIdQueue, json);
			resource.lpush(queue, campIdQueue);
			resource.lrem(productName + ":processing:" + campIdQueue, 1, json);

			if (logger.isDebugEnabled())
				logger.debug(className + methodName + " pushed back to Delivery Q successfuly");

		} catch (Exception e) {
			logger.error("FileConsumer.repushSplitRecord()  Exception " + e);
			throw e;
		} finally {
			if (resource != null) {
				resource.close();
			}
		}
	}

	public void repushToRedisQueueInRoundRobin(String productName, String campIdQueue, String json, int retryCount,
			int retryLimit, String id, String status, String queue, String rid) throws Exception {

		String methodName = " [repushToRedisQueueInRoundRobin] ";

		Jedis resource = null;
		try {
			if (retryCount <= retryLimit) {
				try {
					resource = RedisConnectionTon.getInstance().getJedisConnectionAsRoundRobin();
					Long lrem = resource.lrem(queue, 0, campIdQueue);

					if (logger.isDebugEnabled()) {
						logger.debug(className + methodName + " Request repushing. Removing cm_id:" + campIdQueue
								+ " from queue:" + queue + " lrem:" + lrem);
					}

					resource.lpush(queue, campIdQueue);
					resource.lpush(campIdQueue, json);
					resource.lrem(productName + ":processing:" + campIdQueue, 1, json);
					if (resource != null) {
						resource.close();
					}
				} catch (Exception e) {
					logger.error(className + methodName + "Exception: ", e);
					if (resource != null) {
						resource.close();
					}
					repushToRedisQueueInRoundRobin(productName, campIdQueue, json, ++retryCount, retryLimit, id, status,
							queue, rid);
				}
			} else {
				try {
					// Update to DB as failed
					String sql = new GenericDAO().updateFailedRequestToQueuedSql(id, status,
							"Maximum retried in HandoverStage module", retryCount + 1);

					sendToUpdateSQLQueue(sql, rid);
				} catch (Exception e) {
					logger.error(className + methodName
							+ " Exception sending campaign_file_splits update query to UPDATE QUEUE, hence trying direct campaign_file_splits update. "
							+ e);
					try {
						new GenericDAO().updateFailedRequestToQueued(id, status, "Maximum retried in HandoverStage module",
								retryCount + 1);
					} catch (Exception e1) {
						logger.error(className + methodName + " Exception doing direct campaign_file_splits update "
								+ com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id + e);
						throw e1;
					}
				}
			}
		} catch (Exception e) {
			logger.error(className + methodName + "Exception: ", e);
			throw e;
		}

	}

	public void sendToUpdateSQLQueue(String sql, String rid) throws Exception {

		String methodName = " [sendToUpdateSQLQueue] ";
		Map<String, String> configParamsTon = ConfigParamsTon.getInstance().getConfigurationFromconfigParams();

		String statsUpdateStatusQueryQueueName = configParamsTon
				.get(com.winnovature.utils.utils.Constants.STATS_UPDATE_STATUS_QUERY_QUEUE_NAME).toString();

		Jedis redis = null;
		try {

			redis = RedisConnectionFactory.getInstance().getConnection(rid);
			redis.lpush(statsUpdateStatusQueryQueueName, sql);

		} catch (Exception e) {
			logger.error(className + methodName + "Exception while sending to redis update queue", e);
			throw e;
		} finally {
			if (redis != null) {
				redis.close();
			}
		}
	}
	
	public void removeFromProcessingQueue(String productName,
			String campIdQueue, String json, String queueName, String id) {

		String methodName = " [removeFromProcessingQueue] ";
		Jedis resource = null;

		try {
			if (logger.isDebugEnabled())
				logger.debug(className + methodName + " Begin:");

			resource = RedisConnectionTon.getInstance()
					.getJedisConnectionAsRoundRobin();
			resource.lrem(productName + ":processing:" + campIdQueue, 1, json);

		} catch (Exception e) {
			logger.error(className + methodName
					+ " Exception while removing from processing Q :"
					+ " tagid[" + campIdQueue + " SplitfileData[" + json + "]"
					+ " error:", e);

		} finally {
			if (resource != null) {
				resource.close();
			}
		}
	}
	
	public void insertIntoDupQueue(String productName, String tagidQueue,
			String json) {

		String methodName = "[insertIntoDupQueue]";
		if (logger.isDebugEnabled())
			logger.debug(className + methodName + " Begin:");
		Jedis resource = null;
		String timePattern = new SimpleDateFormat("ddMMyy").format(new Date());

		try {
			resource = RedisConnectionTon.getInstance()
					.getJedisConnectionAsRoundRobin();
			resource.lpush(productName + ":duplicatecheck:" + tagidQueue + ":"
					+ timePattern, json);
			if (logger.isDebugEnabled())
				logger.debug(className + methodName + " Completed.");

		} catch (Exception e) {
			logger.error(className + methodName
					+ " Exception while pushing to duplicate Q :" + " cm_id["
					+ tagidQueue + " SplitfileData[" + json + "]" + " error:",
					e);
		} finally {
			if (resource != null) {
				resource.close();
			}
		}

	}
	
	public String validateMobile(String mobileNo, int total) {
		try {
			String mobile = mobileNo.trim();
			Long.parseLong(mobile);
			return mobile;
		} catch (NumberFormatException ex) {
			if (total == 1 && isHeader(mobileNo)) {
				// to avoid header count
				return Constants.IS_HEADER;
			} else
				return Constants.INVALID_NUMBER;
		}
	}

	private boolean isHeader(String dest) {
		return dest.toLowerCase().startsWith("mobile");
	}
	
	public String removeHypenSpacePlusChars(String mobile) {
		String dest = mobile;

		if (dest.contains(" ")) {
			dest = dest.replaceAll(" ", "");
		}

		if (dest.contains("-")) {
			dest = dest.replaceAll("-", "");
		}

		if (dest.contains("+")) {
			dest = dest.replaceAll("\\+", "");
		}

		return dest;
	}
	
	public String addEscapeChars(String data) {
		String replaceStr = data.replace("\\", "\\\\");
		replaceStr = replaceStr.replace("\"", "\\\"");
		replaceStr = replaceStr.replace("$", "\\$");
		return replaceStr;
	}
	
	public long extractDateAndTime(String ts){
		long time = 0;
		try {
			Date dt = DateTimeUtility.getDateFromString(ts, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
			time = dt.getTime();
	    } catch (Exception e) {
	    	logger.error(className + " [extractDateAndTime] Exception : ", e);
	    	throw e;
	    }
		return time;
	}
	
	public static long getTimeDifference(Instant start) {
		Instant finish = Instant.now();
		return Duration.between(start, finish).toMillis(); 
	}
	
}
