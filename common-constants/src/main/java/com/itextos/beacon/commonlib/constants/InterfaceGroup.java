package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum InterfaceGroup
        implements
        ItextosEnum
{

    API("api"),
    FTP("ftp"),
    SMPP("smpp"),
    UI("ui");

    InterfaceGroup(
            String aKey)
    {
        key = aKey;
    }

    private final String                             key;
    private static final Map<String, InterfaceGroup> mAllTypes = new HashMap<>();

    static
    {
        final InterfaceGroup[] values = InterfaceGroup.values();

        for (final InterfaceGroup ip : values)
            mAllTypes.put(ip.key, ip);
    }

    @Override
    public String getKey()
    {
        return key;
    }

    public static InterfaceGroup getType(
            String aKey)
    {
        return mAllTypes.get(aKey);
    }

}