package com.itextos.beacon.inmemory.rr.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class LoadCustomRetryMsgValidity
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                 log                      = LogFactory.getLog(LoadCustomRetryMsgValidity.class);

    private Map<String, Map<String, String>> mCustomRetryValidityInfo = new HashMap<>();

    public LoadCustomRetryMsgValidity(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, String> getCustomRetryMsgValidity(
            String aClientId)
    {
        if (log.isDebugEnabled())
            log.debug("customRetriesValidity-->" + aClientId + ">>" + mCustomRetryValidityInfo);
        return mCustomRetryValidityInfo.get(aClientId);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from custom_retries_validity

        // Table Name : client_retry_validity

        final Map<String, Map<String, String>> lTempCustomRetryValidityInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final HashMap<String, String> valueMap = new HashMap<>();

            final String                  key      = aResultSet.getString("cli_id");
            valueMap.put("retries", aResultSet.getString("retries"));
            valueMap.put("msg_validity", aResultSet.getString("msg_validity"));
            lTempCustomRetryValidityInfo.put(key, valueMap);
        }

        mCustomRetryValidityInfo = lTempCustomRetryValidityInfo;
    }

}
