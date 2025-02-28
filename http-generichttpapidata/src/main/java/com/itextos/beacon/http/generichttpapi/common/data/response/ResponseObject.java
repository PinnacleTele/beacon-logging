package com.itextos.beacon.http.generichttpapi.common.data.response;

import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;

public class ResponseObject
{

    private InterfaceStatusCode mStatusCode;
    private String              mReason;
    private String              mStatusInfo;
    private String              mMessageId = "N/A";
    private String              mReqTime;

    public InterfaceStatusCode getStatusCode()
    {
        return mStatusCode;
    }

    public void setStatusCode(
            InterfaceStatusCode aStatusCode)
    {
        mStatusCode = aStatusCode;
    }

    public String getReason()
    {
        return mReason;
    }

    public void setReason(
            String aStatusDesc)
    {
        mReason = aStatusDesc;
    }

    public String getStatusInfo()
    {
        return mStatusInfo;
    }

    public void setStatusInfo(
            String aStatusInfo)
    {
        mStatusInfo = aStatusInfo;
    }

    public String getMessageId()
    {
        return mMessageId;
    }

    public void setMessageId(
            String aMessageId)
    {
        final String temp = CommonUtility.nullCheck(aMessageId, true);
        mMessageId = temp.isBlank() ? "N/A" : temp;
    }

    public String getReqTime()
    {
        return mReqTime;
    }

    public void setReqTime(
            String aReqDate)
    {
        mReqTime = aReqDate;
    }

    public static ResponseObject getErrorResponse()
    {
        final ResponseObject errObject = new ResponseObject();
        errObject.setStatusCode(InterfaceStatusCode.INTERNAL_SERVER_ERROR);
        errObject.setStatusInfo(APIConstants.STATUS_INFO_REJECT);
        errObject.setReason(InterfaceStatusCode.INTERNAL_SERVER_ERROR.getStatusDesc());
        return errObject;
    }

}