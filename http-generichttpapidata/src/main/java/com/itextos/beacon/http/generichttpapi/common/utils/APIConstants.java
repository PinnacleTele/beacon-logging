package com.itextos.beacon.http.generichttpapi.common.utils;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public final class APIConstants
{

    private APIConstants()
    {}

    private static final PropertiesConfiguration pc                          = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.GENERICAPI_PROPERTIES, true);

    public static final String                   CLUSTER_INSTANCE            = CommonUtility.nullCheck(pc.getString("cluster.instance"), true).toLowerCase();
    public static final String                   XML_SCHEMA_PATH             = CommonUtility.nullCheck(pc.getString("xml.schema.file.path"), true);
    public static final String                   REQUEST_FILE_PATH           = CommonUtility.nullCheck(pc.getString("request.file.path"), true);
    public static final String                   PROCESSED_REQUEST_FILE_PATH = CommonUtility.nullCheck(pc.getString("processed.request.file.path"), true);
    public static final String                   ERROR_REQUEST_FILE_PATH     = CommonUtility.nullCheck(pc.getString("error.request.file.path"), true);

    public static final String                   START_CONSUMER              = CommonUtility.nullCheck(pc.getString("kafka.async.consumer.start"), true);

    public static final String                   ACCESS_KEY                  = "access_key";
    public static final int                      POLLER_SLEEP_TIME_IN_MILLIS = CommonUtility.getInteger(pc.getString("poller.sleep.time.inmills"), 10 * 1000);
    public static final int                      MAX_THREAD_POOL_COUNT       = CommonUtility.getInteger(pc.getString("max.thread.pool.count"), 20);

    public static final String[]                 ALLOW_VERSIONS              =
    { "1.0" };
    public static final int                      PORT_MIN_VALUE              = 0;
    public static final int                      PORT_MAX_VALUE              = 65535;
    public static final int                      VP_MIN_VALUE                = 1;
    public static final int                      VP_MAX_VALUE                = 1440;
    public static final int                      CUST_REF_MAX_VALUE          = 50;

    public static final int                      PARAMS_MIN_VALUE            = 0;
    public static final int                      MSGTAG_MAX_VALUE            = 50;

    public static final int                      PARAM1_MAX_VALUE            = 50;
    public static final int                      PARAM2_MAX_VALUE            = 50;
    public static final int                      PARAM3_MAX_VALUE            = 50;
    public static final int                      PARAM4_MAX_VALUE            = 50;
    public static final int                      PARAM5_MAX_VALUE            = 50;
    public static final int                      PARAM6_MAX_VALUE            = 100;
    public static final int                      PARAM7_MAX_VALUE            = 100;
    public static final int                      PARAM8_MAX_VALUE            = 100;
    public static final int                      PARAM9_MAX_VALUE            = 250;
    public static final int                      PARAM10_MAX_VALUE           = 500;

    public static final String                   DEFAULT_DEST                = "0";

    public static final char                     SUFFIX                      = (char) 2;
    public static final char                     PREFIX                      = (char) 3;

    public static final String                   PROCESS_TYPE_SYNC           = "sync";
    public static final String                   PROCESS_TYPE_ASYNC          = "async";
    public static final String                   OTP_CLUSTER                 = "otp";
    public static String                         appInstanceId               = "";
    public static final String                   STATUS_INFO_ACCEPT          = "ACCEPTED";
    public static final String                   STATUS_INFO_REJECT          = "REJECTED";

    public static String getAllowSpecialCharacters()
    {
        return pc.getString("allow.special.chars", "");
    }

    public static boolean requestAllowed(
            String aCluster)
    {
        final String allowedVal = pc.getString(aCluster.toLowerCase() + ".instance.allow");
        return ((allowedVal != null) && allowedVal.trim().equals("1"));
    }

    public static String getAppInstanceId()
    {
        return appInstanceId;
    }

    public static void setAppInstanceId(
            String aAppInstanceId)
    {
        appInstanceId = aAppInstanceId;
    }

}