package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum DlrErrorDescType
        implements
        ItextosEnum
{

    GENERIC("generic"),
    CUSTOM("custom");

    private String key;

    DlrErrorDescType(
            String aValue)
    {
        key = aValue;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    private static final Map<String, DlrErrorDescType> mAllTypes = new HashMap<>();

    static
    {
        final DlrErrorDescType[] lValues = DlrErrorDescType.values();
        for (final DlrErrorDescType lDlrDescType : lValues)
            mAllTypes.put(lDlrDescType.getKey(), lDlrDescType);
    }

    public static DlrErrorDescType getDlrErrorDescType(
            String aType)
    {
        return mAllTypes.get(aType);
    }

}
