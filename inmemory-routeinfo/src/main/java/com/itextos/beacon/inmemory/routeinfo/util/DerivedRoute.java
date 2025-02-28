package com.itextos.beacon.inmemory.routeinfo.util;

public class DerivedRoute
{

    private final String mRouteId;
    private final int    mLogicId;
    private final String mDerivedKey;
    private final String mDefaultHeader;
    private String       mCountry;
    private String       mCarrier;
    private String       mCarrierNetworkInfo;

    public DerivedRoute(
            String aRouteID,
            int aLogicId,
            String aDerivedKey)
    {
        this(aRouteID, aLogicId, aDerivedKey, null);
    }

    public DerivedRoute(
            String aRouteID,
            int aLogicId,
            String aDerivedKey,
            String aDefaultHeader)
    {
        super();
        mRouteId       = aRouteID;
        mLogicId       = aLogicId;
        mDerivedKey    = aDerivedKey;
        mDefaultHeader = aDefaultHeader;
    }

    public String getRouteId()
    {
        return mRouteId;
    }

    public String getCountry()
    {
        return mCountry;
    }

    public void setCountry(
            String aCountry)
    {
        mCountry = aCountry;
    }

    public String getCarrier()
    {
        return mCarrier;
    }

    public void setCarrier(
            String aCarrier)
    {
        mCarrier = aCarrier;
    }

    public String getCarrierNetworkInfo()
    {
        return mCarrierNetworkInfo;
    }

    public void setCarrierNetworkInfo(
            String aCarrierNetworkInfo)
    {
        mCarrierNetworkInfo = aCarrierNetworkInfo;
    }

    public int getLogicId()
    {
        return mLogicId;
    }

    public String getDerivedKey()
    {
        return mDerivedKey;
    }

    public String getDefaultHeader()
    {
        return mDefaultHeader;
    }

    @Override
    public String toString()
    {
        return "DerivedRoute [mRouteId=" + mRouteId + ", mLogicId=" + mLogicId + ", mDerivedKey=" + mDerivedKey + ", mDefaultHeader=" + mDefaultHeader + ", mCountry=" + mCountry + ", mCarrier="
                + mCarrier + ", mCarrierNetworkInfo=" + mCarrierNetworkInfo + "]";
    }

}