package com.itextos.beacon.platform.dnpayloadutil;

public class PayloadKey
{

    private final String mMid;
    private final int    mRetryAttempt;

    public PayloadKey(
            String aMid,
            int aRetryAttempt)
    {
        this.mMid          = aMid;
        this.mRetryAttempt = aRetryAttempt;
    }

    public String getMid()
    {
        return mMid;
    }

    public int getRetryAttempt()
    {
        return mRetryAttempt;
    }

    @Override
    public String toString()
    {
        return "PayloadKey [mid=" + mMid + ", retryAttempt=" + mRetryAttempt + "]";
    }

}