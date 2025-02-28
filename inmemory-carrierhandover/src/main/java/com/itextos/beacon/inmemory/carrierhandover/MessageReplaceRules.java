package com.itextos.beacon.inmemory.carrierhandover;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class MessageReplaceRules
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                       log                        = LogFactory.getLog(MessageReplaceRules.class);

    private Map<String, List<Map<String, String>>> mClientMsgReplaceRulesInfo = new HashMap<>();

    public MessageReplaceRules(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    private List<Map<String, String>> getClientList(
            String aClientId)
    {

        if (mClientMsgReplaceRulesInfo != null)
        {
            if (log.isDebugEnabled())
                log.debug("Client Id : " + aClientId);

            final ItextosClient lClient = new ItextosClient(aClientId);

            if (mClientMsgReplaceRulesInfo.get(lClient.getClientId()) != null)
                return mClientMsgReplaceRulesInfo.get(lClient.getClientId());

            if (mClientMsgReplaceRulesInfo.get(lClient.getAdmin()) != null)
                return mClientMsgReplaceRulesInfo.get(lClient.getAdmin());

            if (mClientMsgReplaceRulesInfo.get(lClient.getSuperAdmin()) != null)
                return mClientMsgReplaceRulesInfo.get(lClient.getSuperAdmin());
        }
        return new ArrayList<>();
    }

    public boolean canReplaceKeyword(
            String aClientId,
            String aCarrier,
            String aCircle,
            String aRouteId)
    {
        final List<Map<String, String>> lClientList = getClientList(aClientId);

        if (log.isInfoEnabled())
            log.info("lClientList : " + lClientList);

        if (!lClientList.isEmpty())
        {
            final String lCarrier = CommonUtility.nullCheck(aCarrier, true).toLowerCase();
            final String lCircle  = CommonUtility.nullCheck(aCircle, true).toLowerCase();
            final String lRouteId = CommonUtility.nullCheck(aRouteId, true).toLowerCase();

            for (final Map<String, String> lClientInfo : lClientList)
                if (lClientInfo != null)
                {
                    final String lKey = lClientInfo.get("carrier") + lClientInfo.get("circle") + lClientInfo.get("route");

                    if (lKey.isEmpty())
                    {
                        if (log.isInfoEnabled())
                            log.info("ClientId message replace true...");
                        return true;
                    }
                    else
                        if (lKey.equals(lCarrier))
                        {
                            if (log.isInfoEnabled())
                                log.info("ClientId+carrier message replace true...");
                            return true;
                        }
                        else
                            if (lKey.equals(lCircle))
                            {
                                if (log.isInfoEnabled())
                                    log.info("ClientId+circle message replace true...");

                                return true;
                            }
                            else
                                if (lKey.equals(lRouteId))
                                {
                                    if (log.isInfoEnabled())
                                        log.info("ClientId+route message replace true...");

                                    return true;
                                }
                                else
                                    if (lKey.equals(lCarrier + lCircle))
                                    {
                                        if (log.isInfoEnabled())
                                            log.info("ClientId+carrier+circle message replace true...");

                                        return true;
                                    }
                                    else
                                        if (lKey.equals(lCarrier + lRouteId))
                                        {
                                            if (log.isInfoEnabled())
                                                log.info("ClientId+carrier+route message replace true...");

                                            return true;
                                        }
                                        else
                                            if (lKey.equals(lCircle + lRouteId))
                                            {
                                                if (log.isInfoEnabled())
                                                    log.info("ClientId+circle+route message replace true...");

                                                return true;
                                            }
                                            else
                                                if (lKey.equals(lCarrier + lCircle + lRouteId))
                                                {
                                                    if (log.isInfoEnabled())
                                                        log.info("ClientId+carrier+circle+route message replace true...");

                                                    return true;
                                                }
                }
        }

        return false;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // SELECT * FROM acc_msg_replace_rules

        // Table : msg_replace_route_condition

        final Map<String, List<Map<String, String>>> lTempClientMsgReplaceRulesInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final String                    lClientId               = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String                    lCarrier                = CommonUtility.nullCheck(aResultSet.getString("carrier"), true).toLowerCase();
            final String                    lCircle                 = CommonUtility.nullCheck(aResultSet.getString("circle"), true).toLowerCase();
            final String                    lRouteId                = CommonUtility.nullCheck(aResultSet.getString("route_id"), true).toLowerCase();

            final List<Map<String, String>> lClientReplaceRulesList = lTempClientMsgReplaceRulesInfo.computeIfAbsent(lClientId, k -> new ArrayList<>());

            final Map<String, String>       lClientMap              = new HashMap<>();
            lClientMap.put("carrier", lCarrier);
            lClientMap.put("circle", lCircle);
            lClientMap.put("route", lRouteId);
            lClientReplaceRulesList.add(lClientMap);
        }

        mClientMsgReplaceRulesInfo = lTempClientMsgReplaceRulesInfo;
    }

}
