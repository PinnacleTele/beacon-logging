package com.itextos.beacon.smpp.utils.properties;

import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class SmppProperties
{

    private static class SingletonHolder
    {

        static final SmppProperties INSTANCE = new SmppProperties();

    }

    public static SmppProperties getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final PropertiesConfiguration mSmppProperties;

    private SmppProperties()
    {
        mSmppProperties = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.SMPPAPI_PROPERTIES, true);
    }

    public int getAdminPort()
    {
        return CommonUtility.getInteger(mSmppProperties.getString(SmppPropertiesConstant.SMPP_API_ADMIN_PORT), SmppPropertiesConstant.DEFAULT_ADMIN_PORT);
    }

    public int getClientSocketTimeout()
    {
        return CommonUtility.getInteger(mSmppProperties.getString(SmppPropertiesConstant.SMPP_API_CLIENT_SOCKET_TIMEOUT), SmppPropertiesConstant.DEFAULT_CLIENT_SOCKET_TIMEOUT);
    }

    public int getApiListenPort()
    {
        return CommonUtility.getInteger(mSmppProperties.getString(SmppPropertiesConstant.SMPP_API_LISTEN_PORT), SmppPropertiesConstant.DEFAULT_API_LISTEN_PORT);
    }

    public int getApiBindTimeout()
    {
        return CommonUtility.getInteger(mSmppProperties.getString(SmppPropertiesConstant.SMPP_API_BIND_TIMEOUT), SmppPropertiesConstant.DEFAULT_API_BIND_TIMEOUT);
    }

    public int getApiMaxConnections()
    {
        return CommonUtility.getInteger(mSmppProperties.getString(SmppPropertiesConstant.SMPP_API_INSTANCE_ALLOW_MAX_BINDS), SmppPropertiesConstant.DEFAULT_API_INSTANCE_ALLOW_MAX_BINDS);
    }

    public int getApiWindowSize()
    {
        return CommonUtility.getInteger(mSmppProperties.getString(SmppPropertiesConstant.SMPP_API_WINDOW_SIZE), SmppPropertiesConstant.DEFAULT_API_WINDOW_SIZE);
    }

    public int getApiRequestTimeout()
    {
        return CommonUtility.getInteger(mSmppProperties.getString(SmppPropertiesConstant.SMPP_API_REQ_TIMEOUT), SmppPropertiesConstant.DEFAULT_API_REQ_TIMEOUT);
    }

    /*
     * public int getApiMessageExpireMinutes()
     * {
     * return
     * CommonUtility.getInteger(mSmppProperties.getString(SmppPropertiesConstant.
     * SMPP_API_MSG_EXPIRE_IN_MINUTES),
     * SmppPropertiesConstant.DEFAULT_API_MSG_EXPIRE_MINUTES);
     * }
     */

    public int getApiDnReqTimeout()
    {
        return CommonUtility.getInteger(mSmppProperties.getString(SmppPropertiesConstant.SMPP_API_DN_REQ_TIMEOUT), SmppPropertiesConstant.DEFAULT_API_DN_REQ_TIMEOUT);
    }

    public String getInstanceId()
    {
        return CommonUtility.nullCheck(mSmppProperties.getString(SmppPropertiesConstant.STRING_INSTANCE_ID), true);
    }

    public List<Object> getInstanceBindType()
    {
        return mSmppProperties.getList(SmppPropertiesConstant.SMPP_API_ALLOW_BIND_TYPE, SmppPropertiesConstant.DEFAULT_BIND_TYPE);
    }

    public int getApiSessionWindowSize()
    {
        return CommonUtility.getInteger(mSmppProperties.getString(SmppPropertiesConstant.SMPP_API_SESSION_WINDOW_SIZE), SmppPropertiesConstant.DEFAULT_API_SESSION_WINDOW_SIZE);
    }

    /*
     * public int getSmppListenPort()
     * {
     * return
     * CommonUtility.getInteger(mSmppProperties.getString(SmppPropertiesConstant.
     * SMPP_API_VSMSC_LISTEN_PORT));
     * }
     */

    public int getVsmscDnRequestTimeout()
    {
        return CommonUtility.getInteger(mSmppProperties.getString(SmppPropertiesConstant.VSMS_DN_REQUEST_TIMEOUT), SmppPropertiesConstant.DEFAULT_VSMS_DN_REQUEST_TIMEOUT);
    }

    public boolean isAllowDlrSessions()
    {
        return CommonUtility.isEnabled(mSmppProperties.getString(SmppPropertiesConstant.ALLOW_DLR_SESSIONS, SmppPropertiesConstant.DEFAULT_ALLOW_DLR_SESSIONS));
    }

    public boolean isStoreInvalidBindInfoLog()
    {
        return CommonUtility.isEnabled(mSmppProperties.getString(SmppPropertiesConstant.STORE_INVALID_BINDINFO_LOG, SmppPropertiesConstant.DEFAULT_INVALID_BINDINFO_LOG));
    }

    public String getInvalidBindInfoLogFilePath()
    {
        return CommonUtility.nullCheck(mSmppProperties.getString(SmppPropertiesConstant.INVALID_BINDINFO_LOG_FILE_PATH), true);
    }

    public boolean isDbInsertRequired()
    {
        return CommonUtility.isEnabled(mSmppProperties.getString(SmppPropertiesConstant.DB_INSERT_REQUIRED, SmppPropertiesConstant.DEFAULT_DB_INSERT_REQUIRED));
    }

    public long getDnWaitingTime()
    {
        return CommonUtility.getLong(mSmppProperties.getString(SmppPropertiesConstant.SMPP_DN_WAITING_MS), SmppPropertiesConstant.DEFAULT_SMPP_DN_WAITING_MS);
    }

    public int getSmppInterfaceIdleSessionAllowedTime()
    {
        return CommonUtility.getInteger(mSmppProperties.getString(SmppPropertiesConstant.SMPP_IDLE_SESSION_ALLOWED_TIME), SmppPropertiesConstant.DEFAULT_SMPP_IDLE_SESSION_ALLOWED_TIME);
    }

    public int getDisabledAccountCheckSec()
    {
        return CommonUtility.getInteger(mSmppProperties.getString(SmppPropertiesConstant.SMPP_DISABLED_SCCOUNT_CHECK_SEC), SmppPropertiesConstant.DEFAULT_SMPP_DISABLED_SCCOUNT_CHECK_SEC);
    }

    public int getMaxBindAllowed()
    {
        return CommonUtility.getInteger(mSmppProperties.getString(SmppPropertiesConstant.SMPP_MAX_BINDS_PER_INSTANCE), SmppPropertiesConstant.DEFAULT_SMPP_MAX_BINDS_PER_INSTANCE);
    }

    public boolean isBindInfoDbInsertRequired()
    {
        return CommonUtility.isEnabled(mSmppProperties.getString(SmppPropertiesConstant.SMPP_BIND_DB_INSERT_REQ));
    }

    public ClusterType getInstanceCluster()
    {
        return ClusterType.getCluster(CommonUtility.nullCheck(mSmppProperties.getString(SmppPropertiesConstant.SMPP_INSTANCE_CLUSTER), true));
    }

    public long getConcatRedisAllowedMemoryBytes()
    {
        return mSmppProperties.getLong(SmppPropertiesConstant.CONCAT_REDIS_ALLOWED_MEMORY_BYTES, SmppPropertiesConstant.DEFAULT_ALLOWED_LIMIT);
    }

    public long getDefaultAllowedLimit()
    {
        return SmppPropertiesConstant.DEFAULT_ALLOWED_LIMIT;
    }

    public String getTraceMonitorClientId()
    {
        return CommonUtility.nullCheck(mSmppProperties.getString(SmppPropertiesConstant.TRACE_MONITOR_CLIENT), true);
    }

    public boolean isConcatMessageProcessEnable()
    {
        return CommonUtility.isEnabled(CommonUtility.nullCheck(mSmppProperties.getString(SmppPropertiesConstant.CONCAT_MESSAGE_PROCESS_ENABLE), true));
    }

    public int getConcatMessagePoolerRedisConsumerCount()
    {
        return CommonUtility.getInteger(mSmppProperties.getString(SmppPropertiesConstant.CONCAT_MESSAGE_POOLER_REDIS_CONSUMER_COUNT),
                SmppPropertiesConstant.DEFAULT_CONCAT_MESSAGE_POOLER_REDIS_CONSUMER_COUNT);
    }

    public boolean getClusterInstanceAllow(
            String aCluster)
    {
        return CommonUtility.isEnabled(CommonUtility.nullCheck(mSmppProperties.getString(aCluster.toLowerCase() + SmppPropertiesConstant.SMPP_CLUSTER_INSTANCE_ALLOW)));
    }

}
