package com.itextos.beacon.platform.kannelstatusupdater.xmlparser;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.kannelstatusupdater.beans.DnBean;
import com.itextos.beacon.platform.kannelstatusupdater.beans.SmsBean;
import com.itextos.beacon.platform.kannelstatusupdater.beans.SmscBean;
import com.itextos.beacon.platform.kannelstatusupdater.utility.Utility;

@XmlRootElement(
        name = "gateway")
public class Gateway
{

    private static final Log log = LogFactory.getLog(Gateway.class);

    @XmlElement(
            name = "status")
    private String           status;

    @XmlElementRef
    private Sms              sms;

    @XmlElementRef
    private Dn               dn;

    @XmlElementRef
    private Smscs            smscs;

    @XmlElementRef
    private Boxes            boxes;

    public String getUptime()
    {
        String result = "";

        try
        {
            result = status.substring(status.indexOf("uptime") + 6);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Kannel UpTime", e);
        }
        return result;
    }

    public SmsBean getSMSInfo()
    {
        long   storeSize = 0, smsSentTotal = 0, smsSentQueued = 0;
        double smsTPS1   = 0, smsTPS2 = 0, smsTPS3 = 0;

        try
        {
            storeSize = CommonUtility.getLong(sms.storesize);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Sms Store Size", e);
        }

        try
        {
            smsSentTotal = CommonUtility.getLong(sms.smssent.total);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Sms Sent Total", e);
        }

        try
        {
            smsSentQueued = CommonUtility.getLong(sms.smssent.queued);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Sms Sent Queued", e);
        }

        try
        {
            final StringTokenizer st = new StringTokenizer(sms.outbound, ",");
            smsTPS1 = CommonUtility.getDouble(st.nextToken());
            smsTPS2 = CommonUtility.getDouble(st.nextToken());
            smsTPS3 = CommonUtility.getDouble(st.nextToken());
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Sms TPS Values", e);
        }

        final SmsBean smsBean = new SmsBean();

        try
        {
            smsBean.setStoreSize(storeSize);
            smsBean.setStoreSizeHuman(Utility.humanReadableFormat(storeSize, 0));
            smsBean.setSmsSent(smsSentTotal);
            smsBean.setSmsSentHuman(Utility.humanReadableFormat(smsSentTotal, 0));
            smsBean.setSmsQueued(smsSentQueued);
            smsBean.setSmsQueuedHuman(Utility.humanReadableFormat(smsSentQueued, 0));
            smsBean.setSmsTps1(smsTPS1);
            smsBean.setSmsTps2(smsTPS2);
            smsBean.setSmsTps3(smsTPS3);
        }
        catch (final Exception e)
        {
            log.error("Exception while updating Sms Counts", e);
        }
        return smsBean;
    }

    public DnBean getDNMap()
    {
        long   dnReceived = 0, dnQueued = 0;
        double tps1       = 0, tps2 = 0, tps3 = 0;

        try
        {
            dnReceived = CommonUtility.getLong(dn.dnreceived.total);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Dn Received counts", e);
        }

        try
        {
            dnQueued = CommonUtility.getLong(dn.queued);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Dn Queued count", e);
        }

        try
        {
            final StringTokenizer st = new StringTokenizer(dn.inbound, ",");
            tps1 = CommonUtility.getDouble(st.nextToken());
            tps2 = CommonUtility.getDouble(st.nextToken());
            tps3 = CommonUtility.getDouble(st.nextToken());
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Dn TPS", e);
        }

        final DnBean dnBean = new DnBean();

        try
        {
            dnBean.setDnReceived(dnReceived);
            dnBean.setDnReceivedHuman(Utility.humanReadableFormat(dnReceived, 0));
            dnBean.setDnQueued(dnQueued);
            dnBean.setDnQueuedHuman(Utility.humanReadableFormat(dnQueued, 0));
            dnBean.setDnTps1(tps1);
            dnBean.setDnTps2(tps2);
            dnBean.setDnTps3(tps3);
        }
        catch (final Exception e)
        {
            log.error("Exception while setting Dn Counts", e);
        }
        return dnBean;
    }

    public List<SmscBean> getSMSCS()
    {
        final int smscCount = smscs.smsclist.size();
        if (smscCount == 0)
            return new ArrayList<>(0);

        final List<SmscBean> smscBeanList = new ArrayList<>(smscCount);

        Smsc                 smsc         = null;
        SmscBean             smscBean     = null;
        StringTokenizer      st, st1, st2;
        String               smscID       = "", smscName = "", smscStatus = "";
        long                 smscQueued   = 0, smscFailed = 0, smscSMS = 0, smscDN = 0;

        for (int index = 0; index < smscCount; index++)
        {
            smscID     = "";
            smscName   = "";
            smscStatus = "";
            smscQueued = 0;
            smscFailed = 0;
            smscSMS    = 0;
            smscDN     = 0;

            try
            {
                smsc = smscs.smsclist.get(index);
            }
            catch (final Exception e)
            {
                log.error("Exception while getting the Smsc from the list", e);
                continue;
            }

            try
            {
                smscID = smsc.id;
            }
            catch (final Exception e)
            {
                log.error("Exception while getting the Smsc ID", e);
            }

            try
            {
                smscQueued = CommonUtility.getLong(smsc.queued);
            }
            catch (final Exception e)
            {
                log.error("Exception while getting the Smsc Queued Count", e);
            }

            try
            {
                smscFailed = CommonUtility.getLong(smsc.failed);
            }
            catch (final Exception e)
            {
                log.error("Exception while getting the Smsc Queued Count", e);
            }

            try
            {
                smscSMS = CommonUtility.getLong(smsc.sms.sms);
            }
            catch (final Exception e)
            {
                log.error("Exception while getting the Smsc Sms Count", e);
            }

            try
            {
                smscDN = CommonUtility.getLong(smsc.dn.dn);
            }
            catch (final Exception e)
            {
                log.error("Exception while getting the Smsc Sms Count", e);
            }

            try
            {
                smscName = smsc.name;
            }
            catch (final Exception e)
            {
                log.error("Exception while getting the Smsc Name", e);
            }

            try
            {
                smscStatus = smsc.status;
            }
            catch (final Exception e)
            {
                log.error("Exception while getting the Smsc Status", e);
            }

            try
            {
                smscBean = new SmscBean();
                smscBeanList.add(smscBean);
                smscBean.setId(smscID);

                smscBean.setQueued(smscQueued);
                smscBean.setQueuedhuman(Utility.humanReadableFormat(smscQueued, 0));

                smscBean.setFailed(smscFailed);
                smscBean.setFailedhuman(Utility.humanReadableFormat(smscFailed, 0));

                smscBean.setSms(smscSMS);
                smscBean.setSmshuman(Utility.humanReadableFormat(smscSMS, 0));

                smscBean.setDlr(smscDN);
                smscBean.setDlrhuman(Utility.humanReadableFormat(smscDN, 0));

                st = new StringTokenizer(smscName, ":");
                // Skip the first Element.
                st.nextToken();
                smscBean.setIp(st.nextToken());

                st1 = new StringTokenizer(st.nextToken(), "//");
                final int txport = CommonUtility.getInteger(st1.nextToken());
                final int rxport = CommonUtility.getInteger(st1.nextToken());

                smscBean.setPortTX(txport);
                smscBean.setPortRX(rxport);

                smscBean.setUsername(st.nextToken());

                try
                {
                    smscBean.setSystemType(st.nextToken());
                }
                catch (final Exception e)
                {
                    smscBean.setSystemType("");
                }

                if ((txport != 0) && (rxport != 0))
                    smscBean.setBindtype("trx");
                else
                    if (rxport != 0)
                        smscBean.setBindtype("rx");
                    else
                        smscBean.setBindtype("tx");

                st2 = new StringTokenizer(smscStatus, " ");
                smscBean.setStatus(st2.nextToken());

                if (st2.hasMoreTokens())
                    try
                    {
                        String s = st2.nextToken();
                        s = s.substring(0, s.length() - 1);
                        smscBean.setAlive(Utility.getAliveTime(s));
                    }
                    catch (final Exception e)
                    {
                        smscBean.setAlive("");
                    }
                else
                    smscBean.setAlive("");
            }
            catch (final Exception e)
            {
                log.error("Exception while setting the Smsc details", e);
            }
        }
        return smscBeanList;
    }

    public long getSMSBoxQueued()
    {

        try
        {
            final String type = boxes.box.type;
            if (!"smsbox".equals(type))
                return -1;
        }
        catch (final Exception e)
        {
            log.error("Exception while setting the Box Type", e);
            return -1;
        }

        try
        {
            return Long.parseLong(boxes.box.queue);
        }
        catch (final Exception e)
        {
            return 0;
        }
    }

}