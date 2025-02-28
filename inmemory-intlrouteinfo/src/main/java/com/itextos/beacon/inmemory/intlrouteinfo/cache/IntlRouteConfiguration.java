package com.itextos.beacon.inmemory.intlrouteinfo.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class IntlRouteConfiguration
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                 log                     = LogFactory.getLog(IntlRouteConfiguration.class);

    private Map<String, IntlRouteConfigInfo> mIntlRouteConfiguration = new HashMap<>();

    public IntlRouteConfiguration(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, IntlRouteConfigInfo> getIntlRouteConfig()
    {
        return mIntlRouteConfiguration;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());
        // SELECT * FROM route_intl

        // Table : intl_route_config

        final Map<String, IntlRouteConfigInfo> lTempIntlRouteConfigMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String              lCarrierNw           = aResultSet.getString("carrier_network");
            final String              lCountry             = aResultSet.getString("country");
            final String              lHeaderType          = CommonUtility.nullCheck(aResultSet.getString("header_type"), true).toUpperCase();
            final String              lHeaderSubType       = aResultSet.getString("header_sub_type");

            final String              lDefaultHeader       = aResultSet.getString("default_header");
            final String              lMaxMnumberLen       = aResultSet.getString("mnumber_length_max");
            final String              lMinMnumberLen       = aResultSet.getString("mnumber_length_min");
            final String              lEconomyRouteId      = aResultSet.getString("economy_route_id");
            final String              lRouteId             = aResultSet.getString("route_id");
            final String              lCarrier             = aResultSet.getString("carrier");

            final IntlRouteConfigInfo lIntlRouteConfigInfo = new IntlRouteConfigInfo(lCarrierNw, lCountry, lHeaderType, lDefaultHeader, lMaxMnumberLen, lMinMnumberLen, lEconomyRouteId, lRouteId,
                    lCarrier, lHeaderSubType);

            lTempIntlRouteConfigMap.put(lCarrierNw, lIntlRouteConfigInfo);
        }

        mIntlRouteConfiguration = lTempIntlRouteConfigMap;
    }

}
