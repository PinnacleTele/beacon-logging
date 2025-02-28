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

public class LoadHeeaderStatus
        extends
        AbstractAutoRefreshInMemoryProcessor

{

    private final Log    log                = LogFactory.getLog(LoadHeeaderStatus.class);

    Map<String, Integer> mHeaderRouteStatus = new HashMap<>();

    public LoadHeeaderStatus(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public boolean getHeaderRouteAvailable(
            String aKey)
    {
        return mHeaderRouteStatus.containsKey(aKey);
    }

    public Integer getHeaderRouteStatus(
            String aKey)
    {
        return mHeaderRouteStatus.get(aKey);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // Select * from senderid_route_status

        // Table : header_route_status
        final Map<String, Integer> lTempHeaderRouteStatus = new HashMap<>();

        while (aResultSet.next())
        {
            final String lHeader        = CommonUtility.nullCheck(aResultSet.getString("header"), true).toUpperCase();
            final String lRouteId       = CommonUtility.nullCheck(aResultSet.getString("route_id"), true);
            final int    lAllowedStatus = aResultSet.getInt("allowed_status");

            final String lKey           = CommonUtility.combine(lHeader, lRouteId);
            lTempHeaderRouteStatus.put(lKey, lAllowedStatus);
        }
        mHeaderRouteStatus = lTempHeaderRouteStatus;
    }

}
