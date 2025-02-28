package com.itextos.beacon.inmemory.dnpayload.cache.pojo;

public class DlrClientPercentageInfo
{

    private final String mClientId;
    private final String mRouteId;
    private final String mMaskedRouteId;
    private final String mErrorCode;
    private final double mPercentage;
    private String       mCurrentKey;

    public DlrClientPercentageInfo(
            String aClientId,
            String aRouteId,
            String aMaskedRouteId,
            String aErrorCode,
            double aPercentage)
    {
        super();
        mClientId      = aClientId;
        mRouteId       = aRouteId;
        mMaskedRouteId = aMaskedRouteId;
        mErrorCode     = aErrorCode;
        mPercentage    = aPercentage;
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

    public String getClientId()
    {
        return mClientId;
    }

    public String getRouteId()
    {
        return mRouteId;
    }

    public String getMaskedRouteId()
    {
        return mMaskedRouteId;
    }

    public String getErrorCode()
    {
        return mErrorCode;
    }

    public double getPercentage()
    {
        return mPercentage;
    }

    @Override
    public String toString()
    {
        return "DlrClientPercentageInfo [mClientId=" + mClientId + ", mRouteId=" + mRouteId + ", mMaskedRouteId=" + mMaskedRouteId + ", mErrorCode=" + mErrorCode + ", mPercentage=" + mPercentage
                + ", mCurrentKey=" + mCurrentKey + "]";
    }

}