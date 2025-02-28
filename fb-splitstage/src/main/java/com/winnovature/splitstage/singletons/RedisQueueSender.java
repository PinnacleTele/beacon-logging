package com.winnovature.splitstage.singletons;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

import com.winnovature.splitstage.utils.Constants;
import com.winnovature.utils.dtos.SplitFileData;
import com.winnovature.utils.dtos.TagwiseSplitFiles;

import redis.clients.jedis.Jedis;

public class RedisQueueSender {

	static Log log = LogFactory.getLog(Constants.SplitStageLogger);
	static String className = "RedisQueueSender";

	private static RedisQueueSender instance;

	private RedisQueueSender() {

	}

	public static RedisQueueSender getInstance() {
		if (instance == null) {
			instance = new RedisQueueSender();
		}
		return instance;
	}

	public boolean sendToRedis(TagwiseSplitFiles tagwiseData,
			String queueName,String excludeGrpIds) throws Exception {

		String methodName = " [sendToRedis] ";
		String json = null;
		boolean sent = false;
		Jedis resource = null;

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " Starting to send to Redis:"
					+ tagwiseData);
		}
		try {

			resource = RedisConnectionTon.getInstance()
					.getJedisConnectionAsRoundRobin();
			if (resource != null) {

				Map<String, String> requestMap = tagwiseData.getRequestMap();
				// till here id is ssr id
				for (SplitFileData childFiles : tagwiseData.getChildFiles()) {

					requestMap.put("fileloc", childFiles.getFileName());
					requestMap.put("total",
							String.valueOf(childFiles.getTotal()));
					requestMap.put("c_f_s_id", childFiles.getId());
					json = convertToJSON(requestMap);
					if(excludeGrpIds!=null && excludeGrpIds.trim().length() > 0){
						resource.lpush(tagwiseData.getCampaignId()+"_exclude", json);
					}else{
						resource.lpush(tagwiseData.getCampaignId(), json);
					}
					
				}
				
				if(excludeGrpIds!=null && excludeGrpIds.trim().length() > 0){
					resource.lpush(queueName, tagwiseData.getCampaignId()+"_exclude");
				}else{
					resource.lpush(queueName, tagwiseData.getCampaignId());
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

	private static String convertToJSON(Object data) throws Exception {

		String methodName = "[convertToJSON]";
		ObjectMapper mapper = new ObjectMapper();
		String json = null;
		try {
			json = mapper.writeValueAsString(data);

		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			throw e;
		}

		return json;
	}

}
