package com.itextos.beacon.commonlib.prometheusmetricsutil;

import com.itextos.beacon.commonlib.utility.CommonUtility;

final class Utility
{

    private Utility()
    {}

    static String getUniqueName(
            MetricType aType,
            String aMetricName)
    {
        return CommonUtility.combine('-', aType.toString(), aMetricName);
    }

    static final String getConcatenatedLabels(
            String... aLabels)
    {
        return CommonUtility.combine(':', aLabels);
    }

}