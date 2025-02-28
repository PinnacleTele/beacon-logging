package com.itextos.beacon.inmemory.commonheader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class CommonHeaders
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log log                = LogFactory.getLog(CommonHeaders.class);

    private Set<String>      mCommonHeadersList = new HashSet<>();

    public CommonHeaders(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public boolean isCommonHeader(
            String aHeader)
    {
        return mCommonHeadersList.contains(aHeader.toLowerCase());
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Set<String> lCommonHeaders = new HashSet<>();

        while (aResultSet.next())
            lCommonHeaders.add(aResultSet.getString("header").toLowerCase());

        if (!lCommonHeaders.isEmpty())
            mCommonHeadersList = lCommonHeaders;
    }

}