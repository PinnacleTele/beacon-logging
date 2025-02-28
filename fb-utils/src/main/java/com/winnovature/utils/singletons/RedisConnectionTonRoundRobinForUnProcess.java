package com.winnovature.utils.singletons;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.utils.daos.GenericDao;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.utils.Constants;

import redis.clients.jedis.Jedis;


public class RedisConnectionTonRoundRobinForUnProcess {

	private static String className = "[RedisConnectionTonRoundRobinForUnProcess]";
	static Log log = LogFactory.getLog(Constants.UtilsLogger);

	private static RedisConnectionTonRoundRobinForUnProcess singleton;
	private static List<RedisServerDetailsBean> redisServerDetails = null;
	private static int roundRobinPointer = 0;
	private int connectionErrorCounter;
	private String methodName;

	private RedisConnectionTonRoundRobinForUnProcess() {
	}

	public static RedisConnectionTonRoundRobinForUnProcess getInstance() {

		if (singleton == null) {
			singleton = new RedisConnectionTonRoundRobinForUnProcess();
		}

		return singleton;
	}

	public List<RedisServerDetailsBean> getConfigurationFromconfigParams() {

		if (redisServerDetails == null)
			initializeDBFetch();

		return redisServerDetails;
	}

	public void refreshDBFetch() {
		redisServerDetails = null;
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
			
			redisServerDetails = new GenericDao()
					.selectRedisServerDetailsForUnProcess();

			if (redisServerDetails == null) {
				log.error(className
						+ methodName
						+ " ERROR : ***COULD NOT LOAD REDIS DATA FROM DATABASE ******");
			}

		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	public Jedis getJedisConnectionAsRoundRobin() throws Exception {
		methodName = "[getJedisConnectionAsRoundRobin]";

		// If null, load details from db
		if (redisServerDetails == null) {
			loadFromDB();
		}

		// If not null
		if (redisServerDetails != null && redisServerDetails.size() > 0) {

			// Try to connect to at-least one redis server
			while (connectionErrorCounter <= redisServerDetails.size()) {
				// If increment is at last value, reset it to 0
				if (roundRobinPointer > redisServerDetails.size() - 1) {
					roundRobinPointer = 0;
				}

				// Get JedisPool object. If null, create new connection
				RedisServerDetailsBean bean = redisServerDetails
						.get(roundRobinPointer);
				roundRobinPointer++;
				Jedis resource = null;

				try {
					resource = RedisConnectionFactoryForUnProcess.getInstance()
							.getConnection(bean.getRid());
					connectionErrorCounter = 0;
					return resource;

				} catch (Exception e) {
					log.error(className + methodName
							+ " Exception while connecting to redis for bean:"
							+ bean, e);

					connectionErrorCounter++;
				} 
			}

			connectionErrorCounter = 0;
		} else {
			throw new Exception("Redis not configured/reachable in Task Engine");
		}
		return null;
	}
}
