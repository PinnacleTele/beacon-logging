package com.itextos.beacon.inmemory.routeinfo.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class IntlRouteHeader
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log                       log              = LogFactory.getLog(IntlRouteHeader.class);

    private Map<String, Map<String, String>> mIntlRouteHeader = new HashMap<>();

    public IntlRouteHeader(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, Map<String, String>> getIntlRouteHeaderInfo()
    {
        return mIntlRouteHeader;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // SELECT * FROM route_intl_senderid

        // Table : intl_route_header

        final Map<String, Map<String, String>> lTempRouteHeaderMap = new HashMap<>();

        while (aResultSet.next())
        {
            final Map<String, String> lTempMap = new HashMap<>();

            lTempMap.put("header_type", CommonUtility.nullCheck(aResultSet.getString("header_type"), true));
            lTempMap.put("header_sub_type", aResultSet.getString("header_sub_type"));
            lTempMap.put("default_header", aResultSet.getString("default_header"));
            final String lCountry  = aResultSet.getString("country").toUpperCase();
            final String lRrouteId = aResultSet.getString("route_id");
            lTempRouteHeaderMap.put(CommonUtility.combine(lCountry, lRrouteId), lTempMap);
        }

        mIntlRouteHeader = lTempRouteHeaderMap;
    }

}
