package com.winnovature.downloadhandler.utils;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.downloadhandler.singletons.RedisConnectionTon;
import com.winnovature.utils.utils.JsonUtility;

import redis.clients.jedis.Jedis;

public class FileSender {

	static Log log = LogFactory.getLog(Constants.DownloadHandlerLogger);
	private static final String className = "[FileSender]";

	public static boolean sendToFileQueue(Map<String, String> map, String queueName) throws Exception {

		String methodName = "[sendToFileQueue]";

		//if (log.isDebugEnabled()) {
			//log.debug(className + methodName + " Begin.");
		//}

		boolean sent = false;
		Jedis con = null;
		try {

			con = RedisConnectionTon.getInstance().getJedisConnectionAsRoundRobin();

			if (con != null) {
				String json = new JsonUtility().convertMapToJSON(map);
				con.lpush(queueName, json);

				sent = true;
			} else
				sent = false;

		} catch (Exception e) {
			sent = false;

			log.error(className + methodName + " Exception in sending id[" + map.get("id") + "] to Queue[" + queueName
					+ "]");
			log.error(className + methodName + " Exception in sendToQueue...", e);

		} finally {
			if (con != null)
				con.close();
		}
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " HO status for id[" + map.get("id") + "] to Queue[" + queueName + "] :"
					+ (sent == true ? "Success." : "Failed."));
			log.debug(className + methodName + " json -" + map);
			log.debug(className + methodName + " End.");
		}
		return sent;
	}

}
