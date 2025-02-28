package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum MessageType
        implements
        ItextosEnum
{

    PROMOTIONAL("0", "promotional"),
    TRANSACTIONAL("1", "transactional");

    private final String key;
    private final String desc;

    MessageType(
            String aKey,
            String aDesc)
    {
        key  = aKey;
        desc = aDesc;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    public String getDesc()
    {
        return desc;
    }

    private static final Map<String, MessageType> allTypes = new HashMap<>();

    static
    {
        final MessageType[] temp = MessageType.values();

        for (final MessageType accountType : temp)
            allTypes.put(accountType.getKey(), accountType);
    }

    public static MessageType getMessageType(
            String aKey)
    {
        return allTypes.get(aKey);
    }

}