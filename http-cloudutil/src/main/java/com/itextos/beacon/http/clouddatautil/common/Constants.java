package com.itextos.beacon.http.clouddatautil.common;

public class Constants
{

    public static final String  PARAMETER_VERSION          = "ver";
    public static final String  PARAMETER_KEY              = "key";
    public static final String  PARAMETER_ENCRPTED         = "encrpt";
    public static final String  PARAMETER_SCHEDULE_AT      = "sch_at";

    public static final String  PARAMETER_MSGID            = "msgid";
    public static final String  PARAMETER_MESSAGES         = "messages";
    public static final String  PARAMETER_TEXT             = "text";
    public static final String  PARAMETER_BINDATA          = "bindata";
    public static final String  PARAMETER_SEND             = "send";
    public static final String  PARAMETER_TYPE             = "type";
    public static final String  PARAMETER_DCS              = "dcs";
    public static final String  PARAMETER_UDHI_INCLUDE     = "udhi_inc";
    public static final String  PARAMETER_UDH              = "udh";
    public static final String  PARAMETER_PORT             = "port";
    public static final String  PARAMETER_DLR_REQUIRED     = "dlr_req";
    public static final String  PARAMETER_VALIDITY_PERIOD  = "vp";
    public static final String  PARAMETER_APPEND_COUNTRY   = "app_country";
    public static final String  PARAMETER_COUNTRY_CODE     = "country_cd";
    public static final String  PARAMETER_URL_TRACK        = "urltrack";
    public static final String  PARAMETER_CUST_REF         = "cust_ref";
    public static final String  PARAMETER_TEMPLATE_ID      = "template_id";
    public static final String  PARAMETER_TEMPLATE_VALUES  = "template_values";
    public static final String  PARAMETER_TAG              = "tag";
    public static final String  PARAMETER_TAG1             = "tag1";
    public static final String  PARAMETER_TAG2             = "tag2";
    public static final String  PARAMETER_TAG3             = "tag3";
    public static final String  PARAMETER_TAG4             = "tag4";
    public static final String  PARAMETER_TAG5             = "tag5";
    public static final String  PARAMETER_STATUS_ID        = "statusId";
    public static final String  PARAMETER_STATUS_DESC      = "statusDesc";
    public static final String  PARAMETER_DEST             = "dest";
    public static final String  PARAMETER_CUST_IP          = "custIP";
    public static final String  PARAMETER_MSG_SOURCE       = "msg_source";

    // DATABASE CONFIG
    public static final String  DB_IS_WRITE_RESPONSE_FIRST = "is_write_response_first";
    public static final String  DB_AUTH_KEY                = "auth_key";
    public static final String  DB_REDIS_BATCH_SIZE        = "redis_batch_size";
    public static final String  DB_PROCESS_WAIT_SECS       = "process_wait_secs";
    public static final String  DB_IP_PARAMTER_KEY         = "ip_paramter_key";
    public static final String  DB_IS_BULK                 = "is_bulk";
    public static final String  DB_BULK_BATCH_SIZE         = "bulk_batch_size";
    public static final String  DB_TOTAL_THREADS_HIT       = "total_threads_hit";
    public static final String  DB_PARAMETERS              = "parameters";
    public static final String  DB_SWAP_FROM               = "sap_from";
    public static final String  DB_SWAP_TO                 = "swap_to";
    public static final String  DB_CLIENT_IP               = "client_ip";
    public static final String  DB_CLIENT_ID               = "client_id";
    private static final String REDIS_PROP_KEY_NAME        = "cloud";

    public static final String  REDIS_ENTRY_KEY_NAME       = REDIS_PROP_KEY_NAME + ":requests";
    public static final String  PROPERTY_PLATFORM_URL      = "platform.url";
    public static final String  PROPERTY_PLATFORM_URL_BULK = "platform.url.bulk";
    public static final String  CONCATE_STRING             = "|~|";

    // Response Key
    public static final String  RESPONSE_STATUS            = "status";
    public static final String  RESPONSE_FILE_ID           = "file_id";
    public static final String  RESPONSE_MSG               = "reason";
    public static final String  RESP_PARAMETER_CODE        = "code";
    public static final String  RESP_PARAMETER_INFO        = "info";
    public static final String  RESP_PARAMETER_REASON      = "reason";
    public static final String  RESP_PARAMETER_STATUS      = "status";
    public static final String  RESP_PARAMETER_REQID       = "req_id";
    public static final String  RESP_PARAMETER_REQTIME     = "req_time";
    public static final String  RES_CONTENT_TYPE_JSON      = "text/json";

}
