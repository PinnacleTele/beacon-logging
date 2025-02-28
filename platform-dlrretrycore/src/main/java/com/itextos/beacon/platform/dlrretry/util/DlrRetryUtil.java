package com.itextos.beacon.platform.dlrretry.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.msgvalidity.CommonMsgValidity;
import com.itextos.beacon.inmemory.rr.util.RRUtil;
import com.itextos.beacon.platform.dnpayloadutil.common.TimeAdjustmentUtility;

public class DlrRetryUtil
{

    private static final Log log                = LogFactory.getLog(DlrRetryUtil.class);

    private static final int NO_RETRY           = 0;
    private static final int GLOBAL_RETRY       = 1;
    private static final int SAME_ROUTE_RETRY   = 2;
    private static final int CUSTOM_ROUTE_RETRY = 3;

    private DlrRetryUtil()
    {}

    public static Map<Component, DeliveryObject> processDNRetry(
            DeliveryObject aDeliveryObject)
            throws Exception
    {
        final String                         lCarrierErrorCode = "";
        String                               lReason           = "";
        final Map<Component, DeliveryObject> resultMap         = new HashMap<>();
        final String                         lRouteType        = CommonUtility.nullCheck(aDeliveryObject.getRouteType(), true);

        if (lRouteType.isEmpty())
            aDeliveryObject.setRouteType("SMPP");

        try
        {
            TimeAdjustmentUtility.setCarrierTime(aDeliveryObject);
            if (log.isDebugEnabled())
                log.debug(" msg received::" + aDeliveryObject);

            lReason = CommonUtility.nullCheck(aDeliveryObject.getFailReason(), true);
            final int lSmsRetry = aDeliveryObject.getSmsRetryEnabled();

            if (aDeliveryObject.isIsIntl())
                SetNextComponent.sendMessageToDeliverySM(aDeliveryObject, lCarrierErrorCode, lReason + "-dnretryF:int-Fl");
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Domestic Retry Process ....");

                switch (lSmsRetry)
                {
                    case NO_RETRY:
                        if (log.isInfoEnabled())
                            log.info(" no retry configured for the esme sending to dn and client dn queue....");
                        SetNextComponent.sendMessageToDeliverySM(aDeliveryObject, lCarrierErrorCode, null);
                        break;

                    case GLOBAL_RETRY:
                    case SAME_ROUTE_RETRY:
                        doGlobalOrSameRouteRetry(aDeliveryObject, resultMap);
                        break;

                    case CUSTOM_ROUTE_RETRY:
                        doCustomRouteRetry(aDeliveryObject, resultMap);
                        break;

                    default:
                        log.error(" sending to dn and client dn queue possibly invalid confiurations smsRetry=" + lSmsRetry + " msgMap=" + aDeliveryObject);
                        SetNextComponent.sendMessageToDeliverySM(aDeliveryObject, lCarrierErrorCode, lReason + "-dnretryF:invalid-smsRetryVal:" + lSmsRetry);
                }
            }
        }
        catch (final Exception e)
        {
            SetNextComponent.sendMessageToDeliverySM(aDeliveryObject, lCarrierErrorCode, lReason + "-dnretryF:se");
            throw e;
        }
        return resultMap;
    }

    private static void doCustomRouteRetry(
            DeliveryObject aDeliveryObject,
            Map<Component, DeliveryObject> aResultMap)
            throws Exception
    {
        final String              lCarrierErrorCode      = "";
        String                    lReason                = CommonUtility.nullCheck(aDeliveryObject.getFailReason(), true);
        final int                 lRetryAttempt          = aDeliveryObject.getRetryAttempt();
        final int                 currentRetry           = (lRetryAttempt != 0) ? lRetryAttempt + 1 : 1;
        String                    lClientId              = aDeliveryObject.getClientId();
        final ItextosClient       lClient                = new ItextosClient(lClientId);

        final Map<String, String> lCustomMsgValidityInfo = RRUtil.getCustomRetryMsgValidity(lClientId);

        if (lCustomMsgValidityInfo != null)
        {
            final int maxRetry        = Integer.parseInt(lCustomMsgValidityInfo.get("retries"));
            final int msgValiditySecs = Integer.parseInt(lCustomMsgValidityInfo.get("msg_validity"));

            aDeliveryObject.setMessageExpiryInSec(msgValiditySecs);
            boolean             isDone                = false;
            Map<String, String> lCustomRetryRouteInfo = null;
            int                 lRetryInterval        = 0;

            while (!isDone)
            {
                lCustomRetryRouteInfo = RRUtil.getCustomRetryRoutes(lClientId, aDeliveryObject.getRouteId(), lCarrierErrorCode.isBlank() ? aDeliveryObject.getCarrierStatusCode() : lCarrierErrorCode,
                        Integer.toString(lRetryInterval));

                if (lCustomRetryRouteInfo == null)
                    lCustomRetryRouteInfo = RRUtil.getCustomRetryRoutes(lClientId, aDeliveryObject.getRouteId(), "NULL", Integer.toString(lRetryInterval));

                if (lCustomRetryRouteInfo != null)
                    isDone = true;
                else
                    if (lClientId.length() == 16)
                        lClientId = lClient.getAdmin();
                    else
                        if (lClientId.length() == 12)
                            lClientId = lClient.getSuperAdmin();
                        else
                            isDone = true;
            }

            if (lCustomRetryRouteInfo != null)
            {
                lRetryInterval = Integer.parseInt(lCustomRetryRouteInfo.get("interval"));
                if (log.isInfoEnabled())
                    log.info(" currentRetry=" + currentRetry + " maxRetry=" + maxRetry);

                if ((currentRetry <= maxRetry) && checkValidityTime(aDeliveryObject, lRetryInterval, msgValiditySecs))
                    doCheckAndSend2Q(aDeliveryObject, lCarrierErrorCode, lCustomRetryRouteInfo.get("route_id"), lReason);
                else
                {
                    // failure validity time/retry exceeded...
                    lReason += lReason + "-dnretryF:" + ((currentRetry <= maxRetry) ? "maxretry" : "validitytime");
                    SetNextComponent.sendMessageToDeliverySM(aDeliveryObject, lCarrierErrorCode, lReason);
                }
            }
            else
                SetNextComponent.sendMessageToDeliverySM(aDeliveryObject, lCarrierErrorCode, lReason + "-dnretryF:c-rnf");
        }
        else
            SetNextComponent.sendMessageToDeliverySM(aDeliveryObject, lCarrierErrorCode, lReason + "-dnretryF:c-nv");
    }

    private static void doGlobalOrSameRouteRetry(
            DeliveryObject aDeliveryObject,
            Map<Component, DeliveryObject> resultMap)
            throws Exception
    {
        final String lCarrierErrorCode = "";
        final int    lSmsRetry         = aDeliveryObject.getSmsRetryEnabled();
        final String lReason           = CommonUtility.nullCheck(aDeliveryObject.getFailReason(), true);
        final int    lRetryAttempt     = aDeliveryObject.getRetryAttempt();
        final int    currentRetry      = (lRetryAttempt != 0) ? lRetryAttempt + 1 : 1;

        if (log.isDebugEnabled())
            log.debug("Retry Attempt :" + currentRetry);

        final int lMaxRetries = RRUtil.getGlobalRetryCount(aDeliveryObject.getRouteType(), aDeliveryObject.getMessagePriority(), aDeliveryObject.getMessageType());

        if (log.isDebugEnabled())
            log.debug(" Max Retries :" + lMaxRetries);

        final int lGlobalRetryInterval = RRUtil.getRetryIntervalInfo(aDeliveryObject.getMessagePriority(), lCarrierErrorCode.isEmpty() ? aDeliveryObject.getCarrierStatusCode() : lCarrierErrorCode,
                String.valueOf(currentRetry), aDeliveryObject.getMessageType());

        if (log.isDebugEnabled())
            log.debug(" retryinterval::" + lGlobalRetryInterval);

        final int lGlobalMsgValidity = getMessageValidity(aDeliveryObject.getMessageType().getKey(), aDeliveryObject.getMessagePriority().getKey());

        if (log.isDebugEnabled())
            log.debug(" GlobalMsgValidity::" + lGlobalMsgValidity);
        if (currentRetry > lMaxRetries)
            SetNextComponent.sendMessageToDeliverySM(aDeliveryObject, lCarrierErrorCode, lReason + "-dnretryF:g-rc");
        else
            if (!checkValidityTime(aDeliveryObject, lGlobalRetryInterval, lGlobalMsgValidity))
                SetNextComponent.sendMessageToDeliverySM(aDeliveryObject, lCarrierErrorCode, lReason + "-dnretryF:g-vt");

            else
            {
                String lRouteId = aDeliveryObject.getRouteId();
                if (lSmsRetry == 1)
                    lRouteId = RRUtil.getGlobalRetryRouteInfo(aDeliveryObject.getMessagePriority(), lRouteId, lCarrierErrorCode.isEmpty() ? aDeliveryObject.getCarrierStatusCode() : lCarrierErrorCode,
                            Integer.toString(currentRetry), aDeliveryObject.getMessageType());

                doCheckAndSend2Q(aDeliveryObject, lCarrierErrorCode, lRouteId, lReason);
            }
    }

    private static boolean checkValidityTime(
            DeliveryObject aDeliveryObject,
            int aRetryInterval,
            int aMsgValiditySecs)
    {
        // retry interval not configured..
        if (aRetryInterval < 0)
            return false;
        if (aRetryInterval == 0)
            aDeliveryObject.setCurrent(true);

        try
        {
            final Date lSTime = aDeliveryObject.getMessageReceivedTime();
            if (log.isDebugEnabled())
                log.debug(" received stime-->" + lSTime + " formatter-->" + DateTimeFormat.DEFAULT);

            final long submitTime        = lSTime.getTime();
            final long elapsedTimeMillis = System.currentTimeMillis() - submitTime;

            if (log.isDebugEnabled())
            {
                log.debug(" elapsedTimeMillis=" + elapsedTimeMillis + " msgValiditySecs=" + aMsgValiditySecs);
                log.debug(" Message validity approved-->" + ((elapsedTimeMillis + (aRetryInterval * 1000)) < (aMsgValiditySecs * 1000)));
                log.debug(" retryInterval-->" + aRetryInterval);
            }

            if ((elapsedTimeMillis + (aRetryInterval * 1000)) < (aMsgValiditySecs * 1000))
            {
                // This property is for stats
                aDeliveryObject.setRetryCurrentTime(new Date());
                final Date lSchedDate = new Date(System.currentTimeMillis() + (aRetryInterval * 1000));
                if (log.isDebugEnabled())
                    log.debug(" scheduled=true schedTime-->" + lSchedDate);
                aDeliveryObject.setRetryTime(lSchedDate);
                aDeliveryObject.setRetryInterval(aRetryInterval);
                return true;
            }
            if (log.isDebugEnabled())
                log.debug(" scheduled=false");
        }
        catch (final Exception exp)
        {
            log.error(" problem finding validity time", exp);
        }

        return false;
    }

    private static void doCheckAndSend2Q(
            DeliveryObject aDeliveryObject,
            String aCarrierErrorCode,
            String aRouteId,
            String aReason)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug(" new route found ::" + aRouteId);

        // send to retry with delay queue
        if ((aRouteId != null) && (aRouteId.trim().length() != 0))
        {
            if (log.isInfoEnabled())
                log.info(" sending to retry with delayQ..found route=" + aRouteId);
            SetNextComponent.sendMessageToRetryWithDelayQ(aDeliveryObject, aCarrierErrorCode, null, aRouteId);
        }
        else
        {
            if (log.isInfoEnabled())
                log.info(" sending final dn no valid route found..empty route=" + aRouteId);
            SetNextComponent.sendMessageToDeliverySM(aDeliveryObject, aCarrierErrorCode, aReason + "-dnretryF:er");
        }
        // }
    }

    public static int getMessageValidity(
            String aMsgType,
            String aMsgPriority)
    {
        final CommonMsgValidity lMsgValidity = (CommonMsgValidity) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.COMMON_MSG_VALIDITY);
        return lMsgValidity.getMessageValidity(aMsgType, aMsgPriority);
    }

}
