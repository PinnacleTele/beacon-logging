package com.itextos.beacon.commonlib.redisstatistics.monitor.stats;

public class ConnectionInfo
{

    private final String mTotalConnectionsReceived;
    private final String mTotalCommandsExecuted;
    private final String mRejectedConnections;

    public ConnectionInfo(
            String aTotalConnectionsReceived,
            String aTotalCommandsExecuted,
            String aRejectedConnections)
    {
        super();
        mTotalConnectionsReceived = aTotalConnectionsReceived;
        mTotalCommandsExecuted    = aTotalCommandsExecuted;
        mRejectedConnections      = aRejectedConnections;
    }

    public String getTotalConnectionsReceived()
    {
        return mTotalConnectionsReceived;
    }

    public String getTotalCommandsExecuted()
    {
        return mTotalCommandsExecuted;
    }

    public String getRejectedConnections()
    {
        return mRejectedConnections;
    }

    @Override
    public String toString()
    {
        return "ConnectionInfo [mTotalConnectionsReceived=" + mTotalConnectionsReceived + ", mTotalCommandsExecuted=" + mTotalCommandsExecuted + ", mRejectedConnections=" + mRejectedConnections + "]";
    }

}