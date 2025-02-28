package com.itextos.beacon.platform.prepaiddata.kannelstatus;

import java.io.Serializable;

public class KannelInfo
        implements
        Serializable
{

    private static final long serialVersionUID = -2387851481038383214L;

    private final String      mOperator;
    private final String      mRoute;
    private final boolean     mAvailable;
    private final int         mStoreSize;
    private final String      mKannalIp;
    private final String      mKannelPort;
    private final String      mKannelStatusPort;
    private final String      mLastUpdated;

    public KannelInfo(
            String aOperator,
            String aRoute,
            boolean aAvailable,
            int aStoreSize,
            String aKannalIp,
            String aKannelPort,
            String aKannelStatusPort,
            String aLastUpdated)
    {
        super();
        mOperator         = aOperator;
        mRoute            = aRoute;
        mAvailable        = aAvailable;
        mStoreSize        = aStoreSize;
        mKannalIp         = aKannalIp;
        mKannelPort       = aKannelPort;
        mKannelStatusPort = aKannelStatusPort;
        mLastUpdated      = aLastUpdated;
    }

    public String getOperator()
    {
        return mOperator;
    }

    public String getRoute()
    {
        return mRoute;
    }

    public boolean isAvailable()
    {
        return mAvailable;
    }

    public int getStoreSize()
    {
        return mStoreSize;
    }

    public String getKannalIp()
    {
        return mKannalIp;
    }

    public String getKannelPort()
    {
        return mKannelPort;
    }

    public String getKannelStatusPort()
    {
        return mKannelStatusPort;
    }

    public String getLastUpdated()
    {
        return mLastUpdated;
    }

    @Override
    public String toString()
    {
        return "KannelInfo [mOperator=" + mOperator + ", mRoute=" + mRoute + ", mAvailable=" + mAvailable + ", mStoreSize=" + mStoreSize + ", mKannalIp=" + mKannalIp + ", mKannelPort=" + mKannelPort
                + ", mKannelStatusPort=" + mKannelStatusPort + ", mLastUpdated=" + mLastUpdated + "]";
    }

}