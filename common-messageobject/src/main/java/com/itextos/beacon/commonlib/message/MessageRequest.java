package com.itextos.beacon.commonlib.message;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.errorlog.SMSLog;

public class MessageRequest
        extends
        BaseMessage
{

    private static final long       serialVersionUID   = 7823937163094202459L;
    
    private static final Log log = LogFactory.getLog(MessageRequest.class);


    private Date                    mFirstReceivedTime = null;
    private Date                    mLastReceivedTime  = null;
    private final List<MessagePart> mMessageParts      = new ArrayList<>();

    public MessageRequest(
            ClusterType aClusterType,
            InterfaceType aInterfaceType,
            InterfaceGroup aInterfaceGroup,
            MessageType aMessageType,
            MessagePriority aMessagePriority,
            RouteType aIsInt) throws ItextosRuntimeException
    {
        this(aClusterType, aInterfaceType, aInterfaceGroup, aMessageType, aMessagePriority, aIsInt, null);
    }

    public MessageRequest(
            ClusterType aClusterType,
            InterfaceType aInterfaceType,
            InterfaceGroup aInterfaceGroup,
            MessageType aMessageType,
            MessagePriority aMessagePriority,
            RouteType aRouteType,
            String aAccountJsonString) throws ItextosRuntimeException
    {
        super(aClusterType, aInterfaceType, aInterfaceGroup, aMessageType, aMessagePriority, aRouteType, "MessageRequest", aAccountJsonString);
    }

    public MessageRequest(
            String aCompleteJsonString)
            throws Exception
    {
        super(aCompleteJsonString, "MessageRequest");
        populateChildren(aCompleteJsonString);
    }

    public void addMessagePart(
            MessagePart aMessageObject)
    {
        if (aMessageObject == null)
            return;
        mMessageParts.add(aMessageObject);
        update(aMessageObject);
    }

    public String getActualRouteId()
    {
        return getValue(MiddlewareConstant.MW_ACTUAL_ROUTE_ID);
    }

    public String getAlpha()
    {
        return getValue(MiddlewareConstant.MW_AALPHA);
    }

    public String getAppInstanceId()
    {
        return getValue(MiddlewareConstant.MW_APP_INSTANCE_ID);
    }

    public String getAppType()
    {
        return getValue(MiddlewareConstant.MW_APP_TYPE);
    }

    public String getAttemptCount()
    {
        return getValue(MiddlewareConstant.MW_ATTEMPT_COUNT);
    }

    public String getBaseMessageId()
    {
        return getValue(MiddlewareConstant.MW_BASE_MESSAGE_ID);
    }

    public String getBillingEncryptType()
    {
        return getValue(MiddlewareConstant.MW_BILLING_ENCRYPT_TYPE);
    }

    public int getBillType()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_BILL_TYPE));
    }

    public String getBlockoutType()
    {
        return getValue(MiddlewareConstant.MW_BLOCKOUT_TYPE);
    }

    public String getCampaignId()
    {
        return getValue(MiddlewareConstant.MW_CAMP_ID);
    }

    public String getCampaignName()
    {
        return getValue(MiddlewareConstant.MW_CAMP_NAME);
    }

    
    public String getCarrier()
    {
        return getValue(MiddlewareConstant.MW_CARRIER);
    }
    
    public String getSegment()
    {
        return getValue(MiddlewareConstant.MW_SEGMENT);
    }
    
    public String getMnc()
    {
        return getValue(MiddlewareConstant.MW_MNC);
    }
    
    public String getMcc()
    {
        return getValue(MiddlewareConstant.MW_MCC);
    }

    public String getCarrierDateTimeFormat()
    {
        return getValue(MiddlewareConstant.MW_CARRIER_DATE_TIME_FORMAT);
    }

    public String getCarrierOrigianlStatusCode()
    {
        return getValue(MiddlewareConstant.MW_CARRIER_ORI_STATUS_CODE);
    }

    public String getCarrierOrigianlStatusDesc()
    {
        return getValue(MiddlewareConstant.MW_CARRIER_ORI_STATUS_DESC);
    }

    public String getCircle()
    {
        return getValue(MiddlewareConstant.MW_CIRCLE);
    }

    public String getClientDefaultHeader()
    {
        return getValue(MiddlewareConstant.MW_ACC_DEFAULT_HEADER);
    }

    public int getClientDomesticSmsBlockoutEnabled()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_CLIENT_DOMESTIC_SMS_BLOCKOUT_ENABLED));
    }

    public String getClientDomesticSmsBlockoutStart()
    {
        return getValue(MiddlewareConstant.MW_CLIENT_DOMESTIC_SMS_BLOCKOUT_START);
    }

    public String getClientDomesticSmsBlockoutStop()
    {
        return getValue(MiddlewareConstant.MW_CLIENT_DOMESTIC_SMS_BLOCKOUT_STOP);
    }

    public String getClientHeader()
    {
        return getValue(MiddlewareConstant.MW_CLIENT_HEADER);
    }

    public int getClientMaxSplit()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_CLIENT_MAX_SPLIT));
    }

    public String getClientMessageId()
    {
        return getValue(MiddlewareConstant.MW_CLIENT_MESSAGE_ID);
    }

    public String getClientSourceIp()
    {
        return getValue(MiddlewareConstant.MW_CLIENT_SOURCE_IP);
    }

    public String getClientTemplateId()
    {
        return getValue(MiddlewareConstant.MW_CLIENT_TEMPLATE_ID);
    }

    public String getComponentName()
    {
        return getValue(MiddlewareConstant.MW_COMPONENT_NAME);
    }

    public String getCountry()
    {
        return getValue(MiddlewareConstant.MW_COUNTRY);
    }

    public String getDbInsertJndi()
    {
        return getValue(MiddlewareConstant.MW_DB_BILLING_INSERT_JNDI);
    }

    public String getDbInsertSuffix()
    {
        return getValue(MiddlewareConstant.MW_DB_BILLING_INSERT_CLIENT_SUFFIX);
    }

    public int getDcs()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_DCS));
    }

    public int getDestinationPort()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_DESTINATION_PORT));
    }

    public String getDltEntityId()
    {
        return getValue(MiddlewareConstant.MW_DLT_ENTITY_ID);
    }

    public String getAdditionalErrorInfo()
    {
        return getValue(MiddlewareConstant.MW_ADD_ERROR_INFO);
    }

    public double getDltRate()
    {
        return CommonUtility.getDouble(getValue(MiddlewareConstant.MW_DLT_RATE));
    }

    public String getDltTemplateGroupId()
    {
        return getValue(MiddlewareConstant.MW_DLT_TEMPL_GRP_ID);
    }

    public String getDltTemplateId()
    {
        return getValue(MiddlewareConstant.MW_DLT_TEMPLATE_ID);
    }
    
    
    public String getDltTelemarketerId()
    {
        return getValue(MiddlewareConstant.MW_DLT_TMA_ID);
    }

    public String getDltTemplateType()
    {
        return getValue(MiddlewareConstant.MW_DLT_TEMPLATE_TYPE);
    }

    public String getDnAdjustEnabled()
    {
        return getValue(MiddlewareConstant.MW_DN_ADJUST_ENABLED);
    }

    public String getDndCheckEnabled()
    {
        return getValue(MiddlewareConstant.MW_DND_CHK);
    }

    public int getDndPref()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_DND_PREF));
    }

    public String getDndPreferences()
    {
        return getValue(MiddlewareConstant.MW_DND_ENABLE);
    }

    public String getDnOrigianlstatusCode()
    {
        return getValue(MiddlewareConstant.MW_DN_ORI_STATUS_CODE);
    }

    public String getDnOriStatusDesc()
    {
        return getValue(MiddlewareConstant.MW_DN_ORI_STATUS_DESC);
    }

    public int getDupCheckForUI()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_UI_DUP_CHK));
    }

    public int getDuplicateCheckEnabled()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_DUPLICATE_CHK_REQ));
    }

    public int getDuplicateCheckInterval()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_DUPLICATE_CHK_INTERVAL));
    }

    public String getErrorServerIp()
    {
        return getValue(MiddlewareConstant.MW_ERROR_SERVER_IP);
    }

    public String getErrorStackTrace()
    {
        return getValue(MiddlewareConstant.MW_ERROR_STACKTRACE);
    }

    public String getFailReason()
    {
        return getValue(MiddlewareConstant.MW_FAIL_REASON);
    }

    public String getFeatureCode()
    {
        return getValue(MiddlewareConstant.MW_FEATURE_CODE);
    }

    public String getFileId()
    {
        return getValue(MiddlewareConstant.MW_FILE_ID);
    }

    public String getFileName()
    {
        return getValue(MiddlewareConstant.MW_FILE_NAME);
    }

    public Date getFirstReceivedTime()
    {
        return mFirstReceivedTime;
    }

    public String getFromScheduleBlockout()
    {
        return getValue(MiddlewareConstant.MW_FROM_SCHD_BLOCKOUT);
    }

    public String getHeader()
    {
        return getValue(MiddlewareConstant.MW_HEADER);
    }

    public String getInterfaceCoutryCode()
    {
        return getValue(MiddlewareConstant.MW_INTF_COUNTRY_CODE);
    }

    public String getIntlCarrierNetwork()
    {
        return getValue(MiddlewareConstant.MW_INTL_CARRIER_NW);
    }

    public String getIntlClientFaillistCheck()
    {
        return getValue(MiddlewareConstant.MW_INTL_CLIENT_FAILLIST_CHK);
    }

    public String getIntlClientHeader()
    {
        return getValue(MiddlewareConstant.MW_INTL_CLIENT_HEADER);
    }

    public String getIntlDefaultHeader()
    {
        return getValue(MiddlewareConstant.MW_INTL_DEFAULT_HEADER);
    }

    public String getIntlDefaultHeaderType()
    {
        return getValue(MiddlewareConstant.MW_INTL_DEFAULT_HEADER_TYPE);
    }

    public String getIntlEconomicRouteId()
    {
        return getValue(MiddlewareConstant.MW_INTL_ECONOMIC_ROUTE_ID);
    }

    public String getIntlGlobalFaillistCheck()
    {
        return getValue(MiddlewareConstant.MW_INTL_GLOBAL_FAILLIST_CHK);
    }

    public int getIntlSmsBlockoutEnabled()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_INTL_SMS_BLOCKOUT_ENABLED));
    }

    public String getIntlSmsBlockoutStart()
    {
        return getValue(MiddlewareConstant.MW_INTL_SMS_BLOCKOUT_START);
    }

    public String getIntlSmsBlockoutStop()
    {
        return getValue(MiddlewareConstant.MW_INTL_SMS_BLOCKOUT_STOP);
    }

    public String getIntlStandardRouteId()
    {
        return getValue(MiddlewareConstant.MW_INTL_STANDARD_ROUTE_ID);
    }

    public String getIsHeaderMasked()
    {
        return getValue(MiddlewareConstant.MW_IS_HEADER_MASKED);
    }

    @Override
    public String getJsonString()
    {
        final JSONObject jsonObj      = super.getJson();
        final JSONArray  messageArray = new JSONArray();
        for (final MessagePart msgObj : mMessageParts)
            messageArray.add(msgObj.getJson());

        jsonObj.put(CHILDREN, messageArray);
        return jsonObj.toJSONString();
    }

    public Date getLastReceivedTime()
    {
        return mLastReceivedTime;
    }

    public String getLongMessage()
    {
    	String encodemessage=getValue(MiddlewareConstant.MW_LONG_MSG);
    	
    	/*
    	if(encodemessage!=null) {
    		
    		return URLDecoder.decode(encodemessage);
    	}
    	*/
        return encodemessage ;
    }

    public String getMaskedHeader()
    {
        return getValue(MiddlewareConstant.MW_MASKED_HEADER);
    }

    public int getMaxValidityInSec()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_MAX_VALIDITY_IN_SEC));
    }

    public String getMessageClass()
    {
        return getValue(MiddlewareConstant.MW_MSG_CLASS);
    }

    public int getMessageExpiryInSec()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_MAX_VALIDITY_IN_SEC));
    }

    public List<MessagePart> getMessageParts()
    {
        return mMessageParts;
    }

    public String getMessageTag()
    {
        return getValue(MiddlewareConstant.MW_MSG_TAG);
    }

    public int getMessageTotalParts()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_MSG_TOTAL_PARTS));
    }

    public String getMobileNumber()
    {
        return getValue(MiddlewareConstant.MW_MOBILE_NUMBER);
    }

    public String getMsgAlertCheck()
    {
        return getValue(MiddlewareConstant.MW_MSG_ALTER_CHK);
    }

    public String getMsgSource()
    {
        return getValue(MiddlewareConstant.MW_MSG_SOURCE);
    }

    public String getMsgTag1()
    {
        return getValue(MiddlewareConstant.MW_MSG_TAG1);
    }

    public String getMsgTag2()
    {
        return getValue(MiddlewareConstant.MW_MSG_TAG2);
    }

    public String getMsgTag3()
    {
        return getValue(MiddlewareConstant.MW_MSG_TAG3);
    }

    public String getMsgTag4()
    {
        return getValue(MiddlewareConstant.MW_MSG_TAG4);
    }

    public String getMsgTag5()
    {
        return getValue(MiddlewareConstant.MW_MSG_TAG5);
    }

    public String getMsgType()
    {
        return getValue(MiddlewareConstant.MW_MSG_TYPE);
    }

    public String getMtMessageRetryIdentifier()
    {
        return getValue(MiddlewareConstant.MW_MT_MSGRETRY_IDENTIFIER);
    }

    public String getNewLineReplaceChars()
    {
        return getValue(MiddlewareConstant.MW_NEWLINE_REPLACE_CHAR);
    }

    public String getParam1()
    {
        return getValue(MiddlewareConstant.MW_PARAM_1);
    }

    public String getParam10()
    {
        return getValue(MiddlewareConstant.MW_PARAM_10);
    }

    public String getParam2()
    {
        return getValue(MiddlewareConstant.MW_PARAM_2);
    }

    public String getParam3()
    {
        return getValue(MiddlewareConstant.MW_PARAM_3);
    }

    public String getParam4()
    {
        return getValue(MiddlewareConstant.MW_PARAM_4);
    }

    public String getParam5()
    {
        return getValue(MiddlewareConstant.MW_PARAM_5);
    }

    public String getParam6()
    {
        return getValue(MiddlewareConstant.MW_PARAM_6);
    }

    public String getParam7()
    {
        return getValue(MiddlewareConstant.MW_PARAM_7);
    }

    public String getParam8()
    {
        return getValue(MiddlewareConstant.MW_PARAM_8);
    }

    public String getParam9()
    {
        return getValue(MiddlewareConstant.MW_PARAM_9);
    }

    public String getParentUserId()
    {
        return getValue(MiddlewareConstant.MW_PU_ID);
    }

    public Date getProcessBlockoutTime()
    {
        final String temp = getValue(MiddlewareConstant.MW_PROCESS_BLOCKOUT_TIME);
        return temp == null ? null : DateTimeUtility.getDateFromString(temp, DateTimeFormat.DEFAULT);
    }

    public int getRetryAttempt()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_RETRY_ATTEMPT));
    }

    public String getRouteId()
    {
        return getValue(MiddlewareConstant.MW_ROUTE_ID);
    }

    public int getRouteLogicId()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_ROUTE_LOGIC_ID));
    }

    public String getRouteType()
    {
        return getValue(MiddlewareConstant.MW_ROUTE_TYPE);
    }

    public int getScheduleBlockoutMessage()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_IS_SCHEDULE_BLOCKOUT_MSG));
    }

    public Date getScheduleDateTime()
    {
        final String temp = getValue(MiddlewareConstant.MW_SCHE_DATE_TIME);
        return temp == null ? null : DateTimeUtility.getDateFromString(temp, DateTimeFormat.DEFAULT);
    }

    public String getSmartlinkId()
    {
        return getValue(MiddlewareConstant.MW_SHORTNER_ID);
    }

    public String getSmppEsmClass()
    {
        return getValue(MiddlewareConstant.MW_SMPP_ESM_CLASS);
    }

    public String getSmscId()
    {
        return getValue(MiddlewareConstant.MW_SMSC_ID);
    }

    public String getSmsPriority()
    {
        return getValue(MiddlewareConstant.MW_SMS_PRIORITY);
    }

    public double getSmsRate()
    {
        return CommonUtility.getDouble(getValue(MiddlewareConstant.MW_SMS_RATE));
    }

    public int getSmsRetryEnabled()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_SMS_RETRY_ENABLED));
    }

    public String getSpamCheckEnabled()
    {
        return getValue(MiddlewareConstant.MW_SPAM_CHK);
    }

    public int getSpecificBlockoutCheck()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_SPECIFIC_BLOCKOUT_CHK_ENABLED));
    }

    private SubmissionObject getSubmission(
            MessagePart aMessageObject) throws ItextosRuntimeException
    {
        final SubmissionObject subObj = new SubmissionObject(getClusterType(), getInterfaceType(), getInterfaceGroupType(), getMessageType(), getMessagePriority(), getMessageRouteType());

        // Set Parent Object
        subObj.setActualRouteId(getActualRouteId());
        subObj.setAddSubClientHeader(isAddSubClientHeader());
        subObj.setAlpha(getAlpha());
        subObj.setAppInstanceId(getAppInstanceId());
        subObj.setAppType(getAppType());
        subObj.setBaseMessageId(getBaseMessageId());
        subObj.setBillType(getBillType());
        subObj.setBillingEncryptType(getBillingEncryptType());
        subObj.setBlacklistCheck(getBlacklistCheck());
        subObj.setBlockoutType(getBlockoutType());
        subObj.setCarrier(getCarrier());
        subObj.setMnc(getMnc());
        subObj.setMcc(getMcc());
        subObj.setCarrierDateTimeFormat(getCarrierDateTimeFormat());
        subObj.setCircle(getCircle());
        subObj.setClientDomesticSmsBlockoutEnabled(getClientDomesticSmsBlockoutEnabled());
        subObj.setClientDomesticSmsBlockoutStart(getClientDomesticSmsBlockoutStart());
        subObj.setClientDomesticSmsBlockoutStop(getClientDomesticSmsBlockoutStop());
        subObj.setClientEncryptEnable(isClientEncryptEnable());
        subObj.setClientHeader(getClientHeader());
        subObj.setClientId(getClientId());
        subObj.setClientMaxSplit(getClientMaxSplit());
        subObj.setClientMessageId(getClientMessageId());
        subObj.setClientSourceIp(getClientSourceIp());
        subObj.setClientTemplateId(getClientTemplateId());
        subObj.setClientTemplateMatch(isClientTemplateMatch());
        subObj.setComponentName(getComponentName());
        subObj.setCountry(getCountry());
        subObj.setDbInsertJndi(getDbInsertJndi());
        subObj.setDbInsertSuffix(getDbInsertSuffix());
        subObj.setDcs(getDcs());
        subObj.setDestinationPort(getDestinationPort());
        subObj.setDlrRequestFromClient(isDlrRequestFromClient());
        subObj.setDltCheckEnabled(isDltCheckEnabled());
        subObj.setDltEntityId(getDltEntityId());
        subObj.setDltTemplateGroupId(getDltTemplateGroupId());
        subObj.setDltTemplateId(getDltTemplateId());
        subObj.setDltTemplateType(getDltTemplateType());
        subObj.setDnAdjustEnabled(getDnAdjustEnabled());
        subObj.setDndCheckEnabled(getDndCheckEnabled());
        subObj.setDndPreferences(getDndPreferences());
        subObj.setDomesticPromoTraBlockoutPurge(isDomesticPromoTraBlockoutPurge());
        subObj.setDuplicateCheckEnabled(getDuplicateCheckEnabled());
        subObj.setDuplicateCheckInterval(getDuplicateCheckInterval());
        subObj.setErrorServerIp(getErrorServerIp());
        subObj.setErrorStackTrace(getErrorStackTrace());
        subObj.setFailReason(getFailReason());
        subObj.setFeatureCode(getFeatureCode());
        subObj.setFileId(getFileId());
        subObj.setFromScheduleBlockout(getFromScheduleBlockout());
        subObj.setGovermentHeader(isGovermentHeader());
        subObj.setHeader(getHeader());
        subObj.setInterfaceRejected(isInterfaceRejected());
        subObj.setIntlCarrierNetwork(getIntlCarrierNetwork());
        subObj.setIntlClientFaillistCheck(getIntlClientFaillistCheck());
        subObj.setIntlClientHeader(getIntlClientHeader());
        subObj.setIntlDefaultHeader(getIntlDefaultHeader());
        subObj.setIntlDefaultHeaderType(getIntlDefaultHeaderType());
        subObj.setIntlEconomicRouteId(getIntlEconomicRouteId());
        subObj.setIntlGlobalFaillistCheck(getIntlGlobalFaillistCheck());
        subObj.setIntlSmsBlockoutEnabled(getIntlSmsBlockoutEnabled());
        subObj.setIntlSmsBlockoutStart(getIntlSmsBlockoutStart());
        subObj.setIntlSmsBlockoutStop(getIntlSmsBlockoutStop());
        subObj.setIntlStandardRouteId(getIntlStandardRouteId());
        subObj.setIs16BitUdh(is16BitUdh());
        subObj.setIsDndScrubbed(isDndScrubbed());
        subObj.setIsHeaderMasked(getIsHeaderMasked());
        subObj.setIsHexMessage(isHexMessage());
        subObj.setLongMessage(getLongMessage());
        subObj.setMaskedHeader(getMaskedHeader());
        subObj.setMaxValidityInSec(getMaxValidityInSec());
        subObj.setMessageClass(getMessageClass());
        subObj.setMessageExpiryInSec(getMessageExpiryInSec());
        subObj.setMessageTag(getMessageTag());
        subObj.setMessageTotalParts(getMessageTotalParts());
        subObj.setMobileNumber(getMobileNumber());
        subObj.setMsgAlertCheck(getMsgAlertCheck());
        subObj.setMsgType(getMsgType());
        subObj.setMtMessageRetryIdentifier(getMtMessageRetryIdentifier());
        subObj.setNewLineReplaceChars(getNewLineReplaceChars());
        subObj.setParam1(getParam1());
        subObj.setParam10(getParam10());
        subObj.setParam2(getParam2());
        subObj.setParam3(getParam3());
        subObj.setParam4(getParam4());
        subObj.setParam5(getParam5());
        subObj.setParam6(getParam6());
        subObj.setParam7(getParam7());
        subObj.setParam8(getParam8());
        subObj.setParam9(getParam9());
        subObj.setParentUserId(getParentUserId());
        subObj.setPlatfromRejected(isPlatfromRejected());
        subObj.setProcessBlockoutTime(getProcessBlockoutTime());
        subObj.setRetryAttempt(getRetryAttempt());
        subObj.setRetryMessageRejected(isRetryMessageRejected());
        subObj.setRouteId(getRouteId());
        subObj.setRouteLogicId(getRouteLogicId());
        subObj.setRouteType(getRouteType());
        subObj.setScheduleBlockoutMessage(getScheduleBlockoutMessage());
        subObj.setScheduleDateTime(getScheduleDateTime());
        subObj.setSmsPriority(getSmsPriority());
        subObj.setSmscId(getSmscId());
        subObj.setSpamCheckEnabled(getSpamCheckEnabled());
        subObj.setSpecificBlockoutCheck(getSpecificBlockoutCheck());
        subObj.setSpecificDrop(isSpecificDrop());
        subObj.setSubOriginalStatusCode(getSubOriginalStatusCode());
        subObj.setSuperUserId(getSuperUserId());
        subObj.setTreatDomesticAsSpecialSeries(isTreatDomesticAsSpecialSeries());
        subObj.setUrlTrackEnabled(isUrlTrackEnabled());
        subObj.setUser(getUser());
        subObj.setUserType(getUserType());
        subObj.setVlShortner(getVlShortner());
        subObj.setSyncRequest(isSyncRequest());
        subObj.setCampaignId(getCampaignId());
        subObj.setCampaignName(getCampaignName());
        subObj.setSmartlinkId(getSmartlinkId());
        subObj.setUrlShortned(isUrlShortned());

        subObj.setMsgTag1(getMsgTag1());
        subObj.setMsgTag2(getMsgTag2());
        subObj.setMsgTag3(getMsgTag3());
        subObj.setMsgTag4(getMsgTag4());
        subObj.setMsgTag5(getMsgTag5());

        subObj.putValue(MiddlewareConstant.MW_RETRY_ORG_ROUTE_ID, getValue(MiddlewareConstant.MW_RETRY_ORG_ROUTE_ID));
        subObj.putValue(MiddlewareConstant.MW_RETRY_ALE_ROUTE_ID, getValue(MiddlewareConstant.MW_RETRY_ALE_ROUTE_ID));

        subObj.setSmsRetryEnabled(getSmsRetryEnabled());

        subObj.setMessageId(aMessageObject.getMessageId());
        subObj.setConcatnateReferenceNumber(aMessageObject.getConcatnateReferenceNumber());
        subObj.setMessageActualReceivedDate(aMessageObject.getMessageActualReceivedDate());
        subObj.setMessageActualReceivedTime(aMessageObject.getMessageActualReceivedTime());
        subObj.setMessageReceivedDate(aMessageObject.getMessageReceivedDate());
        subObj.setMessageReceivedTime(aMessageObject.getMessageReceivedTime());
        subObj.setUdh(aMessageObject.getUdh());
        subObj.setMessage(aMessageObject.getMessage());
        subObj.setMessagePartNumber(aMessageObject.getMessagePartNumber());
        subObj.setUdhi(aMessageObject.getUdhi());
        subObj.setCarrierReceivedTime(aMessageObject.getCarrierReceivedTime());
        subObj.setCarrierAcknowledgeId(aMessageObject.getCarrierAcknowledgeId());
        subObj.setCarrierSystemId(aMessageObject.getCarrierSystemId());
        subObj.setCarrierFullDn(aMessageObject.getCarrierFullDn());
        subObj.setDeliveryStatus(aMessageObject.getDeliveryStatus());
        subObj.setCarrierSubmitTime(aMessageObject.getCarrierSubmitTime());
        subObj.setActualCarrierSubmitTime(aMessageObject.getActualCarrierSubmitTime());

        subObj.setCarrierOrigianlStatusCode(getCarrierOrigianlStatusCode());
        subObj.setCarrierOrigianlStatusDesc(getCarrierOrigianlStatusDesc());
        subObj.setDnOrigianlstatusCode(getDnOrigianlstatusCode());
        subObj.setDnOriStatusDesc(getDnOriStatusDesc());

        subObj.setAdditionalErrorInfo(getAdditionalErrorInfo());
        subObj.setSmsRate(getSmsRate());
        subObj.setDltRate(getDltRate());
        subObj.setIsWalletDeduct(isWalletDeduct());

        subObj.setInterfaceCoutryCode(getInterfaceCoutryCode());
        subObj.setMsgSource(getMsgSource());

        subObj.setClientDltTemplateId(getClientDltTemplateId());
        subObj.setClientDltEntityId(getClientDltEntityId());

        subObj.setBillingCurrency(getBillingCurrency());
        subObj.setBillingCurrencyConversionType(getBillingCurrencyConversionType());

        subObj.setBaseCurrency(getBaseCurrency());
        subObj.setBaseSmsRate(getBaseSmsRate());
        subObj.setBaseAddFixedRate(getBaseAddFixedRate());
        subObj.setBillingSmsRate(getBillingSmsRate());
        subObj.setBillingAddFixedRate(getBillingAddFixedRate());
        subObj.setRefCurrency(getRefCurrency());
        subObj.setRefSmsRate(getRefSmsRate());
        subObj.setRefAddFixedRate(getRefAddFixedSmsRate());
        subObj.setBillingExchangeRate(getBillingExchangeRate());
        subObj.setRefExchangeRate(getRefExchangeRate());

        subObj.setUser(getUser());
        subObj.setInvoiceBasedOn(getInvoiceBasedOn());
        subObj.setSmppInstance(getSmppInstance());

        subObj.putValue(MiddlewareConstant.MW_SMPP_MSG_RECEIVED_TIME, aMessageObject.getValueExt(MiddlewareConstant.MW_SMPP_MSG_RECEIVED_TIME));
        subObj.putValue(MiddlewareConstant.MW_CAPPING_CHK_ENABLED, getValue(MiddlewareConstant.MW_CAPPING_CHK_ENABLED));
        subObj.putValue(MiddlewareConstant.MW_CAPPING_INTERVAL_TYPE, getValue(MiddlewareConstant.MW_CAPPING_INTERVAL_TYPE));
        subObj.putValue(MiddlewareConstant.MW_CAPPING_INTERVAL, getValue(MiddlewareConstant.MW_CAPPING_INTERVAL));
        subObj.putValue(MiddlewareConstant.MW_CAPPING_MAX_REQ_COUNT, getValue(MiddlewareConstant.MW_CAPPING_MAX_REQ_COUNT));
        subObj.putValue(MiddlewareConstant.MW_CREDIT_CHECK, getValue(MiddlewareConstant.MW_CREDIT_CHECK));
        subObj.putValue(MiddlewareConstant.MW_CREDIT_CHECK, getValue(MiddlewareConstant.MW_CREDIT_CHECK));
        subObj.putValue(MiddlewareConstant.MW_MCC, getValue(MiddlewareConstant.MW_MCC));
        subObj.putValue(MiddlewareConstant.MW_MNC, getValue(MiddlewareConstant.MW_MNC));
        subObj.putValue(MiddlewareConstant.MW_SEGMENT, getValue(MiddlewareConstant.MW_SEGMENT));

        return subObj;
    }

    public List<BaseMessage> getSubmissions() throws ItextosRuntimeException
    {
        final List<BaseMessage> returnValue = new ArrayList<>();

        for (final MessagePart msgObj : mMessageParts)
            returnValue.add(getSubmission(msgObj));

        return returnValue;
    }

    public String getSubOriginalStatusCode()
    {
        return getValue(MiddlewareConstant.MW_SUB_ORI_STATUS_CODE);
    }

    public String getSuperUserId()
    {
        return getValue(MiddlewareConstant.MW_SU_ID);
    }

    public boolean getTimeboundCheck()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_TIMEBOUND_CHK_ENABLED));
    }

    public int getTimeboundInterval()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_TIMEBOUND_INTERVAL));
    }

    public int getTimeboundMaxReqCount()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_TIMEBOUND_MAX_REQ_COUNT));
    }

    public String getTimeOffset()
    {
        return getValue(MiddlewareConstant.MW_TIME_OFFSET);
    }

    public int getUrlShortCodeLength()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_URL_SHORTCODE_LENGTH));
    }

    public int getUrlSmartlinkEnable()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_URL_SMARTLINK_ENABLE));
    }

    public String getUser()
    {
        return getValue(MiddlewareConstant.MW_USER);
    }

    public String getUserType()
    {
        return getValue(MiddlewareConstant.MW_USER_TYPE);
    }

    public int getVlShortner()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_VL_SHORTNER));
    }

    public int getVlShortnerFromUI()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_UI_VL_SHORTNER_REQ));
    }

    public boolean is16BitUdh()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_IS_16BIT_UDH));
    }

    public boolean isAddSubClientHeader()
    {
        return CommonUtility.isTrue(getValue(MiddlewareConstant.MW_ADD_SUB_CLIENT_HEADER));
    }

    public int getBlacklistCheck()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_BLACKLIST_CHK));
    }

    public boolean isBypassDltCheck()
    {
        return CommonUtility.isTrue(getValue(MiddlewareConstant.MW_BYPASS_DLT_TEMPLATE_CHECK));
    }

    public boolean isClientEncryptEnable()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_CLIENT_ENCRYPT_ENABLED));
    }

    public boolean isClientTemplateMatch()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_IS_CLIENT_TEMPLATE_MATCH));
    }

    public boolean isDefailtHeaderEnable()
    {
        return CommonUtility.isTrue(getValue(MiddlewareConstant.MW_USE_DEFAULT_HEADER_ENABLED));
    }

    public boolean isDefailtHeaderFailEnable()
    {
        return CommonUtility.isTrue(getValue(MiddlewareConstant.MW_USE_DEFAULT_HEADER_FAIL_ENABLED));
    }

    public boolean isDlrRequestFromClient()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_DLR_REQ_FROM_CLI));
    }

    public boolean isDltCheckEnabled()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_DLT_CHECK_ENABLED));
    }

    public boolean isDndRejectEnable()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_DND_REJECT_YN));
    }

    public boolean isDndScrubbed()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_IS_DND_SCRUBBED));
    }

    public boolean isDomesticPromoTraBlockoutPurge()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_DOMESTIC_PROMO_TRAI_BLOCKOUT_PURGE));
    }

    public boolean isGovermentHeader()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_IS_GOVT_HEADER));
    }

    public boolean isHexMessage()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_IS_HEX_MSG));
    }

    public boolean isInterfaceRejected()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_INTERFACE_REJECTED));
    }

    public boolean isPlatfromRejected()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_PLATFROM_REJECTED));
    }

    public boolean isRetryMessageRejected()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_RETRY_MSG_REJECT));
    }

    public boolean isSpecificDrop()
    {
        return CommonUtility.isTrue(getValue(MiddlewareConstant.MW_IS_SPECIFIC_DROP));
    }

    public boolean isSyncRequest()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_IS_SYNC_REQUEST));
    }

    public boolean isTreatDomesticAsSpecialSeries()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_TREAT_DOMESTIC_AS_SPECIAL_SERIES));
    }

    public boolean isUrlShortned()
    {
        return CommonUtility.isTrue(getValue(MiddlewareConstant.MW_IS_MSG_SHORTNED));
    }

    public boolean isUrlTrackEnabled()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_URL_TRACK_ENABLED));
    }

    public boolean isWalletDeduct()
    {
        return CommonUtility.isTrue(getValue(MiddlewareConstant.MW_IS_WC_DEDUCT));
    }

    private void populateChildren(
            String aCompleteJsonString)
            throws Exception
    {
        final JSONParser parser     = new JSONParser();
        final JSONObject jsonObject = (JSONObject) parser.parse(aCompleteJsonString);

        final JSONArray  lJsonArray = (JSONArray) jsonObject.get(CHILDREN);

        for (final Object obj : lJsonArray)
        {
            final JSONObject  jsonObj = (JSONObject) obj;
            final MessagePart msgObj  = new MessagePart(jsonObj);
            addMessagePart(msgObj);
        }
    }

    public boolean removeMessageObject(
            MessagePart aMessageObject)
    {
        return mMessageParts.remove(aMessageObject);
    }

    public void setActualRouteId(
            String aActualRouteId)
    {
        putValue(MiddlewareConstant.MW_ACTUAL_ROUTE_ID, aActualRouteId);
    }

    public void setAddSubClientHeader(
            boolean aAddSubClientHeader)
    {
        putValue(MiddlewareConstant.MW_ADD_SUB_CLIENT_HEADER, MessageUtil.getStringFromBoolean(aAddSubClientHeader));
    }

    public void setAlpha(
            String aAlpha)
    {
        putValue(MiddlewareConstant.MW_AALPHA, aAlpha);
    }

    public void setAppInstanceId(
            String aAppInstanceId)
    {
        putValue(MiddlewareConstant.MW_APP_INSTANCE_ID, aAppInstanceId);
    }

    public void setAppType(
            String aAppType)
    {
        putValue(MiddlewareConstant.MW_APP_TYPE, aAppType);
    }

    public void setAttemptCount(
            String aAttemptCount)
    {
        putValue(MiddlewareConstant.MW_ATTEMPT_COUNT, aAttemptCount);
    }

    public void setBaseMessageId(
            String aBaseMessageId)
    {
        putValue(MiddlewareConstant.MW_BASE_MESSAGE_ID, aBaseMessageId);
    }

    public void setBillingEncryptType(
            String aBillingEncryptType)
    {
        putValue(MiddlewareConstant.MW_BILLING_ENCRYPT_TYPE, aBillingEncryptType);
    }

    public void setBillType(
            int aBillType)
    {
        putValue(MiddlewareConstant.MW_BILL_TYPE, MessageUtil.getStringFromInt(aBillType));
    }

    public void setBlacklistCheckEnabled(
            int aBlacklistCheck)
    {
        putValue(MiddlewareConstant.MW_BLACKLIST_CHK, MessageUtil.getStringFromInt(aBlacklistCheck));
    }

    public void setBlockoutType(
            String aBlockoutType)
    {
        putValue(MiddlewareConstant.MW_BLOCKOUT_TYPE, aBlockoutType);
    }

    public void setBypassDltCheck(
            boolean aBypassDltCheck)
    {
        putValue(MiddlewareConstant.MW_BYPASS_DLT_TEMPLATE_CHECK, MessageUtil.getStringFromBoolean(aBypassDltCheck));
    }

    public void setCampaignId(
            String aCampaignId)
    {
        putValue(MiddlewareConstant.MW_CAMP_ID, aCampaignId);
    }

    public void setCampaignName(
            String aCampaignName)
    {
        putValue(MiddlewareConstant.MW_CAMP_NAME, aCampaignName);
    }

    public void setCarrier(
            String aCarrier)
    {
        putValue(MiddlewareConstant.MW_CARRIER, aCarrier);
    }

    public void setMcc(
            String aMcc)
    {
        putValue(MiddlewareConstant.MW_MCC, aMcc);
    }
    
    public void setMnc(
            String aMnc)
    {
        putValue(MiddlewareConstant.MW_MNC, aMnc);
    }
    public void setSegment(
            String aSegment)
    {
        putValue(MiddlewareConstant.MW_SEGMENT, aSegment);
    }
    public void setCarrierDateTimeFormat(
            String aCarrierDateTimeFormat)
    {
        putValue(MiddlewareConstant.MW_CARRIER_DATE_TIME_FORMAT, aCarrierDateTimeFormat);
    }

    public void setCarrierOrigianlStatusCode(
            String aCarrierOrigianlStatusCode)
    {
        putValue(MiddlewareConstant.MW_CARRIER_ORI_STATUS_CODE, aCarrierOrigianlStatusCode);
    }

    public void setCarrierOrigianlStatusDesc(
            String aCarrierOrigianlStatusDesc)
    {
        putValue(MiddlewareConstant.MW_CARRIER_ORI_STATUS_DESC, aCarrierOrigianlStatusDesc);
    }

    public void setCircle(
            String aCircle)
    {
        putValue(MiddlewareConstant.MW_CIRCLE, aCircle);
    }

    public void setClientDefaultHeader(
            String aClientDefaultHeader)
    {
        putValue(MiddlewareConstant.MW_ACC_DEFAULT_HEADER, aClientDefaultHeader);
    }

    public void setClientDomesticSmsBlockoutEnabled(
            int aClientDomesticSmsBlockoutEnabled)
    {
        putValue(MiddlewareConstant.MW_CLIENT_DOMESTIC_SMS_BLOCKOUT_ENABLED, MessageUtil.getStringFromInt(aClientDomesticSmsBlockoutEnabled));
    }

    public void setClientDomesticSmsBlockoutStart(
            String aClientDomesticSmsBlockoutStart)
    {
        putValue(MiddlewareConstant.MW_CLIENT_DOMESTIC_SMS_BLOCKOUT_START, aClientDomesticSmsBlockoutStart);
    }

    public void setClientDomesticSmsBlockoutStop(
            String aClientDomesticSmsBlockoutStop)
    {
        putValue(MiddlewareConstant.MW_CLIENT_DOMESTIC_SMS_BLOCKOUT_STOP, aClientDomesticSmsBlockoutStop);
    }

    public void setClientEncryptEnable(
            boolean aClientEncryptEnable)
    {
        putValue(MiddlewareConstant.MW_CLIENT_ENCRYPT_ENABLED, MessageUtil.getStringFromBoolean(aClientEncryptEnable));
    }

    public void setClientHeader(
            String aClientHeader)
    {
        putValue(MiddlewareConstant.MW_CLIENT_HEADER, aClientHeader);
    }

    public void setClientId(
            String aClientId)
    {
        putValue(MiddlewareConstant.MW_CLIENT_ID, aClientId);
    }

    public void setClientMaxSplit(
            int aClientMaxSplit)
    {
        putValue(MiddlewareConstant.MW_CLIENT_MAX_SPLIT, MessageUtil.getStringFromInt(aClientMaxSplit));
    }

    public void setClientMessageId(
            String aClientMessageId)
    {
        putValue(MiddlewareConstant.MW_CLIENT_MESSAGE_ID, aClientMessageId);
    }

    public void setClientSourceIp(
            String aClientSourceIp)
    {
        putValue(MiddlewareConstant.MW_CLIENT_SOURCE_IP, aClientSourceIp);
    }

    public void setClientTemplateId(
            String aClientTemplateId)
    {
        putValue(MiddlewareConstant.MW_CLIENT_TEMPLATE_ID, aClientTemplateId);
    }

    public void setClientTemplateMatch(
            boolean aClientTemplateMatch)
    {
        putValue(MiddlewareConstant.MW_IS_CLIENT_TEMPLATE_MATCH, MessageUtil.getStringFromBoolean(aClientTemplateMatch));
    }

    public void setComponentName(
            String aComponentName)
    {
        putValue(MiddlewareConstant.MW_COMPONENT_NAME, aComponentName);
    }

    public void setCountry(
            String aCountry)
    {
        putValue(MiddlewareConstant.MW_COUNTRY, aCountry);
    }

    public void setDbInsertJndi(
            String aDbInsertJndi)
    {
        putValue(MiddlewareConstant.MW_DB_BILLING_INSERT_JNDI, aDbInsertJndi);
    }

    public void setDbInsertSuffix(
            String aDbInsertSuffix)
    {
        putValue(MiddlewareConstant.MW_DB_BILLING_INSERT_CLIENT_SUFFIX, aDbInsertSuffix);
    }

    public void setDcs(
            int aDcs)
    {
        putValue(MiddlewareConstant.MW_DCS, MessageUtil.getStringFromInt(aDcs));
    }

    public void setDefaultHeaderEnable(
            int aHeaderChk)
    {
        putValue(MiddlewareConstant.MW_USE_DEFAULT_HEADER_ENABLED, MessageUtil.getStringFromInt(aHeaderChk));
    }

    public void setDestinationPort(
            int aDestinationPort)
    {
        putValue(MiddlewareConstant.MW_DESTINATION_PORT, MessageUtil.getStringFromInt(aDestinationPort));
    }

    public void setDlrRequestFromClient(
            boolean aDlrRequestFromClient)
    {
        putValue(MiddlewareConstant.MW_DLR_REQ_FROM_CLI, MessageUtil.getStringFromBoolean(aDlrRequestFromClient));
    }

    public void setDltCheckEnabled(
            boolean aDltCheckEnabled)
    {
        putValue(MiddlewareConstant.MW_DLT_CHECK_ENABLED, MessageUtil.getStringFromBoolean(aDltCheckEnabled));
    }

    public void setDltEntityId(
            String aDltEntityId)
    {
        putValue(MiddlewareConstant.MW_DLT_ENTITY_ID, aDltEntityId);
    }

    public void setAdditionalErrorInfo(
            String aAddErrorInfo)
    {
        final String lValue = getValue(MiddlewareConstant.MW_ADD_ERROR_INFO);
        putValue(MiddlewareConstant.MW_ADD_ERROR_INFO, lValue == null ? aAddErrorInfo : lValue + " : " + aAddErrorInfo);
    }

    public void setDltRate(
            double aDltRate)
    {
        putValue(MiddlewareConstant.MW_DLT_RATE, MessageUtil.getStringFromDouble(aDltRate));
    }

    public void setDltTemplateGroupId(
            String aTemplateGroupId)
    {
        putValue(MiddlewareConstant.MW_DLT_TEMPL_GRP_ID, aTemplateGroupId);
    }

    public void setDltTemplateId(
            String aDltTemplateId)
    {
        putValue(MiddlewareConstant.MW_DLT_TEMPLATE_ID, aDltTemplateId);
    }

    
    public void setDltTelemarketerId(
            String aDltTelemarketerId)
    {
        putValue(MiddlewareConstant.MW_DLT_TMA_ID, aDltTelemarketerId);
    }

    
    public void setDltTemplateType(
            String aDltTemmplateType)
    {
        putValue(MiddlewareConstant.MW_DLT_TEMPLATE_TYPE, aDltTemmplateType);
    }

    public void setDnAdjustEnabled(
            String aDnAdjustEnabled)
    {
        putValue(MiddlewareConstant.MW_DN_ADJUST_ENABLED, aDnAdjustEnabled);
    }

    public void setDndCheckEnabled(
            String aDndCheckEnabled)
    {
        putValue(MiddlewareConstant.MW_DND_CHK, aDndCheckEnabled);
    }

    public void setDndPref(
            int aDndPref)
    {
        putValue(MiddlewareConstant.MW_DND_PREF, MessageUtil.getStringFromInt(aDndPref));
    }

    public void setDndPreferences(
            String aDndPreferences)
    {
        putValue(MiddlewareConstant.MW_DND_ENABLE, aDndPreferences);
    }

    public void setDndRejectEnable(
            boolean aDndRejectFlag)
    {
        putValue(MiddlewareConstant.MW_DND_REJECT_YN, MessageUtil.getStringFromBoolean(aDndRejectFlag));
    }

    public void setDnOrigianlstatusCode(
            String aDnOrigianlstatusCode)
    {
        putValue(MiddlewareConstant.MW_DN_ORI_STATUS_CODE, aDnOrigianlstatusCode);
    }

    public void setDnOriStatusDesc(
            String aDnOriStatusDesc)
    {
        putValue(MiddlewareConstant.MW_DN_ORI_STATUS_DESC, aDnOriStatusDesc);
    }

    public void setDomesticPromoTraBlockoutPurge(
            boolean aDomesticPromoTraBlockoutPurge)
    {
        putValue(MiddlewareConstant.MW_DOMESTIC_PROMO_TRAI_BLOCKOUT_PURGE, MessageUtil.getStringFromBoolean(aDomesticPromoTraBlockoutPurge));
    }

    public void setDupCheckForUI(
            int aDupCheckForUI)
    {
        putValue(MiddlewareConstant.MW_UI_DUP_CHK, MessageUtil.getStringFromInt(aDupCheckForUI));
    }

    public void setDuplicateCheckEnabled(
            int aDuplicateCheckEnabled)
    {
        putValue(MiddlewareConstant.MW_DUPLICATE_CHK_REQ, MessageUtil.getStringFromInt(aDuplicateCheckEnabled));
    }

    public void setDuplicateCheckInterval(
            int aDuplicateCheckInterval)
    {
        putValue(MiddlewareConstant.MW_DUPLICATE_CHK_INTERVAL, MessageUtil.getStringFromInt(aDuplicateCheckInterval));
    }

    public void setErrorServerIp(
            String aErrorServerIp)
    {
        putValue(MiddlewareConstant.MW_ERROR_SERVER_IP, aErrorServerIp);
    }

    public void setErrorStackTrace(
            String aErrorStackTrace)
    {
        putValue(MiddlewareConstant.MW_ERROR_STACKTRACE, aErrorStackTrace);
    }

    public void setFailReason(
            String aFailReason)
    {
        putValue(MiddlewareConstant.MW_FAIL_REASON, aFailReason);
    }

    public void setFeatureCode(
            String aFeatureCode)
    {
        putValue(MiddlewareConstant.MW_FEATURE_CODE, aFeatureCode);
    }

    public void setFileId(
            String aFileId)
    {
        putValue(MiddlewareConstant.MW_FILE_ID, aFileId);
    }

    public void setFileName(
            String aFileName)
    {
        putValue(MiddlewareConstant.MW_FILE_NAME, aFileName);
    }

    public void setFromScheduleBlockout(
            String aFromScheduleBlockout)
    {
        putValue(MiddlewareConstant.MW_FROM_SCHD_BLOCKOUT, aFromScheduleBlockout);
    }

    public void setGovermentHeader(
            boolean aGovermentHeader)
    {
        putValue(MiddlewareConstant.MW_IS_GOVT_HEADER, MessageUtil.getStringFromBoolean(aGovermentHeader));
    }

    public void setHeader(
            String aHeader)
    {
        putValue(MiddlewareConstant.MW_HEADER, aHeader);
    }

    public void setInterfaceCoutryCode(
            String aIntfCountryCode)
    {
        putValue(MiddlewareConstant.MW_INTF_COUNTRY_CODE, aIntfCountryCode);
    }

    public void setInterfaceRejected(
            boolean aInterfaceRejected)
    {
        putValue(MiddlewareConstant.MW_INTERFACE_REJECTED, MessageUtil.getStringFromBoolean(aInterfaceRejected));
    }

    public void setIntlCarrierNetwork(
            String aIntlCarrierNetwork)
    {
        putValue(MiddlewareConstant.MW_INTL_CARRIER_NW, aIntlCarrierNetwork);
    }

    public void setIntlClientFaillistCheck(
            String aIntlClientFaillistCheck)
    {
        putValue(MiddlewareConstant.MW_INTL_CLIENT_FAILLIST_CHK, aIntlClientFaillistCheck);
    }

    public void setIntlClientHeader(
            String aIntlClientHeader)
    {
        putValue(MiddlewareConstant.MW_INTL_CLIENT_HEADER, aIntlClientHeader);
    }

    public void setIntlDefaultHeader(
            String aIntlDefaultHeader)
    {
        putValue(MiddlewareConstant.MW_INTL_DEFAULT_HEADER, aIntlDefaultHeader);
    }

    public void setIntlDefaultHeaderType(
            String aIntlDefaultHeaderType)
    {
        putValue(MiddlewareConstant.MW_INTL_DEFAULT_HEADER_TYPE, aIntlDefaultHeaderType);
    }

    public void setIntlEconomicRouteId(
            String aIntlEconomicRouteId)
    {
        putValue(MiddlewareConstant.MW_INTL_ECONOMIC_ROUTE_ID, aIntlEconomicRouteId);
    }

    public void setIntlGlobalFaillistCheck(
            String aIntlGlobalFaillistCheck)
    {
        putValue(MiddlewareConstant.MW_INTL_GLOBAL_FAILLIST_CHK, aIntlGlobalFaillistCheck);
    }

    public void setIntlSmsBlockoutEnabled(
            int aIntlSmsBlockoutEnabled)
    {
        putValue(MiddlewareConstant.MW_INTL_SMS_BLOCKOUT_ENABLED, MessageUtil.getStringFromInt(aIntlSmsBlockoutEnabled));
    }

    public void setIntlSmsBlockoutStart(
            String aIntlSmsBlockoutStart)
    {
        putValue(MiddlewareConstant.MW_INTL_SMS_BLOCKOUT_START, aIntlSmsBlockoutStart);
    }

    public void setIntlSmsBlockoutStop(
            String aIntlSmsBlockoutStop)
    {
        putValue(MiddlewareConstant.MW_INTL_SMS_BLOCKOUT_STOP, aIntlSmsBlockoutStop);
    }

    public void setIntlStandardRouteId(
            String aIntlStandardRouteId)
    {
        putValue(MiddlewareConstant.MW_INTL_STANDARD_ROUTE_ID, aIntlStandardRouteId);
    }

    public void setIs16BitUdh(
            boolean aIs16BitUdh)
    {
        putValue(MiddlewareConstant.MW_IS_16BIT_UDH, MessageUtil.getStringFromBoolean(aIs16BitUdh));
    }

    public void setIsDndScrubbed(
            boolean aIsDndScrubbed)
    {
        putValue(MiddlewareConstant.MW_IS_DND_SCRUBBED, MessageUtil.getStringFromBoolean(aIsDndScrubbed));
    }

    public void setIsHeaderMasked(
            String aIsHeaderMasked)
    {
        putValue(MiddlewareConstant.MW_IS_HEADER_MASKED, aIsHeaderMasked);
    }

    public void setIsHexMessage(
            boolean aIsHexMessage)
    {
        putValue(MiddlewareConstant.MW_IS_HEX_MSG, MessageUtil.getStringFromBoolean(aIsHexMessage));
    }

    public void setIsWalletDeduct(
            boolean isWallectDeduct)
    {
        putValue(MiddlewareConstant.MW_IS_WC_DEDUCT, MessageUtil.getStringFromBoolean(isWallectDeduct));
    }

    public void setLongMessage(
            String aLongMessage)
    {
    	String encodemsg=URLEncoder.encode(aLongMessage);
    	
        //putValue(MiddlewareConstant.MW_LONG_MSG, encodemsg);
    	
    	putValue(MiddlewareConstant.MW_LONG_MSG, aLongMessage);
        
    }

    public void setMaskedHeader(
            String aMaskedHeader)
    {
        putValue(MiddlewareConstant.MW_MASKED_HEADER, aMaskedHeader);
    }

    public void setMaxValidityInSec(
            int aMaxValiditySec)
    {
        putValue(MiddlewareConstant.MW_MAX_VALIDITY_IN_SEC, MessageUtil.getStringFromInt(aMaxValiditySec));
    }

    public void setMessageClass(
            String aMessageClass)
    {
        putValue(MiddlewareConstant.MW_MSG_CLASS, aMessageClass);
    }

    public void setMessageExpiryInSec(
            int aMessageExpiryInSec)
    {
        putValue(MiddlewareConstant.MW_MAX_VALIDITY_IN_SEC, MessageUtil.getStringFromInt(aMessageExpiryInSec));
    }

    public void setMessageTag(
            String aMessageTag)
    {
        putValue(MiddlewareConstant.MW_MSG_TAG, aMessageTag);
    }

    public void setMessageTotalParts(
            int aMessageTotalParts)
    {
        putValue(MiddlewareConstant.MW_MSG_TOTAL_PARTS, MessageUtil.getStringFromInt(aMessageTotalParts));
    }

    public void setMobileNumber(
            String aMobileNumber)
    {
        putValue(MiddlewareConstant.MW_MOBILE_NUMBER, aMobileNumber);
    }

    public void setMsgAlertCheck(
            String aMsgAlertCheck)
    {
        putValue(MiddlewareConstant.MW_MSG_ALTER_CHK, aMsgAlertCheck);
    }

    public void setMsgSource(
            String aMsgSource)
    {
        putValue(MiddlewareConstant.MW_MSG_SOURCE, aMsgSource);
    }

    public void setMsgTag1(
            String aMsgTag1)
    {
        putValue(MiddlewareConstant.MW_MSG_TAG1, aMsgTag1);
    }

    public void setMsgTag2(
            String aMsgTag2)
    {
        putValue(MiddlewareConstant.MW_MSG_TAG2, aMsgTag2);
    }

    public void setMsgTag3(
            String aMsgTag3)
    {
        putValue(MiddlewareConstant.MW_MSG_TAG3, aMsgTag3);
    }

    public void setMsgTag4(
            String aMsgTag4)
    {
        putValue(MiddlewareConstant.MW_MSG_TAG4, aMsgTag4);
    }

    /*
     * public void setTraTransPromoCheck(
     * String aTraTransPromoCheck)
     * {
     * putValue(MiddlewareConstant.MW_TRA_TRANSPROMO_CHK, aTraTransPromoCheck);
     * }
     */

    public void setMsgTag5(
            String aMsgTag5)
    {
        putValue(MiddlewareConstant.MW_MSG_TAG5, aMsgTag5);
    }

    public void setMsgType(
            String aMsgType)
    {
        putValue(MiddlewareConstant.MW_MSG_TYPE, aMsgType);
    }

    public void setMtMessageRetryIdentifier(
            String aMtMessageRetryIdentifier)
    {
        putValue(MiddlewareConstant.MW_MT_MSGRETRY_IDENTIFIER, aMtMessageRetryIdentifier);
    }

    public void setNewLineReplaceChars(
            String aNewLineReplaceChars)
    {
        putValue(MiddlewareConstant.MW_NEWLINE_REPLACE_CHAR, aNewLineReplaceChars);
    }

    public void setParam1(
            String aParam1)
    {
        putValue(MiddlewareConstant.MW_PARAM_1, aParam1);
    }

    public void setParam10(
            String aParam10)
    {
        putValue(MiddlewareConstant.MW_PARAM_10, aParam10);
    }

    public void setParam2(
            String aParam2)
    {
        putValue(MiddlewareConstant.MW_PARAM_2, aParam2);
    }

    public void setParam3(
            String aParam3)
    {
        putValue(MiddlewareConstant.MW_PARAM_3, aParam3);
    }

    public void setParam4(
            String aParam4)
    {
        putValue(MiddlewareConstant.MW_PARAM_4, aParam4);
    }

    public void setParam5(
            String aParam5)
    {
        putValue(MiddlewareConstant.MW_PARAM_5, aParam5);
    }

    public void setParam6(
            String aParam6)
    {
        putValue(MiddlewareConstant.MW_PARAM_6, aParam6);
    }

    public void setParam7(
            String aParam7)
    {
        putValue(MiddlewareConstant.MW_PARAM_7, aParam7);
    }

    public void setParam8(
            String aParam8)
    {
        putValue(MiddlewareConstant.MW_PARAM_8, aParam8);
    }

    public void setParam9(
            String aParam9)
    {
        putValue(MiddlewareConstant.MW_PARAM_9, aParam9);
    }

    public void setParentUserId(
            String aParentUserId)
    {
        putValue(MiddlewareConstant.MW_PU_ID, aParentUserId);
    }

    public void setPlatfromRejected(
            boolean aPlatfromReject)
    {
        putValue(MiddlewareConstant.MW_PLATFROM_REJECTED, MessageUtil.getStringFromBoolean(aPlatfromReject));
    }

    public void setProcessBlockoutTime(
            Date aProcessBlockoutTime)
    {
        if (aProcessBlockoutTime != null)
            putValue(MiddlewareConstant.MW_PROCESS_BLOCKOUT_TIME, DateTimeUtility.getFormattedDateTime(aProcessBlockoutTime, DateTimeFormat.DEFAULT));
    }

    public void setRetryAttempt(
            int aRetryAttempt)
    {
        putValue(MiddlewareConstant.MW_RETRY_ATTEMPT, MessageUtil.getStringFromInt(aRetryAttempt));
    }

    public void setRetryMessageRejected(
            boolean aRetryMessageRejected)
    {
        putValue(MiddlewareConstant.MW_RETRY_MSG_REJECT, MessageUtil.getStringFromBoolean(aRetryMessageRejected));
    }

    public void setRouteId(
            String aRouteId)
    {
        putValue(MiddlewareConstant.MW_ROUTE_ID, aRouteId);
    }

    public void setRouteLogicId(
            int aRouteLogicId)
    {
        putValue(MiddlewareConstant.MW_ROUTE_LOGIC_ID, MessageUtil.getStringFromInt(aRouteLogicId));
    }

    public void setRouteType(
            String aRouteType)
    {
        putValue(MiddlewareConstant.MW_ROUTE_TYPE, aRouteType);
    }

    public void setScheduleBlockoutMessage(
            int aScheduleBlockoutMessage)
    {
        putValue(MiddlewareConstant.MW_IS_SCHEDULE_BLOCKOUT_MSG, MessageUtil.getStringFromInt(aScheduleBlockoutMessage));
    }

    public void setScheduleDateTime(
            Date aScheduleDateTime)
    {
        if (aScheduleDateTime != null)
            putValue(MiddlewareConstant.MW_SCHE_DATE_TIME, DateTimeUtility.getFormattedDateTime(aScheduleDateTime, DateTimeFormat.DEFAULT));
    }

    public void setSmartlinkId(
            String aSmartLinkId)
    {
        putValue(MiddlewareConstant.MW_SHORTNER_ID, aSmartLinkId);
    }

    public void setSmppEsmClass(
            String aEsmClass)
    {
        putValue(MiddlewareConstant.MW_SMPP_ESM_CLASS, aEsmClass);
    }

    public void setSmscId(
            String aSmscId)
    {
        putValue(MiddlewareConstant.MW_SMSC_ID, aSmscId);
    }

    public void setSmsPriority(
            String aSmsPriority)
    {
        putValue(MiddlewareConstant.MW_SMS_PRIORITY, aSmsPriority);
    }

    public void setSmsRate(
            double aSmsRate)
    {
        putValue(MiddlewareConstant.MW_SMS_RATE, MessageUtil.getStringFromDouble(aSmsRate));
    }

    public void setSmsRetryEnabled(
            int aSmsRetryFlag)
    {
        putValue(MiddlewareConstant.MW_SMS_RETRY_ENABLED, MessageUtil.getStringFromInt(aSmsRetryFlag));
    }

    public void setSpamCheckEnabled(
            String aSpamCheckEnabled)
    {
        putValue(MiddlewareConstant.MW_SPAM_CHK, aSpamCheckEnabled);
    }

    public void setSpecificBlockoutCheck(
            int aSpecificBlockoutEnable)
    {
        putValue(MiddlewareConstant.MW_SPECIFIC_BLOCKOUT_CHK_ENABLED, MessageUtil.getStringFromInt(aSpecificBlockoutEnable));
    }

    public void setSpecificDrop(
            boolean aSpecificDrop)
    {
        putValue(MiddlewareConstant.MW_IS_SPECIFIC_DROP, MessageUtil.getStringFromBoolean(aSpecificDrop));
    }

    public void setSubOriginalStatusCode(
            String aSubOriginalStatusCode)
    {
        putValue(MiddlewareConstant.MW_SUB_ORI_STATUS_CODE, aSubOriginalStatusCode);
    }

    public void setSuperUserId(
            String aSuperUserId)
    {
        putValue(MiddlewareConstant.MW_SU_ID, aSuperUserId);
    }

    public void setSyncRequest(
            boolean isSync)
    {
        putValue(MiddlewareConstant.MW_IS_SYNC_REQUEST, MessageUtil.getStringFromBoolean(isSync));
    }

    public void setTimeboundCheck(
            int aTimeBoundCheck)
    {
        putValue(MiddlewareConstant.MW_TIMEBOUND_CHK_ENABLED, MessageUtil.getStringFromInt(aTimeBoundCheck));
    }

    public void setTimeboundInterval(
            int aTimeBoundCheck)
    {
        putValue(MiddlewareConstant.MW_TIMEBOUND_INTERVAL, MessageUtil.getStringFromInt(aTimeBoundCheck));
    }

    public void setTimeboundMaxReqCount(
            int aTimeBoundCheck)
    {
        putValue(MiddlewareConstant.MW_TIMEBOUND_MAX_REQ_COUNT, MessageUtil.getStringFromInt(aTimeBoundCheck));
    }

    public void setTimeOffset(
            String aTimeOffset)
    {
        putValue(MiddlewareConstant.MW_TIME_OFFSET, aTimeOffset);
    }

    public void setTreatDomesticAsSpecialSeries(
            boolean aTreatDomesticAsSpecialSeries)
    {
        putValue(MiddlewareConstant.MW_TREAT_DOMESTIC_AS_SPECIAL_SERIES, MessageUtil.getStringFromBoolean(aTreatDomesticAsSpecialSeries));
    }

    public void setUrlShortCodeLength(
            int aUrlShortCodeLen)
    {
        putValue(MiddlewareConstant.MW_URL_SHORTCODE_LENGTH, MessageUtil.getStringFromInt(aUrlShortCodeLen));
    }

    public void setUrlShortned(
            boolean aUrlShortned)
    {
        putValue(MiddlewareConstant.MW_IS_MSG_SHORTNED, MessageUtil.getStringFromBoolean(aUrlShortned));
    }

    public void setUrlSmartLink(
            int aUrlSmartLink)
    {
        putValue(MiddlewareConstant.MW_URL_SMARTLINK_ENABLE, MessageUtil.getStringFromInt(aUrlSmartLink));
    }

    public void setUrlTrackEnabled(
            boolean aUrlTrackEnabled)
    {
        putValue(MiddlewareConstant.MW_URL_TRACK_ENABLED, MessageUtil.getStringFromBoolean(aUrlTrackEnabled));
    }

    public void setUseDefaultHeaderFailEnable(
            int aUseDefaultHeaderFail)
    {
        putValue(MiddlewareConstant.MW_USE_DEFAULT_HEADER_FAIL_ENABLED, MessageUtil.getStringFromInt(aUseDefaultHeaderFail));
    }

    public void setUser(
            String aUser)
    {
        putValue(MiddlewareConstant.MW_USER, aUser);
    }

    public void setUserType(
            String aUserType)
    {
        putValue(MiddlewareConstant.MW_USER_TYPE, aUserType);
    }

    public void setVlShortner(
            int aVlShortner)
    {
        putValue(MiddlewareConstant.MW_VL_SHORTNER, MessageUtil.getStringFromInt(aVlShortner));
    }

    public void setVlShortnerFromUI(
            int aVlShortnerFromUI)
    {
        putValue(MiddlewareConstant.MW_UI_VL_SHORTNER_REQ, MessageUtil.getStringFromInt(aVlShortnerFromUI));
    }

    public void setClientDltTemplateId(
            String aClientDltTemplateId)
    {
        putValue(MiddlewareConstant.MW_CLI_DLT_TEMPLATE_ID, aClientDltTemplateId);
    }

    public String getClientDltTemplateId()
    {
        return getValue(MiddlewareConstant.MW_CLI_DLT_TEMPLATE_ID);
    }

    public void setClientDltEntityId(
            String aClientDltEntityId)
    {
        putValue(MiddlewareConstant.MW_CLI_DLT_ENTITY_ID, aClientDltEntityId);
    }

    public String getClientDltEntityId()
    {
        return getValue(MiddlewareConstant.MW_CLI_DLT_ENTITY_ID);
    }

    public void setUrlShortnerReq(
            String aUrlShortnerReq)
    {
        putValue(MiddlewareConstant.MW_URL_SHORTNER_REQ, aUrlShortnerReq);
    }

    public boolean getUrlShortnerReq()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_URL_SHORTNER_REQ));
    }

    public void setIntlHeaderSubType(
            String aHeaderSubType)
    {
        putValue(MiddlewareConstant.MW_INTL_HEADER_SUB_TYPE, aHeaderSubType);
    }

    public String getIntlHeaderSubType()
    {
        return getValue(MiddlewareConstant.MW_INTL_HEADER_SUB_TYPE);
    }

    public void setBillingCurrency(
            String aBillingCurrency)
    {
        putValue(MiddlewareConstant.MW_BILLING_CURRENCY, aBillingCurrency);
    }

    public String getBillingCurrency()
    {
        return getValue(MiddlewareConstant.MW_BILLING_CURRENCY);
    }

    public void setBillingCurrencyConversionType(
            int aBillingCurrConvType)
    {
        putValue(MiddlewareConstant.MW_BILLING_CURRENCY_CONVERSION_TYPE, MessageUtil.getStringFromInt(aBillingCurrConvType));
    }

    public int getBillingCurrencyConversionType()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_BILLING_CURRENCY_CONVERSION_TYPE));
    }

    public void setIsIldo(
            String aIsIldo)
    {
        putValue(MiddlewareConstant.MW_IS_IDLO, aIsIldo);
    }

    public boolean isIldo()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_IS_IDLO));
    }

    public void setBaseSmsRate(
            double aBaseSmsRate)
    {
        putValue(MiddlewareConstant.MW_BASE_SMS_RATE, MessageUtil.getStringFromDouble(aBaseSmsRate));
    }

    public double getBaseSmsRate()
    {
        return CommonUtility.getDouble(getValue(MiddlewareConstant.MW_BASE_SMS_RATE));
    }

    public void setBaseAddFixedRate(
            double aBaseAddFixedRate)
    {
        putValue(MiddlewareConstant.MW_BASE_ADD_FIXED_RATE, MessageUtil.getStringFromDouble(aBaseAddFixedRate));
    }

    public double getBaseAddFixedRate()
    {
        return CommonUtility.getDouble(getValue(MiddlewareConstant.MW_BASE_ADD_FIXED_RATE));
    }

    public void setBillingSmsRate(
            double aBillingSMSRate)
    {
        putValue(MiddlewareConstant.MW_BILLING_SMS_RATE, MessageUtil.getStringFromDouble(aBillingSMSRate));
    }

    public double getBillingSmsRate()
    {
        return CommonUtility.getDouble(getValue(MiddlewareConstant.MW_BILLING_SMS_RATE));
    }

    public void setBillingAddFixedRate(
            double aBillingAddFixedRate)
    {
        putValue(MiddlewareConstant.MW_BILLING_ADD_FIXED_RATE, MessageUtil.getStringFromDouble(aBillingAddFixedRate));
    }

    public double getBillingAddFixedRate()
    {
        return CommonUtility.getDouble(getValue(MiddlewareConstant.MW_BILLING_ADD_FIXED_RATE));
    }

    public void setRefSmsRate(
            double aRefSmsRate)
    {
        putValue(MiddlewareConstant.MW_REF_SMS_RATE, MessageUtil.getStringFromDouble(aRefSmsRate));
    }

    public double getRefSmsRate()
    {
        return CommonUtility.getDouble(getValue(MiddlewareConstant.MW_REF_SMS_RATE));
    }

    public void setRefAddFixedRate(
            double aRefAddFixedRate)
    {
        putValue(MiddlewareConstant.MW_REF_ADD_FIXED_RATE, MessageUtil.getStringFromDouble(aRefAddFixedRate));
    }

    public double getRefAddFixedSmsRate()
    {
        return CommonUtility.getDouble(getValue(MiddlewareConstant.MW_REF_ADD_FIXED_RATE));
    }

    public void setForceDndCheck(
            String aForceDndCheck)
    {
        putValue(MiddlewareConstant.MW_FORCE_DND_CHK, aForceDndCheck);
    }

    public boolean isForceDndCheck()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_FORCE_DND_CHK));
    }

    public void setBaseCurrency(
            String aBaseCurrency)
    {
        putValue(MiddlewareConstant.MW_BASE_CURRENCY, aBaseCurrency);
    }

    public String getBaseCurrency()
    {
        return getValue(MiddlewareConstant.MW_BASE_CURRENCY);
    }

    public void setRefCurrency(
            String aRefCurrency)
    {
        putValue(MiddlewareConstant.MW_REF_CURRENCY, aRefCurrency);
    }

    public String getRefCurrency()
    {
        return getValue(MiddlewareConstant.MW_REF_CURRENCY);
    }

    public void setBillingExchangeRate(
            double aBillingExchangeRate)
    {
        putValue(MiddlewareConstant.MW_BILLING_EXCHANGE_RATE, MessageUtil.getStringFromDouble(aBillingExchangeRate));
    }

    public double getBillingExchangeRate()
    {
        return CommonUtility.getDouble(getValue(MiddlewareConstant.MW_BILLING_EXCHANGE_RATE));
    }

    public void setRefExchangeRate(
            double aRefExchangeRate)
    {
        putValue(MiddlewareConstant.MW_REF_EXCHANGE_RATE, MessageUtil.getStringFromDouble(aRefExchangeRate));
    }

    public double getRefExchangeRate()
    {
        return CommonUtility.getDouble(getValue(MiddlewareConstant.MW_REF_EXCHANGE_RATE));
    }

    public void setInvoiceBasedOn(
            String aInvoiceBasedOn)
    {
        putValue(MiddlewareConstant.MW_INVOICE_BASED_ON, aInvoiceBasedOn);
    }

    public String getInvoiceBasedOn()
    {
        return getValue(MiddlewareConstant.MW_INVOICE_BASED_ON);
    }

    public void setSmppInstance(
            String aSmppInstance)
    {
        putValue(MiddlewareConstant.MW_SMPP_INSTANCE_ID, aSmppInstance);
    }

    public String getSmppInstance()
    {
        return getValue(MiddlewareConstant.MW_SMPP_INSTANCE_ID);
    }

    private void update(
            MessagePart aMessageObject)
    {
        updateReceivedTime(aMessageObject);
    }

 public SMSLog getLogBuffer() {
    	
    	return SMSLog.getInstance();
    }
    private void updateReceivedTime(
            MessagePart aMessageObject)
    {
        final Date receivedTime = aMessageObject.getMessageActualReceivedTime();

        if (mFirstReceivedTime == null)
        {
            mFirstReceivedTime = receivedTime;
            mLastReceivedTime  = receivedTime;
            return;
        }

        if (mFirstReceivedTime.after(receivedTime))
            mFirstReceivedTime = receivedTime;

        if (mFirstReceivedTime.before(receivedTime))
            mLastReceivedTime = receivedTime;
    }

}