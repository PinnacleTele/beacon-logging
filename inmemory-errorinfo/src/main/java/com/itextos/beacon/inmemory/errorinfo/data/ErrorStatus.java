package com.itextos.beacon.inmemory.errorinfo.data;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum ErrorStatus
        implements
        ItextosEnum
{

    SUCCESS("Success"),
    FAILED("Failed"),
    REJECTED("Rejected"),
    EXPIRED("Expired"),
    PENDING("Pending"),
    DELIVERED("Delivered");

    private String mCategory;

    ErrorStatus(
            String aCategory)
    {
        mCategory = aCategory;
    }

    @Override
    public String getKey()
    {
        return mCategory;
    }

    public static ErrorStatus getErrorStatus(
            String aCategory)
    {

        switch (aCategory)
        {
            case "SUCCESS":
                return SUCCESS;

            case "FAILED":
                return FAILED;

            case "REJECTED":
                return REJECTED;

            case "EXPIRED":
                return EXPIRED;

            case "PENDING":
                return PENDING;

            case "DELIVERED":
                return DELIVERED;

            default:
                return null;
        }
    }

}
