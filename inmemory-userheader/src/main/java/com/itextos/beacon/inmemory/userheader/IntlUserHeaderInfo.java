package com.itextos.beacon.inmemory.userheader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class IntlUserHeaderInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log         log         = LogFactory.getLog(IntlUserHeaderInfo.class);
    private Map<String, Set<String>> mHeaderInfo = new HashMap<>();

    public IntlUserHeaderInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public boolean isHeaderMatches(
            String aClientId,
            String aHeader)
    {
        final Set<String> lSet = mHeaderInfo.get(aClientId);
        if (lSet != null)
            return lSet.contains(aHeader);
        return false;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, Set<String>> lClientHeaderInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final String lClientId = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lHeader   = CommonUtility.nullCheck(aResultSet.getString("header"), true);

            if ("".equals(lClientId) && "".equals(lHeader))
                continue;

            final Set<String> temp = lClientHeaderInfo.computeIfAbsent(lClientId, k -> new HashSet<>());
            temp.add(lHeader.toLowerCase());
        }

        if (!lClientHeaderInfo.isEmpty())
            mHeaderInfo = lClientHeaderInfo;
    }

}