package com.itextos.beacon.inmemory.msgvalidity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class ClientMsgValidity
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                  log                    = LogFactory.getLog(ClientMsgValidity.class);

    private Map<String, Map<String, Integer>> mClientMsgValidityInfo = new HashMap<>();

    public ClientMsgValidity(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public int getClientMessageValidity(
            String aClientId,
            String aMsgType)
    {
        final ItextosClient lClient = new ItextosClient(aClientId);

        if (mClientMsgValidityInfo.get(lClient.getClientId()) != null)
            return mClientMsgValidityInfo.get(lClient.getClientId()).get(aMsgType);

        if (mClientMsgValidityInfo.get(lClient.getAdmin()) != null)
            return mClientMsgValidityInfo.get(lClient.getAdmin()).get(aMsgType);

        if (mClientMsgValidityInfo.get(lClient.getSuperAdmin()) != null)
            return mClientMsgValidityInfo.get(lClient.getSuperAdmin()).get(aMsgType);

        return 0;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, Map<String, Integer>> lClientMsgValidityMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String               lClientId    = aResultSet.getString("cli_id");

            final Map<String, Integer> lMsgValidity = lClientMsgValidityMap.computeIfAbsent(lClientId, k -> new HashMap());

            lMsgValidity.put("1", aResultSet.getInt("txn_validity"));
            lMsgValidity.put("0", aResultSet.getInt("promo_validity"));
        }

        if (!lClientMsgValidityMap.isEmpty())
            mClientMsgValidityInfo = lClientMsgValidityMap;
    }

}
