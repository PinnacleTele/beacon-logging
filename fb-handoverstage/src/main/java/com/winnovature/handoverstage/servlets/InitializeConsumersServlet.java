package com.winnovature.handoverstage.servlets;

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

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.messageidentifier.MessageIdentifier;
import com.itextos.beacon.http.interfacefallback.inmem.FallbackQReaper;
import com.winnovature.handoverstage.consumers.SplitFileConsumer;
import com.winnovature.handoverstage.singletons.HandoverStagePropertiesTon;
import com.winnovature.handoverstage.singletons.RedisConnectionFactory;
import com.winnovature.handoverstage.utils.Constants;
import com.winnovature.utils.dtos.RedisServerDetailsBean;
import com.winnovature.utils.singletons.ConfigParamsTon;

public class InitializeConsumersServlet extends GenericServlet implements Servlet {
	private static final long serialVersionUID = 1L;
	static Log log = LogFactory.getLog(Constants.HandoverStageLogger);
	private static final String className = "[InitializeConsumersServlet]";
	PropertiesConfiguration prop = null;
	SplitFileConsumer consumer = null;
	final MessageIdentifier lMsgIdentifier = MessageIdentifier.getInstance();
	
	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {

	}
	
	public void init() throws ServletException {
		super.init();
		
		String module=System.getenv("handoverstage");
		if(module!=null&&module.equals("1")) {
	

			try {
				lMsgIdentifier.init(InterfaceType.GUI);
				FallbackQReaper.getInstance();
				
				prop = HandoverStagePropertiesTon.getInstance()
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

						for (int i = 0; i < Integer.parseInt(noofconsumer); i++) {

							consumer = new SplitFileConsumer(queueName, bean, instanceId);
							consumer.setName("Thread" + i + "-" + queueName);
							consumer.start();
							if (log.isDebugEnabled())
								log.debug(className + "[init] >>>>>> STARTING Handover Consumer  " + i + " QUEUE NAME "
										+ queueName + " bean:" + bean.getIpAddress() + " instanceId:" + instanceId);

						} // end of for loop
					}

				}
			} catch (Exception e) {
				log.error(className + "[init]  Exception:", e);
			}

		}
	}

}
