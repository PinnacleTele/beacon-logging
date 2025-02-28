package com.itextos.beacon.platform.kannelstatusupdater.beans;

public class DnBean
{

    private double dnTps1;
    private double dnTps2;
    private double dnTps3;
    private long   dnReceived;
    private String dnReceivedHuman;
    private long   dnQueued;
    private String dnQueuedHuman;

    public double getDnTps1()
    {
        return dnTps1;
    }

    public void setDnTps1(
            double aDnTps1)
    {
        dnTps1 = aDnTps1;
    }

    public double getDnTps2()
    {
        return dnTps2;
    }

    public void setDnTps2(
            double aDnTps2)
    {
        dnTps2 = aDnTps2;
    }

    public double getDnTps3()
    {
        return dnTps3;
    }

    public void setDnTps3(
            double aDnTps3)
    {
        dnTps3 = aDnTps3;
    }

    public long getDnReceived()
    {
        return dnReceived;
    }

    public void setDnReceived(
            long aDnReceived)
    {
        dnReceived = aDnReceived;
    }

    public String getDnReceivedHuman()
    {
        return dnReceivedHuman;
    }

    public void setDnReceivedHuman(
            String aDnReceivedHuman)
    {
        dnReceivedHuman = aDnReceivedHuman;
    }

    public long getDnQueued()
    {
        return dnQueued;
    }

    public void setDnQueued(
            long aDnQueued)
    {
        dnQueued = aDnQueued;
    }

    public String getDnQueuedHuman()
    {
        return dnQueuedHuman;
    }

    public void setDnQueuedHuman(
            String aDnQueuedHuman)
    {
        dnQueuedHuman = aDnQueuedHuman;
    }

    @Override
    public String toString()
    {
        return "DnBean [dnTps1=" + dnTps1 + ", dnTps2=" + dnTps2 + ", dnTps3=" + dnTps3 + ", dnReceived=" + dnReceived + ", dnReceivedHuman=" + dnReceivedHuman + ", dnQueued=" + dnQueued
                + ", dnQueuedHuman=" + dnQueuedHuman + "]";
    }

}
