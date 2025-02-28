package com.itextos.beacon.inmemory.errorinfo.data;

import java.util.HashMap;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum FailureType
        implements
        ItextosEnum
{

    PERMANENT("2"),
    TEMPORARY("3");

    private static HashMap<String, FailureType> allStatus = new HashMap<>();

    static
    {
        final FailureType[] temp = FailureType.values();

        for (final FailureType type : temp)
            allStatus.put(type.getKey(), type);
    }

    public static FailureType getFailureType(
            String aRDErrorCode)
    {
        return allStatus.get(aRDErrorCode);
    }

    private String mRDErrorCode;

    FailureType(
            String aRDErrorCode)
    {
        mRDErrorCode = aRDErrorCode;
    }

    @Override
    public String getKey()
    {
        return mRDErrorCode;
    }

}
