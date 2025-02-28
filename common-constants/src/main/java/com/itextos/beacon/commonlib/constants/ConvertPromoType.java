package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum ConvertPromoType
{

    DISABLE("0"),
    ENABLE_MSGTYPE_BASED_HEADER("1"),
    ENABLE_PROMOTIONAL_HEADER("2"),
    ENABLE_TEMPLATE_OR_OPTIN_FAIL_CASE_HEADER("3");

    ConvertPromoType(
            String aKey)
    {
        key = aKey;
    }

    private final String                               key;
    private static final Map<String, ConvertPromoType> mAllTypes = new HashMap<>();

    static
    {
        final ConvertPromoType[] lValues = ConvertPromoType.values();

        for (final ConvertPromoType ip : lValues)
            mAllTypes.put(ip.key, ip);
    }

    public String getKey()
    {
        return key;
    }

    public static ConvertPromoType getConvertPromoType(
            String aKey)
    {
        return mAllTypes.get(aKey);
    }

}
