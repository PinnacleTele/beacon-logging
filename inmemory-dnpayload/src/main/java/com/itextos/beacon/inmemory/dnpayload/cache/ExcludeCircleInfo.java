package com.itextos.beacon.inmemory.dnpayload.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class ExcludeCircleInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log          log                = LogFactory.getLog(ExcludeCircleInfo.class);

    private Map<String, List<String>> mExcludeCircleInfo = new HashMap<>();

    public ExcludeCircleInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public List<String> getExcludeCircles(
            String aClientId)
    {
        if (log.isDebugEnabled())
            log.debug("ClientId:" + aClientId + " circleExclude:" + mExcludeCircleInfo);

        final ItextosClient lClient = new ItextosClient(aClientId);

        List<String>        lList   = mExcludeCircleInfo.get(lClient.getClientId());
        if (lList != null)
            return lList;

        lList = mExcludeCircleInfo.get(lClient.getAdmin());
        if (lList != null)
            return lList;

        return mExcludeCircleInfo.get(lClient.getSuperAdmin());
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select esmeaddr,circle from circle_exclude
        // Tables: circle_exclude_config

        final Map<String, List<String>> lExcludeCircleInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final String lClientId = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lCircle   = CommonUtility.nullCheck(aResultSet.getString("circle"), true).toLowerCase();

            if (lClientId.isEmpty() || lCircle.isEmpty())
                continue;

            final List<String> list = lExcludeCircleInfo.computeIfAbsent(lClientId, k -> new ArrayList<>());
            list.add(lCircle);
        }

        if (!lExcludeCircleInfo.isEmpty())
            mExcludeCircleInfo = lExcludeCircleInfo;
    }

}