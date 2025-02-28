package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum MessageClass
        implements
        ItextosEnum
{

    PLAIN_MESSAGE("PM"),
    UNICODE_MESSAGE("UC"),
    FLASH_PLAIN_MESSAGE("FLPM"),
    FLASH_UNICODE_MESSAGE("FLUC"),
    SP_PLAIN_MESSAGE("SPPM"),
    SP_UNICODE_MESSAGE("SPUC"),
    BINARY_MESSAGE("BM");

    private String key;

    MessageClass(
            String aValue)
    {
        key = aValue;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    private static final Map<String, MessageClass> mAllMsgClass = new HashMap<>();

    static
    {
        final MessageClass[] lValues = MessageClass.values();
        for (final MessageClass lMsgClassType : lValues)
            mAllMsgClass.put(lMsgClassType.getKey(), lMsgClassType);
    }

    public static MessageClass getMessageClass(
            String aMsgClass)
    {
        return mAllMsgClass.get(aMsgClass);
    }

}