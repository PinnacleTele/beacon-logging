package com.itextos.beacon.commonlib.prometheusmetricsutil;

import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum PrometheusMetricProvider
        implements
        ItextosEnum
{

    UI("ui"),
    FTP("ftp"),
    PLATFORM("platform"),
    SMPP("smpp"),
    API("api"),
    KAFKA("kafka"),
    PLATFORM_REJECTIONS("platform_rejections"),
    GENERIC_ERROR("generic_error"),

    ;

    private String key;

    PrometheusMetricProvider(
            String aKey)
    {
        key = aKey;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    private static final Map<String, PrometheusMetricProvider> allProviders = new HashMap<>();

    static
    {
        final PrometheusMetricProvider[] lValues = PrometheusMetricProvider.values();
        for (final PrometheusMetricProvider pmp : lValues)
            allProviders.put(pmp.getKey(), pmp);
    }

    static PrometheusMetricProvider getProvider(
            String aKey)
    {
        return allProviders.get(aKey);
    }

}