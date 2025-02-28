package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum MsgRetry
        implements
        ItextosEnum
{

    NO_RETRY("0"),
    SINGLE_PART_RETRY("1"),
    SINGLE_AND_MULTIPART_PART_RETRY("2"),
    PARTIAL_RETRY("3");

    private final String key;

    MsgRetry(
            String aKey)
    {
        key = aKey;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    private static final Map<String, MsgRetry> mAllTypes = new HashMap<>();

    static
    {
        final MsgRetry[] values = MsgRetry.values();

        for (final MsgRetry ip : values)
            mAllTypes.put(ip.key, ip);
    }

    public static MsgRetry getMsgRetry(
            String aMsgType)
    {
        return mAllTypes.get(aMsgType);
    }

}
