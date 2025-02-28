package com.winnovature.cronjobs.servlets;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.cronjobs.consumers.CurrencyRatesUpdater;
import com.winnovature.cronjobs.consumers.UnwantedFilesRemoval;
import com.winnovature.cronjobs.utils.Constants;

@WebServlet(name = "ServletInitializer", loadOnStartup = 1)
public class ServletInitializer extends GenericServlet implements Servlet {
	private static final long serialVersionUID = 1L;
	private static final String className = "[ServletInitializer]";
	static Log log = LogFactory.getLog(Constants.CronJobLogger);

	CurrencyRatesUpdater currencyRatesUpdater = null;
	UnwantedFilesRemoval unwantedFilesRemoval = null;

	@Override
	public void init() throws ServletException {
		super.init();
		
		String module=System.getenv("cronjobs");
		if(module!=null&&module.equals("1")) {
		


			try {
				currencyRatesUpdater = new CurrencyRatesUpdater();
				currencyRatesUpdater.setName("CurrencyRatesUpdater");
				currencyRatesUpdater.start();
			} catch (Exception e) {
				log.error(className + " Exception:", e);
				log.error(className + " RESTART FP-CronJobs MODULE ");
			}
			
			try {
				/* 
				 * Unwanted files includes:
				 * 1. Files(abandoned) uploaded through UI but not used (removed, page changed etc).
				 * 2. Files exceeds their usage time - N days (8 days):
				 * 2.1. Campaign files can be removed after n days after campaigns completion.
				 * 2.2. Groups files can be removed after n days after group completion.
				 * 2.3. DLT Template files can be removed after n days after DLT completion.
				*/
				unwantedFilesRemoval = new UnwantedFilesRemoval();
				unwantedFilesRemoval.setName("UnwantedFilesRemoval");
				unwantedFilesRemoval.start();
			} catch (Exception e) {
				log.error(className + " Exception:", e);
				log.error(className + " RESTART FP-CronJobs MODULE ");
			}


		}
	}

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

}
