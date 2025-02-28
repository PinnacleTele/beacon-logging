package com.itextos.beacon.platform.kannelstatusupdater.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextos.beacon.platform.kannelstatusupdater.utility.Utility;

public class KannelStatusInfo
{

    public static final String    BIND_TYPE_TRX  = "trx";
    public static final String    BIND_TYPE_RX   = "rx";
    public static final String    BIND_TYPE_TX   = "tx";
    private static final String   STATUS_ONLINE  = "online";

    private final String          kannelId;
    private String                kannelIp;
    private int                   kannelPort;
    private boolean               isKannelAvailable;
    private String                upTime;
    private long                  smsSent;
    private String                smsSentHuman;
    private long                  dlrReceived;
    private String                dlrReceivedHuman;
    private long                  failed;
    private String                failedHuman;
    private long                  queued;
    private String                queuedHuman;
    private int                   deadRX;
    private int                   deadTRX;
    private int                   deadTX;
    private long                  smsBoxQueue;
    private int                   totalRX;
    private int                   totalTRX;
    private int                   totalTX;

    private SmsBean               smsBean;
    private DnBean                dnBean;

    private List<CountBySmscId>   bySMSCIdList   = new ArrayList<>();
    private List<CountBySmscIdIp> bySMSCIdIPList = new ArrayList<>();
    private List<SmscBean>        smscList       = new ArrayList<>();

    public KannelStatusInfo(
            String aKannelId)
    {
        kannelId = aKannelId;
    }

    public String getKannelId()
    {
        return kannelId;
    }

    public String getKannelIp()
    {
        return kannelIp;
    }

    public void setKannelIp(
            String aKannelIp)
    {
        kannelIp = aKannelIp;
    }

    public int getKannelPort()
    {
        return kannelPort;
    }

    public void setKannelPort(
            int aKannelPort)
    {
        kannelPort = aKannelPort;
    }

    public boolean isKannelAvailable()
    {
        return isKannelAvailable;
    }

    public void setKannelAvailable(
            boolean aAvailable)
    {
        isKannelAvailable = aAvailable;
    }

    public String getUpTime()
    {
        return upTime;
    }

    public void setUpTime(
            String aUpTime)
    {
        upTime = aUpTime;
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

    public long getDlrReceived()
    {
        return dlrReceived;
    }

    public void setDlrReceived(
            long aDlrReceived)
    {
        dlrReceived = aDlrReceived;
    }

    public String getDlrReceivedHuman()
    {
        return dlrReceivedHuman;
    }

    public void setDlrReceivedHuman(
            String aDlrReceivedHuman)
    {
        dlrReceivedHuman = aDlrReceivedHuman;
    }

    public long getFailed()
    {
        return failed;
    }

    public void setFailed(
            long aFailed)
    {
        failed = aFailed;
    }

    public String getFailedHuman()
    {
        return failedHuman;
    }

    public void setFailedHuman(
            String aFailedHuman)
    {
        failedHuman = aFailedHuman;
    }

    public long getQueued()
    {
        return queued;
    }

    public void setQueued(
            long aQueued)
    {
        queued = aQueued;
    }

    public String getQueuedHuman()
    {
        return queuedHuman;
    }

    public void setQueuedHuman(
            String aQueuedHuman)
    {
        queuedHuman = aQueuedHuman;
    }

    public int getDeadRX()
    {
        return deadRX;
    }

    public void setDeadRX(
            int aDeadRX)
    {
        deadRX = aDeadRX;
    }

    public int getDeadTRX()
    {
        return deadTRX;
    }

    public void setDeadTRX(
            int aDeadTRX)
    {
        deadTRX = aDeadTRX;
    }

    public int getDeadTX()
    {
        return deadTX;
    }

    public void setDeadTX(
            int aDeadTX)
    {
        deadTX = aDeadTX;
    }

    public long getSmsBoxQueue()
    {
        return smsBoxQueue;
    }

    public void setSmsBoxQueue(
            long aSmsBoxQueue)
    {
        smsBoxQueue = aSmsBoxQueue;
    }

    public int getTotalRX()
    {
        return totalRX;
    }

    public void setTotalRX(
            int aTotalRX)
    {
        totalRX = aTotalRX;
    }

    public int getTotalTRX()
    {
        return totalTRX;
    }

    public void setTotalTRX(
            int aTotalTRX)
    {
        totalTRX = aTotalTRX;
    }

    public int getTotalTX()
    {
        return totalTX;
    }

    public void setTotalTX(
            int aTotalTX)
    {
        totalTX = aTotalTX;
    }

    public SmsBean getSMS()
    {
        return smsBean;
    }

    public void setSMS(
            SmsBean aSMS)
    {
        smsBean = aSMS;
    }

    public DnBean getDN()
    {
        return dnBean;
    }

    public void setDN(
            DnBean aDN)
    {
        dnBean = aDN;
    }

    public List<CountBySmscId> getBySMSCIdList()
    {
        return bySMSCIdList;
    }

    public List<CountBySmscIdIp> getBySMSCIdIPList()
    {
        return bySMSCIdIPList;
    }

    public List<SmscBean> getSMSCList()
    {
        return smscList;
    }

    public void addBySMSCID(
            CountBySmscId aCountBySmscId)
    {
        bySMSCIdList.add(aCountBySmscId);
    }

    public void addBySMSCIDIP(
            CountBySmscIdIp aCountBySmscIdIp)
    {
        bySMSCIdIPList.add(aCountBySmscIdIp);
    }

    public void addSMSC(
            SmscBean aSmscBean)
    {
        smscList.add(aSmscBean);
    }

    public void setSmscList(
            List<SmscBean> aSmscList)
    {
        smscList = aSmscList;
    }

    public void setBySMSCIdList(
            List<CountBySmscId> aBySMSCIdList)
    {
        bySMSCIdList = aBySMSCIdList;
    }

    public void setBySMSCIdIPList(
            List<CountBySmscIdIp> aBySMSCIdIPList)
    {
        bySMSCIdIPList = aBySMSCIdIPList;
    }

    public void generateSummary()
    {
        int                                bindCountRX     = 0, bindCountTRX = 0, bindCountTX = 0;
        int                                deadBindCountRX = 0, deadBindCountTRX = 0, deadBindCountTX = 0;
        long                               totalSMS        = 0, totalDLR = 0, totalQueued = 0, totalFailed = 0;

        final Map<String, CountBySmscId>   bySMSCIDMap     = new HashMap<>();
        final Map<String, CountBySmscIdIp> bySMSCIdIPMap   = new HashMap<>();

        for (final SmscBean smscBean : smscList)
        {
            bySMSCIDMap.computeIfAbsent(smscBean.getId(), k -> new CountBySmscId(smscBean));
            bySMSCIdIPMap.computeIfAbsent(smscBean.getKey(), k -> new CountBySmscIdIp(smscBean));

            final String bindType = smscBean.getBindtype();

            switch (bindType)
            {
                case BIND_TYPE_TRX:
                    bindCountTRX++;
                    if (!STATUS_ONLINE.equals(smscBean.getStatus()))
                        deadBindCountTRX++;
                    break;

                case BIND_TYPE_RX:
                    bindCountRX++;
                    if (!STATUS_ONLINE.equals(smscBean.getStatus()))
                        deadBindCountRX++;
                    break;

                case BIND_TYPE_TX:
                default:
                    bindCountTX++;
                    if (!STATUS_ONLINE.equals(smscBean.getStatus()))
                        deadBindCountTX++;
                    break;
            }

            totalSMS    += smscBean.getSms();
            totalDLR    += smscBean.getDlr();
            totalQueued += smscBean.getQueued();
            totalFailed += smscBean.getFailed();
        } // End of For

        for (final CountBySmscId temp : bySMSCIDMap.values())
        {
            temp.updateHumanReadableCounts();
            addBySMSCID(temp);
        }

        for (final CountBySmscIdIp temp : bySMSCIdIPMap.values())
        {
            temp.updateHumanReadableCounts();
            addBySMSCIDIP(temp);
        }

        setTotalRX(bindCountRX);
        setTotalTRX(bindCountTRX);
        setTotalTX(bindCountTX);

        setDeadRX(deadBindCountRX);
        setDeadTRX(deadBindCountTRX);
        setDeadTX(deadBindCountTX);

        setFailed(totalFailed);
        setDlrReceived(totalDLR);
        setQueued(totalQueued);
        setSmsSent(totalSMS);

        setFailedHuman(Utility.humanReadableFormat(totalFailed, 0));
        setDlrReceivedHuman(Utility.humanReadableFormat(totalDLR, 0));
        setQueuedHuman(Utility.humanReadableFormat(totalQueued, 0));
        setSmsSentHuman(Utility.humanReadableFormat(totalSMS, 0));
    }

    @Override
    public String toString()
    {
        return "KannelStatusInfo [kannelId=" + kannelId + ", kannelIp=" + kannelIp + ", kannelPort=" + kannelPort + ", isKannelAvailable=" + isKannelAvailable + ", upTime=" + upTime + ", smsSent="
                + smsSent + ", smsSentHuman=" + smsSentHuman + ", dlrReceived=" + dlrReceived + ", dlrReceivedHuman=" + dlrReceivedHuman + ", failed=" + failed + ", failedHuman=" + failedHuman
                + ", queued=" + queued + ", queuedHuman=" + queuedHuman + ", deadRX=" + deadRX + ", deadTRX=" + deadTRX + ", deadTX=" + deadTX + ", smsBoxQueue=" + smsBoxQueue + ", totalRX=" + totalRX
                + ", totalTRX=" + totalTRX + ", totalTX=" + totalTX + ", smsBean=" + smsBean + ", dnBean=" + dnBean + ", bySMSCIdList=" + bySMSCIdList + ", bySMSCIdIPList=" + bySMSCIdIPList
                + ", smscList=" + smscList + "]";
    }

}