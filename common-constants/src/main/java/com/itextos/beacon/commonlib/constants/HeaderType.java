package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum HeaderType
        implements
        ItextosEnum
{

    STATIC("1"),
    LIST("2"),
    DYNAMIC("3");

    private String key;

    HeaderType(
            String aValue)
    {
        key = aValue;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    private static final Map<String, HeaderType> HeaderType_VALUES = new HashMap<>();

    static
    {
        final HeaderType[] lValues = HeaderType.values();
        for (final HeaderType lHeaderType : lValues)
            HeaderType_VALUES.put(lHeaderType.getKey(), lHeaderType);
    }

    public static HeaderType getHeaderType(
            String aKey)
    {
        return HeaderType_VALUES.get(aKey);
    }

}
