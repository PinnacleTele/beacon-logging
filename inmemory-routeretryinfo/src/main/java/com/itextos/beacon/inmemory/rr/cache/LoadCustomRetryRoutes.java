package com.itextos.beacon.inmemory.rr.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class LoadCustomRetryRoutes
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                 log                   = LogFactory.getLog(LoadCustomRetryRoutes.class);

    private Map<String, Map<String, String>> mCustomRetryRouteInfo = new HashMap<>();

    public LoadCustomRetryRoutes(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, String> getCustomRetryRoutes(
            String aClientId,
            String aOriginalRouteId,
            String aErrorCode,
            String aRetryAttempt)
    {
        final String lKey = CommonUtility.combine(aClientId, aOriginalRouteId, aErrorCode, aRetryAttempt);

        if (log.isDebugEnabled())
        {
            log.debug("key : " + lKey);
            log.debug("customRetryRoutes : " + mCustomRetryRouteInfo);
        }
        return mCustomRetryRouteInfo.get(lKey);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from custom_retry_routes

        // Table Name: retry_route_config

        final Map<String, Map<String, String>> lTempCustomRetryRouteInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final Map<String, String> lValueMap = new HashMap<>();

            final String              lKey      = CommonUtility.combine(aResultSet.getString("cli_id"), aResultSet.getString("orig_route_id"), aResultSet.getString("error_code"),
                    aResultSet.getString("retry_attempt"));

            lValueMap.put("interval", aResultSet.getString("interval"));
            lValueMap.put("channel", aResultSet.getString("channel"));
            lValueMap.put("route_id", aResultSet.getString("route_id"));
            lValueMap.put("voice_cfg_id", aResultSet.getString("voice_cfg_id"));

            lTempCustomRetryRouteInfo.put(lKey, lValueMap);
        }

        mCustomRetryRouteInfo = lTempCustomRetryRouteInfo;
    }

}
