package com.winnovature.campaignfinisher.servlets;

import java.io.IOException;
import java.util.Map;

import javax.servlet.GenericServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.campaignfinisher.consumers.DQRedisCleaner;
import com.winnovature.campaignfinisher.consumers.PollerCampaignFilesCompleted;
import com.winnovature.campaignfinisher.consumers.PollerCampaignMasterCompleted;
import com.winnovature.campaignfinisher.consumers.QueryExecutor;
import com.winnovature.campaignfinisher.singletons.CampaignFinisherPropertiesTon;
import com.winnovature.campaignfinisher.singletons.RedisConnectionFactory;
import com.winnovature.campaignfinisher.utils.Constants;
import com.winnovature.utils.dtos.RedisServerDetailsBean;

@WebServlet(name = "ServletInitializer", loadOnStartup = 1)
public class ServletInitializer extends GenericServlet implements Servlet {
	private static final long serialVersionUID = 1L;
	static Log log = LogFactory.getLog(Constants.CampaignFinisherLogger);
	private static final String className = "[QueryExecutor]";
	QueryExecutor queryExecutionConsumer = null;
	PollerCampaignFilesCompleted pollerCampaignFilesCompleted = null;
	PollerCampaignMasterCompleted pollerCampaignMasterCompleted = null;
	DQRedisCleaner dqRedisCleaner = null;

	@Override
	public void init() throws ServletException {
		
		
		
		super.init();
		
		String module=System.getenv("campaignfinisher");
		if(module!=null&&module.equals("1")) {
		

			try {
				int queryExecutionConsumersCount = CampaignFinisherPropertiesTon.getInstance().getPropertiesConfiguration()
						.getInt(Constants.QueryExcecutionConsumersCount, 1);

				Map<String, RedisServerDetailsBean> configurationFromconfigParams = RedisConnectionFactory.getInstance()
						.getConfigurationFromconfigParams();

				for (RedisServerDetailsBean bean : configurationFromconfigParams.values()) {
					for (int i = 1; i <= queryExecutionConsumersCount; i++) {
						queryExecutionConsumer = new QueryExecutor(bean);
						queryExecutionConsumer.setName("QueryExecutionConsumer" + i);
						queryExecutionConsumer.start();

						if (log.isDebugEnabled())
							log.debug(className + " QueryExecutionConsumer" + i + " started.");
					}
				}

				pollerCampaignFilesCompleted = new PollerCampaignFilesCompleted();
				pollerCampaignFilesCompleted.setName("PollerCampaignFilesCompleted");
				pollerCampaignFilesCompleted.start();

				pollerCampaignMasterCompleted = new PollerCampaignMasterCompleted();
				pollerCampaignMasterCompleted.setName("PollerCampaignMasterCompleted");
				pollerCampaignMasterCompleted.start();
				
				dqRedisCleaner = new DQRedisCleaner();
				dqRedisCleaner.setName("DQRedisCleaner");
				dqRedisCleaner.start();

			} catch (Exception e) {
				log.error(className + " Exception:", e);
				log.error(className + " RESTART FP-CampaignFinisher MODULE ");
			}

		}
	}

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

}
