package com.itextos.beacon.platform.msgtimeboundcheck;

public final class TimeBoundConstants
{

    public static final int    DEFAULT_INTERVAL        = 20;
    public static final int    DEFAULT_MAXCOUNT        = 50;
    public static final String SEPERATOR               = "~";

    public static final String REDIS_TYPE              = "timebasedcountcheck";
    public static final String REDIS_KEY_TIMECHECK     = "timecheck:";
    public static final String REDIS_KEY_TIMECHECK_EXP = "timecheckexp:";
    public static final String REDIS_KEY_COUNT         = ":count";
    public static final String REDIS_KEY_EXP_TIME      = ":exptime";

}