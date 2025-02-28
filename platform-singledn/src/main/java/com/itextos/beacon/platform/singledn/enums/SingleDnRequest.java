package com.itextos.beacon.platform.singledn.enums;

import com.itextos.beacon.platform.singledn.data.DeliveryInfo;

public class SingleDnRequest
{

    private final String       mClientId;
    private final String       mDest;
    private final String       mBaseMessageId;
    private final DeliveryInfo mDeliveryInfo;

    public SingleDnRequest(
            String aClientId,
            String aDest,
            String aBaseMessageId,
            DeliveryInfo aDeliveryInfo)
    {
        super();
        mClientId      = aClientId;
        mDest          = aDest;
        mBaseMessageId = aBaseMessageId;
        mDeliveryInfo  = aDeliveryInfo;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public String getDest()
    {
        return mDest;
    }

    public String getBaseMessageId()
    {
        return mBaseMessageId;
    }

    public DeliveryInfo getDeliveryInfo()
    {
        return mDeliveryInfo;
    }

    @Override
    public String toString()
    {
        return "SingleDnRequest [mClientId=" + mClientId + ", mDest=" + mDest + ", mBaseMessageId=" + mBaseMessageId + ", mDeliveryInfo=" + mDeliveryInfo + "]";
    }

}