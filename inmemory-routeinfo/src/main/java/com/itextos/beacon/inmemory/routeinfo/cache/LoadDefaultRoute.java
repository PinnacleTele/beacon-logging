package com.itextos.beacon.inmemory.routeinfo.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;
import com.itextos.beacon.inmemory.routeinfo.util.RouteUtil;

public class LoadDefaultRoute
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private final Log   log           = LogFactory.getLog(LoadDefaultRoute.class);

    Map<String, String> mDefaultRoute = new HashMap<>();

    public LoadDefaultRoute(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, String> getDefaultRoutes()
    {
        return mDefaultRoute;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // Select * from priority_routes

        // Table : priority_routes
        final Map<String, String> lTempDefaultRoute = new HashMap<>();

        while (aResultSet.next())
        {
            final String lTxnRoute   = aResultSet.getString("txn_route_id");
            final String lPromoRoute = aResultSet.getString("promo_route_id");
            final String lPriority   = aResultSet.getString("priority");

            final String lTransKey   = CommonUtility.combine(lPriority, MessageType.TRANSACTIONAL.getKey());
            final String lPromoKey   = CommonUtility.combine(lPriority, MessageType.PROMOTIONAL.getKey());

            if (lTxnRoute != null)
                if (StringUtils.isNumeric(lTxnRoute))
                {
                    if (RouteUtil.isRouteGroupAvailable(lTxnRoute + MessageType.TRANSACTIONAL.getKey()))
                        lTempDefaultRoute.put(lTransKey, lTxnRoute);
                }
                else
                    if (StringUtils.isAlphanumeric(lTxnRoute) && RouteUtil.isTXNRoute(lTxnRoute))
                        lTempDefaultRoute.put(lTransKey, lTxnRoute);

            if (lPromoRoute != null)
                if (StringUtils.isNumeric(lPromoRoute))
                {
                    if (RouteUtil.isRouteGroupAvailable(lPromoRoute + MessageType.PROMOTIONAL.getKey()))
                        lTempDefaultRoute.put(lPromoKey, lPromoRoute);
                }
                else
                    if (StringUtils.isAlphanumeric(lPromoRoute) && RouteUtil.isPromoRoute(lPromoRoute))
                        lTempDefaultRoute.put(lPromoKey, lPromoRoute);
        }

        mDefaultRoute = lTempDefaultRoute;
    }

}
