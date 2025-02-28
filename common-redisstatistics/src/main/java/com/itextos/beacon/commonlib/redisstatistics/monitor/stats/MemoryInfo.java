package com.itextos.beacon.commonlib.redisstatistics.monitor.stats;

import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

public class MemoryInfo
{

    private final String mUsedMemory;
    private final String mUsedMemoryRss;
    private final String mTotalSystemMemory;
    private final String mUsedMemoryDataset;
    private final String mUsedMemoryLua;
    private final String mMaxmemory;

    public MemoryInfo(
            String aUsedMemory,
            String aUsedMemoryRss,
            String aTotalSystemMemory,
            String aUsedMemoryDataset,
            String aUsedMemoryLua,
            String aMaxmemory)
    {
        super();
        mUsedMemory        = aUsedMemory;
        mUsedMemoryRss     = aUsedMemoryRss;
        mTotalSystemMemory = aTotalSystemMemory;
        mUsedMemoryDataset = aUsedMemoryDataset;
        mUsedMemoryLua     = aUsedMemoryLua;
        mMaxmemory         = aMaxmemory;
    }

    public String getUsedMemory()
    {
        return mUsedMemory;
    }

    public String getUsedMemoryRss()
    {
        return mUsedMemoryRss;
    }

    public String getTotalSystemMemory()
    {
        return mTotalSystemMemory;
    }

    public String getUsedMemoryDataset()
    {
        return mUsedMemoryDataset;
    }

    public String getUsedMemoryLua()
    {
        return mUsedMemoryLua;
    }

    public String getMaxmemory()
    {
        return mMaxmemory;
    }

    public float getMemoryUsage()
    {
    	/*
        final long maxMemory     = Long.parseLong(mMaxmemory);
        final long usedMemoryRss = Long.parseLong(mUsedMemoryRss);

        if (maxMemory == 0)
            throw new ItextosRuntimeException("Max memory returned as 0. May be not configured in redis.conf file. Please check.");

        return ((usedMemoryRss / (float) maxMemory) * 100F);
    	*/
    	
    	 long maxMemory     = Long.parseLong(mMaxmemory);
        final long usedMemoryRss = Long.parseLong(mUsedMemoryRss);

      if(maxMemory==0) {
    	  maxMemory=1L;
      }

        return ((usedMemoryRss / (float) maxMemory) * 100F);

    }

    @Override
    public String toString()
    {
        return "MemoryInfo [mUsedMemory=" + mUsedMemory + ", mTotalSystemMemory=" + mTotalSystemMemory + ", mUsedMemoryDataset=" + mUsedMemoryDataset + ", mUsedMemoryLua=" + mUsedMemoryLua
                + ", mMaxmemory=" + mMaxmemory + "]";
    }

}
