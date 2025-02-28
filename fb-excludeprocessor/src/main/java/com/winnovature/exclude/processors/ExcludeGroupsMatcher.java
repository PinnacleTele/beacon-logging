package com.winnovature.exclude.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.exclude.singletons.RedisConnectionTon;
import com.winnovature.exclude.utils.Constants;

import redis.clients.jedis.Jedis;

public class ExcludeGroupsMatcher {

	static Log log = LogFactory.getLog(Constants.ExcludeLogger);
	private static final String className = "[ExcludeGroupsMatcher]";

	public List<Boolean> findExcludeNumbers(String groupId, List<Map<String, String>> numbersToBeChecked) {
		String logName = className + " [findExcludeNumbers] ";
		Jedis jedis = null;
		List<Boolean> response = null;
		String redisKey = null;
		try {
			String[] mobiles = new String[numbersToBeChecked.size()];
			List<String> contacts = new ArrayList<String>();
			for(Map<String, String> row : numbersToBeChecked) {
				contacts.add(row.get("mobile"));
			}
			mobiles = contacts.toArray(mobiles);
			jedis = RedisConnectionTon.getInstance().getExcludeGroupRedisConnection();
			redisKey = Constants.REDIS_QUEUE_EXCLUDE_GROUPS;
			String groupKey = redisKey.replace("group_id", groupId);
			response = jedis.smismember(groupKey, mobiles);
		} catch (Exception e) {
			log.error(logName + "Exception while checking if number is in exclude group", e);
		} finally {
			if (jedis != null) {
				try {
					jedis.close();
				} catch (Exception e) {
					log.error("Error closing redis connection,", e);
				}
			}
		}
		return response;
	}

	public boolean isExcludeNumber(String groupId, String mobile) {
		String logName = className + " [isExcludeNumber] ";
		Jedis jedis = null;
		boolean isExclude = false;
		String redisKey = null;
		try {
			jedis = RedisConnectionTon.getInstance().getExcludeGroupRedisConnection();
			redisKey = Constants.REDIS_QUEUE_EXCLUDE_GROUPS;
			String groupKey = redisKey.replace("group_id", groupId);
			isExclude = jedis.sismember(groupKey, mobile);
		} catch (Exception e) {
			log.error(logName + "Exception while checking if number is in exclude group", e);
		} finally {
			if (jedis != null) {
				try {
					jedis.close();
				} catch (Exception e) {
					log.error("Error closing redis connection,", e);
				}
			}
		}
		return isExclude;
	}

}
