package com.itextos.beacon.commonlib.redisstatistics.monitor.stats;

public class ServerInfo
{

    private final String mRedisVersion;
    private final String mOS;
    private final String mTCPPort;
    private final String mUpTimeinDays;

    public ServerInfo(
            String aRedisVersion,
            String aOS,
            String aTCPPort,
            String aUpTimeinDays)
    {
        super();
        mRedisVersion = aRedisVersion;
        mOS           = aOS;
        mTCPPort      = aTCPPort;
        mUpTimeinDays = aUpTimeinDays;
    }

    public String getRedisVersion()
    {
        return mRedisVersion;
    }

    public String getOS()
    {
        return mOS;
    }

    public String getTCPPort()
    {
        return mTCPPort;
    }

    public String getUpTimeinDays()
    {
        return mUpTimeinDays;
    }

    @Override
    public String toString()
    {
        return "ServerInfo [mRedisVersion=" + mRedisVersion + ", mOS=" + mOS + ", mTCPPort=" + mTCPPort + ", mUpTimeinDays=" + mUpTimeinDays + "]";
    }

}