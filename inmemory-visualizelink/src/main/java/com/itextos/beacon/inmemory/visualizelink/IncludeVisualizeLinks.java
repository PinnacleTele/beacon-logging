package com.itextos.beacon.inmemory.visualizelink;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class IncludeVisualizeLinks
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log         log                 = LogFactory.getLog(IncludeVisualizeLinks.class);

    Map<String, Map<String, String>> mIncludeUrlMap      = new HashMap<>();
    Map<String, String>              mEmptyIncludeUrlMap = new HashMap<>();

    public IncludeVisualizeLinks(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, String> getIncludeVLInfo(
            String aClientId)
    {
        return mIncludeUrlMap.get(aClientId);
    }

    public String getEmptyIncludeVLInfo(
            String aClientId)
    {
        return mEmptyIncludeUrlMap.get(aClientId);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, Map<String, String>> lIncludeUrlMap      = new HashMap<>();
        final Map<String, String>              lEmptyIncludeUrlMap = new HashMap<>();

        // Table Name : r3c_include_url

        while (aResultSet.next())
        {
            final String lClientId    = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lIncludeUrl  = CommonUtility.nullCheck(aResultSet.getString("Include_url"), true);
            final String lSmartLinkId = CommonUtility.nullCheck(aResultSet.getString("smartlink_id"), true);
            final String lIsPartial   = CommonUtility.nullCheck(aResultSet.getString("is_partial"), true);

            if (lIncludeUrl.isEmpty())
                lEmptyIncludeUrlMap.put(lClientId, lSmartLinkId);
            else
            {
                final Map<String, String> lComputeIfAbsent = lIncludeUrlMap.computeIfAbsent(lClientId, k -> new HashMap<>());
                lComputeIfAbsent.put(CommonUtility.combine(lSmartLinkId, lIsPartial), lIncludeUrl);
            }
        }

        mIncludeUrlMap      = lIncludeUrlMap;
        mEmptyIncludeUrlMap = lEmptyIncludeUrlMap;
    }

}
