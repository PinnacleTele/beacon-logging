package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum DlrHandoverMode
        implements
        ItextosEnum
{

    NODLR("0", "NoDlr"),
    API("1", "HTTP"),
    SMPP("2", "SMPP"),
    FTP("3", "FTP");

    private String mKey;
    private String mDesc;

    DlrHandoverMode(
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

    private static final Map<String, DlrHandoverMode> mAllTypes = new HashMap<>();

    static
    {
        final DlrHandoverMode[] temp = DlrHandoverMode.values();
        for (final DlrHandoverMode dlrInterface : temp)
            mAllTypes.put(dlrInterface.getKey(), dlrInterface);
    }

    public static DlrHandoverMode getInterfaceDlrType(
            String aType)
    {
        return mAllTypes.get(aType);
    }

}
