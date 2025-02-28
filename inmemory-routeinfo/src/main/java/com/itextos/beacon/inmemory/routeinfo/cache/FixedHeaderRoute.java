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

public class FixedHeaderRoute
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log  log                = LogFactory.getLog(FixedHeaderRoute.class);

    Map<String, String> mFixedHeaderRoutes = new HashMap<>();

    public FixedHeaderRoute(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, String> getFixedHeaderRoutes()
    {
        return mFixedHeaderRoutes;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // Select * from senderid_fixed_routes

        // Table : header_fixed_routes
        final Map<String, String> lTempFixedHeaderRoutes = new HashMap<>();

        while (aResultSet.next())
        {
            final String lHeader    = CommonUtility.nullCheck(aResultSet.getString("header"), true);

            final String lTxn_route = aResultSet.getString("txn_route_id");

            if ((lTxn_route != null) && StringUtils.isAlphanumeric(lTxn_route))
                lTempFixedHeaderRoutes.put(CommonUtility.combine(lHeader.toUpperCase(), MessageType.TRANSACTIONAL.getKey()), lTxn_route);
        }

        mFixedHeaderRoutes = lTempFixedHeaderRoutes;
    }

}
