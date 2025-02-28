package com.itextos.beacon.inmemory.intlrouteinfo.cache;

public class IntlRouteConfigInfo
{

    private final String mCarrierNetwork;
    private final String mCountry;
    private final String mHeaderType;
    private final String mDefaultHeader;
    private final String mMaxMnumberLength;
    private final String mMinMnumberLength;
    private final String mEconomyRouteId;
    private final String mRouteId;
    private final String mCarrier;
    private final String mHeaderSubType;

    public IntlRouteConfigInfo(
            String aCarrierNetwork,
            String aCountry,
            String aHeaderType,
            String aDefaultHeader,
            String aMaxMnumberLength,
            String aMinMnumberLength,
            String aEconomyRouteId,
            String aRouteId,
            String aCarrier,
            String aHeaderSubType)
    {
        super();
        mCarrierNetwork   = aCarrierNetwork;
        mCountry          = aCountry;
        mHeaderType       = aHeaderType;
        mDefaultHeader    = aDefaultHeader;
        mMaxMnumberLength = aMaxMnumberLength;
        mMinMnumberLength = aMinMnumberLength;
        mEconomyRouteId   = aEconomyRouteId;
        mRouteId          = aRouteId;
        mCarrier          = aCarrier;
        mHeaderSubType    = aHeaderSubType;
    }

    public String getCarrierNetwork()
    {
        return mCarrierNetwork;
    }

    public String getCountry()
    {
        return mCountry;
    }

    public String getHeaderType()
    {
        return mHeaderType;
    }

    public String getDefaultHeader()
    {
        return mDefaultHeader;
    }

    public String getMaxMnumberLength()
    {
        return mMaxMnumberLength;
    }

    public String getMinMnumberLength()
    {
        return mMinMnumberLength;
    }

    public String getEconomyRouteId()
    {
        return mEconomyRouteId;
    }

    public String getRouteId()
    {
        return mRouteId;
    }

    public String getCarrier()
    {
        return mCarrier;
    }

    public String getHeaderSubType()
    {
        return mHeaderSubType;
    }

    @Override
    public String toString()
    {
        return "IntlRouteConfigInfo [mCarrierNetwork=" + mCarrierNetwork + ", mCountry=" + mCountry + ", mHeaderType=" + mHeaderType + ", mDefaultHeader=" + mDefaultHeader + ", mMaxMnumberLength="
                + mMaxMnumberLength + ", mMinMnumberLength=" + mMinMnumberLength + ", mEconomyRouteId=" + mEconomyRouteId + ", mRouteId=" + mRouteId + ", mCarrier=" + mCarrier + ", mHeaderSubType="
                + mHeaderSubType + "]";
    }

}
