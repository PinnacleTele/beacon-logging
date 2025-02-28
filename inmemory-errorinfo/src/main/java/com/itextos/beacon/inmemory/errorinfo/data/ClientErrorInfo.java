package com.itextos.beacon.inmemory.errorinfo.data;

import com.itextos.beacon.commonlib.utility.CommonUtility;

public class ClientErrorInfo
        implements
        IErrorInfo
{

    private final String mClientId;
    private final String mPlatformErrorCode;
    private final String mClientErrorCode;
    private final String mClientErrorDesc;
    private final String mDeliveryStatus;

    public ClientErrorInfo(
            String aClientId,
            String aPlatformErrorCode,
            String aClientErrorCode,
            String aClientErrorDesc,
            String aDeliveryStatus)
    {
        super();
        mClientId          = aClientId;
        mPlatformErrorCode = aPlatformErrorCode;
        mClientErrorCode   = aClientErrorCode;
        mClientErrorDesc   = aClientErrorDesc;
        mDeliveryStatus    = aDeliveryStatus;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public String getDeliveryStatus()
    {
        return mDeliveryStatus;
    }

    @Override
    public String getErrorCode()
    {
        return mClientErrorCode;
    }

    @Override
    public String getErrorDesc()
    {
        return mClientErrorDesc;
    }

    public String getKey()
    {
        return CommonUtility.combine(mClientId, mPlatformErrorCode);
    }

    public String getPlatformErrorCode()
    {
        return mPlatformErrorCode;
    }

    @Override
    public String toString()
    {
        return "ClientErrorInfo [mClientId=" + mClientId + ", mPlatformErrorCode=" + mPlatformErrorCode + ", mClientErrorCode=" + mClientErrorCode + ", mClientErrorDesc=" + mClientErrorDesc
                + ", mDeliveryStatus=" + mDeliveryStatus + "]";
    }

}