package com.itextos.beacon.platform.rch.processor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.httpclient.BasicHttpConnector;
import com.itextos.beacon.commonlib.httpclient.HttpResult;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.errorlog.SMSLog;
import com.itextos.beacon.inmemory.carrierhandover.RouteKannelInfo;
import com.itextos.beacon.inmemory.carrierhandover.util.ICHUtil;
import com.itextos.beacon.platform.carrierhandoverutility.util.CHUtil;
import com.itextos.beacon.platform.carrierhandoverutility.util.GenerateDNUrl;
import com.itextos.beacon.platform.dnpayloadutil.PayloadProcessor;
import com.itextos.beacon.platform.kannelstatusupdater.process.response.KannelStatsCollector;
import com.itextos.beacon.platform.rch.util.RCHProcessUtil;
import com.itextos.beacon.platform.rch.util.RCHProducer;

public class RetryCarrierHandover
{

    private static final Log     log = LogFactory.getLog(RetryCarrierHandover.class);

    private final MessageRequest mMessageRequest;

    public RetryCarrierHandover(
            MessageRequest aMessageRequest)
    {
        this.mMessageRequest = aMessageRequest;
    }

    public void doProcess()
    {

    	SMSLog sb=SMSLog.getInstance();
        try
        {
            final boolean isHexMsg = mMessageRequest.isHexMessage();

            if (isHexMsg)
            {
                final boolean isValidHexMessage = CHUtil.isValidHexMessage(mMessageRequest.getLongMessage());

                if (!isValidHexMessage)
                {
                    log.error("Invalid HEX Message : " + mMessageRequest);
                    sendToPlatfromRejection(mMessageRequest, PlatformStatusCode.INVALID_HEX_MESSAGE);
                    return;
                }
            }

            final String lFeatureCode = mMessageRequest.getFeatureCode();
            String       lRouteId     = mMessageRequest.getRouteId();

            if (lRouteId == null)
            {
                log.error("Unable to find the route : " + lRouteId);
                sendToPlatfromRejection(mMessageRequest, PlatformStatusCode.EMPTY_ROUTE_ID);
                return;
            }

            if (lFeatureCode == null)
            {
                log.error("Unable to find the valid Feature code : " + lFeatureCode);
                sendToPlatfromRejection(mMessageRequest, PlatformStatusCode.EMPTY_FEATURE_CODE);
                return;
            }

            final boolean isBlockout = RCHProcessUtil.isMessageBlockout(mMessageRequest);

            if (isBlockout)
                return;

            final boolean isExpired = CHUtil.isExpired(mMessageRequest);

            if (isExpired)
            {
                log.error("Message Expired :" + mMessageRequest);
                sendToPlatfromRejection(mMessageRequest, PlatformStatusCode.EXPIRED_MESSAGE);
                return;
            }

            final RouteKannelInfo lKannelRouteInfo = ICHUtil.getDeliveryRouteInfo(lRouteId, lFeatureCode);

            if (lKannelRouteInfo == null)
            {
                log.error(" Unable to find  Route Kannel Template for  route : " + lRouteId + " feature cd : " + lFeatureCode + " Message Object  : " + mMessageRequest);
                sendToPlatfromRejection(mMessageRequest, PlatformStatusCode.KANNEL_TEMPLATE_NOT_FOUND);
                return;
            }

            final List<BaseMessage> lBaseMessageList  = mMessageRequest.getSubmissions();

            boolean                 isFirstPartFailed = false;
            boolean                 isPartialSuccess  = false;

            final boolean           canDoMsgRetry     = CHUtil.canMsgRetry(mMessageRequest);

            for (final BaseMessage baseMssage : lBaseMessageList)
            {
                final SubmissionObject lSubmissionObject = (SubmissionObject) baseMssage;
                if (log.isDebugEnabled())
                    log.debug("Splited Message Object : " + lSubmissionObject);

                try
                {

                    if (isFirstPartFailed && !isPartialSuccess)
                    {
                        log.fatal("First part carrier handover is failed, Hence ignoring the remining part messages...Message Id :" + lSubmissionObject.getMessageId());
                        return;
                    }

                    if (isPartialSuccess && isFirstPartFailed)
                    {
                        if (log.isDebugEnabled())
                            log.debug("Unable to process the Multipart request to kannel for the route '" + lRouteId + ", partially failed' , Hence rejecting the request..");
                        sendToPlatfromRejection(lSubmissionObject, PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED, false);
                        continue;
                    }

                    final String lMessageId = lSubmissionObject.getMessageId();
                    String       lUdh       = CommonUtility.nullCheck(lSubmissionObject.getUdh(), true);

                    if (!lUdh.isEmpty())
                    {
                        if (log.isDebugEnabled())
                            log.debug("Udh Value : " + lUdh);

                        if (CHUtil.isValidUDH(lUdh))
                            lUdh = CHUtil.addKannelSpecCharToHex(lUdh);
                        else
                        {
                            log.error("UDH Value is incorrect : " + lUdh);
                            sendToPlatfromRejection(lSubmissionObject, PlatformStatusCode.INVALID_UDH, false);
                            continue;
                        }
                    }

                    final boolean isPromoNoPayload = CommonUtility.isEnabled(RCHProcessUtil.getAppConfigValueAsString(ConfigParamConstants.NOPAYLOAD_FOR_PROMO_MSG));

                    if (lKannelRouteInfo.isDummyRoute() && (!lKannelRouteInfo.getCarrierFullDn().isEmpty()))
                    {
                        PayloadProcessor.removePayload(lSubmissionObject);

                        lSubmissionObject.setCarrierFullDn(lKannelRouteInfo.getCarrierFullDn());

                        final int lRetryAttempt = mMessageRequest.getRetryAttempt();

                        // Set Actual_Ts and Retry-Attempt
                        GenerateDNUrl.setDlrUrl(lSubmissionObject, lRetryAttempt);

                        if (log.isDebugEnabled())
                            log.debug("Sending to dummy route :" + lSubmissionObject.getMessageId() + " retry attempt:" + lRetryAttempt);

                        final MessageType lMsgType = mMessageRequest.getMessageType();

                        if (isPromoNoPayload && (lMsgType == MessageType.PROMOTIONAL) && !mMessageRequest.isIsIntl())
                        {
                            if (log.isInfoEnabled())
                                log.info("skipping sending to dummy route for promo message....");
                        }
                        else
                        {
                            if (log.isInfoEnabled())
                                log.info("sending to dummy route....");
                            RCHProducer.sendToDummyRoute(lSubmissionObject,sb);
                        }

                        if (mMessageRequest.getRetryAttempt() == 0)
                        {
                            lSubmissionObject.setSubOriginalStatusCode(PlatformStatusCode.SUCCESS.getStatusCode());
                            RCHProducer.sendToSubBilling(lSubmissionObject,sb);
                        }
                        continue;
                    }

                    lRouteId = mMessageRequest.getRouteId();

                    final boolean isKannelAvailable = RCHProcessUtil.isKannelAvailable(lRouteId);

                    int           lRetryAttempt     = mMessageRequest.getRetryAttempt();

                    setCallBackUrl(lSubmissionObject);

                    final String lKannelUrl = getKannelUrl(lKannelRouteInfo, lSubmissionObject, lRetryAttempt, lUdh);

                    if (log.isDebugEnabled())
                        log.debug("kannel URL--->" + lKannelUrl);

                    if (!isKannelAvailable && canDoMsgRetry)
                    {
                        // Set the isFirstPartFailed flag to 'true' for Multipart Request.
                        if (mMessageRequest.getMessageTotalParts() > 1)
                            isFirstPartFailed = true;

                        doMessageRetry(lSubmissionObject, mMessageRequest);
                        continue;
                    }

                    final HttpResult lHttpReault = BasicHttpConnector.connect(lKannelUrl);
                    final boolean    lResponse   = lHttpReault.isSuccess();

                    if (log.isDebugEnabled())
                        log.debug("URL : '" + lKannelUrl + "', Response : '" + lResponse + "'");

                    setKannelResponseTime(lKannelUrl, lRouteId, lResponse);

                    if (lResponse)
                    {
                        isPartialSuccess = true;

                        lRouteId         = CommonUtility.nullCheck(mMessageRequest.getRouteId());
                        lRetryAttempt    = mMessageRequest.getRetryAttempt();

                        if (lRetryAttempt != 0)
                        {
                            // retry msg send to interim failure topic
                            RCHProducer.sendToInterim(lSubmissionObject);
                            if (log.isDebugEnabled())
                                log.debug("send to interm queue success Message Id:" + lMessageId);
                        }

                        lSubmissionObject.setSubOriginalStatusCode(PlatformStatusCode.SUCCESS.getStatusCode());

                        if (lRetryAttempt == 0)
                        {
                            RCHProducer.sendToSubBilling(lSubmissionObject,sb);

                            if (log.isDebugEnabled())
                                log.debug("sendToSubBilling success");
                        }
                    }
                    else
                    {
                        if (log.isDebugEnabled())
                            log.debug("\n url : " + lKannelUrl + " : \n response : " + lResponse);

                        isFirstPartFailed = true;

                        if (isPartialSuccess && isFirstPartFailed)
                        {
                            if (log.isDebugEnabled())
                                log.debug("Unable to process the Multipart request to kannel for the route '" + lRouteId + ", partially failed' , Hence rejecting the request..");
                            sendToPlatfromRejection(lSubmissionObject, PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED, true);
                        }
                        else
                        {
                            if (log.isDebugEnabled())
                                log.debug("");

                            if (isFirstPartFailed)
                            {
                                if (log.isDebugEnabled())
                                    log.debug("First part failed ...");

                                if (canDoMsgRetry)
                                {
                                    if (log.isDebugEnabled())
                                        log.debug("Message Retry Enabled, First part failed ...:'" + isFirstPartFailed + "', Hence sending to Message Retry..");
                                    doMessageRetry(lSubmissionObject, mMessageRequest);
                                }
                                else
                                {
                                    if (log.isDebugEnabled())
                                        log.debug("Message Retry Disabled, Unable to send the Multipart request to kannel for the route '" + lRouteId + "' , Hence rejecting the request..");

                                    sendToPlatfromRejection(lSubmissionObject, mMessageRequest, PlatformStatusCode.CARRIER_HANDOVER_FAILED, true);
                                }
                            }
                        }
                    }
                }
                catch (final Exception e2)
                {
                    log.error("Exception occer while processing Carrier Handover ...", e2);
                    isFirstPartFailed = true;

                    if (isPartialSuccess && isFirstPartFailed)
                    {
                        lSubmissionObject.setSubOriginalStatusCode(PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED.getStatusCode());
                        lSubmissionObject.setAdditionalErrorInfo("RCH :" + e2.getMessage());
                        RCHProducer.sendToNextLevel(lSubmissionObject, mMessageRequest, true);
                    }
                    else
                    {
                        if (log.isDebugEnabled())
                            log.debug("");

                        if (isFirstPartFailed)
                        {
                            if (log.isDebugEnabled())
                                log.debug("Due to exception sending to PRC..., Base Mid :'" + mMessageRequest.getBaseMessageId() + "'");

                            mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.CARRIER_HANDOVER_FAILED.getStatusCode());
                            mMessageRequest.setAdditionalErrorInfo("RCH :" + e2.getMessage());

                            RCHProducer.sendToNextLevel(lSubmissionObject, mMessageRequest, false);
                        }
                    }
                }
            }
        }
        catch (final Exception e)
        {
            log.error("Exception occer while processing Carrier Handover ...", e);

            try
            {
                RCHProducer.sendToErrorLog(mMessageRequest, e);
            }
            catch (final Exception e1)
            {
                e1.printStackTrace();
            }
        }
    }

    private static void setKannelResponseTime(
            String aKannelUrl,
            String aRouteId,
            boolean aResponse)
    {

        try
        {
            final long    lKannelHitEndTime   = System.currentTimeMillis();
            final long    lKannelHitStartTime = System.currentTimeMillis();

            final boolean isResponseCheck     = CommonUtility.isEnabled(RCHProcessUtil.getAppConfigValueAsString(ConfigParamConstants.KANNEL_CONN_RESP_CHK));

            if (log.isDebugEnabled())
                log.debug("Check Respone Time '" + isResponseCheck + "'");

            if (isResponseCheck)
                KannelStatsCollector.getInstance().verifyKannelResponseTime(aKannelUrl, aRouteId, lKannelHitStartTime, lKannelHitEndTime, aResponse);
        }
        catch (final Exception e)
        {
            log.error("Some error in calculating the times.", e);
        }
    }

    private String getKannelUrl(
            RouteKannelInfo aKannelRouteInfo,
            SubmissionObject aSubmissionObject,
            int aRetryAttempt,
            String aUdh)
            throws UnsupportedEncodingException
    {
        log.debug("\n Kannel Available for delivery \n");
        final String   lMessage     = CHUtil.getMessage(aSubmissionObject);
        final boolean  isDLTEnable  = CommonUtility.isEnabled(CHUtil.getAppConfigValueAsString(ConfigParamConstants.DLT_ENABLE));

        String         lUrlparams[] = new String[]
        { URLEncoder.encode(aKannelRouteInfo.getKannelIp(), Constants.ENCODER_FORMAT),            // 0
                URLEncoder.encode(aKannelRouteInfo.getKannelPort(), Constants.ENCODER_FORMAT),          // 1
                URLEncoder.encode(aKannelRouteInfo.getSmscId(), Constants.ENCODER_FORMAT),         // 2
                URLEncoder.encode(aSubmissionObject.getMobileNumber(), Constants.ENCODER_FORMAT),          // 3
                URLEncoder.encode(ICHUtil.getHeader(aKannelRouteInfo, MessageUtil.getHeaderId(mMessageRequest)), Constants.ENCODER_FORMAT),         // 4
                lMessage,          // 5
                aUdh,            // 6
                URLEncoder.encode(aSubmissionObject.getCallBackUrl(), Constants.ENCODER_FORMAT),           // 7 chn kannel dlr_url or hexcode value
                URLEncoder.encode(CHUtil.getPriority(mMessageRequest), Constants.ENCODER_FORMAT),          // 8
                URLEncoder.encode((String.valueOf(mMessageRequest.getMaxValidityInSec() / 60)), Constants.ENCODER_FORMAT),          // 9
                URLEncoder.encode(mMessageRequest.getClientId(), Constants.ENCODER_FORMAT),          // 10
                URLEncoder.encode(DateTimeUtility.getFormattedDateTime(aSubmissionObject.getMessageReceivedDate(), DateTimeFormat.DEFAULT_DATE_ONLY), Constants.ENCODER_FORMAT),     // 11
                URLEncoder.encode(mMessageRequest.getMessagePriority().getKey(), Constants.ENCODER_FORMAT),          // 12
                URLEncoder.encode(mMessageRequest.getMessagePriority().getKey(), Constants.ENCODER_FORMAT),            // 13
                URLEncoder.encode(String.valueOf(aRetryAttempt), Constants.ENCODER_FORMAT)          // 14
        };
        final String[] lDltParams   = new String[2];

        if (isDLTEnable)
        {
            lDltParams[0] = URLEncoder.encode(CommonUtility.nullCheck(mMessageRequest.getDltEntityId(), true), Constants.ENCODER_FORMAT);
            lDltParams[1] = URLEncoder.encode(CommonUtility.nullCheck(mMessageRequest.getDltTemplateId(), true), Constants.ENCODER_FORMAT);

            final String[] merged = new String[lUrlparams.length + lDltParams.length];
            System.arraycopy(lUrlparams, 0, merged, 0, lUrlparams.length);
            System.arraycopy(lDltParams, 0, merged, lUrlparams.length, lDltParams.length);
            lUrlparams = merged;
        }

        final String lKannelUrlTemplate = aKannelRouteInfo.getUrlTemplate();
        log.debug("URL_TEMPALTE : " + lKannelUrlTemplate);

        return MessageFormat.format(lKannelUrlTemplate, lUrlparams);
    }

    private void setCallBackUrl(
            SubmissionObject aSubmissionObject)
            throws InterruptedException,
            UnsupportedEncodingException
    {
        if (log.isDebugEnabled())
            log.debug("Attempting to remove payload....");
        final ClusterType lCluster               = mMessageRequest.getClusterType();

        final String      lClusterDNReceiverInfo = ICHUtil.getClusterDNReceiverInfo(lCluster.getKey());

        PayloadProcessor.removePayload(aSubmissionObject);
        if (log.isDebugEnabled())
            log.debug("Remove payload....finished");

        String lPayloadRid = null;

        while (lPayloadRid == null)
        {
            lPayloadRid = PayloadProcessor.storePayload(aSubmissionObject);

            if (lPayloadRid == null)
            {
                if (log.isInfoEnabled())
                    log.info("Payload rid null retrying after 100 millis...");
                Thread.sleep(100);
            }
        }
        aSubmissionObject.setPayloadRedisId(lPayloadRid);

        final String additionalInfoString = CHUtil.getCallBackParams(aSubmissionObject);

        if (log.isInfoEnabled())
        {
            log.info("Payload storing to redis succesful for  payloadrid=" + lPayloadRid);
            log.info("additionalInfoString===>" + additionalInfoString);
        }
        final String encodedAdditionalInfo = URLEncoder.encode(additionalInfoString, Constants.ENCODER_FORMAT);
        final String lDlrUrl               = CHUtil.generateCallBackUrl(lClusterDNReceiverInfo, encodedAdditionalInfo);
        log.info("Kannel dn URL--->" + lDlrUrl);
        aSubmissionObject.setCallBackUrl(lDlrUrl);
    }

    private static void doMessageRetry(
            SubmissionObject aSubmissionObject,
            MessageRequest aMessageRequest)
    {
        final int lRouteRetryCount           = CommonUtility.getInteger(aMessageRequest.getValue(MiddlewareConstant.MW_ROUTE_RETRY_ATTEMPT));

        final int lMaxRouteRetryAttemptCount = CommonUtility.getInteger(CHUtil.getAppConfigValueAsString(ConfigParamConstants.MAX_ROUTE_RETRY_ATTEMPT_COUNT));

        if (log.isDebugEnabled())
        {
            log.debug("RouteRetryCount :" + lRouteRetryCount);
            log.debug("MaxRouteRetryAttemptCount :" + lMaxRouteRetryAttemptCount);
        }

        try
        {
            aSubmissionObject.setMtMessageRetryIdentifier(Constants.ENABLED);
            PayloadProcessor.removePayload(aSubmissionObject);
        }
        catch (final Exception e)
        {
            log.error("Exception while removing payload ..", e);
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

    private static void sendToPlatfromRejection(
            SubmissionObject aSubmissionObject,
            PlatformStatusCode aStatusId,
            boolean isRemovepayload)
    {

        if (isRemovepayload)
        {
            if (log.isDebugEnabled())
                log.debug("Removing the payload ...");
            aSubmissionObject.setMtMessageRetryIdentifier(Constants.ENABLED);
            PayloadProcessor.removePayload(aSubmissionObject);
        }

        aSubmissionObject.setSubOriginalStatusCode(aStatusId.getStatusCode());
        RCHProducer.sendToPlatfromRejection(aSubmissionObject);
    }

    private static void sendToPlatfromRejection(
            SubmissionObject aSubmissionObject,
            MessageRequest aMessageRequest,
            PlatformStatusCode aStatusId,
            boolean isRemovepayload)
    {

        if (isRemovepayload)
        {
            if (log.isDebugEnabled())
                log.debug("Removing the payload ...");
            aSubmissionObject.setMtMessageRetryIdentifier(Constants.ENABLED);
            PayloadProcessor.removePayload(aSubmissionObject);
        }

        aMessageRequest.setSubOriginalStatusCode(aStatusId.getStatusCode());
        RCHProducer.sendToPlatfromRejection(aMessageRequest);
    }

}
