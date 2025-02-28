package com.itextos.beacon.inmemory.routeinfo.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class PriorityHeaderOpenRoute
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log  log                   = LogFactory.getLog(PriorityHeaderOpenRoute.class);

    Map<String, String> mPriorityHeaderRoutes = new HashMap<>();

    public PriorityHeaderOpenRoute(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, String> getPriorityHeaderOpenRoute()
    {
        return mPriorityHeaderRoutes;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // Select * from senderid_priority_open_routes

        // Table : header_priority_open_routes
        final Map<String, String> lTempPriorityHeaderRoutes = new HashMap<>();

        final String              lDisallowedRoute          = Constants.NULL_STRING;

        while (aResultSet.next())
        {
            final String lCluster        = CommonUtility.nullCheck(aResultSet.getString("cluster"), true).toLowerCase();
            final String lMsgPriority    = aResultSet.getString("priority");
            final String lAllowedRouteId = aResultSet.getString("open_route_id");
            if (StringUtils.isAlphanumeric(lAllowedRouteId))
                lTempPriorityHeaderRoutes.put(CommonUtility.combine(lCluster, lDisallowedRoute, lMsgPriority), lAllowedRouteId);
        }

        mPriorityHeaderRoutes = lTempPriorityHeaderRoutes;
    }

}
