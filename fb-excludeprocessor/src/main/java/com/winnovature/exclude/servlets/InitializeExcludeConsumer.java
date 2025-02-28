package com.winnovature.exclude.servlets;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.GenericServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.exclude.consumers.ExcludeConsumer;
import com.winnovature.exclude.singletons.ExcludeProcessorPropertiesTon;
import com.winnovature.exclude.singletons.RedisConnectionFactory;
import com.winnovature.exclude.utils.Constants;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.singletons.ConfigParamsTon;

@WebServlet(name = "ExcludeConsumerInitializer", loadOnStartup = 1)
public class InitializeExcludeConsumer extends GenericServlet implements Servlet {
	private static final long serialVersionUID = 1L;
	static Log log = LogFactory.getLog(Constants.ExcludeLogger);
	private static final String className = "[InitializeExcludeConsumer]";
	PropertiesConfiguration prop = null;
	ExcludeConsumer consumer = null;
	
	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {

	}
	
	public void init() throws ServletException {
		super.init();
		
		String module=System.getenv("excludeprocessor");
		if(module!=null&&module.equals("1")) {
	

			try {
				prop = ExcludeProcessorPropertiesTon.getInstance()
						.getPropertiesConfiguration();
				String instanceId = prop
						.getString(com.winnovature.utils.utils.Constants.MONITORING_INSTANCE_ID);
				Map<String, RedisServerDetailsBean> configurationFromconfigParams = RedisConnectionFactory
						.getInstance().getConfigurationFromconfigParams();

				Map<String, String> configMap = (HashMap<String, String>) ConfigParamsTon
						.getInstance().getConfigurationFromconfigParams();

				String queueNameAndSize = configMap
						.get(Constants.HIGH_MEDIUM_LOW_QUEUES_NOOF_CONSUMERS_COUNTS);
				String[] queueNameAndSizeArray = queueNameAndSize.split(":");
				List<String> queueNameAndSizeList = Arrays
						.asList(queueNameAndSizeArray);

				for (RedisServerDetailsBean bean : configurationFromconfigParams
						.values()) {

					for (String queueNameAndConsumer : queueNameAndSizeList) {
						String noofconsumer = queueNameAndConsumer.split("~")[1];
						String queueName = queueNameAndConsumer.split("~")[0];
						queueName = queueName + com.winnovature.utils.utils.Constants.EXCLUDE;

						for (int i = 0; i < Integer.parseInt(noofconsumer); i++) {
							consumer = new ExcludeConsumer(queueName, bean, instanceId);
							consumer.setName("Thread" + i + "-" + queueName);
							consumer.start();
							log.info(className
									+ "[init] >>>>>> STARTING ExcludeConsumer  "
									+ i + " QUEUE NAME " + queueName + " bean:"
									+ bean.getIpAddress() + " instanceId:"
									+ instanceId);

							log.info(className + "[init] >>>>>>>> Starting "
									+ queueName + " Consumer... Done");
						} // end of for loop
					}

				}
			} catch (Exception e) {
				log.error(className + "[init]  Exception:", e);
			}

		}
	}

}
