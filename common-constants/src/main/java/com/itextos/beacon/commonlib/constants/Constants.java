package com.itextos.beacon.commonlib.constants;

public final class Constants
{

    private Constants()
    {}

    private static final String STRING_ONE                             = "1";

    private static final String STRING_ZERO                            = "0";

    public static final char    DEFAULT_CONCATENATE_CHAR               = '~';
    public static final String  ENCODER_FORMAT                         = "UTF-8";
    public static final String  NULL_STRING                            = "NULL";

    public static final String  FAILED_DLT_TEMPLATE_ID                 = "-1";
    public static final String  DLT_TRANS_SENDERID_REGEX               = "dlt.trans.senderid.regex";
    public static final String  DLT_PROMO_SENDERID_REGEX               = "dlt.promo.senderid.regex";
    public static final String  CLIENT_DLT_TEMPLATE_VALIDATE           = "client.dlt.template.validate";

    public static final String  PROMO_HEADER_REGEX                     = "promo.header.regex";
    public static final String  TRANS_HEADER_REGEX                     = "trans.header.regex";

    public static final String  GOVT_HEADER_MASKING_ALPHA              = "4";
    public static final String  PLATFORM_NEW_LINE_CHAR                 = System.getProperty("line.separator");

    public static final String  ENABLED                                = STRING_ONE;
    public static final String  DISABLED                               = STRING_ZERO;

    public static final String  TRUE                                   = STRING_ONE;
    public static final String  FALSE                                  = STRING_ZERO;

    public static final String  INTERFACE_REJECTED                     = STRING_ONE;
    public static final String  INTERFACE_ACCEPTED                     = STRING_ZERO;

    public static final String  PLATFORM_REJECTED                      = STRING_ONE;
    public static final String  PLATFORM_ACCEPTED                      = STRING_ZERO;

    public static final String  CONNCAT_MESSAGE_FAILED                 = STRING_ONE;
    public static final String  CONNCAT_MESSAGE_SUCCESS                = STRING_ZERO;

    public static final String  CONNCAT_MESSAGE_TEMPLATE_CHECK_FAILED  = STRING_ONE;
    public static final String  CONNCAT_MESSAGE_TEMPLATE_CHECK_SUCCESS = STRING_ZERO;

    public static final String  FASTDN                                 = "fastdn";
    public static final String  AGINGDN                                = "agingdn";

    public static final int     SCHEDULE_MSG                           = 1;
    public static final int     BLOCKOUT_MSG                           = 2;

    public static final String  CHANNEL_VOICE                          = "voice";

    public static final String  FAIL_STATUS                            = "-1";

    public static final int     LENGTH_PARAM_1                         = 50;
    public static final int     LENGTH_PARAM_2                         = 50;
    public static final int     LENGTH_PARAM_3                         = 50;
    public static final int     LENGTH_PARAM_4                         = 50;
    public static final int     LENGTH_PARAM_5                         = 50;
    public static final int     LENGTH_PARAM_6                         = 50;
    public static final int     LENGTH_PARAM_7                         = 50;
    public static final int     LENGTH_PARAM_8                         = 50;
    public static final int     LENGTH_PARAM_9                         = 50;
    public static final int     LENGTH_PARAM_10                        = 50;

    // public static final int NUMERIC_INVALID_ENTRY = -999;
    public static final String  STRING_INVALID_ENTRY                   = "INVALID_ENTRY";

    public static final int     DEFAULT_ENTRY                          = -1;

}