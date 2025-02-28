package com.itextos.beacon.platform.blockoutprocess;

import java.util.HashMap;
import java.util.Map;

public enum BlockoutType
{

    TRAI("trai"),
    CUSTOM("custom"),
    SPECIFIC("specific");

    BlockoutType(
            String aKey)
    {
        key = aKey;
    }

    private final String                           key;
    private static final Map<String, BlockoutType> mAllTypes = new HashMap<>();
    static
    {
        final BlockoutType[] values = BlockoutType.values();

        for (final BlockoutType ip : values)
            mAllTypes.put(ip.key, ip);
    }

    public String getKey()
    {
        return key;
    }

    public static BlockoutType getType(
            String aKey)
    {
        return mAllTypes.get(aKey);
    }

}