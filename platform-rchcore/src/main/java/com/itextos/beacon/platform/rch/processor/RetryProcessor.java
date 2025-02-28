package com.itextos.beacon.platform.rch.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.carrierhandover.util.ICHUtil;
import com.itextos.beacon.inmemory.routeinfo.util.HeaderValidation;
import com.itextos.beacon.platform.carrierhandoverutility.util.CHUtil;
import com.itextos.beacon.platform.rch.util.RCHProcessUtil;
import com.itextos.beacon.platform.rch.util.RCHProducer;

public class RetryProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log           = LogFactory.getLog(RetryProcessor.class);

    private boolean          isProcessing  = true;
    private static String    HEADER_MASKED = "1";

    public RetryProcessor(
            String aThreadName,
            Component aComponent,
            ClusterType aPlatformCluster,
            String aTopicName,
            ConsumerInMemCollection aConsumerInMemCollection,
            int aSleepInMillis)
    {
        super(aThreadName, aComponent, aPlatformCluster, aTopicName, aConsumerInMemCollection, aSleepInMillis);
    }

    @Override
    public void doProcess(
            BaseMessage aBaseMessage)
    {
        final MessageRequest lMessageRequest = (MessageRequest) aBaseMessage;

    	lMessageRequest.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER).append("\n").append(" LOG START");

        if (log.isDebugEnabled())
            log.debug("RHC Received Object .. " + lMessageRequest);

        try
        {
            final String lRouteId   = lMessageRequest.getRouteId();
            final String lMessageId = lMessageRequest.getBaseMessageId();

            if (RCHProcessUtil.isKannelAvailable(lRouteId))
            {
                if (log.isDebugEnabled())
                    log.debug("Kannel available for the routeid: " + lRouteId + " Message Id: " + lMessageId);

                sendToDeliveryProcess(lMessageRequest);
                return;
            }

            final String  lMNumber                 = lMessageRequest.getMobileNumber();

            final boolean isINTL                   = lMessageRequest.isIsIntl();

            // For intl msgs/special series (15digit) - don't look for alternative routied
            // since

            final boolean lIsSpecialSeriesDomestic = lMessageRequest.isTreatDomesticAsSpecialSeries();

            if (isINTL || lIsSpecialSeriesDomestic)
            {
                if (log.isDebugEnabled())
                    log.debug(" International msg/Special series - sending to same retry gateway Message Id: " + lMessageId + " Mobile Number:" + lMNumber);
                sendToRetryQueue(lMessageRequest);
                return;
            }

            // 0-same gateway, 1- look for alternative routeid
            if ((lMessageRequest.getValue(MiddlewareConstant.MW_RETRY_ALTER_ROUTE_LOOKUP) != null) && lMessageRequest.getValue(MiddlewareConstant.MW_RETRY_ALTER_ROUTE_LOOKUP).equals("0"))
            {
                if (log.isDebugEnabled())
                    log.debug(" bypass_mcc_mnc enabled - msg sending to same retry gateway Message Id: " + lMessageId);
                // Should not find alternative route - push to same
                // RETRY queue - Force to same kannel till kannel up/msg
                // expiry
                sendToRetryQueue(lMessageRequest);
                return;
            }

            // find alternative route
            final boolean lFindAlternativeRoute = findAlternativeRoute(lMessageRequest, lRouteId, null);
            if (log.isDebugEnabled())
                log.debug(" findAlternativeRoute:" + lFindAlternativeRoute + " Message Id: " + lMessageId);

            if (lFindAlternativeRoute)
            {
                final String lReRouteId = lMessageRequest.getRouteId();
                if (log.isDebugEnabled())
                    log.debug(" new reroute found: " + lReRouteId + " Message Id: " + lMessageId);

                sendToDeliveryProcess(lMessageRequest);
                return;
            }
            // sending to same retry queue
            sendToRetryQueue(lMessageRequest);
        }
        catch (final Exception e)
        {
            log.error("Exception while processing the message." + lMessageRequest, e);
            RCHProducer.sendToErrorLog(lMessageRequest, e);
        }

        isProcessing = false;
    }

    private static boolean findAlternativeRoute(
            MessageRequest aMessageRequest,
            String aRouteId,
            List<String> lAlternateRouteids)
            throws Exception
    {
        boolean      lStatus        = false;
        final String lMessageId     = aMessageRequest.getBaseMessageId();
        final String isHeaderMasked = aMessageRequest.getIsHeaderMasked();

        if (log.isDebugEnabled())
            log.debug(" routeid:" + aRouteId + " Message Id:" + lMessageId + " isHeaderMasked:" + isHeaderMasked);

        if ((isHeaderMasked != null) && isHeaderMasked.equals(HEADER_MASKED))
        {
            if (log.isDebugEnabled())
                log.debug(" header is masked - sending to same gate way Message Id:" + lMessageId);
            return lStatus;
        }

        // Single Part & Multipart we can find the alternate route.
        if (log.isDebugEnabled())
            log.debug("Message Id:" + lMessageId);

        final String lAlternateRouteId = ICHUtil.getAlternateRoute(aRouteId);
        if (log.isDebugEnabled())
            log.debug(" Alternate Roue Id: " + lAlternateRouteId + " Message Id: " + lMessageId);

        if (lAlternateRouteId != null)
        {
            if (log.isDebugEnabled())
                log.debug(" Found alternate route : " + lAlternateRouteId + " for down routeid:" + aRouteId + " Message Id:" + lMessageId);

            if (!RCHProcessUtil.isMsgTypeEnabled(aMessageRequest.getMessageType(), lAlternateRouteId) || !RCHProcessUtil.isAbsoluteRoute(lAlternateRouteId))
            {
                if (log.isDebugEnabled())
                    log.debug(" Alternate Route : " + lAlternateRouteId + " is disabled for msgtype in route_configuration/route doesn't exists" + " Message Id:" + lMessageId);
                return lStatus;
            }

            if (RCHProcessUtil.isDummyRoute(lAlternateRouteId))
            {
                aMessageRequest.setRouteId(lAlternateRouteId);
                if (log.isDebugEnabled())
                    log.debug(" configured route is dummy route hence kannel availability is not checking and going to process via retry-route:" + lAlternateRouteId);
                return true;
            }

            if ((lAlternateRouteids != null) && lAlternateRouteids.contains(lAlternateRouteId.toLowerCase()))
            {
                if (log.isDebugEnabled())
                    log.debug(" Alternate Route : " + lAlternateRouteId
                            + " already found and executed looks like same reroute configured mutliple times - avoid iteration and sending to same retry queue Message Id:" + lMessageId);
                return lStatus;
            }

            if (lAlternateRouteids == null)
                lAlternateRouteids = new ArrayList<>();
            lAlternateRouteids.add(lAlternateRouteId.toLowerCase());

            if (RCHProcessUtil.isKannelAvailable(lAlternateRouteId))
            {
                if (log.isDebugEnabled())
                    log.debug(" Kannel available for retry-route: " + lAlternateRouteId + " for org routeid: " + aRouteId + " Message Id:" + lMessageId);
                final String lHeader = MessageUtil.getHeaderId(aMessageRequest);

                // if (HeaderValidation.isValidHeader(lHeader, lAlternateRouteId) &&
                // !HeaderValidation.isInValidHeaderForAlterOrRetry(aMessageRequest,
                // lAlternateRouteId))

                if (HeaderValidation.isValidHeader(lHeader, lAlternateRouteId))
                {
                    if (log.isDebugEnabled())
                        log.debug(" Header is valid for retry-route: " + lAlternateRouteId + " Message Id:" + lMessageId);
                    aMessageRequest.setRouteId(lAlternateRouteId);

                    HeaderValidation.prefixDND(aMessageRequest);
                    lStatus = true;
                }
                else
                    if (log.isDebugEnabled())
                        log.debug(" Header is not valid for retry-route: " + lAlternateRouteId + " Message Id:" + lMessageId);
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Kannel is down for retry-route: " + lAlternateRouteId + " for org routeid: " + aRouteId + " Message Id:" + lMessageId);
                return findAlternativeRoute(aMessageRequest, lAlternateRouteId, lAlternateRouteids);
            }
        }
        else
            if (log.isDebugEnabled())
                log.debug(" No reroute configured for routed " + aRouteId + " Message Id:" + lMessageId);

        return lStatus;
    }

    private static void sendToDeliveryProcess(
            MessageRequest aMessageRequest)
    {
        if (log.isDebugEnabled())
            log.debug("Calling Deliveryprocessor Message Id: " + aMessageRequest.getBaseMessageId());

        // aMessageRequest.setMtMessageRetryIdentifier(Constants.ENABLED);

        final RetryCarrierHandover deliveryProcessor = new RetryCarrierHandover(aMessageRequest);
        deliveryProcessor.doProcess();
    }

    private static void sendToRetryQueue(
            MessageRequest aMessageRequest)
    {
        if (log.isDebugEnabled())
            log.debug("Sending to retryqueue Message Id: " + aMessageRequest.getBaseMessageId());

        final int lRouteRetryCount           = CommonUtility.getInteger(aMessageRequest.getValue(MiddlewareConstant.MW_ROUTE_RETRY_ATTEMPT));

        final int lMaxRouteRetryAttemptCount = CommonUtility.getInteger(CHUtil.getAppConfigValueAsString(ConfigParamConstants.MAX_ROUTE_RETRY_ATTEMPT_COUNT));

        if (log.isDebugEnabled())
        {
            log.debug("RouteRetryCount :" + lRouteRetryCount);
            log.debug("MaxRouteRetryAttemptCount :" + lMaxRouteRetryAttemptCount);
        }

        if (lRouteRetryCount < lMaxRouteRetryAttemptCount)
        {
            aMessageRequest.putValue(MiddlewareConstant.MW_ROUTE_RETRY_ATTEMPT, String.valueOf(lRouteRetryCount + 1));
            log.error("Sending to retry queue due to kannel down/storesize/latency route:" + aMessageRequest.getRouteId());
            RCHProducer.sendToRetryRoute(aMessageRequest);
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("Max Route Retry Attempt reached... Hence reject the message ..");
            sendToPlatfromRejection(aMessageRequest, PlatformStatusCode.CARRIER_HANDOVER_FAILED);
        }
    }

    private static void sendToPlatfromRejection(
            MessageRequest aMessageRequest,
            PlatformStatusCode aStatusId)
    {
        aMessageRequest.setSubOriginalStatusCode(aStatusId.getStatusCode());
        RCHProducer.sendToPlatfromRejection(aMessageRequest);
    }

    public boolean isProcessing()
    {
        return isProcessing;
    }

    @Override
    public void doCleanup()
    {
        // TODO Auto-generated method stub
    }

    @Override
    protected void updateBeforeSendBack(
            IMessage aMessage)
    {
        // TODO Auto-generated method stub
    }

}
