package com.itextos.beacon.platform.billing.support;

import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum EncryptionType
        implements
        ItextosEnum
{

    ENCRYPTION_NOT_REQUIRED("0"),
    ENCRYPTION_REQUIRED_MESSAGE_ONLY("1"),
    ENCRYPTION_REQUIRED_MOBILE_ONLY("2"),
    ENCRYPTION_REQUIRED_MESSAGE_AND_MOBILE("3"),
    ENCRYPTION_REQUIRED_BLANK_MESSAGE("4"),
    ENCRYPTION_REQUIRED_BLANK_MOBILE("5"),
    ENCRYPTION_REQUIRED_BLANK_MESSAGE_AND_MOBILE("6");

    private String key;

    EncryptionType(
            String aKey)
    {
        key = aKey;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    private static final Map<String, EncryptionType> allTypes = new HashMap<>();

    static
    {
        final EncryptionType[] lValues = EncryptionType.values();
        for (final EncryptionType temp : lValues)
            allTypes.put(temp.getKey(), temp);
    }

    public static EncryptionType getEncryptionType(
            String aType)
    {
        return allTypes.get(aType);
    }

}