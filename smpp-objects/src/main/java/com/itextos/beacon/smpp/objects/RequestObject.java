package com.itextos.beacon.smpp.objects;

import java.util.Date;

public class RequestObject
{

    private final String mFileId;
    private final String mDest;
    private final String mMessage;
    private final String mHeader;
    private final Date   mReceivedTime;

    private String       mUdh;
    private Date         mScheduleTime;
    private int          mMessagePort;
    private boolean      mIsDlrReq;
    private int          mExpiryInSeconds;
    private String       mCustomerIp;
    private String       mMsgclass;
    private String       udhi;
    private String       custref;
    private String       msgTag;
    private String       dltEntityId;
    private String       dltTemplateId;

    public RequestObject(
            String aFileId,
            String aDest,
            String aMessage,
            String aHeader,
            Date aReceivedTime)
    {
        super();
        mFileId       = aFileId;
        mDest         = aDest;
        mMessage      = aMessage;
        mHeader       = aHeader;
        mReceivedTime = aReceivedTime;
    }

    public String getUdh()
    {
        return mUdh;
    }

    public void setUdh(
            String aUdh)
    {
        mUdh = aUdh;
    }

    public Date getScheduleTime()
    {
        return mScheduleTime;
    }

    public void setScheduleTime(
            Date aScheduleTime)
    {
        mScheduleTime = aScheduleTime;
    }

    public int getMessagePort()
    {
        return mMessagePort;
    }

    public void setMessagePort(
            int aMessagePort)
    {
        mMessagePort = aMessagePort;
    }

    public boolean isIsDlrReq()
    {
        return mIsDlrReq;
    }

    public void setIsDlrReq(
            boolean aIsDlrReq)
    {
        mIsDlrReq = aIsDlrReq;
    }

    public int getExpiryInSeconds()
    {
        return mExpiryInSeconds;
    }

    public void setExpiryInSeconds(
            int aExpiryInSeconds)
    {
        mExpiryInSeconds = aExpiryInSeconds;
    }

    public String getCustomerIp()
    {
        return mCustomerIp;
    }

    public void setCustomerIp(
            String aCustomerIp)
    {
        mCustomerIp = aCustomerIp;
    }

    public String getMsgclass()
    {
        return mMsgclass;
    }

    public void setMsgclass(
            String aMsgclass)
    {
        mMsgclass = aMsgclass;
    }

    public String getUdhi()
    {
        return udhi;
    }

    public void setUdhi(
            String aUdhi)
    {
        udhi = aUdhi;
    }

    public String getCustref()
    {
        return custref;
    }

    public void setCustref(
            String aCustref)
    {
        custref = aCustref;
    }

    public String getMsgTag()
    {
        return msgTag;
    }

    public void setMsgTag(
            String aMsgTag)
    {
        msgTag = aMsgTag;
    }

    public String getDltEntityId()
    {
        return dltEntityId;
    }

    public void setDltEntityId(
            String aDltEntityId)
    {
        dltEntityId = aDltEntityId;
    }

    public String getDltTemplateId()
    {
        return dltTemplateId;
    }

    public void setDltTemplateId(
            String aDltTemplateId)
    {
        dltTemplateId = aDltTemplateId;
    }

    public String getFileId()
    {
        return mFileId;
    }

    public String getDest()
    {
        return mDest;
    }

    public String getMessage()
    {
        return mMessage;
    }

    public String getHeader()
    {
        return mHeader;
    }

    public Date getReceivedTime()
    {
        return mReceivedTime;
    }

    @Override
    public String toString()
    {
        return "RequestObject [mFileId=" + mFileId + ", mDest=" + mDest + ", mMessage=" + mMessage + ", mHeader=" + mHeader + ", mReceivedTime=" + mReceivedTime + ", mUdh=" + mUdh + ", mScheduleTime="
                + mScheduleTime + ", mMessagePort=" + mMessagePort + ", mIsDlrReq=" + mIsDlrReq + ", mExpiryInSeconds=" + mExpiryInSeconds + ", mCustomerIp=" + mCustomerIp + ", mMsgclass=" + mMsgclass
                + ", udhi=" + udhi + ", custref=" + custref + ", msgTag=" + msgTag + ", dltEntityId=" + dltEntityId + ", dltTemplateId=" + dltTemplateId + "]";
    }

}