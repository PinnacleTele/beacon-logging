package com.itextos.beacon.inmemory.commonlib.promoheaderpool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class RandomHeaderPool
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log          log               = LogFactory.getLog(RandomHeaderPool.class);
    private Map<String, List<String>> mRandomHeaderPool = new HashMap<>();

    public RandomHeaderPool(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public synchronized String getRandomHeader(
            String aRoute)
    {

        try
        {
            final List<String> senderidList = mRandomHeaderPool.get(aRoute);
            if ((senderidList != null) && (!senderidList.isEmpty()))
                return senderidList.get(CommonUtility.getRandomNumber(0, senderidList.size()));
        }
        catch (final Exception exp)
        {
            log.error("Exception occer while getting random promo header id...", exp);
        }
        return null;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, List<String>> lRandomHeaderIdInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final List<String> lRandomHeaderIdList = lRandomHeaderIdInfo.computeIfAbsent(aResultSet.getString("route_id"), k -> new ArrayList<>());
            lRandomHeaderIdList.add(aResultSet.getString("header"));
        }

        if (!lRandomHeaderIdInfo.isEmpty())
            mRandomHeaderPool = lRandomHeaderIdInfo;
    }

}