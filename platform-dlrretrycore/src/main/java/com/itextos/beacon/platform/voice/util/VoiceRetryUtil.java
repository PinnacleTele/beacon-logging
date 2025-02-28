package com.itextos.beacon.platform.voice.util;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.httpclient.BasicHttpConnector;
import com.itextos.beacon.commonlib.httpclient.HttpResult;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.rr.util.RRUtil;
import com.itextos.beacon.platform.dlrretry.util.SetNextComponent;
import com.itextos.beacon.platform.dnpayloadutil.AgingDnStatus;
import com.itextos.beacon.platform.voice.redis.VoiceRedis;

public class VoiceRetryUtil
{

    private static final Log log           = LogFactory.getLog(VoiceRetryUtil.class);

    TTSExtraction            mTTSExtractor = new TTSExtraction();

    public Map<Component, DeliveryObject> doVoiceHandover(
            DeliveryObject aDeliveryObject)
            throws ItextosException
    {
        final Map<Component, DeliveryObject> lNextComponentMap = new HashMap<>();

        try
        {
            final String lMNumber    = aDeliveryObject.getMobileNumber();
            final Date   lSDate      = aDeliveryObject.getMessageReceivedDate();
            final String lClientId   = aDeliveryObject.getClientId();
            final String lMessageId  = aDeliveryObject.getMessageId();
            final String lVoiceCfgId = aDeliveryObject.getVoiceConfigId();
            final String lMessage    = aDeliveryObject.getMessage();
            String       lLongMsg    = aDeliveryObject.getLongMessage();

            if (log.isDebugEnabled())
                log.debug("  Long Message - " + lLongMsg);

            // final String dnComeFrom = (String)
            // aNunMessage.getValue(MiddlewareConstant.DN_COME_FROM);

            if ((lLongMsg == null) || lLongMsg.isEmpty())
                lLongMsg = lMessage;

            final String lKey             = CommonUtility.combine(lClientId, lVoiceCfgId);

            boolean      isMTInsertStatus = true;

            if (log.isDebugEnabled())
                log.debug(" Voice MT request:: ClientId :" + lClientId + " | Dest :" + lMNumber + " | mid:" + lMessageId + " | Voice Config Id:" + lVoiceCfgId + " | sdate:" + lSDate + " |  | msg:"
                        + lMessage + " | fullMsg:" + lLongMsg);

            if (!checkAgingDNStatus(lMNumber, lSDate, lClientId, lMessageId))
            {
                if (log.isDebugEnabled())
                    log.debug("  Received BaseMessage voice Dn -" + aDeliveryObject.toString());

                final Map<String, Object> lVoiceAccMap = (Map<String, Object>) RRUtil.getVoiceAccInfo(lKey);

                if (log.isDebugEnabled())
                    log.debug(" Voice Account Mapping -" + lVoiceAccMap);

                if ((lVoiceAccMap != null) && !lVoiceAccMap.isEmpty())
                {
                    final List<Object> lTemplateList = RRUtil.getVoiceTemplateInfo(lKey);

                    if (log.isDebugEnabled())
                        log.debug(" Voice Template List - " + lTemplateList);

                    if ((lTemplateList != null) && !lTemplateList.isEmpty())
                    {
                        final String              lVoiceTemplateUrl = lVoiceAccMap.get(VoiceConstants.VOICE_URLTEMPLATE).toString();
                        final String              lOTT              = lVoiceAccMap.get(VoiceConstants.VOICE_OTT).toString();

                        String                    lTTSInfo          = null;
                        final Map<String, Object> lMatchedTemplate  = this.mTTSExtractor.getMatchingTemplate(lLongMsg, lTemplateList);

                        if (log.isDebugEnabled())
                            log.debug(" Voice Matched Template - " + lMatchedTemplate);

                        if ((lMatchedTemplate != null) && !lMatchedTemplate.isEmpty())
                        {

                            if ((lOTT != null) && lOTT.equals("1"))
                            {
                                lTTSInfo = extractTTS(lMessage, lMatchedTemplate);
                                if (log.isDebugEnabled())
                                    log.debug(" OTT =1 the ttsInfo -" + lTTSInfo);
                            }
                            else
                            {
                                lTTSInfo = lLongMsg;
                                if (log.isDebugEnabled())
                                    log.debug(" OTT !=1 the ttsInfo -" + lTTSInfo);
                            }

                            final Object[] lVoiceDlrParams = VUtil.generateDNParams(aDeliveryObject);

                            final String   lDlrUrl         = (String) lVoiceAccMap.get(VoiceConstants.VOICE_DN_URL_TEMPLATE);

                            if (log.isDebugEnabled())
                                log.debug("Voice DN Url - " + lDlrUrl);

                            final String lFormattedDnUrl = MessageFormat.format(lDlrUrl, lVoiceDlrParams);

                            log.info(" Formatted Voice DN Url : " + lFormattedDnUrl);

                            if ((lTTSInfo != null) && (lTTSInfo.trim().length() != 0))
                            {
                                final String[] lResult      = VUtil.voiceMTRequestParams(lVoiceAccMap, lMNumber, lTTSInfo, lMatchedTemplate.get(VoiceConstants.VOICE_CAMPAIGNID).toString(),
                                        lFormattedDnUrl);

                                final String   formattedUrl = VUtil.applyParams(lVoiceTemplateUrl, lResult);

                                if (log.isDebugEnabled())
                                    log.debug(" Formatted Voice MT Url : " + formattedUrl);

                                // Step:3 :: Insert the Voice MT request to Redis.

                                isMTInsertStatus = voiceMTRequestToRedis(lMessageId, lMNumber, lClientId, lSDate, aDeliveryObject.getJsonString());
                                if (log.isDebugEnabled())
                                    log.debug(" doProcessorMessage() - Voice MT redis insertion status - " + isMTInsertStatus);
                                HttpResult lHttpResult = null;

                                if (!isMTInsertStatus)
                                {
                                    // Step:4 :: Handover the Voice MT request to Voice Plat From
                                    lHttpResult = voiceMTHandover(formattedUrl, 0);

                                    if (log.isDebugEnabled())
                                        log.debug(" Voice MT Handover status : " + lHttpResult);

                                    if (lHttpResult != null)
                                    {
                                        JSONObject       jsonObject = new JSONObject();
                                        final JSONParser parser     = new JSONParser();

                                        if (lHttpResult != null)
                                            jsonObject = (JSONObject) parser.parse(lHttpResult.getResponseString());

                                        if (jsonObject.get("status").toString().trim().equalsIgnoreCase("success"))
                                        {
                                            // Step:2 :: The boolean value is 'true' then send the mapMsg to
                                            // INSERT_AGING_DN_QUEUE
                                            final boolean findFastOrAgeDnScheduleStatus = AgingDnStatus.findAgeDnScheduleTime(aDeliveryObject);

                                            if (log.isDebugEnabled())
                                                log.debug(" findAgeDnScheduleStatus:" + findFastOrAgeDnScheduleStatus + " ::Message Id:" + lMessageId);

                                            if (findFastOrAgeDnScheduleStatus)
                                                lNextComponentMap.put(Component.AGIN, aDeliveryObject);

                                            // Step:5 :: After handover send the request to INTERM_FAILUER_QUEUE
                                            if (log.isDebugEnabled())
                                                log.debug("Voice MT handover response success.. going to push to INTERM_FAILUER_QUEUE..");

                                            sendtoIntrmFailuerQueue(aDeliveryObject, lNextComponentMap);
                                        }
                                        else
                                        {
                                            log.error(" failure response from voice server=" + lHttpResult.getResponseString());

                                            if (log.isDebugEnabled())
                                                log.warn("Voice MT handover response failed. Request send to DN_INTERNAL_QUEUE.");

                                            sendtoDNInternalQueue(aDeliveryObject, lNextComponentMap);
                                        }
                                    }

                                    if (lHttpResult == null)
                                    {
                                        if (log.isDebugEnabled())
                                            log.debug(" Voice MT handover response failed.. going to delete the record form Redis..");

                                        final boolean isDelRecordStatus = deleteVoiceDnFromRedis(lMNumber, lMessageId, lSDate, lClientId);
                                        if (log.isDebugEnabled())
                                            log.debug(" Voice MT record delete status in redis - " + isDelRecordStatus);

                                        if (isDelRecordStatus)
                                        {
                                            if (log.isDebugEnabled())
                                                log.warn(" Voice MT handover response failed. Request send to DN_INTERNAL_QUEUE.");

                                            sendtoDNInternalQueue(aDeliveryObject, lNextComponentMap);
                                        }
                                    }
                                }
                            }
                            else
                            {
                                log.warn(" Voice OTP Extraction failed for the Message : ClientId -" + lClientId + " : msg:" + lMessage + " : fullmsg:" + lLongMsg
                                        + " , request send to DN_INTERNAL_QUEUE");

                                sendtoDNInternalQueue(aDeliveryObject, lNextComponentMap);
                            }
                        }
                        else
                        {
                            // voice template not Matched process to aging_dn_processer_q
                            log.warn("Voice template not matched for ClientId :" + lClientId + " : msg:" + lMessage + " : fullmsg:" + lLongMsg + " , request send to DN_INTERNAL_QUEUE");

                            sendtoDNInternalQueue(aDeliveryObject, lNextComponentMap);
                        }
                    }
                    else
                    {
                        // voice template not configured process to aging_dn_processer_q
                        log.warn(" Voice template not configured for ClientId :" + lClientId + " : msg:" + lMessage + " : fullmsg:" + lLongMsg + " , request send to DN_INTERNAL_QUEUE");

                        sendtoDNInternalQueue(aDeliveryObject, lNextComponentMap);
                    }
                }
                else
                {
                    // voice account not configured process to aging_dn_processer_q
                    log.warn(" Voice account not configured for ClientId :" + lClientId + " , request send to DN_INTERNAL_QUEUE");

                    sendtoDNInternalQueue(aDeliveryObject, lNextComponentMap);
                }
            }
            else
                log.info(" Success DN already processed. Hence record to be ignored.." + lClientId + ": Message Id=" + lMessageId + ":Retry Attempt:" + aDeliveryObject.getRetryAttempt());
        }
        catch (final Exception e)
        {
            log.error(" Exception occer while processing Voice MT request..", e);

            sendtoDNInternalQueue(aDeliveryObject, lNextComponentMap);
        }
        return lNextComponentMap;
    }

    private String extractTTS(
            String aMessage,
            Map<String, Object> aMatchedTemplate)
            throws Exception
    {
        String lTTSInfo = null;

        try
        {
            final String[] sTtsOrder = aMatchedTemplate.get(VoiceConstants.VOICE_TTS_ORDER).toString().split(",");
            final int[]    lTTSOrder = new int[sTtsOrder.length];

            for (int i = 0; i < lTTSOrder.length; i++)
                lTTSOrder[i] = Integer.parseInt(sTtsOrder[i]);

            final List<Object> ttsList = this.mTTSExtractor.extractOTP((String) aMatchedTemplate.get(VoiceConstants.VOICE_MSG_TEMPLATE), aMessage,
                    aMatchedTemplate.get(VoiceConstants.VOICE_TTS_CLEAN).toString().split(","), Integer.parseInt(aMatchedTemplate.get(VoiceConstants.VOICE_TTS_COUNT).toString()), lTTSOrder,
                    aMatchedTemplate.get(VoiceConstants.VOICE_DATATYPE).toString().split(","), (String) aMatchedTemplate.get(VoiceConstants.VOICE_DATEFORMAT));

            for (final Object lTTS : ttsList)
                if (lTTSInfo == null)
                {
                    if (lTTS != null)
                        lTTSInfo = lTTS.toString();
                }
                else
                    if (lTTS != null)
                        lTTSInfo = lTTSInfo + "-$-" + lTTS;
        }
        catch (final Exception exp)
        {
            log.error(" TTS extraction failed due to...", exp);
        }
        return lTTSInfo;
    }

    private HttpResult voiceMTHandover(
            String aFormattedUrl,
            int aRetryCount)
    {
        final String lMaxRetryCnt = getAppConfigValueAsString(ConfigParamConstants.VOICE_MAX_RETRY_ATTEMPT);

        if (log.isDebugEnabled())
            log.debug(" Max Voice Retry attempt count - " + lMaxRetryCnt);

        // String lResponse = null;
        boolean    isDone      = false;
        HttpResult lHttpResult = null;

        while (!isDone)
        {

            if (aRetryCount > Integer.parseInt(lMaxRetryCnt))
            {
                if (log.isDebugEnabled())
                    log.debug(" Max retry reached to Voice MT Handover.." + aRetryCount);
                isDone = true;
                return lHttpResult;
            }

            try
            {
                if (log.isDebugEnabled())
                    log.debug("Handover to Voice Platfrom .....");
                lHttpResult = BasicHttpConnector.connect(aFormattedUrl);

                if (log.isDebugEnabled())
                    log.debug("Voice MT Handover Responser - " + lHttpResult.isSuccess());

                isDone = true;
            }
            catch (final Exception e)
            {
                log.error("Exception occer while Voice MT Handover ..", e);

                log.info(" Exception occer while Voice MT Handover... Going for retry.. Retry Count -" + aRetryCount);
                aRetryCount++;
                gotoSleep();
                isDone = false;
            }
        }

        return lHttpResult;
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    private boolean checkAgingDNStatus(
            String aMNumber,
            Date aSDate,
            String aCLientId,
            String aMid)
    {
        boolean isSuccessAgingDN = false;
        boolean isDone           = false;
        while (!isDone)
            try
            {
                isSuccessAgingDN = VoiceRedis.isAgingDnExists(aMNumber, aSDate, aCLientId, aMid);

                if (log.isDebugEnabled())
                    log.debug("Redis Success Aging DN Status - " + isSuccessAgingDN);
                isDone = true;
            }
            catch (final Exception e)
            {
                log.error(" Exception occer while checking Aging Success DN status in Redis - ", e);
                gotoSleep();
                isDone = false;
            }
        return isSuccessAgingDN;
    }

    private void gotoSleep()
    {

        try
        {
            Thread.sleep(1000L);
        }
        catch (final InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private boolean deleteVoiceDnFromRedis(
            String aMNumber,
            String aMid,
            Date aSDate,
            String aClientId)
    {
        boolean isDelRecordStatus = false;
        boolean isDone            = false;
        while (!isDone)
            try
            {
                isDelRecordStatus = VoiceRedis.deleteVoiceDnFromRedis(aMNumber, aMid, aSDate, aClientId);
                if (log.isDebugEnabled())
                    log.debug(" Redis delete Status - " + isDelRecordStatus);
                isDone = true;
            }
            catch (final Exception e)
            {
                log.error(" Exception occer while deleting Voice MT to Redis - ", e);
                gotoSleep();
                isDone = false;
            }
        return isDelRecordStatus;
    }

    private static void sendtoDNInternalQueue(
            DeliveryObject aDeliveryObject,
            Map<Component, DeliveryObject> aNextComponentMap)
    {

        try
        {
            String lErrorCode = getAppConfigValueAsString(ConfigParamConstants.VOICE_FAILUER_ERROR_CODE);

            if (log.isDebugEnabled())
                log.debug("Voice MT handover Error code :" + lErrorCode);

            if ((lErrorCode == null) || lErrorCode.isEmpty())
                lErrorCode = PlatformStatusCode.DEFAULT_AGING_ERROR_CODE.getStatusCode();

            aDeliveryObject.setDnOrigianlstatusCode(PlatformStatusCode.getStatusDesc(lErrorCode).getStatusCode());
            aDeliveryObject.setIndicateDnFinal(1);
            aDeliveryObject.setDlrFromInternal(Component.VOICE_PROCESS.getKey());
            aNextComponentMap.put(Component.DLRINTLP, aDeliveryObject);
        }
        catch (final Exception e)
        {
            SetNextComponent.sendToErrorLog(aDeliveryObject, e);
            log.error(" Unable to send to sendToErrorLog ..", e);
        }
    }

    private boolean voiceMTRequestToRedis(
            String aMid,
            String aMNumber,
            String aClientId,
            Date aSdate,
            String aJsonNumMessage)
    {
        boolean isRedisInsertSuccess = false;
        boolean isDone               = false;
        while (!isDone)
            try
            {
                isRedisInsertSuccess = VoiceRedis.processVoiceRequest(aMid, aMNumber, aClientId, aSdate, aJsonNumMessage);
                if (log.isDebugEnabled())
                    log.debug(" Redis Insert Status - " + isRedisInsertSuccess);
                isDone = true;
            }
            catch (final Exception e)
            {
                log.error(" Exception occer while insert Voice MT to Redis - ", e);
                gotoSleep();
                isDone = false;
            }
        return isRedisInsertSuccess;
    }

    private static void sendtoIntrmFailuerQueue(
            DeliveryObject aDeliveryObject,
            Map<Component, DeliveryObject> aNextComponentMap)
            throws Exception
    {
        // final ErrorCodeInfo lErrorCodeInfo =
        // DlrErrorUtil.getErrorCodeInfo(aNunMessage.getValue(MiddlewareConstant.MW_DN_ORI_STATUS_CODE));
        // aNunMessage.putValue(MiddlewareConstant.MW_DN_ORI_STATUS_DESC,
        // ((lErrorCodeInfo == null) || (lErrorCodeInfo.getStatusFlag() == null)) ?
        // "FAILED" : lErrorCodeInfo.getStatusFlag());
        aNextComponentMap.put(Component.T2DB_INTERIM_FAILUERS, aDeliveryObject);
    }

}
