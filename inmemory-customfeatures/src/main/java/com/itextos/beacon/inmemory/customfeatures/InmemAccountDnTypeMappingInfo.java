package com.itextos.beacon.inmemory.customfeatures;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.customfeatures.pojo.DNDeliveryMode;
import com.itextos.beacon.inmemory.customfeatures.pojo.DlrTypeInfo;
import com.itextos.beacon.inmemory.customfeatures.pojo.SingleDnProcessType;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class InmemAccountDnTypeMappingInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log         log               = LogFactory.getLog(InmemAccountDnTypeMappingInfo.class);

    private Map<String, DlrTypeInfo> mClientDNTypeInfo = new HashMap<>();

    public InmemAccountDnTypeMappingInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, DlrTypeInfo> getDnTypeInfoMap()
    {
        return mClientDNTypeInfo;
    }

    public DlrTypeInfo getDnTypeInfo(
            String aClientId)
    {
        return mClientDNTypeInfo.get(aClientId);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // Table : client_dntype_config

        final Map<String, DlrTypeInfo> lClientDnTypeInfo = new HashMap<>();
        final List<DlrTypeInfo>        lDnTypeInfo       = new ArrayList<>();

        while (aResultSet.next())
        {
            final String  lClientId          = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String  lDnType            = CommonUtility.nullCheck(aResultSet.getString("dn_type"), true);
            final String  lWaitForDnInSec    = CommonUtility.nullCheck(aResultSet.getString("expiry_in_sec"), true);
            final String  lDnStatusCategory  = CommonUtility.nullCheck(aResultSet.getString("dn_status_category"), true);
            final String  lDnHandoverMode    = CommonUtility.nullCheck(aResultSet.getString("dn_handover_mode"), true);
            final String  lHandoverStatus    = CommonUtility.nullCheck(aResultSet.getString("response_status"), true);
            final String  lAntDNHandoverMode = CommonUtility.nullCheck(aResultSet.getString("alt_dn_handover_mode"), true);
            final String  lAltHandoverStatus = CommonUtility.nullCheck(aResultSet.getString("alt_response_status"), true);
            final boolean isWaitForAllParts  = CommonUtility.isEnabled(aResultSet.getString("wait_for_all_parts"));

            if ("".equals(lClientId))
                continue;

            final SingleDnProcessType lSingleDnProcessType = SingleDnProcessType.getSingleDnProcessType(lDnStatusCategory);
            final DNDeliveryMode      lDnDeliveryMode      = DNDeliveryMode.getDNDeliveryMode(lDnHandoverMode);
            final DNDeliveryMode      lAltDnDeliveryMode   = DNDeliveryMode.getDNDeliveryMode(lAntDNHandoverMode);

            final DlrTypeInfo         lDlrType             = new DlrTypeInfo(lClientId, lDnType, lWaitForDnInSec, lSingleDnProcessType, lDnDeliveryMode, lHandoverStatus, lAltDnDeliveryMode,
                    lAltHandoverStatus, isWaitForAllParts);

            lClientDnTypeInfo.put(lClientId, lDlrType);

            lDnTypeInfo.add(lDlrType);
        }

        if (lClientDnTypeInfo.size() > 0)
            mClientDNTypeInfo = lClientDnTypeInfo;
    }

}