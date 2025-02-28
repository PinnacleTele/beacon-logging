package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum BillType
{

    POST_PAID("0"),
    PRE_PAID("1");

    BillType(
            String aKey)
    {
        key = aKey;
    }

    private final String                       key;
    private static final Map<String, BillType> mAllTypes = new HashMap<>();

    static
    {
        final BillType[] lValues = BillType.values();

        for (final BillType ip : lValues)
            mAllTypes.put(ip.key, ip);
    }

    public String getKey()
    {
        return key;
    }

    public static BillType getBillType(
            String aKey)
    {
        return mAllTypes.get(aKey);
    }

}
