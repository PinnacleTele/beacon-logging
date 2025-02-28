package com.itextos.beacon.commonlib.messageidentifier;

class MessageIdentifierConstants
{

    protected MessageIdentifierConstants()
    {}

    static final String KEY_OUTER_AVAILABLE       = "messageid:available";
    static final String KEY_OUTER_IN_USE          = "messageid:inuse";
    static final String KEY_OUTER_STATUS          = "messageid:status";
    static final String KEY_INNER_INTERFACE_TYPE  = "interface:type";
    static final String KEY_INNER_ALLOCATED_IP    = "allocated:ip";
    static final String KEY_INNER_ALLOCATED_TIME  = "allocated:time";
    static final String KEY_INNER_LAST_UPDATED    = "last:updated";
    static final String KEY_INNER_ADDITIONAL_INFO = "additional:info";
    static final String UPDATE_DATE_FORMAT        = "yyyy-MM-dd HH:mm:ss.SSS";

    static final int    INDEX_INTERFACE_TYPE      = 0;
    static final int    INDEX_ALLOCATED_IP        = 1;
    static final int    INDEX_ALLOCATED_TIME      = 2;
    static final int    INDEX_LAST_UPDATED_TIME   = 3;

    static final String NEW_LINE                  = System.lineSeparator();

}
