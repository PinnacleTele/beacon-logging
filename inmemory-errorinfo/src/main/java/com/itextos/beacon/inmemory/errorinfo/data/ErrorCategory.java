package com.itextos.beacon.inmemory.errorinfo.data;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum ErrorCategory
        implements
        ItextosEnum
{

    INTERFACE("INTERFACE"),
    PLATFORM("PLATFORM"),
    OPERATOR("OPERATOR");

    private String mCategory;

    ErrorCategory(
            String aCategory)
    {
        mCategory = aCategory;
    }

    @Override
    public String getKey()
    {
        return mCategory;
    }

    public static ErrorCategory getErrorCategroy(
            String aCategory)
    {

        switch (aCategory)
        {
            case "INTERFACE":
                return INTERFACE;

            case "PLATFORM":
                return PLATFORM;

            case "OPERATOR":
                return OPERATOR;

            default:
                return null;
        }
    }

}
