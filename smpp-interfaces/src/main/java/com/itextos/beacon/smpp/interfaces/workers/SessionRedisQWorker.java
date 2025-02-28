package com.itextos.beacon.smpp.interfaces.workers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.smpputil.DeliverSmInfo;
import com.itextos.beacon.platform.smpputil.SmppDnStatus;
import com.itextos.beacon.smpp.interfaces.event.handlers.ItextosSmppSessionHandler;
import com.itextos.beacon.smpp.interfaces.sessionhandlers.ItextosSessionManager;
import com.itextos.beacon.smpp.interfaces.util.counters.ClientCounter;
import com.itextos.beacon.smpp.redisoperations.DeliverySmRedisOps;
import com.itextos.beacon.smpp.utils.SmppKafkaProducer;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;

public class SessionRedisQWorker
        extends
        Thread
{

    private static final Log                log          = LogFactory.getLog(SessionRedisQWorker.class);
    private static final Integer            TIME_EXPIRED = new Integer(50);

    private final String                    mClientId;
    private final String                    mSystemId;
    private final ItextosSmppSessionHandler mSessionHandler;

    private final DNWorker                  worker;

    public SessionRedisQWorker(
            String aClientId,
            String aSystemId,
            ItextosSmppSessionHandler aSessionhandler)
    {
        mClientId       = aClientId;
        mSystemId       = aSystemId;
        mSessionHandler = aSessionhandler;
        worker          = new DNWorker(mSystemId);
        setName("SessionRedisQWorker-" + aSessionhandler.getSessionId() + "-" + mClientId);
    }

    @Override
    public void run()
    {

        try
        {
            log.info("started SessionRedisQWorker for Client Id =" + mClientId + " Systemid=" + mSystemId);
            final String hbname = getName();

            while (mSessionHandler.getSession().isBinding() || mSessionHandler.getSession().isOpen())
            {
                log.debug("waiting for session bind to be completed...");

                try
                {
                    Thread.sleep(100);
                }
                catch (final InterruptedException ignore)
                {}
            }

            while (mSessionHandler.getSession().isBound())
            {
                final List<Map<String, String>> aidList = null;

                try
                {
                    CustomerRedisHeartBeatData.getInstance().addHeartBeat(hbname, mSystemId);
                    final List<DeliverSmInfo> dnList = DeliverySmRedisOps.lpopDeliverSm(mClientId);

                    if (dnList == null)
                    {
                        CommonUtility.sleepForAWhile();
                        continue;
                    }

                    if (log.isInfoEnabled())
                        log.info("popped a dnList..." + dnList + " clientId:" + mClientId);

                    processDeliverySmList(dnList);
                }
                catch (final Exception e1)
                {
                    log.error("problem sending dn's possible loss..." + aidList, e1);
                    CommonUtility.sleepForAWhile();
                }
            }

            CustomerRedisHeartBeatData.getInstance().remove(hbname, mSystemId);
            ItextosSessionManager.getInstance().removeSessionRedisQWorker(mSystemId, this);
            log.info("exiting session redis worker for clientId =" + mClientId + " session not bound...");
        }
        catch (final Exception exp)
        {
            log.error("abruptly quiting session thread=" + getName() + " due to...", exp);
        }
    }

    private void processDeliverySmList(
            List<DeliverSmInfo> aDnList)
    {

        for (final DeliverSmInfo aDeliverySmInfo : aDnList)
        {
            log.debug("processing Dn=" + aDeliverySmInfo);

            if (aDeliverySmInfo != null) // This is not required.
            {
                CustomerRedisHeartBeatData.getInstance().addHeartBeat(getName(), mSystemId);

                try
                {
                    final boolean canSendToCustomer = ClientCounter.canProcessMessage(aDeliverySmInfo);

                    if (canSendToCustomer)
                    {

                        if (mSessionHandler.getSession().isBound())
                        {
                            final boolean flag = new DNAndPullWorker(mSystemId).sendMessage(mSessionHandler.getSession(), aDeliverySmInfo, mSessionHandler.getSmppUserInfo());

                            if (flag)
                            {
                                ClientCounter.getInstance().incrementSessionSendCustomerSuccess(mClientId, 1);
                                writeResponse(aDeliverySmInfo, 0);
                            }
                            else
                                writeResponse(aDeliverySmInfo, null);
                        }
                        else
                            writeResponse(aDeliverySmInfo, null);
                    }
                    else
                    {
                        writeResponse(aDeliverySmInfo, TIME_EXPIRED);
                        log.error("DN Expired before handover to customer.., Submit time is greater than Account DN Expiry time : '" + aDeliverySmInfo.getShortMessage() + "'");

                        ClientCounter.getInstance().incrementSessionTimeExpired(mClientId, 1);
                        if (log.isInfoEnabled())
                            log.info("Due to Time Expiry the message will be trated as platform expired.");
                    }
                }
                catch (final Exception exp)
                {
                    writeResponse(aDeliverySmInfo, null);
                    log.error("problem handling dn's sending to no bind queue...", exp);
                }
            }
        }
    }

    private void writeResponse(
            DeliverSmInfo aDeliverySmInfo,
            Integer status)
    {

        try
        {
            aDeliverySmInfo.setRetryInitTime();
            aDeliverySmInfo.incRetryAttempt();

            boolean    postLog = aDeliverySmInfo.updateDnStatus(status);

            final long index   = -1;

            if (!postLog)
            {
                final boolean lExpired = aDeliverySmInfo.isExpired(SmppProperties.getInstance().getDnWaitingTime());

                if (lExpired)
                {
                    postLog = true;

                    if (aDeliverySmInfo.getDnStatus() == null)
                    {
                        aDeliverySmInfo.setDnStatus(SmppDnStatus.VFAILED);
                        log.error("DN Expired.. due to Exceed DN Wait Time.. DN-WaitTime:'" + SmppProperties.getInstance().getDnWaitingTime() + "', RetryTime:'" + aDeliverySmInfo.getRetryInitTime()
                                + "'");
                    }
                    ClientCounter.getInstance().incrementSessionSendExpired(mClientId, 1);
                }
            }

            if (postLog)
            {
                final DeliveryObject lDeliveryObject = DnPostLogGen.getDeliverObject(aDeliverySmInfo, mSessionHandler.getSessionDetail());

                DnPostLogGen.identifySuffix(lDeliveryObject);

                SmppKafkaProducer.sendToPostLog(lDeliveryObject);

                if (log.isDebugEnabled())
                    log.debug("Smpp DN Object sending to DN Post log..");
            }
            else
            {
                final ArrayList<DeliverSmInfo> list = new ArrayList<>();
                list.add(aDeliverySmInfo);
                DeliverySmRedisOps.lpushDeliverSm(aDeliverySmInfo.getClientId(), new Gson().toJson(list));
            }
        }
        catch (final Exception exp)
        {
            log.error("problem writing responses....", exp);
        }
    }

}