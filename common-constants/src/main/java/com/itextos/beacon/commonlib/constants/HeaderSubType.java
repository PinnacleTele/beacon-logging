package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum HeaderSubType
        implements
        ItextosEnum
{

    NA("0"),
    NUMARIC("1"),
    ALPHABET("2"),
    ALPHANUMARIC("3"),
    ALPHANUMARIC_SPECIAL_CHAR("4");

    private String key;

    HeaderSubType(
            String aValue)
    {
        key = aValue;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    private static final Map<String, HeaderSubType> HeaderSubType_VALUES = new HashMap<>();

    static
    {
        final HeaderSubType[] lValues = HeaderSubType.values();
        for (final HeaderSubType lHeaderSubType : lValues)
            HeaderSubType_VALUES.put(lHeaderSubType.getKey(), lHeaderSubType);
    }

    public static HeaderSubType getHeaderSubType(
            String aKey)
    {
        return HeaderSubType_VALUES.get(aKey);
    }

}
