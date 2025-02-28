package com.winnovature.utils.utils;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.singletons.RedisConnectionFactory;

import redis.clients.jedis.Jedis;

public class DuplicateCheckDataCleaner {
	static Log log = LogFactory.getLog(Constants.UtilsLogger);
	private static final String className = "[DuplicateCheckDataCleaner]";

	public void clearDuplicateCheckData(List<Map<String, String>> campaigns, String dupcheckRedisKeyPrefix) {
		String logname = className + " [clearDuplicateCheckData()] ";
		Jedis con = null;
		try {
			List<RedisServerDetailsBean> beansList = RedisConnectionFactory.getInstance().getDupcheckRedisServerDetails();
			if(beansList != null && beansList.size() > 0) {
				for (RedisServerDetailsBean redisServerDetailsBean : beansList) {
					con = RedisConnectionFactory.getInstance().getConnectionForDupcheckRedis(redisServerDetailsBean);
					if (con != null) {
						for (Map<String, String> campaign : campaigns) {
							String hashKey = dupcheckRedisKeyPrefix + "~" + campaign.get("id");
							String setKey = dupcheckRedisKeyPrefix + "~" + campaign.get("id") + "~" + campaign.get("cli_id");
							con.del(hashKey);
							con.del(setKey);
						}
					}
				}
			}			
		} catch (Exception e) {
			log.error(logname + " Exception ::: ", e);
		} finally {
			if (con != null) {
				con.close();
			}
		}
	}

}
