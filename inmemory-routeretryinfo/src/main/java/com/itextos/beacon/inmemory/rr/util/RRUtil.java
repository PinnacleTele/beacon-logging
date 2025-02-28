package com.itextos.beacon.inmemory.rr.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.rr.cache.LoadAlertWaitConfig;
import com.itextos.beacon.inmemory.rr.cache.LoadCustomRetryMsgValidity;
import com.itextos.beacon.inmemory.rr.cache.LoadCustomRetryRoutes;
import com.itextos.beacon.inmemory.rr.cache.LoadGlobalRetryInfo;
import com.itextos.beacon.inmemory.rr.cache.LoadGlobalRetryInterval;
import com.itextos.beacon.inmemory.rr.cache.LoadGlobalRetryRoutes;
import com.itextos.beacon.inmemory.rr.cache.LoadVoiceAccInfo;
import com.itextos.beacon.inmemory.rr.cache.LoadVoiceMessageTemplate;

public class RRUtil
{

    private RRUtil()
    {}

    private static final Log log = LogFactory.getLog(RRUtil.class);

    public static Map<String, String> getAlertWaitConfig(
            String aPriority,
            String aMsgType,
            String aRetryAttempt,
            String aClientId)
    {
        final LoadAlertWaitConfig lAlertWaitConfig = (LoadAlertWaitConfig) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ALERT_WAIT_CONFIG);
        return lAlertWaitConfig.getAlertWaitConfig(aPriority, aMsgType, aRetryAttempt, aClientId);
    }

    public static Map<String, String> getCustomRetryMsgValidity(
            String aClientId)
    {
        final LoadCustomRetryMsgValidity lRetryMsgValidity = (LoadCustomRetryMsgValidity) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.RETRY_MSG_VALIDITY);
        return lRetryMsgValidity.getCustomRetryMsgValidity(aClientId);
    }

    public static Map<String, String> getCustomRetryRoutes(
            String aClientId,
            String aOriginalRouteId,
            String aErrorCode,
            String aRetryAttempt)
    {
        final LoadCustomRetryRoutes lRetryRoutes = (LoadCustomRetryRoutes) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_RETRY_ROUTES);
        return lRetryRoutes.getCustomRetryRoutes(aClientId, aOriginalRouteId, aErrorCode, aRetryAttempt);
    }

    private static Map<String, String> getGlobalRetry(
            String aRouteType,
            String aPriority)
    {
        final LoadGlobalRetryInfo lGlobalRetryInfo = (LoadGlobalRetryInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.GLOBAL_RETRY_INFO);
        return lGlobalRetryInfo.getGlobalRetry(aRouteType, aPriority);
    }

    public static int getGlobalRetryCount(
            String aRouteType,
            MessagePriority aPriority,
            MessageType aMsgType)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Finding retries for routeType :" + aRouteType + " priority :" + aPriority + " msgType:" + aMsgType);

            final Map<String, String> lGlobalRetrysInfo = getGlobalRetry(aRouteType, aPriority.getKey());

            if (log.isDebugEnabled())
                log.debug("Found retries GlobalRetrysInfo " + lGlobalRetrysInfo);

            if ((lGlobalRetrysInfo == null) || lGlobalRetrysInfo.isEmpty())
                return -1;

            if (aMsgType == MessageType.TRANSACTIONAL)
                return Integer.parseInt(lGlobalRetrysInfo.get("txn_retries"));
            else
                if (aMsgType == MessageType.PROMOTIONAL)
                    return Integer.parseInt(lGlobalRetrysInfo.get("promo_retries"));

            return -1;
        }
        catch (final Exception exp)
        {
            log.error("Problem getting configured retry..", exp);
            throw exp;
        }
    }

    private static Map<String, String> getGlobalRetryRoutes(
            String aPriority,
            String aOriginalRouteId,
            String aErrorCode,
            String aRetryAttempt)
    {
        final LoadGlobalRetryRoutes lGlobalRetryRoutes = (LoadGlobalRetryRoutes) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.GLOBAL_RETRY_ROUTES);
        return lGlobalRetryRoutes.getGlobalRetryRouteInfo(aPriority, aOriginalRouteId, aErrorCode, aRetryAttempt);
    }

    public static String getGlobalRetryRouteInfo(
            MessagePriority aPriority,
            String aOriginalRouteId,
            String aErrorCode,
            String aRetryAttempt,
            MessageType aMsgType)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("finding retry routes for priority=" + aPriority + " OriginalRoute=" + aOriginalRouteId + " ErrorCode=" + aErrorCode + " RetryAttempt=" + aRetryAttempt + " MessageType="
                        + aMsgType);

            final Map<String, String> lRetryRouteInfo = getGlobalRetryRoutes(aPriority.getKey(), aOriginalRouteId, aErrorCode, aRetryAttempt);

            if ((lRetryRouteInfo == null) || lRetryRouteInfo.isEmpty())
                return null;

            if (log.isDebugEnabled())
                log.debug("map value for common retry..." + lRetryRouteInfo);

            if (aMsgType == MessageType.TRANSACTIONAL)
                return lRetryRouteInfo.get("txn_retry_route");
            else
                if (aMsgType == MessageType.PROMOTIONAL)
                    return lRetryRouteInfo.get("promo_retry_route");

            return null;
        }
        catch (final Exception exp)
        {
            log.error("problem finding retry route...", exp);
            throw exp;
        }
    }

    private static Map<String, String> getGlobalRetryInterval(
            String aPriority,
            String aErrorCode,
            String aRetryAttempt)
    {
        final LoadGlobalRetryInterval lGlobalRetryInterval = (LoadGlobalRetryInterval) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.GLOBAL_RETRY_INTERVAL);
        return lGlobalRetryInterval.getGlobalRetryInterval(aPriority, aErrorCode, aRetryAttempt);
    }

    public static int getRetryIntervalInfo(
            MessagePriority aPriority,
            String aErrorCode,
            String aRetryAttempt,
            MessageType aMsgType)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("checking global retry interval priority:" + aPriority + " Errorcode:" + aErrorCode + " RetryAttempt:" + aRetryAttempt + " MessageType=" + aMsgType);

            final Map<String, String> lGlobalRetryIntervalInfo = getGlobalRetryInterval(aPriority.getKey(), aErrorCode, aRetryAttempt);

            if (log.isDebugEnabled())
                log.debug("retry interval globalRetry=" + lGlobalRetryIntervalInfo);

            if (lGlobalRetryIntervalInfo == null)
                return -1;

            if (aMsgType == MessageType.TRANSACTIONAL)
                return Integer.parseInt(lGlobalRetryIntervalInfo.get("txn_interval"));
            else
                if (aMsgType == MessageType.PROMOTIONAL)
                    return Integer.parseInt(lGlobalRetryIntervalInfo.get("promo_interval"));

            return -1;
        }
        catch (final Exception exp)
        {
            log.error("problem processing globalretryinterval...", exp);
            throw exp;
        }
    }

    public static Object getVoiceAccInfo(
            String aKey)
    {
        final LoadVoiceAccInfo lVoiceAccInfo = (LoadVoiceAccInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.VOICE_ACC_INFO);
        return lVoiceAccInfo.getVoiceAccountInfo().get(aKey);
    }

    public static List<Object> getVoiceTemplateInfo(
            String aKey)
    {
        final LoadVoiceMessageTemplate lVoiceTemplateInfo = (LoadVoiceMessageTemplate) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.VOICE_TEMPLATE_INFO);
        return lVoiceTemplateInfo.getVoiceTemplateInfo(aKey);
    }

}
