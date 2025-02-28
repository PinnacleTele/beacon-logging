package com.winnovature.groupsprocessor.singletons;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.groupsprocessor.utils.Constants;
import com.winnovature.utils.dtos.SplitFileData;
import com.winnovature.utils.dtos.TagwiseSplitFiles;
import com.winnovature.utils.utils.JsonUtility;

import redis.clients.jedis.Jedis;

public class RedisQueueSender {

	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);
	static String className = "[RedisQueueSender]";

	private static RedisQueueSender instance;

	private RedisQueueSender() {

	}

	public static RedisQueueSender getInstance() {
		if (instance == null) {
			instance = new RedisQueueSender();
		}
		return instance;
	}

	public boolean sendToRedis(TagwiseSplitFiles tagwiseData, String queueName) throws Exception {

		String methodName = " [sendToRedis] ";
		String json = null;
		boolean sent = false;
		Jedis resource = null;

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " Starting to send to Redis Q: "+queueName+"  metadata:"
					+ tagwiseData);
		}
		JsonUtility jsonUtility = null;
		try {
			jsonUtility = new JsonUtility();
			resource = RedisConnectionTon.getInstance()
					.getJedisConnectionAsRoundRobin();
			if (resource != null) {

				Map<String, String> requestMap = tagwiseData.getRequestMap();
				// till here id is ssr id
				//String ssrId = requestMap.get("cm_id");
				for (SplitFileData childFiles : tagwiseData.getChildFiles()) {
					requestMap.put("fileloc", childFiles.getFileName());
					requestMap.put("total",
							String.valueOf(childFiles.getTotal()));
					// here id will get changed to ssd id
					requestMap.put("g_f_s_id", childFiles.getId());

					json = jsonUtility.convertMapToJSON(requestMap);
					resource.lpush(queueName, json);
				}
				sent = true;

				if (log.isDebugEnabled()) {
					log.debug(className + methodName + " Sent to Redis:"
							+ tagwiseData + " QueueName:" + queueName);
				}

			} else
				sent = false;
		} catch (Exception e) {
			log.error(className + methodName + "Exception : ", e);
			sent = false;
			throw e;
		} finally {
			if (resource != null)
				resource.close();
		}

		return sent;
	}

}
