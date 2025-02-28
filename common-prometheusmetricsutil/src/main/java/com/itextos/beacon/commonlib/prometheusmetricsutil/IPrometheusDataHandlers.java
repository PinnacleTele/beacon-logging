package com.itextos.beacon.commonlib.prometheusmetricsutil;

import io.prometheus.client.Histogram.Timer;

interface IPrometheusDataHandlers
{

    String getUniqueName();

    void inc(
            String... aLabelValues);

    void inc(
            double d,
            String... aLabelValues);

    void dec(
            String... aLabelValues);

    void dec(
            double d,
            String... aLabelValues);

    void set(
            double d,
            String... aLabelValues);

    double get(
            String... aLabelValues);

    Timer startTimer(
            String... aLabelValues);

    // double observeDuration(
    // String... aLabelValues);
    void closeTimer(
            Timer aTimer);

}