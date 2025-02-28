package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum Services
        implements
        ItextosEnum
{

    SMS("sms"),
    VOICE("voice"),
    EMAIL("email");

    Services(
            String aKey)
    {
        key = aKey;
    }

    private final String                       key;
    private static final Map<String, Services> mAllTypes = new HashMap<>();

    static
    {
        final Services[] values = Services.values();

        for (final Services ip : values)
            mAllTypes.put(ip.key, ip);
    }

    @Override
    public String getKey()
    {
        return key;
    }

    public static Services getType(
            String aKey)
    {
        return mAllTypes.get(aKey);
    }

}
