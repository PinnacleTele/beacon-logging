package com.winnovature.groupsprocessor.singletons;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.groupsprocessor.daos.GenericDAO;
import com.winnovature.groupsprocessor.utils.Constants;
import com.winnovature.utils.dtos.RedisServerDetailsBean;

import redis.clients.jedis.Jedis;

public class RedisConnectionTon {

	private static String className = "[RedisConnectionTon]";
	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);

	private static RedisConnectionTon singleton;
	private static List<RedisServerDetailsBean> normalRedisServerDetails = null;
	private static List<RedisServerDetailsBean> excludeRedisServerDetails = null;
	private static int roundRobinPointer = 0;
	private int connectionErrorCounter;
	private String methodName;

	private RedisConnectionTon() {
	}

	public static RedisConnectionTon getInstance() {

		if (singleton == null) {
			singleton = new RedisConnectionTon();
		}

		return singleton;
	}

	public List<RedisServerDetailsBean> getConfigurationFromconfigParams() {

		if (normalRedisServerDetails == null)
			initializeDBFetch();

		return normalRedisServerDetails;
	}

	public void refreshDBFetch() {
		normalRedisServerDetails = null;
		initializeDBFetch();
	}

	protected void initializeDBFetch() {
		if (log.isDebugEnabled())
			log.debug(className + "[initializeDBFetch()]");

		try {
			loadFromDB();
		} catch (Exception e) {
			log.error(className + "[initializeDBFetch]", e);
		}
	}

	protected void loadFromDB() throws Exception {
		String methodName = "[loadFromDB]";
		try {
			normalRedisServerDetails = new GenericDAO().selectRedisServerDetails(com.winnovature.utils.utils.Constants.REDIS_STATUS_FOR_NORMAL_GROUPS);
			excludeRedisServerDetails = new GenericDAO().selectRedisServerDetails(com.winnovature.utils.utils.Constants.REDIS_STATUS_FOR_EXCLUDE_GROUPS);
			if (normalRedisServerDetails == null) {
				log.error(className + methodName + " ERROR : ***COULD NOT LOAD NORMAL REDIS DATA FROM DATABASE ******");
			}
			if (excludeRedisServerDetails == null) {
				log.error(className + methodName + " ERROR : ***COULD NOT LOAD EXCLUDE REDIS DATA FROM DATABASE ******");
			}
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	public Jedis getJedisConnectionAsRoundRobin() throws Exception {
		methodName = "[getJedisConnectionAsRoundRobin]";

		// If null, load details from db
		if (normalRedisServerDetails == null) {
			loadFromDB();
		}

		// If not null
		if (normalRedisServerDetails != null && normalRedisServerDetails.size() > 0) {

			// Try to connect to at-least one redis server
			while (connectionErrorCounter <= normalRedisServerDetails.size()) {
				// If increment is at last value, reset it to 0
				if (roundRobinPointer > normalRedisServerDetails.size() - 1) {
					roundRobinPointer = 0;
				}

				// Get JedisPool object. If null, create new connection
				RedisServerDetailsBean bean = normalRedisServerDetails.get(roundRobinPointer);
				roundRobinPointer++;
				Jedis resource = null;

				try {
					resource = RedisConnectionFactory.getInstance().getNormalRedisConnection(bean.getRid());
					connectionErrorCounter = 0;
					return resource;

				} catch (Exception e) {
					log.error(className + methodName + " Exception while connecting to redis for bean:" + bean, e);

					connectionErrorCounter++;
				}
			}

			connectionErrorCounter = 0;
		} else {
			throw new Exception("Redis not configured/reachable in Task Processor");
		}
		return null;
	}

	public Jedis getNormalGroupRedisConnection() throws Exception {
		methodName = "[getNormalGroupRedisConnection]";

		// If null, load details from db
		if (normalRedisServerDetails == null) {
			loadFromDB();
		}

		// If not null
		if (normalRedisServerDetails != null && normalRedisServerDetails.size() > 0) {
			RedisServerDetailsBean bean = normalRedisServerDetails.get(0);
			Jedis resource = null;
			try {
				resource = RedisConnectionFactory.getInstance().getNormalRedisConnection(bean.getRid());
				return resource;
			} catch (Exception e) {
				log.error(className + methodName + " Exception while connecting to normal redis for bean:" + bean, e);
			}

		} else {
			throw new Exception("Redis not configured/reachable in GroupProcessor");
		}
		return null;
	}
	
	public Jedis getExcludeGroupRedisConnection() throws Exception {
		methodName = "[getExcludeGroupRedisConnection]";

		// If null, load details from db
		if (excludeRedisServerDetails == null) {
			loadFromDB();
		}

		// If not null
		if (excludeRedisServerDetails != null && excludeRedisServerDetails.size() > 0) {
			RedisServerDetailsBean bean = excludeRedisServerDetails.get(0);
			Jedis resource = null;
			try {
				resource = RedisConnectionFactory.getInstance().getExcludeRedisConnection(bean.getRid());
				return resource;
			} catch (Exception e) {
				log.error(className + methodName + " Exception while connecting to exclude redis for bean:" + bean, e);
			}

		} else {
			throw new Exception("Redis not configured/reachable in GroupProcessor");
		}
		return null;
	}

}
