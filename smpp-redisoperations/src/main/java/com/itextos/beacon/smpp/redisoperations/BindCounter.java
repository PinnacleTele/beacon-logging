package com.itextos.beacon.smpp.redisoperations;

public class BindCounter
{

    BindCounter()
    {}

    int mClientsTotalCount       = -1;
    int mInstanceWiseClientCount = -1;

    void setClientsTotalCount(
            int aClientsTotalCount)
    {
        mClientsTotalCount = aClientsTotalCount;
    }

    void setInstanceWiseClientCount(
            int aInstanceWiseClientCount)
    {
        mInstanceWiseClientCount = aInstanceWiseClientCount;
    }

    public int getClientsTotalCount()
    {
        return mClientsTotalCount;
    }

    public int getInstanceWiseClientCount()
    {
        return mInstanceWiseClientCount;
    }

    @Override
    public String toString()
    {
        return "BindCounter [mClientsTotalCount=" + mClientsTotalCount + ", mInstanceWiseClientCount=" + mInstanceWiseClientCount + "]";
    }

}
