package com.itextos.beacon.commonlib.message;

import java.util.Date;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

public class SubmissionObject
        extends
        BaseMessage
{

    private static final long serialVersionUID = 7464496301359006826L;

    SubmissionObject(
            ClusterType aClusterType,
            InterfaceType aInterfaceType,
            InterfaceGroup aInterfaceGroup,
            MessageType aMessageType,
            MessagePriority aMessagePriority,
            RouteType aIsInt) throws ItextosRuntimeException
    {
        super(aClusterType, aInterfaceType, aInterfaceGroup, aMessageType, aMessagePriority, aIsInt, "SubmissionObject");
    }
    
    
    public SubmissionObject(
            String aJsonString)
            throws Exception
    {
        super(aJsonString, "SubmissionObject");
    }

    public Date getActualCarrierSubmitTime()
    {
        final String temp = getValue(MiddlewareConstant.MW_ACTUAL_CARRIER_SUBMIT_TIME);
        return temp == null ? null : DateTimeUtility.getDateFromString(temp, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    }

    public String getAlpha()
    {
        return getValue(MiddlewareConstant.MW_AALPHA);
    }

    public String getAlterMessage()
    {
        return getValue(MiddlewareConstant.MW_ALTER_MSG);
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

    public int getBillType()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_BILL_TYPE));
    }

    public String getCallBackUrl()
    {
        return getValue(MiddlewareConstant.MW_CALLBACK_URL);
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

    public String getMcc()
    {
        return getValue(MiddlewareConstant.MW_MCC);
    }
    
    public String getParam6()
    {
        return getValue(MiddlewareConstant.MW_PARAM_6);
    }
    
    public String getMnc()
    {
        return getValue(MiddlewareConstant.MW_MNC);
    }
    
    public String getSegment()
    {
        return getValue(MiddlewareConstant.MW_SEGMENT);
    }
    
    public String getCarrierAcknowledgeId()
    {
        return getValue(MiddlewareConstant.MW_CARRIER_ACKNOWLEDGE_ID);
    }

    public String getCarrierDateTimeFormat()
    {
        return getValue(MiddlewareConstant.MW_CARRIER_DATE_TIME_FORMAT);
    }

    public String getCarrierFullDn()
    {
        return getValue(MiddlewareConstant.MW_CARRIER_FULL_DN);
    }

    public String getCarrierOrigianlStatusCode()
    {
        return getValue(MiddlewareConstant.MW_CARRIER_ORI_STATUS_CODE);
    }

    public String getCarrierOrigianlStatusDesc()
    {
        return getValue(MiddlewareConstant.MW_CARRIER_ORI_STATUS_DESC);
    }

    public Date getCarrierReceivedTime()
    {
        final String temp = getValue(MiddlewareConstant.MW_CARRIER_RECEIVED_TIME);
        return temp == null ? null : DateTimeUtility.getDateFromString(temp, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    }

    public Date getCarrierSubmitTime()
    {
        final String temp = getValue(MiddlewareConstant.MW_CARRIER_SUBMIT_TIME);
        return temp == null ? null : DateTimeUtility.getDateFromString(temp, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    }

    public String getCarrierSystemId()
    {
        return getValue(MiddlewareConstant.MW_CARRIER_SYSTEM_ID);
    }

    public String getCircle()
    {
        return getValue(MiddlewareConstant.MW_CIRCLE);
    }

    public String getClientHeader()
    {
        return getValue(MiddlewareConstant.MW_CLIENT_HEADER);
    }

    public String getClientMessageId()
    {
        return getValue(MiddlewareConstant.MW_CLIENT_MESSAGE_ID);
    }

    public String getCountry()
    {
        return getValue(MiddlewareConstant.MW_COUNTRY);
    }

    public int getDcs()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_DCS));
    }

 public SMSLog getLogBuffer() {
    	
    	return SMSLog.getInstance();
    }
 
    public DeliveryObject getDeliveryObject() throws ItextosRuntimeException
    {
        final DeliveryObject lDeliveryObject = new DeliveryObject(getClusterType(), getInterfaceType(), getInterfaceGroupType(), getMessageType(), getMessagePriority(), getMessageRouteType());

        // TODO Need to add the remaining data
        lDeliveryObject.setClientId(getClientId());
        lDeliveryObject.setBaseMessageId(getBaseMessageId());
        lDeliveryObject.setMessage(getMessage());
        lDeliveryObject.setMessageId(getMessageId());
        lDeliveryObject.setRouteId(getRouteId());
        lDeliveryObject.setHeader(getHeader());
        lDeliveryObject.setMobileNumber(getMobileNumber());
        lDeliveryObject.setMsgReceivedTime(getMessageReceivedTime());
        lDeliveryObject.setAalpha(getAlpha());
        lDeliveryObject.setUdhi(getUdhi());
        lDeliveryObject.setDcs(getDcs());
        lDeliveryObject.setIsHexMessage(isHexMessage());
        lDeliveryObject.setDlrRequestFromClient(isDlrRequestFromClient());
        lDeliveryObject.setSmsPriority(getSmsPriority());
        lDeliveryObject.setFileId(getFileId());
        lDeliveryObject.setCarrier(getCarrier());
        lDeliveryObject.setMcc(getMcc());
        lDeliveryObject.setMnc(getMnc());
        lDeliveryObject.setParam6(getParam6());
        lDeliveryObject.setCircle(getCircle());
        lDeliveryObject.setClientMessageId(getClientMessageId());
        lDeliveryObject.setMessageTotalParts(getMessageTotalParts());
        lDeliveryObject.setMessagePartNumber(getMessagePartNumber());
        lDeliveryObject.setScheduleDateTime(getScheduleDateTime());
        lDeliveryObject.setBillType(getBillType());
        lDeliveryObject.setMessageTag(getMessageTag());
        lDeliveryObject.setTerminatedCarrier(getTerminatedCarrier());
        lDeliveryObject.setTerminatedCircle(getTerminatedCircle());
        lDeliveryObject.setUdh(getUdh());
        lDeliveryObject.setAttemptCount(getAttemptCount());
        lDeliveryObject.setCountry(getCountry());
        lDeliveryObject.setClientHeader(getClientHeader());
        lDeliveryObject.setMaskedHeader(getMaskedHeader());
        lDeliveryObject.setSmsRetryEnabled(getSmsRetryEnabled());
        lDeliveryObject.setSmscId(getSmscId());
        lDeliveryObject.setBaseMessageId(getBaseMessageId());
        lDeliveryObject.setFeatureCode(getFeatureCode());
        lDeliveryObject.setMessageClass(getMessageClass());
        lDeliveryObject.setMessageExpiryInSec(getMessageExpiryInSec());
        lDeliveryObject.setCarrierDateTimeFormat(getCarrierDateTimeFormat());
        lDeliveryObject.setRetryOriginalRouteId(getRetryOriginalRouteId());
        lDeliveryObject.setLongMessage(getLongMessage());
        lDeliveryObject.setCarrierSubmitTime(getCarrierSubmitTime());
        lDeliveryObject.setActualCarrierSubmitTime(getActualCarrierSubmitTime());
        lDeliveryObject.setTimeOffset(getTimeOffset());
        lDeliveryObject.setTreatDomesticAsSpecialSeries(isTreatDomesticAsSpecialSeries());
        lDeliveryObject.setFileName(getFileName());
        lDeliveryObject.setDltEntityId(getDltEntityId());
        lDeliveryObject.setDltTemplateId(getDltTemplateId());
        lDeliveryObject.setAppType(getAppType());
        lDeliveryObject.setSubOriStatusCode(getSubOriginalStatusCode());
        lDeliveryObject.setMessageActualReceivedTime(getMessageActualReceivedTime());
        lDeliveryObject.setMessageReceivedDate(getMessageReceivedDate());
        lDeliveryObject.setMessageActualReceivedDate(getMessageActualReceivedDate());
        lDeliveryObject.setSyncRequest(isSyncRequest());
        lDeliveryObject.setCampaignId(getCampaignId());
        lDeliveryObject.setMsgTag1(getMsgTag1());
        lDeliveryObject.setMsgTag2(getMsgTag2());
        lDeliveryObject.setMsgTag3(getMsgTag3());
        lDeliveryObject.setMsgTag4(getMsgTag4());
        lDeliveryObject.setMsgTag5(getMsgTag5());
        lDeliveryObject.setIsWalletDeduct(isWalletDeduct());
        lDeliveryObject.setSmsRate(getSmsRate());
        lDeliveryObject.setDltRate(getDltRate());

        lDeliveryObject.putValue(MiddlewareConstant.MW_RETRY_ORG_ROUTE_ID, getValue(MiddlewareConstant.MW_RETRY_ORG_ROUTE_ID));
        lDeliveryObject.putValue(MiddlewareConstant.MW_RETRY_ALE_ROUTE_ID, getValue(MiddlewareConstant.MW_RETRY_ALE_ROUTE_ID));
        lDeliveryObject.setInterfaceRejected(isInterfaceRejected());

        lDeliveryObject.setBaseSmsRate(getBaseSmsRate());
        lDeliveryObject.setBaseAddFixedRate(getBaseAddFixedRate());
        lDeliveryObject.setBillingSmsRate(getBillingSmsRate());
        lDeliveryObject.setBillingAddFixedRate(getBillingAddFixedRate());
        lDeliveryObject.setRefSmsRate(getRefSmsRate());
        lDeliveryObject.setRefAddFixedRate(getRefAddFixedRate());
        lDeliveryObject.setUser(getUser());
        lDeliveryObject.setInvoiceBasedOn(getInvoiceBasedOn());
        lDeliveryObject.setSmppInstance(getSmppInstance());
        
        lDeliveryObject.putValue(MiddlewareConstant.MW_CREDIT_CHECK, getValue(MiddlewareConstant.MW_CREDIT_CHECK));

        return lDeliveryObject;
    }

    public String getDeliveryStatus()
    {
        return getValue(MiddlewareConstant.MW_DELIVERY_STATUS);
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

    public String getDltTemplateId()
    {
        return getValue(MiddlewareConstant.MW_DLT_TEMPLATE_ID);
    }

    public String getDltTemplateType()
    {
        return getValue(MiddlewareConstant.MW_DLT_TEMPLATE_TYPE);
    }

    public String getDnAdjustEnabled()
    {
        return getValue(MiddlewareConstant.MW_DN_ADJUST_ENABLED);
    }

    public String getDnOrigianlstatusCode()
    {
        return getValue(MiddlewareConstant.MW_DN_ORI_STATUS_CODE);
    }

    public String getDnOriStatusDesc()
    {
        return getValue(MiddlewareConstant.MW_DN_ORI_STATUS_DESC);
    }

    public String getDnPayloadStatus()
    {
        return getValue(MiddlewareConstant.MW_DN_PAYLOAD_STATUS);
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

    public String getHeader()
    {
        return getValue(MiddlewareConstant.MW_HEADER);
    }

    public String getInterfaceCoutryCode()
    {
        return getValue(MiddlewareConstant.MW_INTF_COUNTRY_CODE);
    }

    public String getLongMessage()
    {
        return getValue(MiddlewareConstant.MW_LONG_MSG);
    }

    public String getMaskedHeader()
    {
        return getValue(MiddlewareConstant.MW_MASKED_HEADER);
    }

    public String getMessage()
    {
        return getValue(MiddlewareConstant.MW_MSG);
    }

    public Date getMessageActualReceivedDate()
    {
        final String temp = getValue(MiddlewareConstant.MW_MSG_ACTUAL_RECEIVED_DATE);
        return temp == null ? null : DateTimeUtility.getDateFromString(temp, DateTimeFormat.DEFAULT_DATE_ONLY);
    }

    public Date getMessageActualReceivedTime()
    {
        final String temp = getValue(MiddlewareConstant.MW_MSG_ACTUAL_RECEIVED_TIME);
        return temp == null ? null : DateTimeUtility.getDateFromString(temp, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    }

    public String getMessageClass()
    {
        return getValue(MiddlewareConstant.MW_MSG_CLASS);
    }

    public int getMessageExpiryInSec()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_MAX_VALIDITY_IN_SEC));
    }

    public String getMessageId()
    {
        return getValue(MiddlewareConstant.MW_MESSAGE_ID);
    }

    public int getMessagePartNumber()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_MSG_PART_NUMBER));
    }

    public Date getMessageReceivedDate()
    {
        final String temp = getValue(MiddlewareConstant.MW_MSG_RECEIVED_DATE);
        return temp == null ? null : DateTimeUtility.getDateFromString(temp, DateTimeFormat.DEFAULT_DATE_ONLY);
    }

    public Date getMessageReceivedTime()
    {
        final String temp = getValue(MiddlewareConstant.MW_MSG_RECEIVED_TIME);
        return temp == null ? null : DateTimeUtility.getDateFromString(temp, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
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

    public String getMtMessageRetryIdentifier()
    {
        return getValue(MiddlewareConstant.MW_MT_MSGRETRY_IDENTIFIER);
    }

    public String getOperatorJson(
            Iterator<String> aKeys)
    {
        final JSONObject defaultValues = new JSONObject();

        defaultValues.put(MiddlewareConstant.MW_PLATFORM_CLUSTER.getName(), getClusterType().getKey());
        defaultValues.put(MiddlewareConstant.MW_INTERFACE_TYPE.getName(), getInterfaceType().getKey());
        defaultValues.put(MiddlewareConstant.MW_INTERFACE_GROUP_TYPE.getName(), getInterfaceGroupType().getKey());
        defaultValues.put(MiddlewareConstant.MW_MSG_TYPE.getName(), getMessageType().getKey());
        defaultValues.put(MiddlewareConstant.MW_SMS_PRIORITY.getName(), getMessagePriority().getKey());
        defaultValues.put(MiddlewareConstant.MW_INTL_MESSAGE.getName(), getMessageRouteType().getKey());
        defaultValues.put(MiddlewareConstant.MW_MESSAGE_CREATED_TIMESTAMP.getName(), getValue(MiddlewareConstant.MW_MESSAGE_CREATED_TIMESTAMP));

        while (aKeys.hasNext())
        {
            final String             key  = aKeys.next();
            final MiddlewareConstant temp = MiddlewareConstant.getMiddlewareConstantByName(key);
            if (temp != null)
                defaultValues.put(temp.getKey(), getValue(temp));
        }

        defaultValues.put(MiddlewareConstant.MW_PAYLOAD_REDIS_ID.getKey(), getPayloadRedisId());
        return defaultValues.toJSONString();
    }

    public String getPayloadExpiry()
    {
        return getValue(MiddlewareConstant.MW_PAYLOAD_EXPIRY);
    }

    public String getPayloadRedisId()
    {
        return getValue(MiddlewareConstant.MW_PAYLOAD_REDIS_ID);
    }

    public int getRetryAttempt()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_RETRY_ATTEMPT));
    }

    public String getRetryOriginalRouteId()
    {
        return getValue(MiddlewareConstant.MW_RETRY_ORG_ROUTE_ID);
    }

    public String getRouteId()
    {
        return getValue(MiddlewareConstant.MW_ROUTE_ID);
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

    public int getSpecificBlockoutCheck()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_SPECIFIC_BLOCKOUT_CHK_ENABLED));
    }

    public String getSubOriginalStatusCode()
    {
        return getValue(MiddlewareConstant.MW_SUB_ORI_STATUS_CODE);
    }

    public String getSubStatus()
    {
        return getValue(MiddlewareConstant.MW_SUB_STATUS);
    }

    public String getTerminatedCarrier()
    {
        return getValue(MiddlewareConstant.MW_TERM_CARRIER);
    }

    public String getTerminatedCircle()
    {
        return getValue(MiddlewareConstant.MW_TERM_CIRCLE);
    }

    public String getTimeOffset()
    {
        return getValue(MiddlewareConstant.MW_TIME_OFFSET);
    }

    public String getUdh()
    {
        return getValue(MiddlewareConstant.MW_UDH);
    }

    public int getUdhi()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_UDHI));
    }

    public boolean isDlrRequestFromClient()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_DLR_REQ_FROM_CLI));
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

    public boolean isWalletDeduct()
    {
        return CommonUtility.isTrue(getValue(MiddlewareConstant.MW_IS_WC_DEDUCT));
    }

    public void setActualCarrierSubmitTime(
            Date aActualCarrierSubmitTime)
    {
        putValue(MiddlewareConstant.MW_ACTUAL_CARRIER_SUBMIT_TIME, DateTimeUtility.getFormattedDateTime(aActualCarrierSubmitTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
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

    public void setAgingScheduleTime(
            Date aAgingScheduleTime)
    {
        putValue(MiddlewareConstant.MW_AGING_SCHE_TIME, DateTimeUtility.getFormattedDateTime(aAgingScheduleTime, DateTimeFormat.DEFAULT));
    }

    public void setAgingType(
            String aAgingType)
    {
        putValue(MiddlewareConstant.MW_AGING_TYPE, aAgingType);
    }

    public void setAlpha(
            String aAlpha)
    {
        putValue(MiddlewareConstant.MW_AALPHA, aAlpha);
    }

    public void setAlterMessage(
            String aAlterMessage)
    {
        putValue(MiddlewareConstant.MW_ALTER_MSG, aAlterMessage);
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

    public void setBlacklistCheck(
            int aBlacklistCheck)
    {
        putValue(MiddlewareConstant.MW_BLACKLIST_CHK, MessageUtil.getStringFromInt(aBlacklistCheck));
    }

    public void setBlockoutType(
            String aBlockoutType)
    {
        putValue(MiddlewareConstant.MW_BLOCKOUT_TYPE, aBlockoutType);
    }

    public void setCallBackUrl(
            String aCallBackUrl)
    {
        putValue(MiddlewareConstant.MW_CALLBACK_URL, aCallBackUrl);
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
    public void setCarrierAcknowledgeId(
            String aCarrierAcknowledgeId)
    {
        putValue(MiddlewareConstant.MW_CARRIER_ACKNOWLEDGE_ID, aCarrierAcknowledgeId);
    }

    public void setCarrierDateTimeFormat(
            String aCarrierDateTimeFormat)
    {
        putValue(MiddlewareConstant.MW_CARRIER_DATE_TIME_FORMAT, aCarrierDateTimeFormat);
    }

    public void setCarrierFullDn(
            String aCarrierFullDn)
    {
        putValue(MiddlewareConstant.MW_CARRIER_FULL_DN, aCarrierFullDn);
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

    public void setCarrierReceivedTime(
            Date aCarrierReceivedTime)
    {
        putValue(MiddlewareConstant.MW_CARRIER_RECEIVED_TIME, DateTimeUtility.getFormattedDateTime(aCarrierReceivedTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
    }

    public void setCarrierSubmitTime(
            Date aCarrierSubmitTime)
    {
        putValue(MiddlewareConstant.MW_CARRIER_SUBMIT_TIME, DateTimeUtility.getFormattedDateTime(aCarrierSubmitTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
    }

    public void setCarrierSystemId(
            String aCarrierSystemId)
    {
        putValue(MiddlewareConstant.MW_CARRIER_SYSTEM_ID, aCarrierSystemId);
    }

    public void setCircle(
            String aCircle)
    {
        putValue(MiddlewareConstant.MW_CIRCLE, aCircle);
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

    public void setConcatnateReferenceNumber(
            int aConcatnateReferenceNumber)
    {
        putValue(MiddlewareConstant.MW_CONCAT_REF_NUM, Integer.toString(aConcatnateReferenceNumber));
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

    public void setDeliveryStatus(
            String aDeliveryStatus)
    {
        putValue(MiddlewareConstant.MW_DELIVERY_STATUS, aDeliveryStatus);
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
        putValue(MiddlewareConstant.MW_ADD_ERROR_INFO, lValue == null ? aAddErrorInfo : (lValue + " : " + aAddErrorInfo));
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

    public void setDndPreferences(
            String aDndPreferences)
    {
        putValue(MiddlewareConstant.MW_DND_ENABLE, aDndPreferences);
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

    public void setDnPayloadStatus(
            String aDnPayloadStatus)
    {
        putValue(MiddlewareConstant.MW_DN_PAYLOAD_STATUS, aDnPayloadStatus);
    }

    public void setDomesticPromoTraBlockoutPurge(
            boolean aDomesticPromoTraBlockoutPurge)
    {
        putValue(MiddlewareConstant.MW_DOMESTIC_PROMO_TRAI_BLOCKOUT_PURGE, MessageUtil.getStringFromBoolean(aDomesticPromoTraBlockoutPurge));
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

    public void setIndicateFinalDn(
            String aIndicateFinalDn)
    {
        putValue(MiddlewareConstant.MW_INDICATE_DN_FINAL, aIndicateFinalDn);
    }

    public void setInterfaceCoutryCode(
            String aIntfCountryCode)
    {
        putValue(MiddlewareConstant.MW_INTF_COUNTRY_CODE, aIntfCountryCode);
    }

    public void setInterfaceRejected(
            boolean aInterfaceReject)
    {
        putValue(MiddlewareConstant.MW_INTERFACE_REJECTED, MessageUtil.getStringFromBoolean(aInterfaceReject));
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
            String aHeaderMasked)
    {
        putValue(MiddlewareConstant.MW_IS_HEADER_MASKED, aHeaderMasked);
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
        putValue(MiddlewareConstant.MW_LONG_MSG, aLongMessage);
    }

    public void setMaskedHeader(
            String aMaskedHeader)
    {
        putValue(MiddlewareConstant.MW_MASKED_HEADER, aMaskedHeader);
    }

    public void setMaxValidityInSec(
            int aMaxValidityInSec)
    {
        putValue(MiddlewareConstant.MW_MAX_VALIDITY_IN_SEC, MessageUtil.getStringFromInt(aMaxValidityInSec));
    }

    public void setMessage(
            String aMessage)
    {
        putValue(MiddlewareConstant.MW_MSG, aMessage);
    }

    public void setMessageActualReceivedDate(
            Date aMessageActualReceivedDate)
    {
        putValue(MiddlewareConstant.MW_MSG_ACTUAL_RECEIVED_DATE, DateTimeUtility.getFormattedDateTime(aMessageActualReceivedDate, DateTimeFormat.DEFAULT_DATE_ONLY));
    }

    public void setMessageActualReceivedTime(
            Date aMessageActualReceivedTime)
    {
        putValue(MiddlewareConstant.MW_MSG_ACTUAL_RECEIVED_TIME, DateTimeUtility.getFormattedDateTime(aMessageActualReceivedTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
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

    public void setMessageId(
            String aMessageId)
    {
        putValue(MiddlewareConstant.MW_MESSAGE_ID, aMessageId);
    }

    public void setMessagePartNumber(
            int aMessagePartNumber)
    {
        putValue(MiddlewareConstant.MW_MSG_PART_NUMBER, Integer.toString(aMessagePartNumber));
    }

    public void setMessageReceivedDate(
            Date aMessageReceivedDate)
    {
        putValue(MiddlewareConstant.MW_MSG_RECEIVED_DATE, DateTimeUtility.getFormattedDateTime(aMessageReceivedDate, DateTimeFormat.DEFAULT_DATE_ONLY));
    }

    public void setMessageReceivedTime(
            Date aMessageReceivedTime)
    {
        if (aMessageReceivedTime != null)
            putValue(MiddlewareConstant.MW_MSG_RECEIVED_TIME, DateTimeUtility.getFormattedDateTime(aMessageReceivedTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
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

    public void setPayloadExpiry(
            String aPayloadExpiry)
    {
        putValue(MiddlewareConstant.MW_PAYLOAD_EXPIRY, aPayloadExpiry);
    }

    public void setPayloadRedisId(
            String aPayloadRedisId)
    {
        putValue(MiddlewareConstant.MW_PAYLOAD_REDIS_ID, aPayloadRedisId);
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
            boolean aRetryMessageReject)
    {
        putValue(MiddlewareConstant.MW_RETRY_MSG_REJECT, MessageUtil.getStringFromBoolean(aRetryMessageReject));
    }

    public void setRetryOriginalRouteId(
            String aRetryOriginalRouteId)
    {
        putValue(MiddlewareConstant.MW_RETRY_ORG_ROUTE_ID, aRetryOriginalRouteId);
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

    public void setSubClientStatusCode(
            String aSubClientStatusCode)
    {
        putValue(MiddlewareConstant.MW_SUB_CLI_STATUS_CODE, aSubClientStatusCode);
    }

    public void setSubClientStatusDesc(
            String aSubClientStatusDesc)
    {
        putValue(MiddlewareConstant.MW_SUB_CLI_STATUS_DESC, aSubClientStatusDesc);
    }

    public void setSubOriginalStatusCode(
            String aSubOriginalStatusCode)
    {
        putValue(MiddlewareConstant.MW_SUB_ORI_STATUS_CODE, aSubOriginalStatusCode);
    }

    public void setSubOriStatusDesc(
            String aSubOriStatusDesc)
    {
        putValue(MiddlewareConstant.MW_SUB_ORI_STATUS_DESC, aSubOriStatusDesc);
    }

    public void setSubStatus(
            String aSubStatus)
    {
        putValue(MiddlewareConstant.MW_SUB_STATUS, aSubStatus);
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

    public void setTerminatedCarrier(
            String aTerminatedCarrier)
    {
        putValue(MiddlewareConstant.MW_TERM_CARRIER, aTerminatedCarrier);
    }

    public void setTerminatedCircle(
            String aTerminatedCircle)
    {
        putValue(MiddlewareConstant.MW_TERM_CIRCLE, aTerminatedCircle);
    }

    public void setTimeOffset(
            String aTimeOffset)
    {
        putValue(MiddlewareConstant.MW_TIME_OFFSET, aTimeOffset);
    }

    /*
     * public void setTraTransPromoCheck(
     * String aTraTransPromoCheck)
     * {
     * putValue(MiddlewareConstant.MW_TRA_TRANSPROMO_CHK, aTraTransPromoCheck);
     * }
     */
    public void setTreatDomesticAsSpecialSeries(
            boolean aTreatDomesticAsSpecialSeries)
    {
        putValue(MiddlewareConstant.MW_TREAT_DOMESTIC_AS_SPECIAL_SERIES, MessageUtil.getStringFromBoolean(aTreatDomesticAsSpecialSeries));
    }

    public void setUdh(
            String aUdh)
    {
        putValue(MiddlewareConstant.MW_UDH, aUdh);
    }

    public void setUdhi(
            int aUdhi)
    {
        putValue(MiddlewareConstant.MW_UDHI, MessageUtil.getStringFromInt(aUdhi));
    }

    public void setUrlShortned(
            boolean aUrlShortned)
    {
        putValue(MiddlewareConstant.MW_IS_MSG_SHORTNED, MessageUtil.getStringFromBoolean(aUrlShortned));
    }

    public void setUrlTrackEnabled(
            boolean aUrlTrackEnabled)
    {
        putValue(MiddlewareConstant.MW_URL_TRACK_ENABLED, MessageUtil.getStringFromBoolean(aUrlTrackEnabled));
    }

    public void setUser(
            String aUser)
    {
        putValue(MiddlewareConstant.MW_USER, aUser);
    }

    public String getUser()
    {
        return getValue(MiddlewareConstant.MW_USER);
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

    public double getRefAddFixedRate()
    {
        return CommonUtility.getDouble(getValue(MiddlewareConstant.MW_REF_ADD_FIXED_RATE));
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

}