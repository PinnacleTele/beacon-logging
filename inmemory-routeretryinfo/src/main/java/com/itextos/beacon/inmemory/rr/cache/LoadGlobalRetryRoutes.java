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

public class LoadGlobalRetryRoutes
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                 log                   = LogFactory.getLog(LoadGlobalRetryRoutes.class);

    private Map<String, Map<String, String>> mGlobalRetryRouteInfo = new HashMap<>();

    public LoadGlobalRetryRoutes(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, String> getGlobalRetryRouteInfo(
            String aPriority,
            String aOriginalRouteId,
            String aErrorCode,
            String aRetryAttempt)
    {
        final String lKey = getKey(aPriority, aOriginalRouteId, aErrorCode, aRetryAttempt);
        if (log.isDebugEnabled())
            log.debug("globalRetryRoutes==>" + lKey + ">>" + mGlobalRetryRouteInfo);

        return mGlobalRetryRouteInfo.get(lKey);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from global_retry_routes

        // Table Name : common_retry_route_config

        final Map<String, Map<String, String>> lTempGlobalRetryRouteInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final Map<String, String> lValueMap = new HashMap<>();

            final String              lKey      = getKey(aResultSet.getString("priority"), aResultSet.getString("orig_route_id"), aResultSet.getString("error_code"),
                    aResultSet.getString("retry_attempt"));

            lValueMap.put("txn_retry_route", aResultSet.getString("txn_retry_route_id"));
            lValueMap.put("promo_retry_route", aResultSet.getString("promo_retry_route_id"));

            lTempGlobalRetryRouteInfo.put(lKey, lValueMap);
        }
        mGlobalRetryRouteInfo = lTempGlobalRetryRouteInfo;
    }

    private static String getKey(
            String aPriority,
            String aOriginalRouteId,
            String aErrorCode,
            String aRetryAttempt)
    {
        return CommonUtility.combine(aPriority, aOriginalRouteId, aErrorCode, aRetryAttempt);
    }

}
