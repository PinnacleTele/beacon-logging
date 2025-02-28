package com.winnovature.utils.singletons;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.utils.daos.GenericDao;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.utils.Constants;



public class RedisConnectionFactoryForHeartBeat {
	static Log log = LogFactory.getLog(Constants.UtilsLogger);
	/**
	 * RedisConnectionFactory singleton instance.
	 */
	private static RedisConnectionFactoryForHeartBeat conFact = new RedisConnectionFactoryForHeartBeat();

	/**
	 * class name for logging purpose.
	 */
	private static final String className = "[RedisConnectionFactoryForHeartBeat]";
	private  RedisServerDetailsBean redisServerDetailsBeanForHeartBeat;

	/**
	 * Private constructor.
	 */

	private RedisConnectionFactoryForHeartBeat() {

		try {
			loadFromDBForHeartBeat();
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
	public static RedisConnectionFactoryForHeartBeat getInstance() {
		return conFact;
	}


	public  RedisServerDetailsBean getConfigurationFromconfigParamsForHeartBeat()
			throws Exception {

		if (redisServerDetailsBeanForHeartBeat == null)
			loadFromDBForHeartBeat();

		return redisServerDetailsBeanForHeartBeat;
	}

	public void loadFromDBForHeartBeat() throws Exception {
		String methodName = "[loadFromDBForHeartBeat]";
		try {
			
			redisServerDetailsBeanForHeartBeat = new GenericDao()
					.selectRedisServerDetailsAsMapForHeartBeat();

			if (redisServerDetailsBeanForHeartBeat == null) {
				log.error(className
						+ methodName
						+ " ERROR : ***COULD NOT LOAD REDIS CONFIG FROM DATABASE for Heart Beat ******");
			}

		} catch (Exception e) {
			log.error(className + methodName + "Exception: ",e);
			throw e;
		}
	}
	
	
	}
