package com.winnovature.utils.singletons;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.winnovature.utils.daos.GenericDao;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.utils.Constants;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


public class RedisConnectionFactoryForUnProcess {

	static Log log = LogFactory.getLog(Constants.UtilsLogger);
	/**
	 * RedisConnectionFactory singleton instance.
	 */
	private static RedisConnectionFactoryForUnProcess conFact = new RedisConnectionFactoryForUnProcess();

	/**
	 * class name for logging purpose.
	 */
	private static final String className = "[RedisConnectionFactoryForUnProcess]";
	private Map<String, RedisServerDetailsBean> redisServerDetailsForUnProcess =null;

	private static int roundRobinPointerForUnProcess = 0;
	private int connectionErrorCounterForUnProcess;

	/**
	 * Private constructor.
	 */

	private RedisConnectionFactoryForUnProcess() {

		try {
			loadFromDBForUnProcess();
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
	public static RedisConnectionFactoryForUnProcess getInstance() {
		return conFact;
	}


	// refer JIRA AIR-190
	public Map<String, RedisServerDetailsBean> getConfigurationFromconfigParamsForUnProcess()
			throws Exception {

		if (redisServerDetailsForUnProcess == null)
			loadFromDBForUnProcess();

		return redisServerDetailsForUnProcess;
	}

	public void loadFromDBForUnProcess() throws Exception {
		String methodName = "[loadFromDBForUnProcess]";
		try {
			
			redisServerDetailsForUnProcess = new GenericDao()
					.selectRedisServerDetailsAsMapForUnProcess();
			
			if (redisServerDetailsForUnProcess == null) {
				log.error(className
						+ methodName
						+ " ERROR : ***COULD NOT LOAD REDIS DATA FROM DATABASE for UnProcess ******");
			}

		} catch (Exception e) {
			log.error(className + methodName + "Exception: ",e);
			throw e;
		}
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
			Jedis con =null;
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


	public Jedis getConnectionForUnProcess(String rid) throws Exception {
		String methodName = "[getConnectionForUnProcess]";

		if (log.isDebugEnabled())
			log.debug(className + methodName + " Begin.");

		Jedis con = null;

		try {
			if (redisServerDetailsForUnProcess == null) {
				loadFromDBForUnProcess();
			}

			if (rid != null) {
				if (redisServerDetailsForUnProcess.containsKey(rid)) {
					RedisServerDetailsBean bean = redisServerDetailsForUnProcess
							.get(rid);
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
		return con;

	}
	
	public Jedis getConnection(String rid) throws Exception {
		String methodName = "[getRedisConnection]";

		if (log.isDebugEnabled())
			log.debug(className + methodName + " Begin.");

		Jedis con = null;

		try {
			if (redisServerDetailsForUnProcess == null) {
				loadFromDBForUnProcess();
			}

			if (rid != null) {
				if (redisServerDetailsForUnProcess.containsKey(rid)) {
					RedisServerDetailsBean bean = redisServerDetailsForUnProcess.get(rid);
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
		return con;

	}



}
