package com.itextos.beacon.commonlib.dnddataloader.enums;

public enum RedisDbCompareResults
{
    NO_MISMATCH,
    NOT_AVAILABLE_IN_REDIS,
    NOT_AVAILABLE_IN_DATABASE,
    DEST_MISMATCH,
    PREFERENCES_MISMATCH;
}