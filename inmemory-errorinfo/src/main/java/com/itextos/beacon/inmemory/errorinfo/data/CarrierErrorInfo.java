package com.itextos.beacon.inmemory.errorinfo.data;

import java.util.Objects;

import com.itextos.beacon.commonlib.utility.CommonUtility;

public class CarrierErrorInfo
        implements
        IErrorInfo
{

    private final String      mRouteId;
    private final String      mErrorCode;
    private final String      mErrorDesc;
    private final String      mErrorStatus;
    private final FailureType mFailureType;
    private final String      mPlatformErrorCode;

    public CarrierErrorInfo(
            String aRouteId,
            String aErrorCode,
            String aErrorDesc,
            String aErrorStatus,
            String aFailureType,
            String aPlatformErrorCode)
    {
        super();
        mRouteId           = aRouteId;
        mErrorCode         = aErrorCode;
        mErrorDesc         = aErrorDesc;
        mErrorStatus       = aErrorStatus;
        mFailureType       = FailureType.getFailureType(aFailureType);
        mPlatformErrorCode = aPlatformErrorCode;
    }

    @Override
    public String getErrorCode()
    {
        return mErrorCode;
    }

    @Override
    public String getErrorDesc()
    {
        return mErrorDesc;
    }

    public String getErrorStatus()
    {
        return mErrorStatus;
    }

    public FailureType getFailureType()
    {
        return mFailureType;
    }

    public String getKey()
    {
        return CommonUtility.combine(mRouteId, mErrorCode);
    }

    public String getPlatformErrorCode()
    {
        return mPlatformErrorCode;
    }

    public String getRouteId()
    {
        return mRouteId;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mErrorCode, mRouteId);
    }

    @Override
    public boolean equals(
            Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final CarrierErrorInfo other = (CarrierErrorInfo) obj;
        return Objects.equals(mErrorCode, other.mErrorCode) && Objects.equals(mRouteId, other.mRouteId);
    }

    @Override
    public String toString()
    {
        return "CarrierErrorInfo [mRouteId=" + mRouteId + ", mErrorCode=" + mErrorCode + ", mErrorDesc=" + mErrorDesc + ", mErrorStatus=" + mErrorStatus + ", mFailureType=" + mFailureType
                + ", mPlatformErrorCode=" + mPlatformErrorCode + "]";
    }

}