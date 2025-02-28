package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum FeatureCode
        implements
        ItextosEnum
{

    PLAIN_MESSAGE_SINGLE("PMS", MessageClass.PLAIN_MESSAGE),
    PLAIN_MESSAGE_MULTI("PMM", MessageClass.PLAIN_MESSAGE),
    UNICODE_SINGLE("US", MessageClass.UNICODE_MESSAGE),
    UNICODE_MULTI("UM", MessageClass.UNICODE_MESSAGE),
    FLASH_PLAIN_MESSAGE_SINGLE("FLS", MessageClass.FLASH_PLAIN_MESSAGE),
    FLASH_PLAIN_MESSAGE_MULTI("FLM", MessageClass.FLASH_PLAIN_MESSAGE),
    FLASH_UNICODE_SINGLE("FLUS", MessageClass.FLASH_UNICODE_MESSAGE),
    FLASH_UNICODE_MULTI("FLUM", MessageClass.FLASH_UNICODE_MESSAGE),
    SPECIAL_PORT_PLAIN_MESSAGE_SINGLE("SPS", MessageClass.SP_PLAIN_MESSAGE),
    SPECIAL_PORT_PLAIN_MESSAGE_MULTI("SPM", MessageClass.SP_PLAIN_MESSAGE),
    SPECIAL_PORT_UNICODE_SINGLE("SPUS", MessageClass.SP_UNICODE_MESSAGE),
    SPECIAL_PORT_UNICODE_MULTI("SPUM", MessageClass.SP_UNICODE_MESSAGE),
    BINARY_MSG("BM", MessageClass.BINARY_MESSAGE);

    private final String       key;
    private final MessageClass messageClass;

    FeatureCode(
            String aValue,
            MessageClass aClass)
    {
        key          = aValue;
        messageClass = aClass;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    public MessageClass getMessageClass()
    {
        return messageClass;
    }

    private static final Map<String, FeatureCode> mAllTypes = new HashMap<>();

    static
    {
        final FeatureCode[] lValues = FeatureCode.values();
        for (final FeatureCode fc : lValues)
            mAllTypes.put(fc.getKey(), fc);
    }

    public static FeatureCode getFeatureCode(
            String aType)
    {
        return mAllTypes.get(aType);
    }

}