package com.itextos.beacon.smpp.concatenate;

class PendingMessageInfo
{

    private final String mRefNumber;
    private final int    mReceivedPartsCount;
    private int          mTotalPartsCount;
    private long         mReceivedTime;

    PendingMessageInfo(
            String aRefNumber,
            int aReceivedPartsCount)
    {
        super();
        mRefNumber          = aRefNumber;
        mReceivedPartsCount = aReceivedPartsCount;
    }

    int getTotalPartsCount()
    {
        return mTotalPartsCount;
    }

    void setTotalPartsCount(
            int aTotalPartsCount)
    {
        mTotalPartsCount = aTotalPartsCount;
    }

    long getReceivedTime()
    {
        return mReceivedTime;
    }

    void setReceivedTime(
            long aReceivedTime)
    {
        mReceivedTime = aReceivedTime;
    }

    String getRefNumber()
    {
        return mRefNumber;
    }

    int getReceivedPartsCount()
    {
        return mReceivedPartsCount;
    }

    boolean isAllPartsReceived()
    {
        return mTotalPartsCount == mReceivedPartsCount;
    }

    boolean isExpired(
            long aTimeCanWait)
    {
        return (mReceivedTime + aTimeCanWait) < System.currentTimeMillis();
    }

    @Override
    public String toString()
    {
        return "PendingMessageInfo [mRefNumber=" + mRefNumber + ", mReceivedPartsCount=" + mReceivedPartsCount + ", mTotalPartsCount=" + mTotalPartsCount + ", mReceivedTime=" + mReceivedTime + "]";
    }

}