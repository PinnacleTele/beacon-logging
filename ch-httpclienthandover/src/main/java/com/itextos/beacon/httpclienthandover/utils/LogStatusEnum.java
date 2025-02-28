package com.itextos.beacon.httpclienthandover.utils;

public enum LogStatusEnum
{

    SUCCESS("Sucess"),
    FAILED("Failed"),
    RETRY_SUCCESS("Retry Sucess"),
    RETRY_FAILED("Retry Failed"),
    RETRY_EXPIRED("Retry Expired");

    private String key;

    LogStatusEnum(
            String aKey)
    {
        key = aKey;
    }

    public String getKey()
    {
        return key;
    }

    public static LogStatusEnum getEnum(
            String value)
    {
        for (final LogStatusEnum v : values())
            if (v.getKey().equalsIgnoreCase(value))
                return v;
        throw new IllegalArgumentException();
    }

}
