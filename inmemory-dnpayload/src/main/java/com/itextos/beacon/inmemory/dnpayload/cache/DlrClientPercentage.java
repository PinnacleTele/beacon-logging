package com.itextos.beacon.inmemory.dnpayload.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.dnpayload.cache.pojo.DlrClientPercentageInfo;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class DlrClientPercentage
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                     log                      = LogFactory.getLog(DlrClientPercentage.class);

    private Map<String, DlrClientPercentageInfo> mDlrClientPercentageInfo = new HashMap<>();

    public DlrClientPercentage(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public DlrClientPercentageInfo getDlrClientPercentageInfo(
            String aClientId,
            String aRouteID,
            String aErrorCode)
    {

        for (int index = 1; index < 9; index++)
        {
            final String                  key                      = createListKey(index, aClientId, aRouteID, aErrorCode);
            final DlrClientPercentageInfo lDlrClientPercentageInfo = mDlrClientPercentageInfo.get(key);

            if (lDlrClientPercentageInfo != null)
            {
                if (log.isDebugEnabled())
                    log.debug("Found for the key:" + key);

                lDlrClientPercentageInfo.setCurrentKey(key);
                return lDlrClientPercentageInfo;
            }
        }
        return null;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from acc_dn_percentage_mapping
        // Table Name : client_dn_gen_percentage_map

        final Map<String, DlrClientPercentageInfo> lTempClientPercentageInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final String                  lClientId                = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String                  lRouteId                 = CommonUtility.nullCheck(aResultSet.getString("route_id"), true);
            final String                  lErrorCode               = CommonUtility.nullCheck(aResultSet.getString("error_code"), true);
            final String                  LMaskedRouteId           = CommonUtility.nullCheck(aResultSet.getString("masked_route_id"), true);
            final String                  lPercentage              = CommonUtility.nullCheck(aResultSet.getString("success_percentage"), true);

            final String                  lKey                     = getKey(lClientId, lRouteId, lErrorCode);
            final DlrClientPercentageInfo lDlrClientPercentageInfo = new DlrClientPercentageInfo(lClientId, lRouteId, LMaskedRouteId, lErrorCode, CommonUtility.getDouble(lPercentage));

            lTempClientPercentageInfo.put(lKey, lDlrClientPercentageInfo);
        }
        mDlrClientPercentageInfo = lTempClientPercentageInfo;
    }

    private static String createListKey(
            int aLogicId,
            String aClientId,
            String aRouteId,
            String aErrorCode)
    {
        String              key     = null;

        final ItextosClient lClient = new ItextosClient(aClientId);

        switch (aLogicId)
        {
            case 1:
                key = getKey(lClient.getClientId(), aRouteId, aErrorCode);
                break;

            case 2:
                aClientId = lClient.getAdmin();
                key = getKey(aClientId, aRouteId, aErrorCode);
                break;

            case 3:
                aClientId = lClient.getSuperAdmin();
                key = getKey(aClientId, aRouteId, aErrorCode);
                break;

            case 4:
                aRouteId = Constants.NULL_STRING;
                key = getKey(aClientId, aRouteId, aErrorCode);
                break;

            case 5:
                aClientId = lClient.getAdmin();
                aRouteId = Constants.NULL_STRING;
                key = getKey(aClientId, aRouteId, aErrorCode);
                break;

            case 6:
                aClientId = lClient.getSuperAdmin();
                aRouteId = Constants.NULL_STRING;
                key = getKey(aClientId, aRouteId, aErrorCode);
                break;

            case 7:
                aClientId = Constants.NULL_STRING;
                key = getKey(aClientId, aRouteId, aErrorCode);
                break;

            case 8:
                aClientId = Constants.NULL_STRING;
                aRouteId = Constants.NULL_STRING;
                key = getKey(aClientId, aRouteId, aErrorCode);
                break;

            default:
                break;
        }
        return key;
    }

    private static String getKey(
            String aClientId,
            String aRouteId,
            String aErrorCode)
    {
        return CommonUtility.combine(aClientId, aRouteId, aErrorCode);
    }

}