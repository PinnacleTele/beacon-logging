package com.itextos.beacon.commonlib.prometheusmetricsutil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram.Timer;

class PrometheusCounter
        extends
        AbstractPrometheusDataHandlers
{

    private static final Log log = LogFactory.getLog(PrometheusCounter.class);
    private final Counter    mThisCounter;

    PrometheusCounter(
            MetricType aMetricType,
            String aMetricName,
            String aHelpInfo,
            String[] aLabels)
    {
        super(aMetricType, aMetricName, aHelpInfo, aLabels);

        if (log.isDebugEnabled())
            log.debug("Registering the Prometeus Counter Metrics.");

        if (mIsHavingLabels)
            mThisCounter = Counter.build().name(mMetricName).help(mHelpInfo).labelNames(mLabels).register();
        else
            mThisCounter = Counter.build().name(mMetricName).help(mHelpInfo).register();

        if (log.isInfoEnabled())
            log.info("Prometheus Counter object is created and registered. Metric Name : '" + mMetricName + "', HelpInfo : '" + mHelpInfo + "', Labels : '{" + mLabelString + "}'");
    }

    @Override
    public void inc(
            String... aLabelValues)
    {
        inc(1, aLabelValues);
    }

    @Override
    public void inc(
            double aAmounToInc,
            String... aLabelValues)
    {

        try
        {
            if (mIsHavingLabels)
                mThisCounter.labels(aLabelValues).inc(aAmounToInc);
            else
                mThisCounter.inc(aAmounToInc);
        }
        catch (final Exception e)
        {
            log.error("Exception while incrementing the counter. Labels: " + Utility.getConcatenatedLabels(aLabelValues) + "'", e);
        }
    }

    @Override
    public void dec(
            String... aLabelValues)
    {

        try
        {
            throw new ItextosRuntimeException("Decrement is not applicable for the Counter Metrics");
        }
        catch (final Exception e)
        {
            log.error("", e);
        }
    }

    @Override
    public void dec(
            double aD,
            String... aLabelValues)
    {

        try
        {
            throw new ItextosRuntimeException("Decrement is not applicable for the Counter Metrics");
        }
        catch (final Exception e)
        {
            log.error("", e);
        }
    }

    @Override
    public Timer startTimer(
            String... aLabelValues)
    {

        try
        {
            throw new ItextosRuntimeException("Timer is not applicable for the Counter Metrics");
        }
        catch (final Exception e)
        {
            log.error("", e);
        }
        return null;
    }

    @Override
    public void closeTimer(
            Timer aTimer)
    {

        try
        {
            throw new ItextosRuntimeException("Timer is not applicable for the Counter Metrics");
        }
        catch (final Exception e)
        {
            log.error("", e);
        }
    }

    @Override
    public double get(
            String... aLabelValues)
    {

        try
        {
            if (mIsHavingLabels)
                return mThisCounter.labels(aLabelValues).get();
            return mThisCounter.get();
        }
        catch (final Exception e)
        {
            log.error("Exception while incrementing the counter. Labels: '" + Utility.getConcatenatedLabels(aLabelValues) + "'", e);
        }

        return -1;
    }

    @Override
    public void set(
            double aD,
            String... aLabelValues)
    {

        try
        {
            throw new ItextosRuntimeException("Set is not applicable for the Counter Metrics");
        }
        catch (final Exception e)
        {
            log.error("", e);
        }
    }

}