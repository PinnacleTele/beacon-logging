package com.winnovature.scheduleProcessor.servlets;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.scheduleProcessor.pollers.ScheduleCampaignPoller;
import com.winnovature.scheduleProcessor.utils.Constants;

public class InitializeScheduleCampaignPoller extends GenericServlet implements Servlet {

	private static final long serialVersionUID = 1L;
	static Log log = LogFactory.getLog(Constants.ScheduleProcessorLogger);
	private static final String className = "InitializeScheduleCampaignPoller";
	ScheduleCampaignPoller CSAPoller = null;

	@Override
	public void init() throws ServletException {
		super.init();
		
		String module=System.getenv("scheduleprocessor");
		if(module!=null&&module.equals("1")) {
			

			try {
				
				// thread to fetch records from campaign_master & campaign_files tables and HO to fileSplitQ/groupQ
				if (log.isDebugEnabled())
					log.debug(className + " CampaignMasterPoller[CSAPoller] starting...");

				CSAPoller = new ScheduleCampaignPoller("CSAPoller");
				CSAPoller.setName("CSAPoller");
				CSAPoller.start();

				if (log.isDebugEnabled())
					log.debug(className + " CampaignMasterPoller[CSAPoller] started.");

			} catch (Exception e) {
				log.error(className + " Exception:", e);
				log.error(className + " RESTART THIS MODULE ");
			}

		}
		
	}

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {

	}

}
