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

public class LoadGlobalRetryInterval
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                 log                      = LogFactory.getLog(LoadGlobalRetryInterval.class);

    private Map<String, Map<String, String>> mGlobalRetryIntervalInfo = new HashMap<>();

    public LoadGlobalRetryInterval(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, String> getGlobalRetryInterval(
            String aPriority,
            String aErrorCode,
            String aRetryAttempt)
    {
        final String lKey = getKey(aPriority, aErrorCode, aRetryAttempt);

        if (log.isDebugEnabled())
            log.debug("globalRetryInterval==>" + lKey + ">>" + mGlobalRetryIntervalInfo);
        return mGlobalRetryIntervalInfo.get(lKey);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from global_retry_interval

        // Table Name : common_retry_validity

        final Map<String, Map<String, String>> lTempGlobalRetryIntervalInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final Map<String, String> lValueMap = new HashMap<>();

            final String              lKey      = getKey(aResultSet.getString("priority"), aResultSet.getString("error_code"), aResultSet.getString("retry_attempt"));

            lValueMap.put("txn_interval", aResultSet.getString("txn_retry_validity_in_secs"));
            lValueMap.put("promo_interval", aResultSet.getString("promo_retry_validity_in_secs"));

            lTempGlobalRetryIntervalInfo.put(lKey, lValueMap);
        }

        mGlobalRetryIntervalInfo = lTempGlobalRetryIntervalInfo;
    }

    private static String getKey(
            String aPriority,
            String aErrorCode,
            String aRetryAttempt)
    {
        return CommonUtility.combine(aPriority, aErrorCode, aRetryAttempt);
    }

}
