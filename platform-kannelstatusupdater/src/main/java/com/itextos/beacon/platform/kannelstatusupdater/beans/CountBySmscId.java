package com.itextos.beacon.platform.kannelstatusupdater.beans;

import com.itextos.beacon.platform.kannelstatusupdater.utility.Utility;

public class CountBySmscId
{

    private final String smscID;
    private long         sms;
    private String       smsHuman;
    private long         dlr;
    private String       dlrHuman;
    private long         queued;
    private String       queuedHuman;
    private long         failed;
    private String       failedHuman;
    private long         totalRX;
    private long         totalTRX;
    private long         totalTX;

    public CountBySmscId(
            SmscBean aSmscBean)
    {
        smscID = aSmscBean.getId();

        final String bindType = aSmscBean.getBindtype();

        switch (bindType)
        {
            case KannelStatusInfo.BIND_TYPE_TRX:
                incremenetTotalTRX();
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
        setSmsHuman(Utility.humanReadableFormat(getSms(), 0));
        setDlrHuman(Utility.humanReadableFormat(getDlr(), 0));
        setQueuedHuman(Utility.humanReadableFormat(getQueued(), 0));
        setFailedHuman(Utility.humanReadableFormat(getFailed(), 0));
    }

    public String getSmscID()
    {
        return smscID;
    }

    public long getSms()
    {
        return sms;
    }

    private void incrementSms(
            long aSms)
    {
        sms += aSms;
    }

    public String getSmsHuman()
    {
        return smsHuman;
    }

    private void setSmsHuman(
            String aSmsHuman)
    {
        smsHuman = aSmsHuman;
    }

    public long getDlr()
    {
        return dlr;
    }

    private void incrementDlr(
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

    private void incrementQueued(
            long aDlr)
    {
        dlr += aDlr;
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

    private void incrementFailed(
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

    public long getTotalRX()
    {
        return totalRX;
    }

    private void incrementTotalRX()
    {
        totalRX++;
    }

    public long getTotalTRX()
    {
        return totalTRX;
    }

    private void incremenetTotalTRX()
    {
        totalTRX++;
    }

    public long getTotalTX()
    {
        return totalTX;
    }

    private void incrementTotalTX()
    {
        totalTX++;
    }

    @Override
    public String toString()
    {
        return "CountBySmscId [smscID=" + smscID + ", sms=" + sms + ", smsHuman=" + smsHuman + ", dlr=" + dlr + ", dlrHuman=" + dlrHuman + ", queued=" + queued + ", queuedHuman=" + queuedHuman
                + ", failed=" + failed + ", failedHuman=" + failedHuman + ", totalRX=" + totalRX + ", totalTRX=" + totalTRX + ", totalTX=" + totalTX + "]";
    }

}