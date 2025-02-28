package com.itextos.beacon.commonlib.prometheusmetricsutil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram.Timer;

class PrometheusGauge
        extends
        AbstractPrometheusDataHandlers
{

    private static final Log log = LogFactory.getLog(PrometheusGauge.class);
    private final Gauge      mThisGauge;

    PrometheusGauge(
            MetricType aMetricType,
            String aMetricName,
            String aHelpInfo,
            String[] aLabels)
    {
        super(aMetricType, aMetricName, aHelpInfo, aLabels);

        if (log.isDebugEnabled())
            log.debug("Registering the Prometheus Gauage Metrics.");

        if (mIsHavingLabels)
            mThisGauge = Gauge.build().name(mMetricName).help(mHelpInfo).labelNames(mLabels).register();
        else
            mThisGauge = Gauge.build().name(mMetricName).help(mHelpInfo).register();

        if (log.isInfoEnabled())
            log.info("Prometheus Guage object is created and registered. Metric Name : '" + mMetricName + "', HelpInfo : '" + mHelpInfo + "', Labels : '{" + mLabelString + "}'");
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
                mThisGauge.labels(aLabelValues).inc(aAmounToInc);
            else
                mThisGauge.inc(aAmounToInc);
        }
        catch (final Exception e)
        {
            log.error("Exception while incrementing the Gauge. Labels: '" + Utility.getConcatenatedLabels(aLabelValues) + "'", e);
        }
    }

    @Override
    public void dec(
            String... aLabelValues)
    {
        dec(1, aLabelValues);
    }

    @Override
    public void dec(
            double aAmountToDec,
            String... aLabelValues)
    {

        try
        {
            if (mIsHavingLabels)
                mThisGauge.labels(aLabelValues).dec(aAmountToDec);
            else
                mThisGauge.dec(aAmountToDec);
        }
        catch (final Exception e)
        {
            log.error("Exception while decrementing the counter. Labels: '" + Utility.getConcatenatedLabels(aLabelValues) + "'", e);
        }
    }

    @Override
    public Timer startTimer(
            String... aLabelValues)
    {

        try
        {
            throw new ItextosRuntimeException("Timer is not applicable for the Guage Metrics");
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
                return mThisGauge.labels(aLabelValues).get();
            return mThisGauge.get();
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
            if (mIsHavingLabels)
                mThisGauge.labels(aLabelValues).set(aD);
            else
                mThisGauge.set(aD);
        }
        catch (final Exception e)
        {
            log.error("Exception while incrementing the counter. Labels: '" + Utility.getConcatenatedLabels(aLabelValues) + "'", e);
        }
    }

}