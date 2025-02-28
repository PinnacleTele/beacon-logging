package com.itextos.beacon.platform.cappingcheck;

import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum CappingIntervalType
        implements
        ItextosEnum
{

    NONE("0"),
    MINUTE("1"),
    HOUR("2"),
    DATE("3"),
    WEEK("4"),
    MONTH("5"),
    YEAR("6");

    private final String key;

    CappingIntervalType(
            String aKey)
    {
        key = aKey;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    private static final Map<String, CappingIntervalType> allTypes = new HashMap<>();

    static
    {
        final CappingIntervalType[] temp = CappingIntervalType.values();

        for (final CappingIntervalType cappingIntervalType : temp)
            allTypes.put(cappingIntervalType.getKey(), cappingIntervalType);
    }

    public static CappingIntervalType getCappingIntervalType(
            String aKey)
    {
        return allTypes.get(aKey);
    }

}