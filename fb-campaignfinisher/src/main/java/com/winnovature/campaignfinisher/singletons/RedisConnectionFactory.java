package com.winnovature.campaignfinisher.singletons;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.winnovature.campaignfinisher.daos.GenericDAO;
import com.winnovature.campaignfinisher.utils.Constants;
import com.winnovature.utils.dtos.RedisServerDetailsBean;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisConnectionFactory {

	static Log log = LogFactory.getLog(Constants.CampaignFinisherLogger);
	/**
	 * RedisConnectionFactory singleton instance.
	 */
	private static RedisConnectionFactory conFact = new RedisConnectionFactory();

	/**
	 * class name for logging purpose.
	 */
	private static final String className = "[RedisConnectionFactory]";
	private Map<String, RedisServerDetailsBean> redisServerDetails;


	/**
	 * Private constructor.
	 */

	private RedisConnectionFactory() {

		try {
			loadFromDB();
		} catch (Exception e) {
			log.error(className + "Exception: ",e);
			e.printStackTrace();
		}
	}

	/**
	 * This method returns the instance of this class.
	 * 
	 * @return RedisConnectionFactory instance.
	 */
	public static RedisConnectionFactory getInstance() {
		return conFact;
	}

	/**
	 * This method returns the JedisPool Connection object.
	 * 
	 * @return JedisPool connection instance.
	 */
	public Jedis getConnection(String rid) throws Exception {
		String methodName = "[getRedisConnection]";

		if (log.isDebugEnabled())
			log.debug(className + methodName + " Begin.");

		Jedis con = null;

		try {
			if (redisServerDetails == null) {
				loadFromDB();
			}

			if (rid != null) {
				if (redisServerDetails.containsKey(rid)) {
					RedisServerDetailsBean bean = redisServerDetails.get(rid);
					if (bean.getConPool() == null) {
						JedisPool conPool = getConnection(bean);
						bean.setConPool(conPool);
					}

					con = bean.getConPool().getResource();
				}
			}
		} catch (Exception e) {
			log.error(className + methodName + " Exception:", e);
			throw e;
		}
		if (log.isDebugEnabled())
			log.debug(className + methodName + " Jedis " + con + " End.");
		return con;
	}

	public void loadFromDB() throws Exception {
		String methodName = "[loadFromDB]";
		try {
			redisServerDetails = new GenericDAO()
					.selectRedisServerDetailsAsMap();

			if (redisServerDetails == null) {
				log.error(className
						+ methodName
						+ " ERROR : ***COULD NOT LOAD REDIS DATA FROM DATABASE ******");
			}

		} catch (Exception e) {
			log.error(className + methodName + "Exception: ",e);
			throw e;
		}
	}

	public Map<String, RedisServerDetailsBean> getConfigurationFromconfigParams()
			throws Exception {

		if (redisServerDetails == null)
			loadFromDB();

		return redisServerDetails;
	}

	// Always private
	private JedisPool getConnection(RedisServerDetailsBean bean)
			throws Exception {
		String methodName = "[getConnection]";
		JedisPool conPool = null;

		String mDb = bean.getMdb();
		String mIp = bean.getIpAddress();
		String mPass = bean.getPassword();
		mPass = (mPass == null || mPass.isEmpty()) ? null : mPass;

		String mPort = bean.getPort();

		String maxpool = bean.getMaxPool();
		String maxWait = bean.getMaxWait();
		String timeout = bean.getTimeout();
		Jedis con = null;
		try {
			GenericObjectPoolConfig config = new GenericObjectPoolConfig();
			config.setMaxTotal(Integer.parseInt(maxpool));
			config.setMaxWaitMillis(Long.parseLong(maxWait) * 1000l);

			conPool = new JedisPool(config, mIp, Integer.parseInt(mPort),
					Integer.parseInt(timeout) * 1000, mPass,
					Integer.parseInt(mDb));
			con = conPool.getResource();
			
		} catch (Exception e) {
			log.error(className + methodName + " Exception:", e);
			throw e;
		}finally{
			if(con!=null)
			con.close();
		}
		return conPool;
	}

}
