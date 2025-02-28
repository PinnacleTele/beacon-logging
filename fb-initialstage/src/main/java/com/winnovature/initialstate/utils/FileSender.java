package com.winnovature.initialstate.utils;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

import com.winnovature.initialstate.singletons.RedisConnectionTon;

import redis.clients.jedis.Jedis;

public class FileSender {

	static Log log = LogFactory.getLog(Constants.InitialStageLogger);
	private static final String className = "[FileSender]";

	public static boolean sendToFileQueue(Map<String, String> map,
			String queueName) throws Exception {

		String methodName = "[sendToFileQueue]";

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " Begin.");
		}

		boolean sent = false;
		Jedis con = null;
		try {

			con = RedisConnectionTon.getInstance()
					.getJedisConnectionAsRoundRobin();

			if (con != null) {
				String json = convertToJSON(map);
				con.lpush(queueName, json);

				if (log.isDebugEnabled()) {
					log.debug(className + methodName + "HO request campaign id["
							+ map.get("cm_id") + "] to Queue[" + queueName
							+ "]");
					log.debug(className + methodName
							+ " FileSender-request sent-" + json);
				}

				sent = true;
			} else
				sent = false;

		} catch (Exception e) {
			sent = false;
			
				log.error(className + methodName
						+ " Exception in sending tagid[" + map.get("TAGID")
						+ "] to Queue[" + queueName + "]");
				log.error(className + methodName
						+ " Exception in sendToQueue...", e);
			
			
		} finally {
			if (con != null)
				con.close();
		}
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " HO status for tagid["+map.get("TAGID")+"] to Queue["+queueName+"] :"
					+ (sent == true ? "Success." : "Failed."));
			log.debug(className + methodName + " End.");
		}
		return sent;
	}

	private static String convertToJSON(Map<String, String> act)
			throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		String json = null;

		json = mapper.writeValueAsString(act);

		return json;
	}

}
