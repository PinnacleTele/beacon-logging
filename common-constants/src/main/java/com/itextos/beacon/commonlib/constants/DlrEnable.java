package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum DlrEnable
        implements
        ItextosEnum
{

    ALWAYS_OFF("0", "AlwaysOff"),
    ALWAYS_ON("1", "AlwaysOn"),
    EXPLICIT_REQ("2", "ExplicitRequest");

    private String mKey;
    private String mDesc;

    DlrEnable(
            String aKey,
            String aDesc)
    {
        mKey  = aKey;
        mDesc = aDesc;
    }

    @Override
    public String getKey()
    {
        return mKey;
    }

    public String getDesc()
    {
        return mDesc;
    }

    private static final Map<String, DlrEnable> mAllTypes = new HashMap<>();

    static
    {
        final DlrEnable[] temp = DlrEnable.values();
        for (final DlrEnable dlrConfig : temp)
            mAllTypes.put(dlrConfig.getKey(), dlrConfig);
    }

    public static DlrEnable getDlrConfig(
            String aType)
    {
        return mAllTypes.get(aType);
    }

}
