package com.itextos.beacon.inmemory.errorinfo.data;

public class CarrierRouteMap
{

    private final String  mRouteId;
    private final String  mCarrierId;
    private final String  mCarrierName;
    private final boolean mIsCircleBasedErrorCode;

    public CarrierRouteMap(
            String aRouteId,
            String aCarrierId,
            String aCarrierName,
            boolean aIsCircleBasedErrorCode)
    {
        super();
        mRouteId                = aRouteId;
        mCarrierId              = aCarrierId;
        mCarrierName            = aCarrierName;
        mIsCircleBasedErrorCode = aIsCircleBasedErrorCode;
    }

    public String getRouteId()
    {
        return mRouteId;
    }

    public String getCarrierId()
    {
        return mCarrierId;
    }

    public String getCarrierName()
    {
        return mCarrierName;
    }

    public boolean isCircleBasedErrorCode()
    {
        return mIsCircleBasedErrorCode;
    }

    @Override
    public String toString()
    {
        return "CarrierRouteMap [mRouteId=" + mRouteId + ", mCarrierId=" + mCarrierId + ", mCarrierName=" + mCarrierName + ", mIsCircleBasedErrorCode=" + mIsCircleBasedErrorCode + "]";
    }

}