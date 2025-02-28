package com.itextos.smpp.client.util;

abstract class PropertyConstants
{

    /**
     * Bind Request
     */
    public static final String BIND_TYPE               = "bind.type";
    public static final String HOST                    = "host";
    public static final String PORT                    = "port";
    public static final String SYSTEM_ID               = "system.id";
    public static final String PASSWORD                = "password";
    public static final String WINDOW_SIZE             = "window.size";

    /**
     * Submit_SM Request
     */
    public static final String SOURCE_ADDR             = "source.header";
    public static final String DEST_ADDR               = "dests";
    public static final String DCS                     = "dcs";
    public static final String REGISTERED_DELIVERY     = "dlr.req";
    public static final String VALIDITY_PERIOD         = "message.expiry";
    public static final String ESM_CLASS               = "esm.class";
    public static final String OPTIONAL_PARAMS         = "optional.params";
    public static final String MESSAGE                 = "message";
    public static final String SCHEDULE_DELIVERY_TIME  = "schedule.delivery.time";
    public static final String NO_OF_MESSAGES          = "no.of.messages";
    public static final String CHARSET_VALUE           = "charset.value";

    public static final String IS_16_BIT_UDF_REF       = "is.16bit.udf.ref";
    public static final String IS_CONTINEOUS_HIT       = "is.contineous.hit";
    public static final String SLEEP_BETWEEN_EVERY_HIT = "sleep.interval.millis";
    public static final String NUMBER_OF_SESSIONS      = "session.bind.count";

}
