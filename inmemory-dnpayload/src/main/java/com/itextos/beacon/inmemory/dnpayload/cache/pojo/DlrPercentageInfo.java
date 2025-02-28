package com.itextos.beacon.inmemory.dnpayload.cache.pojo;

public class DlrPercentageInfo
{

    private final String mMsgType;
    private final String mRouteId;
    private final String mMaskedRouteId;
    private final double mPercentage;
    private final String mPriority;
    private final String mErrorCode;
    private String       mCurrentKey;

    public DlrPercentageInfo(
            String aMsgType,
            String aRouteId,
            String aMaskedRouteId,
            double aPercentage,
            String aPriority,
            String aErrorCode)
    {
        super();
        mMsgType       = aMsgType;
        mRouteId       = aRouteId;
        mMaskedRouteId = aMaskedRouteId;
        mPercentage    = aPercentage / 100.0;
        mPriority      = aPriority;
        mErrorCode     = aErrorCode;
    }

    public String getCurrentKey()
    {
        return mCurrentKey;
    }

    public void setCurrentKey(
            String aCurrentKey)
    {
        mCurrentKey = aCurrentKey;
    }

    public String getMsgType()
    {
        return mMsgType;
    }

    public String getRouteId()
    {
        return mRouteId;
    }

    public String getMaskedRouteId()
    {
        return mMaskedRouteId;
    }

    public double getPercentage()
    {
        return mPercentage;
    }

    public String getPriority()
    {
        return mPriority;
    }

    public String getErrorCode()
    {
        return mErrorCode;
    }

    @Override
    public String toString()
    {
        return "DlrPercentageInfo [mMsgType=" + mMsgType + ", mRouteId=" + mRouteId + ", mMaskedRouteId=" + mMaskedRouteId + ", mPercentage=" + mPercentage + ", mPriority=" + mPriority
                + ", mErrorCode=" + mErrorCode + ", mCurrentKey=" + mCurrentKey + "]";
    }

}