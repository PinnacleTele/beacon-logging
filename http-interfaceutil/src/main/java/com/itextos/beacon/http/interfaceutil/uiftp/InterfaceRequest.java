package com.itextos.beacon.http.interfaceutil.uiftp;

import java.util.Date;
import java.util.EnumMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.MessagePart;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.mobilevalidation.MobileNumberValidator;
import com.itextos.beacon.http.interfaceutil.InterfaceUtil;

public class InterfaceRequest
{

    private static final Log log = LogFactory.getLog(InterfaceRequest.class);

    private InterfaceRequest()
    {}

    public static void sendToKafka(
            EnumMap<InterfaceConstant, String> aRequest,
            String aAccJson,StringBuffer sb)
            throws Exception
    {
        InterfaceStatusCode lInterfaceStatusCode = InterfaceStatusCode.SUCCESS;

        try
        {
            final JSONObject lAccountInfo = parseJSON(aAccJson);

            ClusterType      lClusterType = ClusterType.getCluster(CommonUtility.nullCheck(lAccountInfo.get(MiddlewareConstant.MW_PLATFORM_CLUSTER.getName())).toLowerCase());

            if (log.isDebugEnabled())
                log.debug("Platform Cluster Received from Account :" + lClusterType);

            if (lClusterType == ClusterType.OTP)
            {
                log.fatal("UI Request received with Cluster '" + lClusterType + "', Hence rejected the Message");

                lInterfaceStatusCode = InterfaceStatusCode.ACCESS_VIOLATION;

                throw new ItextosException("Unable to Process OTP Request from UI");
            }

            if (lClusterType == null)
                lClusterType = ClusterType.BULK;

            final String                lMobileNumber                     = aRequest.get(InterfaceConstant.MOBILE_NUMBER);

            final String                lCountryCD                        = InterfaceUtil.getDefaultCountryCode();
            final boolean               isConsiderDefaultLengthAsDomestic = CommonUtility
                    .isEnabled(CommonUtility.nullCheck(lAccountInfo.get(MiddlewareConstant.MW_CONSIDER_DEFAULTLENGTH_AS_DOMESTIC.getName()), true));

            final boolean               isDomesticSpecialSeriesAllow      = CommonUtility
                    .isEnabled(CommonUtility.nullCheck(lAccountInfo.get(MiddlewareConstant.MW_DOMESTIC_SPECIAL_SERIES_ALLOW.getName()), true));

            final boolean               isIntlServiceAllow                = InterfaceUtil.isIntlServiceAllow(lAccountInfo);

            final MobileNumberValidator lMobileValidator                  = InterfaceUtil.validateMobile(lMobileNumber, lCountryCD, isIntlServiceAllow, isConsiderDefaultLengthAsDomestic, false, "",
                    isDomesticSpecialSeriesAllow);

            if (log.isDebugEnabled())
                log.debug("Account Json String:" + lAccountInfo.toJSONString());

            final MessageType lMsgType     = MessageType.getMessageType(lAccountInfo.get(MiddlewareConstant.MW_MSG_TYPE.getName()).toString());
            MessagePriority   lSmsPriority = MessagePriority.getMessagePriority((String) lAccountInfo.get(MiddlewareConstant.MW_SMS_PRIORITY.getName()));

            RouteType         lRouteType   = RouteType.DOMESTIC;

            if (lInterfaceStatusCode == InterfaceStatusCode.SUCCESS)
            {

                if (!lMobileValidator.isValidMobileNumber())
                {
                    lRouteType           = RouteType.DOMESTIC;
                    lInterfaceStatusCode = InterfaceStatusCode.DESTINATION_INVALID;
                }

                if (lMobileValidator.isIntlMobileNumber())
                {
                    if (log.isDebugEnabled())
                        log.debug("It is International Number : " + lMobileValidator.getMobileNumber());

                    lRouteType = RouteType.INTERNATIONAL;

                    // Reject as INTL Serivce not available.
                    if (!isIntlServiceAllow)
                        lInterfaceStatusCode = InterfaceStatusCode.INTL_SERVICE_DISABLED;
                }
            }

            final MessagePriority lPriority = MessagePriority.getMessagePriority(CommonUtility.nullCheck(aRequest.get(InterfaceConstant.PRIORITY), true));

            if (lPriority != null)
                lSmsPriority = lPriority;

            final MessageRequest lMessageRequest = new MessageRequest(lClusterType, InterfaceType.GUI, InterfaceGroup.UI, lMsgType, lSmsPriority, lRouteType, lAccountInfo.toJSONString());
            lMessageRequest.setAppInstanceId(aRequest.get(InterfaceConstant.APP_INSTANCE_ID));
            lMessageRequest.setClientId(aRequest.get(InterfaceConstant.CLIENT_ID));
            lMessageRequest.setLongMessage(aRequest.get(InterfaceConstant.MESSAGE));
            lMessageRequest.setFileId(aRequest.get(InterfaceConstant.FILE_ID));
            lMessageRequest.setBaseMessageId(aRequest.get(InterfaceConstant.BASE_MESSAGE_ID));
            lMessageRequest.setClientSourceIp(aRequest.get(InterfaceConstant.CLIENT_IP));
            lMessageRequest.setMobileNumber(lMobileValidator.getMobileNumber());
            lMessageRequest.setHeader(aRequest.get(InterfaceConstant.HEADER));
            lMessageRequest.setClientMessageId(aRequest.get(InterfaceConstant.CLIENT_MESSAGE_ID));
            lMessageRequest.setDltEntityId(aRequest.get(InterfaceConstant.DLT_ENTITY_ID));
            lMessageRequest.setDltTemplateId(aRequest.get(InterfaceConstant.DLT_TEMPLATE_ID));
            lMessageRequest.setDltTelemarketerId(aRequest.get(InterfaceConstant.DLT_TMA_ID));

            lMessageRequest.setDlrRequestFromClient(CommonUtility.isEnabled(aRequest.get(InterfaceConstant.DLR_REQURIED)));
            lMessageRequest.setIsHexMessage(CommonUtility.isTrue(aRequest.get(InterfaceConstant.IS_HEX_MSG)));
            lMessageRequest.setMessageClass(aRequest.get(InterfaceConstant.MESSAGE_CLASS));
            lMessageRequest.setMessageTag(aRequest.get(InterfaceConstant.MESSAGE_TAG));
            lMessageRequest.setDupCheckForUI(CommonUtility.getInteger(aRequest.get(InterfaceConstant.UI_DUP_CHECK_ENABLE)));
            lMessageRequest.setCampaignId(aRequest.get(InterfaceConstant.CAMPAIGN_ID));
            lMessageRequest.setCampaignName(aRequest.get(InterfaceConstant.CAMPAIGN_NAME));
            lMessageRequest.setVlShortnerFromUI(CommonUtility.getInteger(aRequest.get(InterfaceConstant.UI_VL_SHORT_REQ)));
            lMessageRequest.setMsgTag1(aRequest.get(InterfaceConstant.MSG_TAG1));
            lMessageRequest.setMsgTag2(aRequest.get(InterfaceConstant.MSG_TAG2));
            lMessageRequest.setMsgTag3(aRequest.get(InterfaceConstant.MSG_TAG3));
            lMessageRequest.setMsgTag4(aRequest.get(InterfaceConstant.MSG_TAG4));
            lMessageRequest.setMsgTag5(aRequest.get(InterfaceConstant.MSG_TAG5));

            lMessageRequest.setDcs(-1);
            lMessageRequest.setMsgSource(InterfaceType.GUI.getKey());

            setMaxValidatyTime(lMessageRequest, aRequest.get(InterfaceConstant.MAX_MESSAGE_VALIDITY_SEC));

            if (lInterfaceStatusCode == InterfaceStatusCode.SUCCESS)
            {
                if (lMobileValidator.isSpecialSeriesNumber())
                    lMessageRequest.setTreatDomesticAsSpecialSeries(lMobileValidator.isSpecialSeriesNumber());

                lInterfaceStatusCode = validateMesssage(lMessageRequest);
            }

            InterfaceUtil.setMessageStatus(lMessageRequest, lInterfaceStatusCode, lMobileNumber);

            if ((lRouteType != null) && (lRouteType == RouteType.INTERNATIONAL))
            {
                lMessageRequest.setCountry("");
                lMessageRequest.setHeader(aRequest.get(InterfaceConstant.INTL_HEADER));
            }
            else
                lMessageRequest.setCountry(InterfaceUtil.getCountry());

            final MessagePart lMessagePart  = new MessagePart(aRequest.get(InterfaceConstant.BASE_MESSAGE_ID));

            final Date        lReceivedTime = new Date();

            if (log.isDebugEnabled())
                log.debug("Received Time : " + lReceivedTime);

            lMessagePart.setMessage(aRequest.get(InterfaceConstant.MESSAGE));
            lMessagePart.setMessageReceivedTime(lReceivedTime);
            lMessagePart.setMessageReceivedDate(lReceivedTime);
            lMessagePart.setMessageActualReceivedDate(lReceivedTime);
            lMessagePart.setMessageActualReceivedTime(lReceivedTime);
            lMessageRequest.addMessagePart(lMessagePart);

            InterfaceUtil.sendToKafka(lMessageRequest,sb);
        }
        catch (final Exception e)
        {

            if (e instanceof ItextosException)
            {
                log.fatal(e.getMessage());

                throw e;
            }

            log.debug("Unable to process the request to Kafka...............", e);
        }
    }

    private static void setMaxValidatyTime(
            MessageRequest aMessageRequest,
            String aMsgExpiry)
    {
        if ((aMsgExpiry != null) && !aMsgExpiry.isBlank())
            try
            {
                final int lIntExpiry = CommonUtility.getInteger(aMsgExpiry) * 60;
                aMessageRequest.setMaxValidityInSec(lIntExpiry);
            }
            catch (final Exception e)
            {
                log.error("Exception occer while converting the Message Expiry..", e);
            }
    }

    private static JSONObject parseJSON(
            String aJsonString)
    {
        JSONObject lJsonObject = new JSONObject();

        try
        {
            lJsonObject = (JSONObject) new JSONParser().parse(aJsonString);
        }
        catch (final ParseException e)
        {
            log.debug("Invalid Json reqiest " + aJsonString + e);
        }
        return lJsonObject;
    }

    private static InterfaceStatusCode validateMesssage(
            MessageRequest aMessageRequest)
    {
        if (CommonUtility.nullCheck(aMessageRequest.getLongMessage(), true).isEmpty())
            return InterfaceStatusCode.MESSAGE_EMPTY;

        return InterfaceStatusCode.SUCCESS;
    }

}