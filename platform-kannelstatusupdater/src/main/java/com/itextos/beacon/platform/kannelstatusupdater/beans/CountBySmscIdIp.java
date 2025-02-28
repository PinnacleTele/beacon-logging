package com.itextos.beacon.platform.kannelstatusupdater.beans;

import com.itextos.beacon.platform.kannelstatusupdater.utility.Utility;

public class CountBySmscIdIp
{

    private final String smscID;
    private String       ip;
    private long         sms;
    private String       smsHuman;
    private long         dlr;
    private String       dlrHuman;
    private long         queued;
    private String       queuedHuman;
    private long         failed;
    private String       failedHuman;
    private int          portRX;
    private int          portTX;
    private long         totalRX;
    private long         totalTRX;
    private long         totalTX;

    public CountBySmscIdIp(
            SmscBean aSmscBean)
    {
        smscID = aSmscBean.getId();

        setPortRX(aSmscBean.getPortRX());
        setPortTX(aSmscBean.getPortTX());
        setIp(aSmscBean.getIp());

        final String bindType = aSmscBean.getBindtype();

        switch (bindType)
        {
            case KannelStatusInfo.BIND_TYPE_TRX:
                incrementTotalTRX();
                break;

            case KannelStatusInfo.BIND_TYPE_RX:
                incrementTotalRX();
                break;

            case KannelStatusInfo.BIND_TYPE_TX:
            default:
                incrementTotalTX();
                break;
        }

        incrementSms(aSmscBean.getSms());
        incrementDlr(aSmscBean.getDlr());
        incrementQueued(aSmscBean.getQueued());
        incrementFailed(aSmscBean.getFailed());
    }

    public void updateHumanReadableCounts()
    {
        setSMSHuman(Utility.humanReadableFormat(getSms(), 0));
        setDlrHuman(Utility.humanReadableFormat(getDlr(), 0));
        setQueuedHuman(Utility.humanReadableFormat(getQueued(), 0));
        setFailedHuman(Utility.humanReadableFormat(getFailed(), 0));
    }

    public String getSmscID()
    {
        return smscID;
    }

    public String getIp()
    {
        return ip;
    }

    void setIp(
            String aIP)
    {
        ip = aIP;
    }

    public long getSms()
    {
        return sms;
    }

    public void incrementSms(
            long aSms)
    {
        sms += aSms;
    }

    public String getSmsHuman()
    {
        return smsHuman;
    }

    private void setSMSHuman(
            String aSmsHuman)
    {
        smsHuman = aSmsHuman;
    }

    public long getDlr()
    {
        return dlr;
    }

    public void incrementDlr(
            long aDlr)
    {
        dlr += aDlr;
    }

    public String getDlrHuman()
    {
        return dlrHuman;
    }

    private void setDlrHuman(
            String aDlrHuman)
    {
        dlrHuman = aDlrHuman;
    }

    public long getQueued()
    {
        return queued;
    }

    public void incrementQueued(
            long aQueued)
    {
        queued += aQueued;
    }

    public String getQueuedHuman()
    {
        return queuedHuman;
    }

    private void setQueuedHuman(
            String aQueuedHuman)
    {
        queuedHuman = aQueuedHuman;
    }

    public long getFailed()
    {
        return failed;
    }

    public void incrementFailed(
            long aFailed)
    {
        failed += aFailed;
    }

    public String getFailedHuman()
    {
        return failedHuman;
    }

    private void setFailedHuman(
            String aFailedHuman)
    {
        failedHuman = aFailedHuman;
    }

    public int getPortRX()
    {
        return portRX;
    }

    void setPortRX(
            int aPortRX)
    {
        portRX = aPortRX;
    }

    public int getPortTX()
    {
        return portTX;
    }

    void setPortTX(
            int aPortTX)
    {
        portTX = aPortTX;
    }

    public long getTotalRX()
    {
        return totalRX;
    }

    public void incrementTotalRX()
    {
        totalRX++;
    }

    public long getTotalTRX()
    {
        return totalTRX;
    }

    public void incrementTotalTRX()
    {
        totalTRX++;
    }

    public long getTotalTX()
    {
        return totalTX;
    }

    public void incrementTotalTX()
    {
        totalTX++;
    }

    @Override
    public String toString()
    {
        return "CountBySmscIdIp [smscID=" + smscID + ", ip=" + ip + ", sms=" + sms + ", smsHuman=" + smsHuman + ", dlr=" + dlr + ", dlrHuman=" + dlrHuman + ", queued=" + queued + ", queuedHuman="
                + queuedHuman + ", failed=" + failed + ", failedHuman=" + failedHuman + ", portRX=" + portRX + ", portTX=" + portTX + ", totalRX=" + totalRX + ", totalTRX=" + totalTRX + ", totalTX="
                + totalTX + "]";
    }

}