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

public class LoadGlobalRetryInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                 log              = LogFactory.getLog(LoadGlobalRetryInfo.class);

    private Map<String, Map<String, String>> mGlobalRetryInfo = new HashMap<>();

    public LoadGlobalRetryInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, String> getGlobalRetry(
            String aOriginalRouteType,
            String aPriority)
    {
        final String lKey = getKey(aOriginalRouteType, aPriority);
        if (log.isDebugEnabled())
            log.debug("mGlobalRetryInfo : " + lKey + ">>" + mGlobalRetryInfo);
        return mGlobalRetryInfo.get(lKey);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from global_retries

        // Table Name : retry_config

        final Map<String, Map<String, String>> lTempGlobalRetryInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final Map<String, String> lValueMap = new HashMap<>();

            final String              lKey      = getKey(aResultSet.getString("orig_route_type"), aResultSet.getString("priority"));

            lValueMap.put("txn_retries", aResultSet.getString("txn_retries"));
            lValueMap.put("promo_retries", aResultSet.getString("promo_retries"));

            lTempGlobalRetryInfo.put(lKey, lValueMap);
        }

        mGlobalRetryInfo = lTempGlobalRetryInfo;
    }

    private static String getKey(
            String aOriginalRouteType,
            String aPriority)
    {
        return CommonUtility.combine(aOriginalRouteType, aPriority);
    }

}
