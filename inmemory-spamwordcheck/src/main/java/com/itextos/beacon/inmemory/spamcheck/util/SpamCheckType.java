package com.itextos.beacon.inmemory.spamcheck.util;

import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum SpamCheckType
        implements
        ItextosEnum
{

    DISABLED("0"),
    GLOBAL_LEVEL("1"),
    MSGTYPE_LEVEL("2"),
    CLIENT_LEVEL("3"),
    ALL("4");

    SpamCheckType(
            String aKey)
    {
        key = aKey;
    }

    private static final Map<String, SpamCheckType> mAllTypes = new HashMap<>();

    static
    {
        final SpamCheckType[] lValues = SpamCheckType.values();

        for (final SpamCheckType ip : lValues)
            mAllTypes.put(ip.key, ip);
    }

    private final String key;

    @Override
    public String getKey()
    {
        return key;
    }

    public static SpamCheckType getSpamCheckType(
            String aKey)
    {
        return mAllTypes.get(aKey);
    }

}
