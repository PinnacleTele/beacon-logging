package com.itextos.beacon.smpp.interfaces.workers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.google.gson.Gson;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.smpputil.DeliverSmInfo;
import com.itextos.beacon.platform.smpputil.SmppDnStatus;
import com.itextos.beacon.smpp.interfaces.event.handlers.ItextosSmppSessionHandler;
import com.itextos.beacon.smpp.interfaces.sessionhandlers.ItextosSessionManager;
import com.itextos.beacon.smpp.interfaces.sessionhandlers.objects.DelvierySmWindowFutureHolder;
import com.itextos.beacon.smpp.interfaces.util.counters.ClientCounter;
import com.itextos.beacon.smpp.redisoperations.DeliverySmRedisOps;
import com.itextos.beacon.smpp.utils.SmppKafkaProducer;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;

public class CustomerRedisQWorker
        extends
        Thread
{

    private static final Log log  = LogFactory.getLog(CustomerRedisQWorker.class);

    private final String     systemId;
    private final String     clientId;
    private final DNWorker   worker;
    private boolean          done = false;

    public CustomerRedisQWorker(
            String aClientId,
            String aSystemId)
    {
        clientId = aClientId;
        systemId = aSystemId;
        worker   = new DNWorker(systemId);

        setName("CustomerRedisQWorker-" + clientId);
    }

    @Override
    public void run()
    {

        try
        {
            log.info("CustomerRedisQWorker for clientId=" + clientId + " SystemId=" + systemId);

            while (!done)
                try
                {
                    final List<DeliverSmInfo> delvieryInfoList = DeliverySmRedisOps.lpopDeliverSm(clientId);
                    CustomerRedisHeartBeatData.getInstance().addHeartBeat(getName(), systemId);

                    if (delvieryInfoList == null)
                    {
                        CommonUtility.sleepForAWhile();
                        continue;
                    }

                    processDeliverSmList(delvieryInfoList);
                }
                catch (final Exception e1)
                {
                    log.error("problem sending dn's possible loss...", e1);
                    CommonUtility.sleepForAWhile();
                }
            CustomerRedisHeartBeatData.getInstance().remove(getName(), systemId);
            log.info("exiting customerwise redis worker for ClientId =" + clientId + " no bind exists...");
        }
        catch (final Exception exp)
        {
            log.error("problem abruptly quiting thread for the customer " + systemId + "due to...", exp);
        }
    }

    private void processDeliverSmList(
            List<DeliverSmInfo> aDelvieryInfoList)
    {

        for (final DeliverSmInfo deliveryInfo : aDelvieryInfoList)
        {
            if (log.isDebugEnabled())
                log.debug("popped a aDn ..." + deliveryInfo);

            if (deliveryInfo != null)
            {
                CustomerRedisHeartBeatData.getInstance().addHeartBeat(getName(), systemId);

                try
                {
                    final boolean canSendToCustomer = ClientCounter.canProcessMessage(deliveryInfo);

                    if (canSendToCustomer)
                    {
                        boolean sent = false;

                        while (!sent)
                        {
                            sent = sendMessage(deliveryInfo);

                            if (!sent)
                            {
                                if (log.isDebugEnabled())
                                    log.debug("waiting for session to be returned...");
                                Thread.sleep(1);
                            }
                        }
                    }
                    else
                    {
                        writeResponse(deliveryInfo, DeliverSmInfo.TIME_EXPIRED);
                        deliveryInfo.setReason("API-Expired Before Send To Customer, DN:'" + deliveryInfo.getShortMessage() + "'");

                        log.error("DN Expired before handover to customer.., Submit time is greater than Account DN Expiry time : '" + deliveryInfo.getShortMessage() + "'");
                        ClientCounter.getInstance().incrementCustomerTimeExpired(clientId, 1);

                        if (log.isInfoEnabled())
                            log.info("Due to Time Expiry the message will be trated as platform expired.");
                    }
                }
                catch (final Exception exp)
                {
                    writeResponse(deliveryInfo, null);
                    log.error("DN Failed Due to some exception..");
                    log.error("problem handling dn's sending to no bind queue...", exp);
                }
            }
        }
    }

    private boolean sendMessage(
            DeliverSmInfo aDeliveryInfo)
    {
        final ItextosSmppSessionHandler sessionHandler = getSessionHandler();
        boolean                         sent           = false;

        if (sessionHandler != null)
        {
            final DelvierySmWindowFutureHolder tempBean = send(aDeliveryInfo, sessionHandler);

            ClientCounter.getInstance().incrementCustomerDLRSendCustomerSuccess(clientId, 1);

            if (tempBean.getWindowfuture() != null)
            {
                log.info("message sent.. adding to in memory queue");
                sessionHandler.updateLastUsedTime();
                sent = true;
            }
            sessionHandler.setInUse(false);
        }
        return sent;
    }

    private ItextosSmppSessionHandler getSessionHandler()
    {

        try
        {
            return ItextosSessionManager.getInstance().getAvailableSession(systemId);
        }
        catch (final Exception e)
        {
            if (log.isWarnEnabled())
                log.warn("Session Not available for systemId - " + systemId);
        }
        return null;
    }

    public boolean isDone()
    {
        return done;
    }

    public void setDone(
            boolean aDone)
    {
        this.done = aDone;
    }

    private void writeResponse(
            DeliverSmInfo aDeliverySmInfo,
            Integer status)
    {

        try
        {
            aDeliverySmInfo.setRetryInitTime();
            aDeliverySmInfo.incRetryAttempt();

            boolean postLog = aDeliverySmInfo.updateDnStatus(status);

            if (log.isDebugEnabled())
                log.debug("DN Post log status: " + postLog);

            final long index = -1;

            if (!postLog)
            {
                final boolean lExpired = aDeliverySmInfo.isExpired(SmppProperties.getInstance().getDnWaitingTime());

                if (lExpired)
                {
                    postLog = true;
                    aDeliverySmInfo.setReason("API-Expired Before Send To Customer");

                    if (aDeliverySmInfo.getDnStatus() == null)
                    {
                        aDeliverySmInfo.setDnStatus(SmppDnStatus.VFAILED);
                        log.error("DN Expired.. due to Exceed DN Wait Time.. DN-WaitTime:'" + SmppProperties.getInstance().getDnWaitingTime() + "', RetryTime:'" + aDeliverySmInfo.getRetryInitTime()
                                + "'");
                        aDeliverySmInfo.setReason("API-Failed Before Send To Customer");
                    }
                    ClientCounter.getInstance().incrementSessionSendExpired(clientId, 1);
                }
            }

            if (postLog)
            {
                final ItextosSmppSessionHandler sessionHandler  = getSessionHandler();
                final DeliveryObject            lDeliveryObject = DnPostLogGen.getDeliverObject(aDeliverySmInfo, sessionHandler.getSessionDetail());

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

    private DelvierySmWindowFutureHolder send(
            DeliverSmInfo aDeliverySmInfo,
            ItextosSmppSessionHandler aSessionHandler)
    {
        aDeliverySmInfo.resetDnRts();
        aDeliverySmInfo.resetDnSts();
        final WindowFuture<Integer, PduRequest, PduResponse> windowFuture = worker.sendMessage(aSessionHandler, aDeliverySmInfo);
        return new DelvierySmWindowFutureHolder(aDeliverySmInfo, aSessionHandler, windowFuture);
    }

}
