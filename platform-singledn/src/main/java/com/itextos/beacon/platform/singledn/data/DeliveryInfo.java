package com.itextos.beacon.platform.singledn.data;

public class DeliveryInfo
{

    private final int    mTotalPartNos;
    private final int    mPartNumber;
    private final String mJsonObj;

    public DeliveryInfo(
            int aTotalPartNos,
            int aPartNumber,
            String aJsonObj)
    {
        mTotalPartNos = aTotalPartNos;
        mPartNumber   = aPartNumber;
        mJsonObj      = aJsonObj;
    }

    public int getTotalPartNos()
    {
        return mTotalPartNos;
    }

    public int getPartNo()
    {
        return mPartNumber;
    }

    public String getDnJson()
    {
        return mJsonObj;
    }

    public boolean isSuccess()
    {
        return true;
    }

}