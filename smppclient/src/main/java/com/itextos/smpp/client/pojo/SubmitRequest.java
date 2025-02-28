package com.itextos.smpp.client.pojo;

import java.util.List;

import com.cloudhopper.smpp.tlv.Tlv;

public class SubmitRequest
{

    private String    mSourceHeader;
    private String    mMobileNumber;
    private String    mMessage;
    private byte      mDcs = 0x00;
    private int       mRegisteredDelivery;
    private String    mValidityPeriod;
    private byte      mEsmClass;
    private List<Tlv> mOptionalParams;
    private long      mNoOfMessages;
    private String    mScheduleDeliveryTime;
    private String    mCharset;

    public String getSourceHeader()
    {
        return mSourceHeader;
    }

    public void setSourceHeader(
            String aMSourceHeader)
    {
        mSourceHeader = aMSourceHeader;
    }

    public String getMobileNumber()
    {
        return mMobileNumber;
    }

    public void setMobileNumber(
            String aMMobileNumber)
    {
        mMobileNumber = aMMobileNumber;
    }

    public String getMessage()
    {
        return mMessage;
    }

    public void setMessage(
            String aMMessage)
    {
        mMessage = aMMessage;
    }

    public int getRegisteredDelivery()
    {
        return mRegisteredDelivery;
    }

    public void setRegisteredDelivery(
            int aMRegisteredDelivery)
    {
        mRegisteredDelivery = aMRegisteredDelivery;
    }

    public String getValidityPeriod()
    {
        return mValidityPeriod;
    }

    public void setValidityPeriod(
            String aMValidityPeriod)
    {
        mValidityPeriod = aMValidityPeriod;
    }

    public byte getEsmClass()
    {
        return mEsmClass;
    }

    public void setEsmClass(
            byte aMEsmClass)
    {
        mEsmClass = aMEsmClass;
    }

    public List<Tlv> getOptionalParams()
    {
        return mOptionalParams;
    }

    public void setOptionalParams(
            List<Tlv> aMOptionalParams)
    {
        mOptionalParams = aMOptionalParams;
    }

    public byte getDcs()
    {
        return mDcs;
    }

    public void setDcs(
            byte aDcs)
    {
        mDcs = aDcs;
    }

    public long getNoOfMessages()
    {
        return mNoOfMessages;
    }

    public void setNoOfMessages(
            long aMNoOfMessages)
    {
        mNoOfMessages = aMNoOfMessages;
    }

    public String getScheduleDeliveryTime()
    {
        return mScheduleDeliveryTime;
    }

    public void setScheduleDeliveryTime(
            String aMScheduleDeliveryTime)
    {
        mScheduleDeliveryTime = aMScheduleDeliveryTime;
    }

    public String getCharset()
    {
        return mCharset;
    }

    public void setCharset(
            String aMCharset)
    {
        mCharset = aMCharset;
    }

    @Override
    public String toString()
    {
        return "SubmitRequest [mSourceHeader=" + mSourceHeader + ", mMobileNumber=" + mMobileNumber + ", mMessage=" + mMessage + ", mDcs=" + mDcs + ", mRegisteredDelivery=" + mRegisteredDelivery
                + ", mValidityPeriod=" + mValidityPeriod + ", mEsmClass=" + mEsmClass + ", mOptionalParams=" + mOptionalParams + ", mNoOfMessages=" + mNoOfMessages + ", mScheduleDeliveryTime="
                + mScheduleDeliveryTime + ", mCharset=" + mCharset + "]";
    }

}
