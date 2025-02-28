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

public class LoadFirstAttemptMobileRoute
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log          log           = LogFactory.getLog(LoadFirstAttemptMobileRoute.class);

    private Map<String, String> mMobileRoutes = new HashMap<>();

    public LoadFirstAttemptMobileRoute(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public String getFirstMobileRoute(
            String aMobileNumber)
    {
     
        return mMobileRoutes.get(aMobileNumber);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // Select * from mobile_routes

        // Table : mobile_route_config
        final Map<String, String> lTempMobileRoutes = new HashMap<>();

        while (aResultSet.next())
        {
            final String lTxnRoute    = aResultSet.getString("txn_route_id");
            final String lPromoRoute  = aResultSet.getString("promo_route_id");
            final String lMobileNumer = aResultSet.getString("mnumber");

            if (lTxnRoute != null)
                if (StringUtils.isNumeric(lTxnRoute))
                {
                    if (RouteUtil.isRouteGroupAvailable(lTxnRoute + MessageType.TRANSACTIONAL.getKey()))
                        lTempMobileRoutes.put(CommonUtility.combine(lMobileNumer, MessageType.TRANSACTIONAL.getKey()), lTxnRoute);
                }
                else
                    if (StringUtils.isAlphanumeric(lTxnRoute) && RouteUtil.isTXNRoute(lTxnRoute))
                        lTempMobileRoutes.put(CommonUtility.combine(lMobileNumer, MessageType.TRANSACTIONAL.getKey()), lTxnRoute);

            if (lPromoRoute != null)
                if (StringUtils.isNumeric(lPromoRoute))
                {
                    if (RouteUtil.isRouteGroupAvailable(lPromoRoute + MessageType.PROMOTIONAL.getKey()))
                        lTempMobileRoutes.put(CommonUtility.combine(lMobileNumer, MessageType.PROMOTIONAL.getKey()), lPromoRoute);
                }
                else
                    if (StringUtils.isAlphanumeric(lPromoRoute) && RouteUtil.isPromoRoute(lPromoRoute))
                        lTempMobileRoutes.put(CommonUtility.combine(lMobileNumer, MessageType.PROMOTIONAL.getKey()), lPromoRoute);
        }
        mMobileRoutes = lTempMobileRoutes;
    }

}
