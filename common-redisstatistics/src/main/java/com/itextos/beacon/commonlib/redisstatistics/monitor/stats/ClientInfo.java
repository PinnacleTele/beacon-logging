package com.itextos.beacon.commonlib.redisstatistics.monitor.stats;

public class ClientInfo
{

    private final String mConnectedClients;
    private final String mBlockedClients;
    private final String mTrackingClients;

    public ClientInfo(
            String aConnectedClients,
            String aBlockedClients,
            String aTrackingClients)
    {
        super();
        mConnectedClients = aConnectedClients;
        mBlockedClients   = aBlockedClients;
        mTrackingClients  = aTrackingClients;
    }

    public String getConnectedClients()
    {
        return mConnectedClients;
    }

    public String getBlockedClients()
    {
        return mBlockedClients;
    }

    public String getTrackingClients()
    {
        return mTrackingClients;
    }

    @Override
    public String toString()
    {
        return "ClientInfo [mConnectedClients=" + mConnectedClients + ", mBlockedClients=" + mBlockedClients + ", mTrackingClients=" + mTrackingClients + "]";
    }

}