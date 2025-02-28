package com.itextos.beacon.inmemory.routeinfo.cache;

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
import com.itextos.beacon.inmemory.routeinfo.util.DerivedRoute;
import com.itextos.beacon.inmemory.routeinfo.util.IntlRUtils;

public class ClientKeywordHeaderInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log                       log                             = LogFactory.getLog(ClientKeywordHeaderInfo.class);

    private Map<String, Map<String, String>> mClientCountryKeywordHeaderInfo = new HashMap<>();

    public ClientKeywordHeaderInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from intl_account_template

        // Table : intl_client_header_template
        final Map<String, Map<String, String>> lTempClientKeywordHeaderMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String lClientId = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lCountry  = CommonUtility.nullCheck(aResultSet.getString("country"), true);
            final String lKeywords = CommonUtility.nullCheck(aResultSet.getString("keywords"), true);
            final String lHeader   = CommonUtility.nullCheck(aResultSet.getString("header"), true);
            final String lRouteid  = CommonUtility.nullCheck(aResultSet.getString("route_id"), true);

            if ("".equals(lRouteid))
                continue;

            final String              lKey     = CommonUtility.combine(lClientId, CommonUtility.getAnyString(lCountry), CommonUtility.getAnyString(lHeader));

            final Map<String, String> lTempMap = lTempClientKeywordHeaderMap.computeIfAbsent(lKey, k -> new HashMap<>());

            lTempMap.put(lKeywords, lRouteid);
        }

        mClientCountryKeywordHeaderInfo = lTempClientKeywordHeaderMap;
    }

    public DerivedRoute getDerivedRoute(
            String aClientId,
            String aCountry,
            String aMessage,
            String aHeader)
    {
        final ItextosClient lClient       = new ItextosClient(aClientId);
        DerivedRoute        lDerivedRoute = getDerivedRouteLocally(lClient.getClientId(), aCountry, aMessage, aHeader, 80);
        if (lDerivedRoute != null)
            return lDerivedRoute;

        lDerivedRoute = getDerivedRouteLocally(lClient.getAdmin(), aCountry, aMessage, aHeader, 30);
        if (lDerivedRoute != null)
            return lDerivedRoute;

        return getDerivedRouteLocally(lClient.getSuperAdmin(), aCountry, aMessage, aHeader, 40);
    }

    private DerivedRoute getDerivedRouteLocally(
            String aClientId,
            String aCountry,
            String aMessage,
            String aHeader,
            int aLogicIndicator)
    {
        aCountry = CommonUtility.nullCheck(aCountry, true).toLowerCase();
        aHeader  = CommonUtility.nullCheck(aHeader, true).toLowerCase();

        String key = CommonUtility.combine(aClientId, aCountry, aHeader);

        if (log.isDebugEnabled())
        {
            log.debug("Derived Route Key :" + key);
            log.debug("ClientCountryKeywordHeader map :" + mClientCountryKeywordHeaderInfo);
        }

        String routeID = (String) IntlRUtils.verifyKeywordInMessage(mClientCountryKeywordHeaderInfo.get(key), aMessage, false);

        if (routeID != null)
            return new DerivedRoute(routeID, (aLogicIndicator + 1), key);

        // ClientId + Country + Keyword/Phrase + Any Header
        key     = CommonUtility.combine(aClientId, aCountry, CommonUtility.ANY_VALUE);
        routeID = (String) IntlRUtils.verifyKeywordInMessage(mClientCountryKeywordHeaderInfo.get(key), aMessage, false);

        if (routeID != null)
            return new DerivedRoute(routeID, (aLogicIndicator + 2), key);

        // ClientId + Country + Any keyword/Phrase + Header
        key     = CommonUtility.combine(aClientId, aCountry, aHeader);
        routeID = (String) IntlRUtils.verifyKeywordInMessage(mClientCountryKeywordHeaderInfo.get(key), aMessage, true);

        if (routeID != null)
            return new DerivedRoute(routeID, (aLogicIndicator + 3), key);

        // ClientId + Any Country + Keyword/Phrase + Header
        key     = CommonUtility.combine(aClientId, CommonUtility.ANY_VALUE, aHeader);
        routeID = (String) IntlRUtils.verifyKeywordInMessage(mClientCountryKeywordHeaderInfo.get(key), aMessage, false);

        if (routeID != null)
            return new DerivedRoute(routeID, (aLogicIndicator + 4), key);

        // ClientId + Any Country + Keyword/Phrase + Any Header
        key     = CommonUtility.combine(aClientId, CommonUtility.ANY_VALUE, CommonUtility.ANY_VALUE);
        routeID = (String) IntlRUtils.verifyKeywordInMessage(mClientCountryKeywordHeaderInfo.get(key), aMessage, false);

        if (routeID != null)
            return new DerivedRoute(routeID, (aLogicIndicator + 5), key);

        // ClientId + Any Country + Any keyword/Phrase + Header
        key     = CommonUtility.combine(aClientId, CommonUtility.ANY_VALUE, aHeader);
        routeID = (String) IntlRUtils.verifyKeywordInMessage(mClientCountryKeywordHeaderInfo.get(key), aMessage, true);

        if (routeID != null)
            return new DerivedRoute(routeID, (aLogicIndicator + 6), key);

        // ClientId + Any Country + Any keyword/Phrase + Header -- Rest of the World
        key     = CommonUtility.combine(aClientId, CommonUtility.REST_OF_THE_WORLD, aHeader);
        routeID = (String) IntlRUtils.verifyKeywordInMessage(mClientCountryKeywordHeaderInfo.get(key), aMessage, false);

        if (routeID != null)
            return new DerivedRoute(routeID, (aLogicIndicator + 7), key);

        return null;
    }

}
