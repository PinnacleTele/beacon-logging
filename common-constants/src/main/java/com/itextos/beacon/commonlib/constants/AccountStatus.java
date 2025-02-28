package com.itextos.beacon.commonlib.constants;

import java.util.HashMap;
import java.util.Map;

public enum AccountStatus
        implements
        ItextosEnum
{

    ACTIVE(0),
    DEACTIVATED(1),
    EXPIRY(2),
    SUSPENDED(3),
    INACTIVE(4),
    INVALID(-1),;

    private final int status;

    AccountStatus(
            int aStatus)
    {
        status = aStatus;
    }

    private static final Map<Integer, AccountStatus> allStatus = new HashMap<>();

    static
    {
        final AccountStatus[] lValues = AccountStatus.values();
        for (final AccountStatus status : lValues)
            allStatus.put(status.getStatus(), status);
    }

    public static AccountStatus getAccountStatus(
            int aStatus)
    {
        final AccountStatus lAccountStatus = allStatus.get(aStatus);
        return lAccountStatus == null ? INVALID : lAccountStatus;
    }

    public int getStatus()
    {
        return status;
    }

    @Override
    public String getKey()
    {
        return Integer.toString(status);
    }

}