package com.itextos.beacon.inmemory.routeinfo.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;
import com.itextos.beacon.inmemory.routeinfo.util.RouteUtil;

public class LoadCustomerRouteInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log  log           = LogFactory.getLog(LoadCustomerRouteInfo.class);

    Map<String, String> mCustomRoutes = new HashMap<>();

    public LoadCustomerRouteInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, String> getCustomRouteInfo()
    {
        return mCustomRoutes;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // Select * from custom_routes

        // Table : client_route_config

        final Map<String, String> lTempCustomRoutes = new HashMap<>();

        final String              lClientRouteType  = Constants.NULL_STRING;

        while (aResultSet.next())
        {
            String lClientId = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            lClientId = lClientId.isBlank() ? Constants.NULL_STRING : lClientId;

            String lCarrier = CommonUtility.nullCheck(aResultSet.getString("carrier"), true);
            lCarrier = lCarrier.isBlank() ? Constants.NULL_STRING : lCarrier.toLowerCase();

            String lCircle = CommonUtility.nullCheck(aResultSet.getString("circle"), true);
            lCircle = lCircle.isBlank() ? Constants.NULL_STRING : lCircle.toLowerCase();

            final String lTxnRoute   = CommonUtility.nullCheck(aResultSet.getString("txn_route_id"), true);
            final String lPromoRoute = CommonUtility.nullCheck(aResultSet.getString("promo_route_id"), true);

            final String lTransKey   = CommonUtility.combine(lClientId, lCarrier, lCircle, lClientRouteType, MessageType.TRANSACTIONAL.getKey());
            final String lPromoKey   = CommonUtility.combine(lClientId, lCarrier, lCircle, lClientRouteType, MessageType.PROMOTIONAL.getKey());

            if (lTxnRoute != null)
                if (StringUtils.isNumeric(lTxnRoute))
                {
                    if (RouteUtil.isRouteGroupAvailable(lTxnRoute + MessageType.TRANSACTIONAL.getKey()))
                        lTempCustomRoutes.put(lTransKey, lTxnRoute);
                }
                else
                    if (StringUtils.isAlphanumeric(lTxnRoute) && RouteUtil.isTXNRoute(lTxnRoute))
                        lTempCustomRoutes.put(lTransKey, lTxnRoute);

            if (lPromoRoute != null)
                if (StringUtils.isNumeric(lPromoRoute))
                {
                    if (RouteUtil.isRouteGroupAvailable(lPromoRoute + MessageType.PROMOTIONAL.getKey()))
                        lTempCustomRoutes.put(lPromoKey, lPromoRoute);
                }
                else
                    if (StringUtils.isAlphanumeric(lPromoRoute) && RouteUtil.isPromoRoute(lPromoRoute))
                        lTempCustomRoutes.put(lPromoKey, lPromoRoute);
        }

        mCustomRoutes = lTempCustomRoutes;
    }

}
