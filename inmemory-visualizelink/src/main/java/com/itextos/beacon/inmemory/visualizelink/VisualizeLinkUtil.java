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

public class VisualizeLinkUtil
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log    log                = LogFactory.getLog(VisualizeLinkUtil.class);

    private Map<String, String> mVisualizeLinkInfo = new HashMap<>();

    public VisualizeLinkUtil(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public String getVisualizeLinkIds(
            String aVisualizeLinkid)
    {
        return mVisualizeLinkInfo.get(aVisualizeLinkid);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, String> lVLInfoMap = new HashMap<>();

        // Table Name: r3c_smartlink_info

        while (aResultSet.next())
        {
            final String lClientId    = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lSmartlinkId = CommonUtility.nullCheck(aResultSet.getString("smartlink_id"), true);
            final String lDomainUrl   = CommonUtility.nullCheck(aResultSet.getString("domain_url"), true);
            final String lRedirectUrl = CommonUtility.nullCheck(aResultSet.getString("redirect_url"), true);

            if (lClientId.isEmpty() || lSmartlinkId.isEmpty())
                continue;

            if (lRedirectUrl.isEmpty() && lDomainUrl.isEmpty())
                continue;

            lVLInfoMap.put(CommonUtility.combine(lClientId, lSmartlinkId), CommonUtility.combine(lRedirectUrl, lDomainUrl));
        }

        if (!lVLInfoMap.isEmpty())
            mVisualizeLinkInfo = lVLInfoMap;
    }

}