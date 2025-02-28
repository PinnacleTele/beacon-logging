package com.itextos.beacon.http.generichttpapi.common.utils;

import java.util.HashMap;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum InterfaceMessageClass
        implements
        ItextosEnum
{
    // TODO KP this to be replaced by constants.messageclass

    FLASH("FL"),
    FLASH_UNICODE("FU"),
    PLAIN("PM"),
    UNICODE("UC"),
    BINARY("BM"),
    SPECIFIC_PORT("SP"),
    SPECIFIC_PORT_UNICODE("SPU"),
    ADVANCE("AD"),
    UNICODE_HEX("UH");

    private final String mMessageClass;

    InterfaceMessageClass(
            String aMessageType)
    {
        mMessageClass = aMessageType;
    }

    private static HashMap<String, InterfaceMessageClass> mMessageClassMap = new HashMap<>();

    static
    {
        final InterfaceMessageClass[] temp = InterfaceMessageClass.values();

        for (final InterfaceMessageClass type : temp)
            mMessageClassMap.put(type.getMessageType(), type);
    }

    public String getMessageType()
    {
        return mMessageClass;
    }

    public static InterfaceMessageClass getMessageType(
            String aMessageType)
    {
        return mMessageClassMap.get(aMessageType.toUpperCase());
    }

    @Override
    public String getKey()
    {
        return mMessageClass;
    }

}
