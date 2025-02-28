package com.itextos.beacon.inmemory.loader.sample;

public class SampleUserInfo
{

    private final String mClientId;
    private final String mBillType;

    public SampleUserInfo(
            String aClientId,
            String aBillType)
    {
        super();
        mClientId = aClientId;
        mBillType = aBillType;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public String getBillType()
    {
        return mBillType;
    }

    @Override
    public String toString()
    {
        return "SampleUserInfo [mClientId=" + mClientId + ", mBillType=" + mBillType + "]";
    }

}