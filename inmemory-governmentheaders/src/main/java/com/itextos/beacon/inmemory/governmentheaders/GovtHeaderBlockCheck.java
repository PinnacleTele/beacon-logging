package com.itextos.beacon.inmemory.governmentheaders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class GovtHeaderBlockCheck
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log    log          = LogFactory.getLog(GovtHeaderBlockCheck.class);

    private Map<String, String> mGovtHeaders = new HashMap<>();

    public GovtHeaderBlockCheck(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public boolean isGovernmentHeader(
            String aHeader)
    {
        return mGovtHeaders.containsKey(aHeader.toLowerCase());
    }

    public String getGovernmentRoute(
            String aHeader)
    {
        return mGovtHeaders.get(aHeader.toLowerCase());
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, String> loadGovtHeaders = new HashMap<>();

        while (aResultSet.next())
        {
            final String lAlpha = CommonUtility.nullCheck(aResultSet.getString("alpha"), true);

            if ("".equals(lAlpha))
                continue;

            loadGovtHeaders.put(aResultSet.getString("header").trim().toLowerCase(), lAlpha);
        }

        mGovtHeaders = loadGovtHeaders;
    }

}