package com.itextos.beacon.inmemory.carrierhandover;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class SmppRoutingConfig
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                 log            = LogFactory.getLog(SmppRoutingConfig.class);

    private Map<String, Map<String, String>> mNextRouteInfo = new HashMap<>();
    // private final Map<String, String> mDefaultHeader = new HashMap<>();

    public SmppRoutingConfig(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public long getStoreSize(
            String aRouteId)
    {
        aRouteId = aRouteId.toLowerCase();

        if (mNextRouteInfo.containsKey(aRouteId))
        {
            final Map<String, String> map = mNextRouteInfo.get(aRouteId);
            if ((map != null) && (map.get("storesize") != null) && (map.get("storesize").trim().length() != 0))
                return Long.valueOf(map.get("storesize"));
        }
        return 0;
    }

    public boolean isAlternateRouteAvailable(
            String aRouteId)
    {
        return mNextRouteInfo.containsKey(aRouteId);
    }

    public String getAlternateRoute(
            String aRouteId)
    {
        aRouteId = aRouteId.toLowerCase();

        if (log.isDebugEnabled())
            log.debug("Alternate Routes :" + mNextRouteInfo);

        if (mNextRouteInfo.containsKey(aRouteId))
        {
            final Map<String, String> lAlternateRouteMap = mNextRouteInfo.get(aRouteId);
            if (!lAlternateRouteMap.isEmpty() && !lAlternateRouteMap.get("next_route").isBlank())
                return lAlternateRouteMap.get("next_route");
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

        // SELECT a.route,a.next_route,a.storesize,a.storesize_until_insec FROM
        // smpp_rerouting_config a,route_config b WHERE a.route=b.route AND
        // is_manual='0'

        // Table : rerouting_config, route_configuration

        final Map<String, Map<String, String>> lNextAvailRouteInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final Map<String, String> lAlternateRouteMap = new HashMap<>();
            lAlternateRouteMap.put("next_route", CommonUtility.nullCheck(aResultSet.getString("next_route_id"), true));
            lAlternateRouteMap.put("storesize", aResultSet.getString("kannel_storesize"));
            lAlternateRouteMap.put("storesize_until_insec", aResultSet.getString("storesize_until_in_sec"));
            lNextAvailRouteInfo.put(aResultSet.getString("route_id").toLowerCase(), lAlternateRouteMap);
        }

        mNextRouteInfo = lNextAvailRouteInfo;
    }

}
