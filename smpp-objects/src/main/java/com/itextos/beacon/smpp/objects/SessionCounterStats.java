package com.itextos.beacon.smpp.objects;

import com.cloudhopper.smpp.SmppSessionCounters;

public class SessionCounterStats
{

    private final int    mTxDeliverSmRequest;
    private final int    mTxDeliverSmResponse;
    private final int    mTxDeliverSmRequestExpired;
    private final double mTxDeliverSmRequestResponseTime;
    private final double mTxDeliverSmRequestWaitTime;

    public SessionCounterStats(
            SmppSessionCounters aCounter)
    {
        mTxDeliverSmRequest             = aCounter.getTxDeliverSM().getRequest();
        mTxDeliverSmResponse            = aCounter.getTxDeliverSM().getResponse();
        mTxDeliverSmRequestExpired      = aCounter.getTxDeliverSM().getRequestExpired();
        mTxDeliverSmRequestResponseTime = aCounter.getTxDeliverSM().getRequestResponseTime();
        mTxDeliverSmRequestWaitTime     = aCounter.getTxDeliverSM().getRequestWaitTime();
    }

    public int getTxDeliverSmRequest()
    {
        return mTxDeliverSmRequest;
    }

    public int getTxDeliverSmResponse()
    {
        return mTxDeliverSmResponse;
    }

    public int getTxDeliverSmRequestExpired()
    {
        return mTxDeliverSmRequestExpired;
    }

    public double getTxDeliverSmRequestResponseTime()
    {
        return mTxDeliverSmRequestResponseTime;
    }

    public double getTxDeliverSmRequestWaitTime()
    {
        return mTxDeliverSmRequestWaitTime;
    }

    @Override
    public String toString()
    {
        return "SessionCounterStats [mTxDeliverSmRequest=" + mTxDeliverSmRequest + ", mTxDeliverSmResponse=" + mTxDeliverSmResponse + ", mTxDeliverSmRequestExpired=" + mTxDeliverSmRequestExpired
                + ", mTxDeliverSmRequestResponseTime=" + mTxDeliverSmRequestResponseTime + ", mTxDeliverSmRequestWaitTime=" + mTxDeliverSmRequestWaitTime + "]";
    }

}
