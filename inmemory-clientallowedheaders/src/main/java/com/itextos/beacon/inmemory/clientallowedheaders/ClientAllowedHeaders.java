package com.itextos.beacon.inmemory.clientallowedheaders;

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

public class ClientAllowedHeaders
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log                log            = LogFactory.getLog(ClientAllowedHeaders.class);

    private Map<String, List<String>> mClientHeaders = new HashMap<>();

    public ClientAllowedHeaders(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public boolean isHeaderAllowed(
            String aClientId,
            String aHeader)
    {
        final List<String> lHeadersLst = mClientHeaders.get(aClientId);

        return !lHeadersLst.isEmpty() && lHeadersLst.contains(aHeader.toLowerCase());
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // SELECT * FROM esme_allowed_senderids

        // Table : client_headers_map
        final Map<String, List<String>> lTempClientAllowedHeaders = new HashMap<>();

        while (aResultSet.next())
        {
            final String lClientId = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lHeader   = CommonUtility.nullCheck(aResultSet.getString("header"), true);

            if (lTempClientAllowedHeaders.get(lClientId) == null)
            {
                final List<String> lTempHeadersList = new ArrayList<>();
                lTempHeadersList.add(lHeader.toLowerCase());
                lTempClientAllowedHeaders.put(lHeader, lTempHeadersList);
                continue;
            }
            final List<String> list = lTempClientAllowedHeaders.get(lClientId);
            list.add(lHeader.toLowerCase());
        }

        mClientHeaders = lTempClientAllowedHeaders;
    }

}
