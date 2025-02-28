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

public class LoadCarrierSupportHeaders
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log  log                    = LogFactory.getLog(LoadCarrierSupportHeaders.class);

    Map<String, String> mCarrierSupoortHeaders = new HashMap<>();

    public LoadCarrierSupportHeaders(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, String> getCarrierSupportHeaders()
    {
        return mCarrierSupoortHeaders;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // SELECT * FROM intl_telco_support_senderids

        // Table : intl_carrier_support_headers

        final Map<String, String> lTempCarrierSupoortHeaderMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String lCarrierSupportedHeader = CommonUtility.nullCheck(aResultSet.getString("carrier_header"), true);

            if (lCarrierSupportedHeader.isBlank())
                continue;

            final String lRouteId = CommonUtility.nullCheck(aResultSet.getString("route_id"), true).toUpperCase();
            final String lHeader  = CommonUtility.nullCheck(aResultSet.getString("header"), true).toLowerCase();

            lTempCarrierSupoortHeaderMap.put(CommonUtility.combine(lRouteId, lHeader), lCarrierSupportedHeader);
        }

        mCarrierSupoortHeaders = lTempCarrierSupoortHeaderMap;
    }

}
