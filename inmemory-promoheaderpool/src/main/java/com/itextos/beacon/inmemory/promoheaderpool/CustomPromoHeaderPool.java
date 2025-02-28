package com.itextos.beacon.inmemory.promoheaderpool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class CustomPromoHeaderPool
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log          log                 = LogFactory.getLog(CustomPromoHeaderPool.class);
    private Map<String, List<String>> mCustomHeaderIdPool = new HashMap<>();

    public CustomPromoHeaderPool(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public synchronized boolean isCustomPromoHeader(
            String aRoute,
            String aHeaderId)
    {

        try
        {
            final List<String> lHeaderidList = mCustomHeaderIdPool.get(aRoute);
            if (lHeaderidList != null)
                return lHeaderidList.contains(aHeaderId);
        }
        catch (final Exception exp)
        {
            log.error("Exception occer while getting  custom promo heade id...", exp);
        }
        return false;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, List<String>> lCustomHeaderIdInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final List<String> lHeaderIdList = lCustomHeaderIdInfo.computeIfAbsent(aResultSet.getString("route_id"), k -> new ArrayList<>());
            lHeaderIdList.add(aResultSet.getString("header"));
        }

        if (!lCustomHeaderIdInfo.isEmpty())
            mCustomHeaderIdPool = lCustomHeaderIdInfo;
    }

}
