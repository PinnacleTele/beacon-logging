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

public class MobileNumberRouteInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log    log                   = LogFactory.getLog(MobileNumberRouteInfo.class);

    private Map<String, String> mCustomMobileRouteMap = new HashMap<>();

    public MobileNumberRouteInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public DerivedRoute getDerivedRoute(
            String aClientId,
            String aMobileNumber)
    {
        final ItextosClient lClient  = new ItextosClient(aClientId);

        String              lKey     = CommonUtility.combine(lClient.getClientId(), aMobileNumber);
        String              lRouteId = mCustomMobileRouteMap.get(lKey);
        int                 lLogicId = 91;

        if (lRouteId == null)
        {
            lKey     = CommonUtility.combine(lClient.getAdmin(), aMobileNumber);
            lRouteId = mCustomMobileRouteMap.get(lKey);
            lLogicId = 92;
        }

        if (lRouteId == null)
        {
            lKey     = CommonUtility.combine(lClient.getSuperAdmin(), aMobileNumber);
            lRouteId = mCustomMobileRouteMap.get(lKey);
            lLogicId = 93;
        }

        if (lRouteId == null)
        {
            lKey     = CommonUtility.combine(Constants.NULL_STRING, aMobileNumber);
            lRouteId = mCustomMobileRouteMap.get(lKey);
            lLogicId = 94;
        }
        if (log.isDebugEnabled())
            log.debug("Key: '" + lKey + "', RouteId :'" + lRouteId + "', LogicId:'" + lLogicId + "'");

        if (lRouteId != null)
            return new DerivedRoute(lRouteId, lLogicId, lKey);

        return null;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // Table : intl_mobile_routes

        final Map<String, String> lTempCustomMobileRouteMap = new HashMap<>();

        while (aResultSet.next())
        {
            String lClientId = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);

            if (lClientId.isBlank())
                lClientId = Constants.NULL_STRING;

            final String lMobileNumber = CommonUtility.nullCheck(aResultSet.getString("mnumber"), true);
            final String lRouteId      = CommonUtility.nullCheck(aResultSet.getString("route_id"), true);

            lTempCustomMobileRouteMap.put(CommonUtility.combine(lClientId, lMobileNumber), lRouteId);
        }

        mCustomMobileRouteMap = lTempCustomMobileRouteMap;
    }

}