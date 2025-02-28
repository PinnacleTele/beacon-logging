package com.itextos.beacon.commonlib.prometheusmetricsutil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import io.prometheus.client.exporter.MetricsServlet;

public class PrometheusStartup {

    private static final Log log = LogFactory.getLog(PrometheusDataHolder.class);

	private static PrometheusStartup obj =new PrometheusStartup();
	
	private PrometheusStartup() {
		
		init();
	}
	
	private void init() {
		


        try
        {
            final String aPort               = "1075";
            final int    prometheusJettyPort = Integer.parseInt(aPort);

            if (log.isDebugEnabled())
                log.debug(" Jetty Port : '" + prometheusJettyPort + "'");

            if (prometheusJettyPort > 0)
            {
                final Server                server  = new Server(prometheusJettyPort);
                final ServletContextHandler context = new ServletContextHandler();
                context.setContextPath("/");
                context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
                server.setHandler(context);
                server.start();

                if (log.isDebugEnabled())
                    log.debug("Prometheus server started... in port " + prometheusJettyPort);
            }
            else
                log.warn("Invalid Port Specified for the Prometheus server..." + prometheusJettyPort);
        }
        catch (final Throwable arg4)
        {
            log.error("Something went wrong when starting the Prometheus Server. Please have a look.", arg4);
        }
    
	}
	
	public static PrometheusStartup getInstance() {
		
		return obj;
	}
}
