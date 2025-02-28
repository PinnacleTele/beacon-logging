package com.itextos.beacon.dlrquery.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.itextos.beacon.commonlib.constants.AccountStatus;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.ipvalidation.IPValidator;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.dlrquery.request.enums.DlrQueryStatus;
import com.itextos.beacon.dlrquery.request.enums.DlrQueryType;
import com.itextos.beacon.inmemdata.account.ClientAccountDetails;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.itextos.beacon.inmemory.clidlrpref.ClientDlrConfigUtil;

class ValidateRequest
{

    private static final Log  log             = LogFactory.getLog(ValidateRequest.class);
    private static final char PARAM_SEPARATOR = '~';

    private final RequestData mRequestData;
    private JsonObject        mAccountDetails;

    ValidateRequest(
            RequestData aRequestData)
    {
        mRequestData = aRequestData;
    }

    void validate()
    {
        final boolean isValidUser = validateUser();

        if (!isValidUser)
        {
            mRequestData.mDlrQueryStatus = DlrQueryStatus.INVALID_ACCESS_KEY;
            return;
        }

        final boolean isValidSourceIp = validateSourceIp();

        if (!isValidSourceIp)
        {
            mRequestData.mDlrQueryStatus = DlrQueryStatus.IP_RESTRICTED;
            return;
        }

        final boolean isDlrQueryEnabled = isDlrQueryEnabled();

        if (!isDlrQueryEnabled)
        {
            mRequestData.mDlrQueryStatus = DlrQueryStatus.DLR_QUERY_FEATURE_DISABLED;
            return;
        }

        mRequestData.mCusrRefIdList = validateForMultiple(mRequestData.mCustRefId);
        mRequestData.mDestList      = validateForMultiple(mRequestData.mDest);
        mRequestData.mFileIdList    = validateForMultiple(mRequestData.mFileId);

        validateCounts();

        logDetails(false);

        if (mRequestData.mDlrQueryStatus != DlrQueryStatus.VALID_REQUEST)
            logDetails(true);
    }

    private void validateCounts()
    {
        final int fileIdListCount   = mRequestData.mFileIdList.size();
        final int cliMsgIdListCount = mRequestData.mCusrRefIdList.size();
        final int destListCount     = mRequestData.mDestList.size();

        if (mRequestData.mDlrQueryStatus == DlrQueryStatus.VALID_REQUEST)
        {
            final byte   destCount      = checkValue(destListCount);
            final byte   fileIdCount    = checkValue(fileIdListCount);
            final byte   custRefIdCount = checkValue(cliMsgIdListCount);
            final String value          = "" + destCount + fileIdCount + custRefIdCount;

            switch (value)
            {
                case "000":
                    mRequestData.mDlrQueryStatus = DlrQueryStatus.INVALID_NO_VALID_INPUT_PARAMETERS;
                    break;

                case "001":
                case "010":
                case "100":
                case "011":
                case "101":
                case "110":
                case "111":
                    mRequestData.mDlrQueryStatus = DlrQueryStatus.VALID_REQUEST;
                    mRequestData.mCustRefIdListBased = false;
                    mRequestData.mDestListBased = false;
                    mRequestData.mFileIdListBased = false;
                    mRequestData.mIsMultiple = false;
                    mRequestData.mDlrQueryType = DlrQueryType.REGULAR;
                    break;

                case "200":
                    mRequestData.mDlrQueryType = DlrQueryType.MULTI_DESTINATION;
                    mRequestData.mDestListBased = true;
                    //$FALL-THROUGH$
                case "020":
                    mRequestData.mDlrQueryType = DlrQueryType.MULTI_FILE_ID;
                    mRequestData.mFileIdListBased = true;
                    //$FALL-THROUGH$
                case "002":
                    mRequestData.mDlrQueryType = DlrQueryType.MULTI_CUST_REF_ID;
                    mRequestData.mCustRefIdListBased = true;

                    mRequestData.mDlrQueryStatus = DlrQueryStatus.VALID_REQUEST;
                    mRequestData.mIsMultiple = true;
                    break;

                default:
                    mRequestData.mDlrQueryStatus = DlrQueryStatus.INVALID_MORE_THAN_ONE_MULTIPLE_PARAMETERS;
                    mRequestData.mIsMultiple = false;
            }
        }
    }

    private static byte checkValue(
            int aListCount)
    {
        if (aListCount == 0)
            return 0;
        else
            if (aListCount == 1)
                return 1;
        return 2;
    }

    private static List<String> validateForMultiple(
            String aMultipleParams)
    {
        if (aMultipleParams.isBlank())
            return new ArrayList<>();

        final String[] lSplit = StringUtils.split(aMultipleParams, PARAM_SEPARATOR);
        return Arrays.asList(lSplit);
    }

    private boolean validateUser()
    {
        if (mRequestData.mAccessKey.isBlank())
            return false;

        final UserInfo lUserInfo = ClientAccountDetails.getUserDetailsByAccessKey(mRequestData.mAccessKey);

        if (lUserInfo == ClientAccountDetails.INVALID_USER)
            return false;

        final AccountStatus lAccountStatus = lUserInfo.getAccountStatus();

        if (lAccountStatus != AccountStatus.ACTIVE)
            return false;

        mRequestData.setClientId(lUserInfo.getClientId());
        mAccountDetails = parseJSON(lUserInfo.getAccountDetails());

        return true;
    }

    private boolean validateSourceIp()
    {
        if (mRequestData.mRequestedIp.isBlank())
            return false;

        final String isIPValidationEnable = CommonUtility.nullCheck(mAccountDetails.get(MiddlewareConstant.MW_IP_VALIDATION.getName()), true);
        final String lIPList              = CommonUtility.nullCheck(mAccountDetails.get(MiddlewareConstant.MW_IP_LIST.getName()));

        return IPValidator.getInstance().isValidIP(isIPValidationEnable, mRequestData.getClientId(), lIPList, mRequestData.mRequestedIp);
    }

    private boolean isDlrQueryEnabled()
    {
        final boolean lClientDlrQueryEnabled = ClientDlrConfigUtil.getDlrQueryConfig(mRequestData.getClientId(), "sms");

        if (log.isDebugEnabled())
            log.debug("Client Dlr Query Enabled :" + lClientDlrQueryEnabled);

        return lClientDlrQueryEnabled;
    }

    private void logDetails(
            boolean aLogErrorLevel)
    {

        if (!aLogErrorLevel)
        {

            if (log.isInfoEnabled())
            {
                log.info("AccessKey : '" + mRequestData.mAccessKey + "' CustRefId : '" + mRequestData.mCustRefId + "' Dest : '" + mRequestData.mDest + "' FileId : '" + mRequestData.mFileId
                        + "' Requested Ip : '" + mRequestData.mRequestedIp + "' Requested Time '" + new Date(mRequestData.mRequestedTime) + "'");
                log.info("Cli Id : '" + mRequestData.mCliId + "' Dest List : '" + mRequestData.mDestList + "' FileId List : '" + mRequestData.mFileIdList + "' CustRefId List : '"
                        + mRequestData.mCusrRefIdList + "' isMultiple '" + mRequestData.mIsMultiple + "' DLR Query Type :'" + mRequestData.mDlrQueryType + "' DLR Query Status '"
                        + mRequestData.mDlrQueryStatus + "'");
            }
        }
        else
        {
            log.error("AccessKey : '" + mRequestData.mAccessKey + "' CustRefId : '" + mRequestData.mCustRefId + "' Dest : '" + mRequestData.mDest + "' FileId : '" + mRequestData.mFileId
                    + "' Requested Ip : '" + mRequestData.mRequestedIp + "' Requested Time '" + new Date(mRequestData.mRequestedTime) + "'");
            log.error("Cli Id : '" + mRequestData.mCliId + "' Dest List : '" + mRequestData.mDestList + "' FileId List : '" + mRequestData.mFileIdList + "' CustRefId List : '"
                    + mRequestData.mCusrRefIdList + "' isMultiple '" + mRequestData.mIsMultiple + "' DLR Query Type :'" + mRequestData.mDlrQueryType + "' DLR Query Status '"
                    + mRequestData.mDlrQueryStatus + "'");
        }
    }

    public static JsonObject parseJSON(
            String aJsonString)
    {
        return new Gson().fromJson(aJsonString, JsonObject.class);
    }

}
