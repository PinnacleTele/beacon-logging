package com.itextos.beacon.platform.kannelstatusupdater.beans;

public class SmsBean
{

    private double smsTps1;
    private double smsTps2;
    private double smsTps3;
    private long   smsSent;
    private String smsSentHuman;
    private long   storeSize;
    private String storeSizeHuman;
    private long   smsQueued;
    private String smsQueuedHuman;

    public double getSmsTps1()
    {
        return smsTps1;
    }

    public void setSmsTps1(
            double aSmsTPS1)
    {
        smsTps1 = aSmsTPS1;
    }

    public double getSmsTps2()
    {
        return smsTps2;
    }

    public void setSmsTps2(
            double aSmsTPS2)
    {
        smsTps2 = aSmsTPS2;
    }

    public double getSmsTps3()
    {
        return smsTps3;
    }

    public void setSmsTps3(
            double aSmsTPS3)
    {
        smsTps3 = aSmsTPS3;
    }

    public long getSmsSent()
    {
        return smsSent;
    }

    public void setSmsSent(
            long aSmsSent)
    {
        smsSent = aSmsSent;
    }

    public String getSmsSentHuman()
    {
        return smsSentHuman;
    }

    public void setSmsSentHuman(
            String aSmsSentHuman)
    {
        smsSentHuman = aSmsSentHuman;
    }

    public long getStoreSize()
    {
        return storeSize;
    }

    public void setStoreSize(
            long aStoreSize)
    {
        storeSize = aStoreSize;
    }

    public String getStoreSizeHuman()
    {
        return storeSizeHuman;
    }

    public void setStoreSizeHuman(
            String aStoreSizeHuman)
    {
        storeSizeHuman = aStoreSizeHuman;
    }

    public long getSmsQueued()
    {
        return smsQueued;
    }

    public void setSmsQueued(
            long aSmsQueued)
    {
        smsQueued = aSmsQueued;
    }

    public String getSmsQueuedHuman()
    {
        return smsQueuedHuman;
    }

    public void setSmsQueuedHuman(
            String aSmsQueuedHuman)
    {
        smsQueuedHuman = aSmsQueuedHuman;
    }

    @Override
    public String toString()
    {
        return "Sms [smsTps1=" + smsTps1 + ", smsTps2=" + smsTps2 + ", smsTps3=" + smsTps3 + ", smsSent=" + smsSent + ", smsSentHuman=" + smsSentHuman + ", storeSize=" + storeSize
                + ", storeSizeHuman=" + storeSizeHuman + ", smsQueued=" + smsQueued + ", smsQueuedHuman=" + smsQueuedHuman + "]";
    }

}