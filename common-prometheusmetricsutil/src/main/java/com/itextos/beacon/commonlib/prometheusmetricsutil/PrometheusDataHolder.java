package com.itextos.beacon.commonlib.prometheusmetricsutil;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

import io.prometheus.client.Histogram.Timer;
import io.prometheus.client.exporter.MetricsServlet;

class PrometheusDataHolder
{

    private static final Log log = LogFactory.getLog(PrometheusDataHolder.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final PrometheusDataHolder INSTANCE = new PrometheusDataHolder();

    }

    static PrometheusDataHolder getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<String, IPrometheusDataHandlers> metricCollection = new HashMap<>();

    private PrometheusDataHolder()
    {}

    static void startPrometheusServer()
    {
    	
    	PrometheusStartup.getInstance();
    }

    String createMetrics(
            MetricType aMetricType,
            String aMetricName,
            String aHelpInfo,
            String... aLabels) throws ItextosRuntimeException
    {
        return createMetrics(aMetricType, aMetricName, null, aHelpInfo, aLabels);
    }

    String createMetrics(
            MetricType aMetricType,
            String aMetricName,
            double[] aBuckets,
            String aHelpInfo,
            String... aLabels) throws ItextosRuntimeException
    {
        IPrometheusDataHandlers dataHandlers = null;

        switch (aMetricType)
        {
            case COUNTER:
                dataHandlers = new PrometheusCounter(aMetricType, aMetricName, aHelpInfo, aLabels);
                break;

            case GAUGE:
                dataHandlers = new PrometheusGauge(aMetricType, aMetricName, aHelpInfo, aLabels);
                break;

            case HISTOGRAM:
                dataHandlers = new PrometheusHistogram(aMetricType, aMetricName, aBuckets, aHelpInfo, aLabels);
                break;

            default:
                throw new ItextosRuntimeException("Invalid MetricType specified. MetricType : '" + aMetricType + "'");
        }

        metricCollection.put(dataHandlers.getUniqueName(), dataHandlers);
        return dataHandlers.getUniqueName();
    }

    void increment(
            MetricType aMetricType,
            String aMetricName)
    {
        increment(aMetricType, aMetricName, (String[]) null);
    }

    void increment(
            MetricType aMetricType,
            String aMetricName,
            String... aLabels)
    {
        increment(Utility.getUniqueName(aMetricType, aMetricName), aLabels);
    }

    void increment(
            String aCombinedName,
            String... aLabels)
    {
        increment(aCombinedName, 1, aLabels);
    }

    void increment(
            MetricType aMetricType,
            String aMetricName,
            double aIncrement,
            String... aLabels)
    {
        increment(Utility.getUniqueName(aMetricType, aMetricName), aIncrement, aLabels);
    }

    void increment(
            String aCombinedName,
            double aIncrement,
            String... aLabels)
    {
        final IPrometheusDataHandlers dataHandlers = metricCollection.get(aCombinedName);

        if (dataHandlers != null)
            dataHandlers.inc(aIncrement, aLabels);
        else
            log.error("Invalid Type, Name specified. Combined Name : '" + aCombinedName + "' collection '" + metricCollection + "'",
                    new Exception("Unable to get the Metic Type '" + aCombinedName + "'"));
    }

    void decrement(
            MetricType aMetricType,
            String aMetricName)
    {
        decrement(aMetricType, aMetricName, (String[]) null);
    }

    void decrement(
            MetricType aMetricType,
            String aMetricName,
            String... aLabels)
    {
        decrement(Utility.getUniqueName(aMetricType, aMetricName), aLabels);
    }

    void decrement(
            String aCombinedName,
            String... aLabels)
    {
        decrement(aCombinedName, 1, aLabels);
    }

    void decrement(
            String aCombinedName,
            double aDecrement,
            String... aLabels)
    {
        final IPrometheusDataHandlers dataHandlers = metricCollection.get(aCombinedName);

        if (dataHandlers != null)
            dataHandlers.dec(aDecrement, aLabels);
        else
            log.error("Invalid Type, Name specified. Combined Name : '" + aCombinedName + "' collection '" + metricCollection + "'",
                    new Exception("Unable to get the Metic Type '" + aCombinedName + "'"));
    }

    Timer startTimer(
            MetricType aMetricType,
            String aMetricName)
    {
        return startTimer(aMetricType, aMetricName, (String[]) null);
    }

    Timer startTimer(
            MetricType aMetricType,
            String aMetricName,
            String... aLabels)
    {
        return startTimer(Utility.getUniqueName(aMetricType, aMetricName), aLabels);
    }

    Timer startTimer(
            String aCombinedName,
            String... aLabels)
    {
        Timer lTimer = null;

        if (log.isDebugEnabled())
            log.debug("Timer START for the group : '" + aCombinedName + "'");

        final IPrometheusDataHandlers dataHandlers = metricCollection.get(aCombinedName);

        if (dataHandlers != null)
            lTimer = dataHandlers.startTimer(aLabels);
        else
            log.error("Invalid Type, Name specified. Combined Name : '" + aCombinedName + "' collection '" + metricCollection + "'",
                    new Exception("Unable to get the Metic Type '" + aCombinedName + "'"));
        return lTimer;
    }

    void closeTimer(
            MetricType aMetricType,
            String aMetricName,
            Timer aTimer)
    {
        closeTimer(Utility.getUniqueName(aMetricType, aMetricName), aTimer);
    }

    void closeTimer(
            String aCombinedName,
            Timer aTimer)
    {
        if (log.isDebugEnabled())
            log.debug("Timer OBSERVE for the group : '" + aCombinedName + "'");

        final IPrometheusDataHandlers dataHandlers = metricCollection.get(aCombinedName);

        if (dataHandlers != null)
            dataHandlers.closeTimer(aTimer);
        else
            log.error(
                    "Have you enabled the lacency Calculations while applications are running?" + " If yes. <<< It will throw this Exception >>>:" + " else need to check."
                            + " Invalid Type, Name specified. Combined Name : '" + aCombinedName + "' collection '" + metricCollection + "'",
                    new Exception("Unable to get the Metic Type '" + aCombinedName + "'"));
    }

    void get(
            String aCombinedName,
            String... aLabels)
    {
        final IPrometheusDataHandlers dataHandlers = metricCollection.get(aCombinedName);

        if (dataHandlers != null)
            dataHandlers.get(aLabels);
        else
            log.error("Invalid Type, Name specified. Combined Name : '" + aCombinedName + "' collection '" + metricCollection + "'",
                    new Exception("Unable to get the Metic Type '" + aCombinedName + "'"));
    }

    void set(
            String aCombinedName,
            double aDoubleValue,
            String... aLabels)
    {
        final IPrometheusDataHandlers dataHandlers = metricCollection.get(aCombinedName);

        if (dataHandlers != null)
            dataHandlers.set(aDoubleValue, aLabels);
        else
            log.error("Invalid Type, Name specified. Combined Name : '" + aCombinedName + "' collection '" + metricCollection + "'",
                    new Exception("Unable to get the Metic Type '" + aCombinedName + "'"));
    }

}