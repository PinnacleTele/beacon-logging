package com.itextos.beacon.platform.walletreminder.utils;

public enum SortBy
{

    CLIENT_ID_ASCENDING(0),
    USER_NAME_ASCENDING(1),
    BALANCE_ASCENDING(2),
    BALANCE_DESCENDING(3);

    private int value;

    SortBy(
            int aValue)
    {
        value = aValue;
    }

    public static SortBy getSortBy(
            int aValue)
    {

        switch (aValue)
        {
            default:
            case 0:
                return CLIENT_ID_ASCENDING;

            case 1:
                return USER_NAME_ASCENDING;

            case 2:
                return BALANCE_ASCENDING;

            case 3:
                return BALANCE_DESCENDING;
        }
    }

}