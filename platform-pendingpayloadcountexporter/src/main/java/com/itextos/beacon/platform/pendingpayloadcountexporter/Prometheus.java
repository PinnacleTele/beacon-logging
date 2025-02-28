package com.itextos.beacon.platform.pendingpayloadcountexporter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.MetricsServlet;

class Prometheus
{

    private static final Log   log         = LogFactory.getLog(Prometheus.class);
    public static final String METRIC_NAME = "PendingPayloadCount";
    public static final String HELP_INFO   = "PendingPayloadCount";

    static void registerServer()
    {

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

    private static Gauge lCounter;

    static void registerMetrics()
    {
        final String[] labels =
        { "ClusterType", "RedisIndex", "Year", "Month", "Date", "Hour" };

        lCounter = Gauge.build().name(METRIC_NAME).help(HELP_INFO).labelNames(labels).register();
    }

    static void setGaugeValue(
            String aClusterType,
            String aRedisIndex,
            String aYear,
            String aMonth,
            String aDate,
            String aHour,
            long aCount)
    {
        final String[] temp =
        { aClusterType, aRedisIndex, aYear, aMonth, aDate, aHour };
        lCounter.labels(temp).set(aCount);
    }

    static void resetOldValues()
    {
        lCounter.clear();
    }

}