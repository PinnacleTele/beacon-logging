package com.itextos.beacon.inmemory.errorinfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.errorinfo.data.ErrorCategory;
import com.itextos.beacon.inmemory.errorinfo.data.ErrorStatus;
import com.itextos.beacon.inmemory.errorinfo.data.PlatformErrorInfo;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class PlatformErrorInfoCollection
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log               log                          = LogFactory.getLog(PlatformErrorInfoCollection.class);

    private static final String            DEFAULT_PLATFORM_ERROR_CODE  = "499";
    private static final String            DEFAULT_INTERFACE_ERROR_CODE = "399";
    private static final String            DEFAULT_OPERATOR_ERROR_CODE  = "699";
    private static final String            DEFAULT_UNKNOWN_ERROR_CODE   = "999";

    private Map<String, PlatformErrorInfo> mPlatformErrorInfoCollection = new HashMap<>();

    public PlatformErrorInfoCollection(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    protected PlatformErrorInfo getErrorInfo(
            ErrorCategory aErrorCategory,
            String aErrorCode)
    {
        PlatformErrorInfo returnValue = getErrorInfo(aErrorCode);

        if (returnValue == null)
            returnValue = getDefaultErrorCode(aErrorCategory);
        return returnValue;
    }

    private PlatformErrorInfo getDefaultErrorCode(
            ErrorCategory aErrorCategory)
    {
        String defaultErrorCode = DEFAULT_UNKNOWN_ERROR_CODE;

        if (aErrorCategory != null)
            switch (aErrorCategory)
            {
                case INTERFACE:
                    defaultErrorCode = DEFAULT_INTERFACE_ERROR_CODE;
                    break;

                case OPERATOR:
                    defaultErrorCode = DEFAULT_OPERATOR_ERROR_CODE;
                    break;

                case PLATFORM:
                    defaultErrorCode = DEFAULT_PLATFORM_ERROR_CODE;
                    break;

                default:
                    break;
            }

        PlatformErrorInfo returnValue = getErrorInfo(defaultErrorCode);

        if (returnValue == null)
            returnValue = getDefaultErrorCode();
        return returnValue;
    }

    private static PlatformErrorInfo getDefaultErrorCode()
    {
        return new PlatformErrorInfo(DEFAULT_UNKNOWN_ERROR_CODE, "UNKNOWN ERROR", "UNKNOWN ERROR", ErrorCategory.PLATFORM.getKey(), ErrorStatus.FAILED.getKey(), 0, false, false);
    }

    private PlatformErrorInfo getErrorInfo(
            String aErrorCode)
    {
        final PlatformErrorInfo lPlatformErrorInfo = mPlatformErrorInfoCollection.get(aErrorCode);

        if (lPlatformErrorInfo == null)
            log.error("Unable to find a Platform error info the error code " + aErrorCode);

        return lPlatformErrorInfo;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        final Map<String, PlatformErrorInfo> lTempErrorCodeMap = new HashMap<>();

        while (aResultSet.next())
        {
            final PlatformErrorInfo lErrorInfo = new PlatformErrorInfo(CommonUtility.nullCheck(aResultSet.getString("error_code"), true),//
                    CommonUtility.nullCheck(aResultSet.getString("error_desc"), true), //
                    CommonUtility.nullCheck(aResultSet.getString("display_error"), true), //
                    CommonUtility.nullCheck(aResultSet.getString("category"), true).toUpperCase(), //
                    CommonUtility.nullCheck(aResultSet.getString("status_flag"), true).toUpperCase(), //
                    CommonUtility.getInteger(aResultSet.getString("handover_all_parts")), CommonUtility.isEnabled(aResultSet.getString("is_dom_sms_rate_refundable")),
                    CommonUtility.isEnabled(aResultSet.getString("is_dom_dlt_rate_refundable")));
            lTempErrorCodeMap.put(lErrorInfo.getErrorCode(), lErrorInfo);
        }

        if (!lTempErrorCodeMap.isEmpty())
            mPlatformErrorInfoCollection = lTempErrorCodeMap;
    }

}