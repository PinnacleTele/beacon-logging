package com.itextos.beacon.inmemory.clidlrpref;

public enum ClientDlrAdminDelivery
{

    NONE(0),
    ADMIN_USER(1),
    SUPER_USER(2);

    private final int value;

    ClientDlrAdminDelivery(
            int aValue)
    {
        value = aValue;
    }

    public static ClientDlrAdminDelivery getAdminDelivery(
            int aValue)
    {

        switch (aValue)
        {
            case 1:
                return ADMIN_USER;

            case 2:
                return SUPER_USER;

            case 0:
            default:
                return NONE;
        }
    }

}
