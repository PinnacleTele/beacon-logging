package com.winnovature.utils.utils;

import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

import com.winnovature.utils.daos.UnprocessNumbersInsertDAO;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.dtos.UnprocessRow;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.singletons.RedisConnectionTonRoundRobinForUnProcess;
import com.winnovature.utils.singletons.UtilsPropertiesTon;

import redis.clients.jedis.Jedis;

public class UnProcessListsCollector {
	private static final String className = "[UnProcessListsCollector]";
	PropertiesConfiguration props = null;
	static Log log = LogFactory.getLog(Constants.UtilsLogger);
	String methodName = null;

	Map<String, String> configParamsTon = null;
	RedisServerDetailsBean bean = null;

	public UnProcessListsCollector() {
		methodName = " [UnProcessListsCollector] ";
		try {
			props = UtilsPropertiesTon.getInstance().getPropertiesConfiguration();
		} catch (Exception e) {
			log.error(className + methodName, e);
		}
	}

	public void unprocessHandoverToQueue(List<UnprocessRow> lstUnprocess) throws Exception{
		methodName = " [unprocessHandoverToQueue] ";
		try {
			configParamsTon = ConfigParamsTon.getInstance()
					.getConfigurationFromconfigParams();
		} catch (Exception e) {
			log.error(className + methodName, e);
		}
		Jedis redisCon = null;
		if (lstUnprocess != null && lstUnprocess.size() != 0) {
			try {
				String unprocessedHandoverQueueName = configParamsTon.get(Constants.EXCLUDE_NUMBERS_INSERT_QUEUE_NAME)
						.toString();
				redisCon = RedisConnectionTonRoundRobinForUnProcess.getInstance().getJedisConnectionAsRoundRobin();
				ObjectMapper mapper = new ObjectMapper();
					String lsUnprocessAsJson = mapper.writeValueAsString(lstUnprocess);
					if(log.isDebugEnabled()) {
						log.debug(className+methodName
								+ "Input JsonString ::"
								+ lsUnprocessAsJson);
					}
					redisCon.lpush(unprocessedHandoverQueueName, lsUnprocessAsJson);
					if (redisCon != null) {
						redisCon.close();
					}

			} catch (Exception e) {
				log.error(className+methodName+" Exception HO exclude numbers to redis,\n" + e);
				try {
					if (redisCon != null) {
						redisCon.close();
					}
					// direct DB insert
					int batchcount = props.getInt(Constants.BATCH_UPDATE_COUNT);
					new UnprocessNumbersInsertDAO().insertUnProcessNumbers(batchcount,lstUnprocess);
				} catch (Exception e1) {
					log.error(className + methodName + "List Of UnProcessRow : "+lstUnprocess.toString()+" \n Exception : ", e1);
				}
			}
			
		}

	}
	
}
