package com.itextos.beacon.platform.dlrretry.util;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.RouteLogic;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.routeinfo.util.HeaderValidation;
import com.itextos.beacon.inmemory.routeinfo.util.RouteFinder;
import com.itextos.beacon.inmemory.routeinfo.util.RouteUtil;
import com.itextos.beacon.inmemory.rr.util.RRUtil;

public class DlrWaitRetryUtil
{

    private static final Log log                = LogFactory.getLog(DlrWaitRetryUtil.class);

    private static final int GLOBAL_RETRY       = 1;
    private static final int SAME_ROUTE_RETRY   = 2;
    private static final int CUSTOM_ROUTE_RETRY = 3;

    private DlrWaitRetryUtil()
    {}

    public static void process(
            DeliveryObject aDeliveryObject)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("mapMsg recived by DNRetryWaitQProcessor-->" + aDeliveryObject);

        try
        {
            boolean        sendToRouteQ                 = true;
            final String   lOriginalRouteId             = aDeliveryObject.getRouteId();
            final int      lDlrRetryEnable              = aDeliveryObject.getSmsRetryEnabled();

            // handover split msg or goverenment header msgs will be on same route id
            final boolean  isSplitOrGovtSameRouteStatus = isSplitOrGovtSameRouteStatus(aDeliveryObject);
            MessageRequest lMessageRequest              = null;

            if (isSplitOrGovtSameRouteStatus)
            {
                if (log.isDebugEnabled())
                    log.debug("Going to fetch routeid:" + aDeliveryObject);

                // reset the masked header to original header
                if (!CommonUtility.nullCheck(aDeliveryObject.getMaskedHeader(), true).isEmpty())
                    MessageUtil.setHeaderId(aDeliveryObject, aDeliveryObject.getMaskedHeader());

                if (lDlrRetryEnable == GLOBAL_RETRY)
                {
                    final String lRouteId = RRUtil.getGlobalRetryRouteInfo(aDeliveryObject.getMessagePriority(), lOriginalRouteId, aDeliveryObject.getCarrierStatusCode(),
                            Integer.toString(aDeliveryObject.getRetryAttempt()), aDeliveryObject.getMessageType());

                    if (lRouteId != null)
                    {
                        if (log.isDebugEnabled())
                            log.debug("new route found ::" + lRouteId);
                        aDeliveryObject.setRouteId(lRouteId);
                    }
                    else
                        sendToRouteQ = false;
                }
                else
                    if (lDlrRetryEnable == CUSTOM_ROUTE_RETRY)
                        sendToRouteQ = doCustomeRouteRetry(aDeliveryObject);

                if (!Constants.CHANNEL_VOICE.equalsIgnoreCase(aDeliveryObject.getOtpRetyChannel()))
                {
                    lMessageRequest = aDeliveryObject.getMessageRequestForRetry();

                    if (sendToRouteQ)
                        sendToRouteQ = validateRoute(lMessageRequest, aDeliveryObject);

                    if (sendToRouteQ)
                        sendToRouteQ = checkForHeader(lMessageRequest, aDeliveryObject);
                }
            }

            if (sendToRouteQ)
            {
                if (log.isInfoEnabled())
                    log.info("sending to route queue " + aDeliveryObject.getRouteId());

                // these are all for stats
                aDeliveryObject.setRetryOriginalRouteId(lOriginalRouteId);
                aDeliveryObject.setRetryAlternateRouteId(aDeliveryObject.getRouteId());

                if (lMessageRequest != null)
                {
                    lMessageRequest = aDeliveryObject.getMessageRequestForRetry();

                    lMessageRequest.putValue(MiddlewareConstant.MW_RETRY_ORG_ROUTE_ID, lOriginalRouteId);
                    lMessageRequest.putValue(MiddlewareConstant.MW_RETRY_ALE_ROUTE_ID, aDeliveryObject.getRouteId());
                }

                if (Constants.CHANNEL_VOICE.equalsIgnoreCase(aDeliveryObject.getOtpRetyChannel()))
                    SetNextComponent.sendMessageToVoiceQ(aDeliveryObject);
                else
                    SetNextComponent.sendMessageToRC(lMessageRequest);

                if (log.isDebugEnabled())
                    log.debug("Sent to route queue success " + aDeliveryObject.getRouteId() + "mapmsg:" + aDeliveryObject);
            }
            else
            {
                // deliveries.header filed should have header what was handover to carrier
                // always
                final String lDelvHeader = MessageUtil.getHeaderId(aDeliveryObject);
                MessageUtil.setHeaderId(aDeliveryObject, lDelvHeader);

                if (log.isDebugEnabled())
                    log.debug("Route not found... sending to biller failure");
                SetNextComponent.sendMessageToDeliverySM(aDeliveryObject, "", "");
            }
        }
        catch (final Exception exp)
        {
            log.error("sending message failed for " + aDeliveryObject + "\n" + " sending to error queue...", exp);
            throw exp;
        }
    }

    private static boolean checkForHeader(
            MessageRequest lMessageRequest,
            DeliveryObject aDeliveryObject)
    {
        HeaderValidation.prefixDND(lMessageRequest);

        boolean sendToRouteQ = true;

        if ((aDeliveryObject.getHeaderMasked() == null) && HeaderValidation.isInValidHeaderForAlterOrRetry(lMessageRequest, aDeliveryObject.getRouteId()))
        {
            if (log.isDebugEnabled())
                log.debug("Header route check failed...");
            sendToRouteQ = false;
        }

        if (!RouteUtil.isRouteAvailable(lMessageRequest.getRouteId()))
        {
            if (log.isDebugEnabled())
                log.debug("isAbsoluteRoute route check failed...");
            sendToRouteQ = false;
        }
        return sendToRouteQ;
    }

    private static boolean doCustomeRouteRetry(
            DeliveryObject aDeliveryObject)
    {
        boolean             finished         = false;
        Map<String, String> lCustomRouteId   = null;
        String              lClientId        = aDeliveryObject.getClientId();
        final ItextosClient lClient          = new ItextosClient(lClientId);
        boolean             sendToRouteQ     = true;
        final String        lOriginalRouteId = aDeliveryObject.getRouteId();

        while (!finished)
        {
            lCustomRouteId = RRUtil.getCustomRetryRoutes(lClientId, lOriginalRouteId, aDeliveryObject.getCarrierStatusCode(), Integer.toString(aDeliveryObject.getRetryAttempt()));

            if (lCustomRouteId == null)
                lCustomRouteId = RRUtil.getCustomRetryRoutes(lClientId, lOriginalRouteId, "NULL", Integer.toString(aDeliveryObject.getRetryAttempt()));

            if (log.isInfoEnabled())
                log.info("Client Id :" + lClientId + " Custom Route Info :" + lCustomRouteId);

            if (lCustomRouteId != null)
                finished = true;
            else
                if (lClientId.length() == 16)
                    lClientId = lClient.getAdmin();
                else
                    if (lClientId.length() == 12)
                        lClientId = lClient.getSuperAdmin();
                    else
                        finished = true;
        }

        if ((lCustomRouteId != null) && (lCustomRouteId.get("channel") != null))
        {

            if (lCustomRouteId.get("channel").equalsIgnoreCase("sms"))
            {
                aDeliveryObject.setOtpRetyChannel(lCustomRouteId.get("channel"));
                aDeliveryObject.setRouteId(lCustomRouteId.get("route_id"));
            }
            else
            {
                aDeliveryObject.setOtpRetyChannel(lCustomRouteId.get("channel"));
                aDeliveryObject.setVoiceConfigId(lCustomRouteId.get("voice_config_id"));
                aDeliveryObject.setRouteId(lCustomRouteId.get("route_id"));
            }
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("no custom route found failure...");
            sendToRouteQ = false;
        }
        return sendToRouteQ;
    }

    private static boolean isSplitOrGovtSameRouteStatus(
            DeliveryObject aDeliveryObject)
    {
        boolean   splitOrGovtSameRouteStatus = true;
        final int lDlrRetryEnable            = aDeliveryObject.getSmsRetryEnabled();

        if ((lDlrRetryEnable == GLOBAL_RETRY) || (lDlrRetryEnable == SAME_ROUTE_RETRY) || (lDlrRetryEnable == CUSTOM_ROUTE_RETRY))
        {
            final String lMaskingAlpha = aDeliveryObject.getAalpha();
            if (log.isDebugEnabled())
                log.debug("maskingAlpha:" + lMaskingAlpha);

            final int splitPart = aDeliveryObject.getMessagePartNumber();

            if (splitPart >= 1)
            {
                if (log.isDebugEnabled())
                    log.debug("multipart msg handover to same route**********************" + aDeliveryObject);

                splitOrGovtSameRouteStatus = false;
            }

            // special series (15digit) handover via same gateway
            if (aDeliveryObject.isTreatDomesticAsSpecialSeries())
            {
                if (log.isDebugEnabled())
                    log.debug("Special series (15digit) msg handover to same route**********************" + aDeliveryObject);
                splitOrGovtSameRouteStatus = false;
            }
        }
        return splitOrGovtSameRouteStatus;
    }

    private static boolean validateRoute(
            MessageRequest aMessageRequest,
            DeliveryObject aDeliveryObject)
    {
        boolean lSendToRouteComponent = true;

        if (!HeaderValidation.isValidHeader(MessageUtil.getHeaderId(aMessageRequest), aMessageRequest.getRouteId()))
        {
            if (log.isDebugEnabled())
                log.debug("route header validation failed...reouteid=" + aMessageRequest.getRouteId());

            if (!RouteFinder.setRouteTryWithHeaderFailedRoute(aMessageRequest))
            {
                if (log.isDebugEnabled())
                    log.debug("Header failed route not found...");
                final int logicid = aDeliveryObject.getRouteLogicId() == null ? 0 : aMessageRequest.getRouteLogicId();

                if (RouteLogic.DEAULT.getKey().equals(logicid + ""))
                {
                    aDeliveryObject.setDnOrigianlstatusCode(PlatformStatusCode.ROUTE_BASED_HEADER_FAILED.getStatusCode());
                    lSendToRouteComponent = false;
                }
                else
                {
                    if (log.isDebugEnabled())
                        log.debug("");

                    if (!RouteFinder.setRouteTryWithDefaultRoute(aMessageRequest))
                    {
                        if (log.isDebugEnabled())
                            log.debug("default route not found....");

                        aDeliveryObject.setDnOrigianlstatusCode(PlatformStatusCode.INVALID_ROUTE_ID.getStatusCode());
                        lSendToRouteComponent = false;
                    }
                    else
                    {
                        if (log.isDebugEnabled())
                            log.debug("checking again route senderid validation....");

                        if (!HeaderValidation.isValidHeader(MessageUtil.getHeaderId(aMessageRequest), aMessageRequest.getRouteId()))
                        {
                            if (log.isDebugEnabled())
                                log.debug("2nd attempt route header validation failed...reouteid=" + aMessageRequest.getRouteId());

                            if (!RouteFinder.setRouteTryWithHeaderFailedRoute(aMessageRequest))
                            {
                                if (log.isDebugEnabled())
                                    log.debug("2nd attempt senderid failed route not found...");

                                aDeliveryObject.setDnOrigianlstatusCode(PlatformStatusCode.ROUTE_BASED_HEADER_FAILED.getStatusCode());
                                lSendToRouteComponent = false;
                            }
                        }
                    }
                }
            }
        }
        return lSendToRouteComponent;
    }

}
