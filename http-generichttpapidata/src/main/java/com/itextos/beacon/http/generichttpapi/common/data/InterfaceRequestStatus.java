package com.itextos.beacon.http.generichttpapi.common.data;

import java.io.Serializable;

import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;

public class InterfaceRequestStatus
        implements
        Serializable
{

    private static final long         serialVersionUID = -3724089523467408727L;

    private final InterfaceStatusCode mStatusCode;
    private final String              mStatusDesc;
    private String                    mMessageId;
    private long                      mResponseTime;
    private String                    mBatchNo;

    public InterfaceRequestStatus(
            InterfaceStatusCode aStatusCode,
            String aStatusDesc)
    {
        mStatusCode = aStatusCode;
        mStatusDesc = aStatusDesc;
    }

    public String getMessageId()
    {
        return mMessageId;
    }

    public void setMessageId(
            String aMid)
    {
        mMessageId = aMid;
    }

    public long getResponseTime()
    {
        return mResponseTime;
    }

    public void setResponseTime(
            long aResponseTime)
    {
        mResponseTime = aResponseTime;
    }

    public String getBatchNo()
    {
        return mBatchNo;
    }

    public void setBatchNo(
            String aBatchNo)
    {
        mBatchNo = aBatchNo;
    }

    public InterfaceStatusCode getStatusCode()
    {
        return mStatusCode;
    }

    public String getStatusDesc()
    {
        return mStatusDesc;
    }

    @Override
    public String toString()
    {
        return "RequestStatus [mStatusCode=" + mStatusCode + ", mStatusDesc=" + mStatusDesc + ", mMessageid=" + mMessageId + ", mResponseTime=" + mResponseTime + ", mBatchNo=" + mBatchNo + "]";
    }

}