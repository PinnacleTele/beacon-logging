package com.itextos.beacon.inmemory.userheader;

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

public class VerifiedSmsUserHeaderInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log          log                 = LogFactory.getLog(VerifiedSmsUserHeaderInfo.class);
    private Map<String, List<String>> mVerifiedSmsHeaders = new HashMap<>();

    public VerifiedSmsUserHeaderInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public boolean isVerifiedSMSUserHeader(
            String aClientId,
            String aHeader)
    {
        aHeader = aHeader.toLowerCase();

        final ItextosClient lCustomer   = new ItextosClient(aClientId);

        List<String>        lHeadersIds = mVerifiedSmsHeaders.get(lCustomer.getClientId());

        if ((lHeadersIds != null) && lHeadersIds.contains(aHeader))
            return true;

        lHeadersIds = mVerifiedSmsHeaders.get(lCustomer.getAdmin());

        if ((lHeadersIds != null) && lHeadersIds.contains(aHeader))
            return true;

        lHeadersIds = mVerifiedSmsHeaders.get(lCustomer.getSuperAdmin());

        if ((lHeadersIds != null) && lHeadersIds.contains(aHeader))
            return true;

        return false;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, List<String>> lVerifiedSMSEnabledHeaderIds = new HashMap<>();

        while (aResultSet.next())
        {
            final String lClientId = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lHeaderId = CommonUtility.nullCheck(aResultSet.getString("vsms_registered_header"), true);

            if ("".equals(lClientId) && "".equals(lHeaderId))
                continue;

            final List<String> lVsmsHederIds = lVerifiedSMSEnabledHeaderIds.computeIfAbsent(lClientId, k -> new ArrayList<>());
            lVsmsHederIds.add(lHeaderId.toLowerCase());
        }

        if (!lVerifiedSMSEnabledHeaderIds.isEmpty())
            mVerifiedSmsHeaders = lVerifiedSMSEnabledHeaderIds;
    }

}
