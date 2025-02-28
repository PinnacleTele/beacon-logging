package com.itextos.beacon.inmemory.dnpayload.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class AgingInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                 log          = LogFactory.getLog(AgingInfo.class);
    private static final String              AGINGTIME    = "agingtime";

    private Map<String, Map<String, String>> mAgingDnInfo = new HashMap<>();

    public AgingInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public int getAgingDNInfo(
            String aClientId,
            String aRetryAttempt)
    {

        try
        {
            final ItextosClient lClient = new ItextosClient(aClientId);
            String              lKey    = CommonUtility.combine(lClient.getClientId(), aRetryAttempt);
            Map<String, String> info    = mAgingDnInfo.get(lKey);

            if (info == null)
            {
                lKey = CommonUtility.combine(lClient.getAdmin(), aRetryAttempt);
                info = mAgingDnInfo.get(lKey);

                if (info == null)
                    lKey = CommonUtility.combine(lClient.getSuperAdmin(), aRetryAttempt);

                info = mAgingDnInfo.get(lKey);
            }

            if (info != null)
                return Integer.parseInt(info.get(AGINGTIME));
        }
        catch (final Exception exp)
        {
            log.error("returning -1 issue loading dlr ageing wait time ", exp);
        }
        return -1;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from custom_pending_dlr_ageing

        // Table :pending_dlr_ageing

        final Map<String, Map<String, String>> lTempAgingDNMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String lClientId     = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lRetryAttempt = CommonUtility.nullCheck(aResultSet.getString("retry_attempt"), true);
            final String lAgingTime    = CommonUtility.nullCheck(aResultSet.getString("ageing_time"), true);

            if (lClientId.isBlank() || lRetryAttempt.isBlank() || lAgingTime.isBlank())
                continue;

            final String              key      = CommonUtility.combine(lClientId, lRetryAttempt);
            final Map<String, String> lTempMap = new HashMap<>();

            lTempMap.put(AGINGTIME, lAgingTime);
            lTempAgingDNMap.put(key, lTempMap);
        }
        if (!lTempAgingDNMap.isEmpty())
            mAgingDnInfo = lTempAgingDNMap;
    }

}
