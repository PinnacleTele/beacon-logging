package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum ClusterType
        implements
        ItextosEnum
{

    BULK("bulk"),
    TRANSACTION("trans"),
    OTP("otp"),
    INTL("intl"),
    GUI("gui"),
    COMMON("common");

    ClusterType(
            String aKey)
    {
        key = aKey;
    }

    private static final Map<String, ClusterType> mAllTypes = new HashMap<>();

    static
    {
        final ClusterType[] lValues = ClusterType.values();

        for (final ClusterType ip : lValues)
            mAllTypes.put(ip.key, ip);
    }

    private final String key;

    @Override
    public String getKey()
    {
        return key;
    }

    public static ClusterType getCluster(
            String aKey)
    {
        return mAllTypes.get(aKey);
    }

}