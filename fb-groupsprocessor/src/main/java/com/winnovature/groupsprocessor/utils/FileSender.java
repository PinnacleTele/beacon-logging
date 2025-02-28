package com.winnovature.groupsprocessor.utils;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.groupsprocessor.singletons.RedisConnectionTon;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.utils.JsonUtility;

import redis.clients.jedis.Jedis;

public class FileSender {

	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);
	private static final String className = "[FileSender]";

	public static boolean sendToFileQueue(Map<String, String> map, String queueName) throws Exception {

		String methodName = "[sendToFileQueue]";

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " Begin.");
		}

		boolean sent = false;
		Jedis con = null;
		try {

			con = RedisConnectionTon.getInstance().getJedisConnectionAsRoundRobin();

			if (con != null) {
				String json = new JsonUtility().convertMapToJSON(map);
				con.lpush(queueName, json);

				if (log.isDebugEnabled()) {
					log.debug(className + methodName + "HO request gf_id[" + map.get("gf_id") + "] to Queue["
							+ queueName + "]");
					log.debug(className + methodName + " FileSender-request sent-" + json);
				}

				sent = true;
			} else
				sent = false;

		} catch (Exception e) {
			sent = false;
			log.error(className + methodName + " Exception in sending tagid[" + map.get("TAGID") + "] to Queue["
					+ queueName + "]");
			log.error(className + methodName + " Exception in sendToQueue...", e);
		} finally {
			if (con != null)
				con.close();
		}
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " HO status for tagid[" + map.get("TAGID") + "] to Queue[" + queueName
					+ "] :" + (sent == true ? "Success." : "Failed."));
			log.debug(className + methodName + " End.");
		}
		return sent;
	}

	public static boolean sendToUpdateQueue(String sql, String id) throws Exception {

		Map<String, String> configParamsTon = ConfigParamsTon.getInstance().getConfigurationFromconfigParams();

		String statsUpdateStatusQueryQueueName = configParamsTon
				.get(com.winnovature.utils.utils.Constants.STATS_UPDATE_STATUS_QUERY_QUEUE_NAME);
		boolean succuess = false;
		Jedis redis = null;
		try {
			redis = com.winnovature.utils.singletons.RedisConnectionTon.getInstance().getJedisConnectionAsRoundRobin();
			long count = redis.lpush(statsUpdateStatusQueryQueueName, sql);
			if(count > 0) {
				succuess = true;
			}
		} catch (Exception e) {
			log.error(className + " [sendToUpdateQueue] " + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id
					+ " Exception while sending to redis update queue", e);
		} finally {
			if (redis != null) {
				redis.close();
			}
		}
		return succuess;
	}
}
