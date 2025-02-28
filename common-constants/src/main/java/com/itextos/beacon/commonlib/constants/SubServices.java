package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum SubServices
        implements
        ItextosEnum
{

    INTERNATIONAL("international"),
    SMPP("smpp"),
    API("api"),
    UI("ui");

    SubServices(
            String aKey)
    {
        key = aKey;
    }

    private final String                          key;
    private static final Map<String, SubServices> mAllTypes = new HashMap<>();

    static
    {
        final SubServices[] values = SubServices.values();

        for (final SubServices ip : values)
            mAllTypes.put(ip.key, ip);
    }

    @Override
    public String getKey()
    {
        return key;
    }

    public static SubServices getType(
            String aKey)
    {
        return mAllTypes.get(aKey);
    }

}
