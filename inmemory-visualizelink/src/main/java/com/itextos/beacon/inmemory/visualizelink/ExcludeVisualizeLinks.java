package com.itextos.beacon.inmemory.visualizelink;

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

public class ExcludeVisualizeLinks
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log          log                  = LogFactory.getLog(ExcludeVisualizeLinks.class);

    private Map<String, List<String>> mExcludeDomainUrlMap = new HashMap<>();

    public ExcludeVisualizeLinks(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public List<String> getExcludeDomainUrls(
            String aClientId)
    {
        return mExcludeDomainUrlMap.get(aClientId);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, List<String>> lExcludeDomainUrlMap = new HashMap<>();

        // Table Name : r3c_exclude_url

        while (aResultSet.next())
        {
            final String lClientId   = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lExcludeUrl = CommonUtility.nullCheck(aResultSet.getString("exclude_url"), true);
            final String lIsPartial  = CommonUtility.nullCheck(aResultSet.getString("is_partial"), true);

            if (lClientId.isBlank() || lExcludeUrl.isBlank() || lIsPartial.isBlank())
                continue;

            final List<String> list = lExcludeDomainUrlMap.computeIfAbsent(lClientId, k -> new ArrayList<>());
            list.add(CommonUtility.combine(lIsPartial, lExcludeUrl));
        }

        mExcludeDomainUrlMap = lExcludeDomainUrlMap;
    }

}