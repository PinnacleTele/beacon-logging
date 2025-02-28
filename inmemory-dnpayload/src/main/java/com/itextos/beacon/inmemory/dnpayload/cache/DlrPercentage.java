package com.itextos.beacon.inmemory.dnpayload.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.dnpayload.cache.pojo.DlrPercentageInfo;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class DlrPercentage
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log               log               = LogFactory.getLog(DlrPercentage.class);

    private Map<String, DlrPercentageInfo> mDlrPercentageMap = new HashMap<>();

    public DlrPercentage(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public DlrPercentageInfo getDlrPercentageInfo(
            String aMsgType,
            String aPriority,
            String aRouteID,
            String aErrorCode)
    {
        final String            lKey = getKey(aMsgType, aPriority, aRouteID, aErrorCode);
        final DlrPercentageInfo info = mDlrPercentageMap.get(lKey);

        if (log.isDebugEnabled())
            log.debug("getDlrPercentageInfo() - found for the key:" + lKey);

        if (info != null)
            info.setCurrentKey(lKey);
        return info;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from dn_percentage_mapping
        // Table Name: dn_gen_percentage_map

        final Map<String, DlrPercentageInfo> lDlrPercentageMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String            lMsgType           = CommonUtility.nullCheck(aResultSet.getString("msg_type"), true);
            final String            lPriority          = CommonUtility.nullCheck(aResultSet.getString("priority"), true);
            final String            lRouteId           = CommonUtility.nullCheck(aResultSet.getString("route_id"), true);
            final String            lErrorCode         = CommonUtility.nullCheck(aResultSet.getString("error_code"), true);
            final String            lMaskedRouteId     = CommonUtility.nullCheck(aResultSet.getString("masked_route_id"), true);
            final String            lPercentage        = CommonUtility.nullCheck(aResultSet.getString("success_percentage"), true);

            final String            lKey               = getKey(lMsgType, lPriority, lRouteId, lErrorCode);
            final DlrPercentageInfo lDlrPercentageInfo = new DlrPercentageInfo(lMsgType, lRouteId, lMaskedRouteId, CommonUtility.getDouble(lPercentage), lPriority, lErrorCode);

            lDlrPercentageMap.put(lKey, lDlrPercentageInfo);
        }

        mDlrPercentageMap = lDlrPercentageMap;
    }

    private static String getKey(
            String aMsgType,
            String aPriority,
            String aRouteId,
            String aErrorCode)
    {
        return CommonUtility.combine(aMsgType, aPriority, aRouteId, aErrorCode);
    }

}