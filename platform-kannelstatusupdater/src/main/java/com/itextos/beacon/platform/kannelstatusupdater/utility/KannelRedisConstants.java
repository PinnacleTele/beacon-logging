package com.itextos.beacon.platform.kannelstatusupdater.utility;

public class KannelRedisConstants
{

    public KannelRedisConstants()
    {}

    public static final String KANNEL_KEY                   = "kannel:available:";

    public static final String KANNEL_KEY_IP_PORT           = "ip:port:statusport";
    public static final String KANNEL_KEY_AVAILABLE         = "available";
    public static final String KANNEL_KEY_STORESIZE         = "storesize";
    public static final String KANNEL_KEY_LAST_UPDATED      = "lastupdated";

    public static final String LAST_UPDATED                 = "-last-updated";
    public static final String KANNEL_RESPONSE_TIME         = "response-time-in-millis";
    public static final String KANNEL_RESPONSE_COUNT        = "response-count";
    public static final String KANNEL_FAILED_COUNT          = "failed-count";
    public static final String KANNEL_RESPONSE_TIME_UPDATED = "response-time" + LAST_UPDATED;
    public static final String KANNEL_FAILED_COUNT_UPDATE   = KANNEL_FAILED_COUNT + LAST_UPDATED;

    public static final long   ONE_HOUR                     = 1 * 60 * 60 * 1000L;
    public static final int    REPONSE_MAX_COUNTER          = 1000;

}