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

public class AlternateHeaderRoute
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log  log                    = LogFactory.getLog(AlternateHeaderRoute.class);

    Map<String, String> mAlternateHeaderRoutes = new HashMap<>();

    public AlternateHeaderRoute(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, String> getAlternateHeaderRoutes()
    {
        return mAlternateHeaderRoutes;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // Select * from senderid_alternate_routes

        // Table : header_alternate_routes
        final Map<String, String> lTempAlternateHeaderRoutes = new HashMap<>();

        while (aResultSet.next())
        {
            final String lCluster         = CommonUtility.nullCheck(aResultSet.getString("cluster"), true).toLowerCase();

            final String lDisallowedRoute = aResultSet.getString("disallowed_route");
            final String lMsgPriority     = aResultSet.getString("priority");
            final String lAllowedroute    = aResultSet.getString("alternate_route");

            final String lKey             = CommonUtility.combine(lCluster, lDisallowedRoute, lMsgPriority);
            lTempAlternateHeaderRoutes.put(lKey, lAllowedroute);
        }

        mAlternateHeaderRoutes = lTempAlternateHeaderRoutes;
    }

}
