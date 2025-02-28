package com.itextos.beacon.inmemory.smpp.account;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class SmppAccountInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log log             = LogFactory.getLog(SmppAccountInfo.class);

    Map<String, SmppAccInfo> mSmppAccInfoMap = new HashMap<>();

    public SmppAccountInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public SmppAccInfo getSmppAccountInfo(
            String aClientId)
    {
        final ItextosClient lClient      = new ItextosClient(aClientId);

        SmppAccInfo         lSmppAccInfo = mSmppAccInfoMap.get(lClient.getClientId());
        if (lSmppAccInfo != null)
            return lSmppAccInfo;

        lSmppAccInfo = mSmppAccInfoMap.get(lClient.getAdmin());
        if (lSmppAccInfo != null)
            return lSmppAccInfo;

        return mSmppAccInfoMap.get(lClient.getSuperAdmin());
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, SmppAccInfo> lSmppAccInfoMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String      lClientId       = aResultSet.getString("cli_id");
            final String      lBindType       = CommonUtility.nullCheck(aResultSet.getString("bind_type"), true);
            final int         lMaxConnAllowed = CommonUtility.getInteger(aResultSet.getString("max_allowed_connections"));
            final int         lMaxSpeed       = CommonUtility.getInteger(aResultSet.getString("max_speed"));
            final String      lCharSet        = CommonUtility.nullCheck(aResultSet.getString("charset"), true);
            final String      lDltEntityId    = CommonUtility.nullCheck(aResultSet.getString("dlt_entityid_tag"), true);
            final String      lDltTemplateId  = CommonUtility.nullCheck(aResultSet.getString("dlt_templateid_tag"), true);
            final int         lDNExpiryInSec  = CommonUtility.getInteger(aResultSet.getString("dn_expiry_in_sec"));
            final String      lDNDateFormat   = CommonUtility.nullCheck(aResultSet.getString("dn_date_format"), true);
            final String      lClientMidTag   = CommonUtility.nullCheck(aResultSet.getString("cli_mid_tag"), true);

            final SmppAccInfo lSmppAccInfo    = new SmppAccInfo(lBindType, lMaxConnAllowed, lMaxSpeed, lCharSet, lDltEntityId, lDltTemplateId, lDNExpiryInSec, lDNDateFormat, lClientMidTag);

            lSmppAccInfoMap.put(lClientId, lSmppAccInfo);
        }

        if (!lSmppAccInfoMap.isEmpty())
            mSmppAccInfoMap = lSmppAccInfoMap;
    }

}