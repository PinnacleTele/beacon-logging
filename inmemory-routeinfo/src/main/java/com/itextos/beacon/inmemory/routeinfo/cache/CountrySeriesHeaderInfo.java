package com.itextos.beacon.inmemory.routeinfo.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;
import com.itextos.beacon.inmemory.routeinfo.util.DerivedRoute;

public class CountrySeriesHeaderInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log          log                            = LogFactory.getLog(CountrySeriesHeaderInfo.class);

    private Map<String, String> mClientCountrySeriesHeaderInfo = new HashMap<>();

    public CountrySeriesHeaderInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public DerivedRoute getClientCountrySeriesHeaderDerivedRoute(
            String aClientId,
            String aCountry,
            String aCarrier,
            String aHeader)
    {
        final ItextosClient lClient      = new ItextosClient(aClientId);
        DerivedRoute        derivedRoute = getDerivedRouteLocally(lClient.getClientId(), aCountry, aCarrier, aHeader, -50);

        if (derivedRoute != null)
            return derivedRoute;

        derivedRoute = getDerivedRouteLocally(lClient.getAdmin(), aCountry, aCarrier, aHeader, -63);

        if (derivedRoute != null)
            return derivedRoute;

        return getDerivedRouteLocally(lClient.getSuperAdmin(), aCountry, aCarrier, aHeader, -76);
    }

    private DerivedRoute getDerivedRouteLocally(
            String aClientId,
            String aCountry,
            String aCarrier,
            String aHeader,
            int aLogicIndicator)
    {
        aCountry = CommonUtility.nullCheck(aCountry, true).toLowerCase();
        aCarrier = CommonUtility.nullCheck(aCarrier, true).toLowerCase();
        aHeader  = CommonUtility.nullCheck(aHeader, true).toLowerCase();

        String lKey = CommonUtility.combine(aClientId, aCountry, aCarrier, aHeader);

        if (log.isDebugEnabled())
        {
            log.debug("Key :'" + lKey + "'");
            log.debug("intl_client_route_info Map data :" + mClientCountrySeriesHeaderInfo);
        }

        String lRouteIdWithDefaultHeader = mClientCountrySeriesHeaderInfo.get(lKey);

        if (lRouteIdWithDefaultHeader != null)
            return constructDerivedRoute(lRouteIdWithDefaultHeader, (aLogicIndicator - 1), lKey);

        lKey                      = CommonUtility.combine(aClientId, aCountry, CommonUtility.REST_OF_THE_SERIES, aHeader);
        
        if (log.isDebugEnabled())
        {
            log.debug("Key :'" + lKey + "'");
            log.debug("intl_client_route_info Map data :" + mClientCountrySeriesHeaderInfo);
        }
        
        lRouteIdWithDefaultHeader = mClientCountrySeriesHeaderInfo.get(lKey);

        if (lRouteIdWithDefaultHeader != null)
            return constructDerivedRoute(lRouteIdWithDefaultHeader, (aLogicIndicator - 2), lKey);

        lKey                      = CommonUtility.combine(aClientId, CommonUtility.REST_OF_THE_WORLD, aCarrier, aHeader);
 
        if (log.isDebugEnabled())
        {
            log.debug("Key :'" + lKey + "'");
            log.debug("intl_client_route_info Map data :" + mClientCountrySeriesHeaderInfo);
        }
        lRouteIdWithDefaultHeader = mClientCountrySeriesHeaderInfo.get(lKey);

        if (lRouteIdWithDefaultHeader != null)
            return constructDerivedRoute(lRouteIdWithDefaultHeader, (aLogicIndicator - 3), lKey);

        lKey                      = CommonUtility.combine(aClientId, CommonUtility.REST_OF_THE_WORLD, CommonUtility.REST_OF_THE_SERIES, aHeader);
        
        if (log.isDebugEnabled())
        {
            log.debug("Key :'" + lKey + "'");
            log.debug("intl_client_route_info Map data :" + mClientCountrySeriesHeaderInfo);
        }
        
        lRouteIdWithDefaultHeader = mClientCountrySeriesHeaderInfo.get(lKey);

        if (lRouteIdWithDefaultHeader != null)
            return constructDerivedRoute(lRouteIdWithDefaultHeader, (aLogicIndicator - 4), lKey);

        lKey                      = CommonUtility.combine(aClientId, aCountry, Constants.NULL_STRING, aHeader);
        
        if (log.isDebugEnabled())
        {
            log.debug("Key :'" + lKey + "'");
            log.debug("intl_client_route_info Map data :" + mClientCountrySeriesHeaderInfo);
        }
        
        lRouteIdWithDefaultHeader = mClientCountrySeriesHeaderInfo.get(lKey);

        if (lRouteIdWithDefaultHeader != null)
            return constructDerivedRoute(lRouteIdWithDefaultHeader, (aLogicIndicator - 5), lKey);

        lKey                      = CommonUtility.combine(aClientId, CommonUtility.REST_OF_THE_WORLD, Constants.NULL_STRING, aHeader);
        
        if (log.isDebugEnabled())
        {
            log.debug("Key :'" + lKey + "'");
            log.debug("intl_client_route_info Map data :" + mClientCountrySeriesHeaderInfo);
        }
        
        lRouteIdWithDefaultHeader = mClientCountrySeriesHeaderInfo.get(lKey);

        if (lRouteIdWithDefaultHeader != null)
            return constructDerivedRoute(lRouteIdWithDefaultHeader, (aLogicIndicator - 6), lKey);

        lKey                      = CommonUtility.combine(aClientId, aCountry, aCarrier, Constants.NULL_STRING);
        
        if (log.isDebugEnabled())
        {
            log.debug("Key :'" + lKey + "'");
            log.debug("intl_client_route_info Map data :" + mClientCountrySeriesHeaderInfo);
        }
        lRouteIdWithDefaultHeader = mClientCountrySeriesHeaderInfo.get(lKey);

        if (lRouteIdWithDefaultHeader != null)
            return constructDerivedRoute(lRouteIdWithDefaultHeader, (aLogicIndicator - 7), lKey);

        lKey                      = CommonUtility.combine(aClientId, aCountry, CommonUtility.REST_OF_THE_SERIES, Constants.NULL_STRING);
       
        if (log.isDebugEnabled())
        {
            log.debug("Key :'" + lKey + "'");
            log.debug("intl_client_route_info Map data :" + mClientCountrySeriesHeaderInfo);
        }
        
        lRouteIdWithDefaultHeader = mClientCountrySeriesHeaderInfo.get(lKey);

        if (lRouteIdWithDefaultHeader != null)
            return constructDerivedRoute(lRouteIdWithDefaultHeader, (aLogicIndicator - 8), lKey);

        lKey                      = CommonUtility.combine(aClientId, CommonUtility.REST_OF_THE_WORLD, aCarrier, Constants.NULL_STRING);
        
        if (log.isDebugEnabled())
        {
            log.debug("Key :'" + lKey + "'");
            log.debug("intl_client_route_info Map data :" + mClientCountrySeriesHeaderInfo);
        }
        
        lRouteIdWithDefaultHeader = mClientCountrySeriesHeaderInfo.get(lKey);

        if (lRouteIdWithDefaultHeader != null)
            return constructDerivedRoute(lRouteIdWithDefaultHeader, (aLogicIndicator - 9), lKey);

        lKey                      = CommonUtility.combine(aClientId, CommonUtility.REST_OF_THE_WORLD, CommonUtility.REST_OF_THE_SERIES, Constants.NULL_STRING);
        
        if (log.isDebugEnabled())
        {
            log.debug("Key :'" + lKey + "'");
            log.debug("intl_client_route_info Map data :" + mClientCountrySeriesHeaderInfo);
        }
        lRouteIdWithDefaultHeader = mClientCountrySeriesHeaderInfo.get(lKey);

        if (lRouteIdWithDefaultHeader != null)
            return constructDerivedRoute(lRouteIdWithDefaultHeader, (aLogicIndicator - 10), lKey);

        lKey                      = CommonUtility.combine(aClientId, aCountry, Constants.NULL_STRING, Constants.NULL_STRING);
        
        if (log.isDebugEnabled())
        {
            log.debug("Key :'" + lKey + "'");
            log.debug("intl_client_route_info Map data :" + mClientCountrySeriesHeaderInfo);
        }
        lRouteIdWithDefaultHeader = mClientCountrySeriesHeaderInfo.get(lKey);

        if (lRouteIdWithDefaultHeader != null)
            return constructDerivedRoute(lRouteIdWithDefaultHeader, (aLogicIndicator - 11), lKey);

        lKey                      = CommonUtility.combine(aClientId, Constants.NULL_STRING, Constants.NULL_STRING, aHeader);
        
        if (log.isDebugEnabled())
        {
            log.debug("Key :'" + lKey + "'");
            log.debug("intl_client_route_info Map data :" + mClientCountrySeriesHeaderInfo);
        }
        lRouteIdWithDefaultHeader = mClientCountrySeriesHeaderInfo.get(lKey);

        if (lRouteIdWithDefaultHeader != null)
            return constructDerivedRoute(lRouteIdWithDefaultHeader, (aLogicIndicator - 12), lKey);

        lKey                      = CommonUtility.combine(aClientId, CommonUtility.REST_OF_THE_WORLD, Constants.NULL_STRING, Constants.NULL_STRING);
        
        if (log.isDebugEnabled())
        {
            log.debug("Key :'" + lKey + "'");
            log.debug("intl_client_route_info Map data :" + mClientCountrySeriesHeaderInfo);
        }
        lRouteIdWithDefaultHeader = mClientCountrySeriesHeaderInfo.get(lKey);

        if (lRouteIdWithDefaultHeader != null)
            return constructDerivedRoute(lRouteIdWithDefaultHeader, (aLogicIndicator - 13), lKey);

        return null;
    }

    private static DerivedRoute constructDerivedRoute(
            String aRouteIDAndDefaultSenderID,
            int aLogicIndicator,
            String aKey)
    {

        if (aRouteIDAndDefaultSenderID != null)
        {
            final String[] lRouteIdWithHeader = CommonUtility.split(aRouteIDAndDefaultSenderID);
            String         lRouteId;
            final String   lHeader;

            if (lRouteIdWithHeader.length == 2)
            {
                lRouteId = lRouteIdWithHeader[0];
                lHeader  = "".equals(CommonUtility.nullCheck(lRouteIdWithHeader[1], true)) ? null : lRouteIdWithHeader[1];
            }
            else
            {
                lRouteId = lRouteIdWithHeader[0];
                lHeader  = null;
            }
            if (log.isDebugEnabled())
                log.debug("aRouteIDAndDefaultSenderID : '" + aRouteIDAndDefaultSenderID + "', Route ID : '" + lRouteId + "', Header : '" + lHeader + "' for the Key : '" + aKey
                        + "' and logic id is : '" + aLogicIndicator + "'");

            return new DerivedRoute(lRouteId, aLogicIndicator, aKey, lHeader);
        }
        return null;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from acct_route_intl

        // Table : intl_client_route_info

        final Map<String, String> lTempCountrySeriesHeaderMap = new HashMap<>();

        while (aResultSet.next())
        {
            String lClientId            = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            lClientId = lClientId.isBlank() ? Constants.NULL_STRING : lClientId;

            String lCountry             = CommonUtility.nullCheck(aResultSet.getString("country"), true).toLowerCase();
            lCountry = lCountry.isBlank() ? Constants.NULL_STRING : lCountry;

            String lCountryCodeNWSeries = CommonUtility.nullCheck(aResultSet.getString("carrier_network"), true).toLowerCase();
            
            lCountryCodeNWSeries = lCountryCodeNWSeries.isBlank() ? Constants.NULL_STRING : lCountryCodeNWSeries;

            String lHeader              = CommonUtility.nullCheck(aResultSet.getString("header"), true).toLowerCase();
            lHeader = lHeader.isBlank() ? Constants.NULL_STRING : lHeader;

            
            String lRouteId             = CommonUtility.nullCheck(aResultSet.getString("route_id"), true);
            lRouteId = lRouteId.isBlank() ? Constants.NULL_STRING : lRouteId;

             String lDefaultHeader       = CommonUtility.nullCheck(aResultSet.getString("default_header"), true);

             lDefaultHeader = lDefaultHeader.isBlank() ? Constants.NULL_STRING : lDefaultHeader;

            if (lRouteId.isEmpty())
                continue;

            final String lKey = CommonUtility.combine(lClientId, lCountry, lCountryCodeNWSeries, lHeader);
            lTempCountrySeriesHeaderMap.put(lKey, CommonUtility.combine(lRouteId, lDefaultHeader));
        }

        mClientCountrySeriesHeaderInfo = lTempCountrySeriesHeaderMap;
    }

}
