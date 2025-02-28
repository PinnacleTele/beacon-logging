package com.itextos.beacon.commonlib.prometheusmetricsutil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

import io.prometheus.client.Histogram;
import io.prometheus.client.Histogram.Timer;

class PrometheusHistogram
        extends
        AbstractPrometheusDataHandlers
{

    private static final Log log = LogFactory.getLog(PrometheusHistogram.class);
    private final Histogram  mThisHistogram;
    // private final Map<String, Timer> timerList = new HashMap<>();

    PrometheusHistogram(
            MetricType aMetricType,
            String aMetricName,
            String aHelpInfo,
            String[] aLabels)
    {
        this(aMetricType, aMetricName, null, aHelpInfo, aLabels);
    }

    PrometheusHistogram(
            MetricType aMetricType,
            String aMetricName,
            double[] aSpecialRange,
            String aHelpInfo,
            String[] aLabels)
    {
        super(aMetricType, aMetricName, aHelpInfo, aLabels);

        if (log.isDebugEnabled())
            log.debug("Registering the Prometheus Histogram Metrics.");

        if ((aSpecialRange != null) && (aSpecialRange.length > 0))
        {
            if (mIsHavingLabels)
                mThisHistogram = Histogram.build().buckets(aSpecialRange).name(mMetricName).help(mHelpInfo).labelNames(mLabels).register();
            else
                mThisHistogram = Histogram.build().buckets(aSpecialRange).name(mMetricName).help(mHelpInfo).register();
        }
        else
            if (mIsHavingLabels)
                mThisHistogram = Histogram.build().name(mMetricName).help(mHelpInfo).labelNames(mLabels).register();
            else
                mThisHistogram = Histogram.build().name(mMetricName).help(mHelpInfo).register();

        if (log.isInfoEnabled())
            log.info("Prometheus Histogram object is created and registered. Metric Name : '" + mMetricName + "', HelpInfo : '" + mHelpInfo + "', Labels : '{" + mLabelString + "}'");
    }

    @Override
    public void inc(
            String... aLabelValues)
    {
        inc(1);
    }

    @Override
    public void inc(
            double aAmountToIncrease,
            String... aLabelValues)
    {

        try
        {
            throw new ItextosRuntimeException("Increment is not applicable for the Histogram Metrics");
        }
        catch (final Exception e)
        {
            log.error("", e);
        }
    }

    @Override
    public void dec(
            String... aLabelValues)
    {
        dec(1);
    }

    @Override
    public void dec(
            double aAmountToDecrease,
            String... aLabelValues)
    {

        try
        {
            throw new ItextosRuntimeException("Decrement is not applicable for the Histogram Metrics");
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
        Timer lTimer = null;

        try
        {
            if (mIsHavingLabels)
                lTimer = mThisHistogram.labels(aLabelValues).startTimer();
            else
                lTimer = mThisHistogram.startTimer();

            final String lKey = Thread.currentThread().getName() + "~" + Utility.getConcatenatedLabels(aLabelValues);

            if (log.isDebugEnabled())
                log.debug("Timer started for '" + lKey + "'");

            // timerList.put(lKey, lTimer);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the timer. Labels '" + Utility.getConcatenatedLabels(aLabelValues) + "'", e);
        }

        return lTimer;
    }

    @Override
    public void closeTimer(
            Timer aTimer)
    {
        if (aTimer != null)
            aTimer.close();
    }
    // @Override
    // public double observeDuration(
    // String... aLabelValues)
    // {
    // final double retunValue = -1d;
    // final String lKey = Thread.currentThread().getName() + "~" +
    // Utility.getConcatenatedLabels(aLabelValues);
    //
    // if (log.isDebugEnabled())
    // log.debug("Timer Observed for '" + lKey + "'");
    //
    // /**
    // * By closing the timer object the observeDuration method called. So no need
    // to
    // * call the observeDuration as it double the counts. By closing the timer
    // object
    // * will be claimed by the GC. No need to specify the null value.
    // * Also we are not worrying about the return value.
    // */
    // try (final Timer lTimer = timerList.remove(lKey);)
    // {
    // // if (lTimer != null)
    // // retunValue = lTimer.observeDuration();
    // }
    // catch (final Exception e)
    // {
    // log.error("Exception while calling the observer duration on timer '" +
    // Utility.getConcatenatedLabels(aLabelValues) + "'", e);
    // }
    // return retunValue;
    // }

    @Override
    public double get(
            String... aLabelValues)
    {

        try
        {
            throw new ItextosRuntimeException("get() is  not applicable for the Histogram Metrics");
        }
        catch (final Exception e)
        {
            log.error("", e);
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
            throw new ItextosRuntimeException("Set is  not applicable for the Histogram Metrics");
        }
        catch (final Exception e)
        {
            log.error("", e);
        }
    }

}