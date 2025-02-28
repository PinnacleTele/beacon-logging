package com.itextos.beacon.inmemory.carrierhandover.bean;

public class KannelInfo
{

    private final String mRouteId;
    private final String mKannelIp;
    private final String mKannelPort;
    private final String mStatusPort;
    private final int    mStoreSize;

    public KannelInfo(
            String aRouteId,
            String aKannelIp,
            String aKannelPort,
            String aStatusPort,
            int aStoreSize)
    {
        mRouteId    = aRouteId;
        mKannelIp   = aKannelIp;
        mKannelPort = aKannelPort;
        mStatusPort = aStatusPort;
        mStoreSize  = aStoreSize;
    }

    public String getRouteId()
    {
        return mRouteId;
    }

    public String getKannelIp()
    {
        return mKannelIp;
    }

    public String getKannelPort()
    {
        return mKannelPort;
    }

    public String getStatusPort()
    {
        return mStatusPort;
    }

    public int getStoreSize()
    {
        return mStoreSize;
    }

    @Override
    public String toString()
    {
        return "KannelInfoCache [mRouteId=" + mRouteId + ", mKannelIp=" + mKannelIp + ", mKannelPort=" + mKannelPort + ", mStatusPort=" + mStatusPort + ", mStoreSize=" + mStoreSize + "]";
    }

}