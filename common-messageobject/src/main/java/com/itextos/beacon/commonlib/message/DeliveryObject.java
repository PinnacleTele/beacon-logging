package com.itextos.beacon.commonlib.message;

import java.util.Date;

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

public class DeliveryObject
        extends
        BaseMessage
{

    private static final long serialVersionUID = -4245459699674724263L;

    public DeliveryObject(
            ClusterType aClusterType,
            InterfaceType aInterfaceType,
            InterfaceGroup aInterfaceGroup,
            MessageType aMessageType,
            MessagePriority aMessagePriority,
            RouteType aIsInt) throws ItextosRuntimeException
    {
        super(aClusterType, aInterfaceType, aInterfaceGroup, aMessageType, aMessagePriority, aIsInt, "DeliveryObject");
    }

    public DeliveryObject(
            String aJsonString)
            throws Exception
    {
        super(aJsonString, "DeliveryObject");
    }

    public String getAalpha()
    {
        return getValue(MiddlewareConstant.MW_AALPHA);
    }

    public Date getActualCarrierSubmitTime()
    {
        final String temp = getValue(MiddlewareConstant.MW_ACTUAL_CARRIER_SUBMIT_TIME);
        return temp == null ? null : DateTimeUtility.getDateFromString(temp, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    }

    public Date getActualDeliveryTime()
    {
        final String temp = getValue(MiddlewareConstant.MW_ACTUAL_DELIVERY_TIME);
        return temp == null ? null : DateTimeUtility.getDateFromString(temp, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    }

    public SMSLog getLogBuffer() {
    	
    	return SMSLog.getInstance();
    }
 
    public String getAgeingType()
    {
        return getValue(MiddlewareConstant.MW_AGING_TYPE);
    }

    public String getAlpha()
    {
        return getValue(MiddlewareConstant.MW_AALPHA);
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

    public String getCampaignId()
    {
        return getValue(MiddlewareConstant.MW_CAMP_ID);
    }

    public String getCarrier()
    {
        return getValue(MiddlewareConstant.MW_CARRIER);
    }
    
    
    public String getMcc()
    {
        return getValue(MiddlewareConstant.MW_MCC);
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

    public String getCarrierDeliveryStatus()
    {
        return getValue(MiddlewareConstant.MW_CARRIER_DELIVERY_STATUS);
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

    public String getCarrierStatusCode()
    {
        return getValue(MiddlewareConstant.MW_CARRIER_STATUS_CODE);
    }

    public String getCarrierStatusDesc()
    {
        return getValue(MiddlewareConstant.MW_CARRIER_STATUS_DESC);
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
    
    
    public String getParam6()
    {
        return getValue(MiddlewareConstant.MW_PARAM_6);
    }

    public DeliveryObject getClonedDeliveryObject()
    {
        return (DeliveryObject) super.getClonedObject();
    }

    public String getCountry()
    {
        return getValue(MiddlewareConstant.MW_COUNTRY);
    }

    public int getDcs()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_DCS));
    }

    public String getDeliveryStatus()
    {
        return getValue(MiddlewareConstant.MW_DELIVERY_STATUS);
    }

    public Date getDeliveryTime()
    {
        final String temp = getValue(MiddlewareConstant.MW_DELIVERY_TIME);
        return temp == null ? null : DateTimeUtility.getDateFromString(temp, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    }

    public String getDlrFromInternal()
    {
        return getValue(MiddlewareConstant.MW_DLR_FROM_INTERNAL);
    }

    public String getDltEntityId()
    {
        return getValue(MiddlewareConstant.MW_DLT_ENTITY_ID);
    }

    public double getDltRate()
    {
        return CommonUtility.getDouble(getValue(MiddlewareConstant.MW_DLT_RATE));
    }

    public String getDltTemplateId()
    {
        return getValue(MiddlewareConstant.MW_DLT_TEMPLATE_ID);
    }
    
    
    public String getDltTelemarketerId()
    {
        return getValue(MiddlewareConstant.MW_DLT_TMA_ID);
    }

    public String getDnClientStatusCode()
    {
        return getValue(MiddlewareConstant.MW_DN_CLI_STATUS_CODE);
    }

    public String getDnClientStatusDesc()
    {
        return getValue(MiddlewareConstant.MW_DN_CLI_STATUS_DESC);
    }

    public String getDnFilureType()
    {
        return getValue(MiddlewareConstant.MW_DN_FAILURE_TYPE);
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

    public String getHeader()
    {
        return getValue(MiddlewareConstant.MW_DELIVERY_HEADER);
    }

    public String getHeaderMasked()
    {
        return getValue(MiddlewareConstant.MW_IS_HEADER_MASKED);
    }

    public int getIndicateDnFinal()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_INDICATE_DN_FINAL));
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

    public MessageRequest getMessageRequestForRetry() throws ItextosRuntimeException
    {
        final MessageRequest lMessageRequest = new MessageRequest(getClusterType(), getInterfaceType(), getInterfaceGroupType(), getMessageType(), getMessagePriority(), getMessageRouteType());

        lMessageRequest.setClientId(getClientId());
        lMessageRequest.setBaseMessageId(getBaseMessageId());
        lMessageRequest.setRouteId(getRouteId());
        lMessageRequest.setHeader(getHeader());
        lMessageRequest.setMobileNumber(getMobileNumber());
        lMessageRequest.setAlpha(getAlpha());
        lMessageRequest.setDcs(getDcs());
        lMessageRequest.setIsHexMessage(isHexMessage());
        lMessageRequest.setDlrRequestFromClient(isDlrRequestFromClient());
        lMessageRequest.setSmsPriority(getSmsPriority());
        lMessageRequest.setFileId(getFileId());
        lMessageRequest.setCarrier(getCarrier());
        lMessageRequest.setMcc(getMcc());
        lMessageRequest.setMnc(getMnc());
        lMessageRequest.setCircle(getCircle());
        lMessageRequest.setClientMessageId(getClientMessageId());
        lMessageRequest.setParam6(getParam6());
        lMessageRequest.setMessageTotalParts(getMessageTotalParts());
        lMessageRequest.setScheduleDateTime(getScheduleDateTime());
        lMessageRequest.setBillType(getBillType());
        lMessageRequest.setMessageTag(getMessageTag());
        lMessageRequest.setAttemptCount(getAttemptCount());
        lMessageRequest.setCountry(getCountry());
        lMessageRequest.setClientHeader(getClientHeader());
        lMessageRequest.setMaskedHeader(getMaskedHeader());
        lMessageRequest.setSmsRetryEnabled(getSmsRetryEnabled());
        lMessageRequest.setSmscId(getSmscId());
        lMessageRequest.setBaseMessageId(getBaseMessageId());
        lMessageRequest.setFeatureCode(getFeatureCode());
        lMessageRequest.setMessageClass(getMessageClass());
        lMessageRequest.setMessageExpiryInSec(getMessageExpiryInSec());
        lMessageRequest.setCarrierDateTimeFormat(getCarrierDateTimeFormat());
        lMessageRequest.setLongMessage(getLongMessage());
        lMessageRequest.setTimeOffset(getTimeOffset());
        lMessageRequest.setTreatDomesticAsSpecialSeries(isTreatDomesticAsSpecialSeries());
        lMessageRequest.setFileName(getFileName());
        lMessageRequest.setDltEntityId(getDltEntityId());
        lMessageRequest.setDltTemplateId(getDltTemplateId());
        lMessageRequest.setDltTelemarketerId(getDltTelemarketerId());

        lMessageRequest.setAppType(getAppType());
        lMessageRequest.setSubOriginalStatusCode(getSubOriStatusCode());
        lMessageRequest.setSyncRequest(isSyncRequest());
        lMessageRequest.setRetryAttempt(getRetryAttempt());
        lMessageRequest.setCarrierOrigianlStatusCode(getCarrierOrigianlStatusCode());
        lMessageRequest.setCarrierOrigianlStatusDesc(getCarrierOrigianlStatusDesc());
        lMessageRequest.setDnOrigianlstatusCode(getDnOrigianlstatusCode());
        lMessageRequest.setDnOriStatusDesc(getDnOriStatusDesc());
        lMessageRequest.setIsWalletDeduct(isWalletDeduct());
        lMessageRequest.setSmsRate(getSmsRate());
        lMessageRequest.setDltRate(getDltRate());
        lMessageRequest.setUser(getUser());
        lMessageRequest.setBaseCurrency(getBaseCurrency());
        lMessageRequest.setBaseSmsRate(getBaseSmsRate());
        lMessageRequest.setBaseAddFixedRate(getBaseAddFixedRate());
        lMessageRequest.setBillingSmsRate(getBillingSmsRate());
        lMessageRequest.setBillingAddFixedRate(getBillingAddFixedRate());
        lMessageRequest.setRefCurrency(getRefCurrency());
        lMessageRequest.setRefSmsRate(getRefSmsRate());
        lMessageRequest.setRefAddFixedRate(getRefAddFixedSmsRate());
        lMessageRequest.setInvoiceBasedOn(getInvoiceBasedOn());
        lMessageRequest.setSmppInstance(getSmppInstance());

        final MessagePart MessagePart = new MessagePart(getMessageId());
        MessagePart.setMessage(getMessage());
        MessagePart.setMessageReceivedTime(getMessageReceivedTime());
        MessagePart.setMessageReceivedDate(getMessageReceivedDate());
        MessagePart.setMessagePartNumber(getMessagePartNumber());
        MessagePart.setMessageActualReceivedTime(getMessageActualReceivedTime());
        MessagePart.setMessageActualReceivedDate(getMessageActualReceivedDate());
        MessagePart.setUdhi(getUdhi());
        MessagePart.setCarrierReceivedTime(getCarrierReceivedTime());
        MessagePart.setCarrierAcknowledgeId(getCarrierAcknowledgeId());
        MessagePart.setCarrierSystemId(getCarrierSystemId());
        MessagePart.setCarrierFullDn(getCarrierFullDn());
        MessagePart.setDeliveryStatus(getDeliveryStatus());
        MessagePart.setCarrierSubmitTime(getCarrierSubmitTime());
        MessagePart.setActualCarrierSubmitTime(getActualCarrierSubmitTime());

        lMessageRequest.addMessagePart(MessagePart);

        return lMessageRequest;
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

    public String getOtpRetyChannel()
    {
        return getValue(MiddlewareConstant.MW_OTP_RETRY_CHANNEL);
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

    public Date getRetryCurrentTime()
    {
        final String temp = getValue(MiddlewareConstant.MW_RETRY_CURRENT_TIME);
        return temp == null ? null : DateTimeUtility.getDateFromString(temp, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    }

    public int getRetryInterval()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_RETRY_INTERVAL));
    }

    public String getRetryMsgReject()
    {
        return getValue(MiddlewareConstant.MW_RETRY_MSG_REJECT);
    }

    public Date getRetryTime()
    {
        final String temp = getValue(MiddlewareConstant.MW_RETRY_TIME);
        return temp == null ? null : DateTimeUtility.getDateFromString(temp, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    }

    public String getRouteId()
    {
        return getValue(MiddlewareConstant.MW_ROUTE_ID);
    }

    public String getRouteLogicId()
    {
        return getValue(MiddlewareConstant.MW_ROUTE_LOGIC_ID);
    }

    public String getRouteType()
    {
        return getValue(MiddlewareConstant.MW_ROUTE_TYPE);
    }

    public Date getScheduleDateTime()
    {
        final String temp = getValue(MiddlewareConstant.MW_SCHE_DATE_TIME);
        return temp == null ? null : DateTimeUtility.getDateFromString(temp, DateTimeFormat.DEFAULT);
    }

    public String getSmppDestAddrNpi()
    {
        return getValue(MiddlewareConstant.MW_SMPP_DEST_ADDR_NPI);
    }

    public String getSmppDestAddrTon()
    {
        return getValue(MiddlewareConstant.MW_SMPP_DEST_ADDR_TON);
    }

    public String getSmppEsmClass()
    {
        return getValue(MiddlewareConstant.MW_SMPP_ESM_CLASS);
    }

    public String getSmppLastSent()
    {
        return getValue(MiddlewareConstant.MW_SMPP_LAST_SENT);
    }

    public String getSmppServiceType()
    {
        return getValue(MiddlewareConstant.MW_SMPP_SERVICE_TYPE);
    }

    public String getSmppSourceAddrNpi()
    {
        return getValue(MiddlewareConstant.MW_SMPP_SOURCE_ADDR_NPI);
    }

    public String getSmppSourceAddrTon()
    {
        return getValue(MiddlewareConstant.MW_SMPP_SOURCE_ADDR_TON);
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

    public String getSubOriStatusCode()
    {
        return getValue(MiddlewareConstant.MW_SUB_ORI_STATUS_CODE);
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

    public String getVoiceConfigId()
    {
        return getValue(MiddlewareConstant.MW_VOICE_CONFIG_ID);
    }

    public boolean isCurrent()
    {
        return CommonUtility.isTrue(getValue(MiddlewareConstant.MW_IS_CURRENT));
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

    public boolean isVoiceDlr()
    {
        return CommonUtility.isEnabled(getValue(MiddlewareConstant.MW_IS_VOICE_DLR));
    }

    public boolean isWalletDeduct()
    {
        return CommonUtility.isTrue(getValue(MiddlewareConstant.MW_IS_WC_DEDUCT));
    }

    public void setAalpha(
            String aAalpha)
    {
        putValue(MiddlewareConstant.MW_AALPHA, aAalpha);
    }

    public void setActualCarrierSubmitTime(
            Date aActualCarrierSubmitTime)
    {
        if (aActualCarrierSubmitTime != null)
            putValue(MiddlewareConstant.MW_ACTUAL_CARRIER_SUBMIT_TIME, DateTimeUtility.getFormattedDateTime(aActualCarrierSubmitTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
    }

    public void setActualDeliveryTime(
            Date aActualDeliveryTime)
    {
        putValue(MiddlewareConstant.MW_ACTUAL_DELIVERY_TIME, DateTimeUtility.getFormattedDateTime(aActualDeliveryTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
    }

    public void setAgeingType(
            String aAgeingType)
    {
        putValue(MiddlewareConstant.MW_AGING_TYPE, aAgeingType);
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

    public void setBillType(
            int aBillType)
    {
        putValue(MiddlewareConstant.MW_BILL_TYPE, MessageUtil.getStringFromInt(aBillType));
    }

    public void setCampaignId(
            String aCampaignId)
    {
        putValue(MiddlewareConstant.MW_CAMP_ID, aCampaignId);
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

    public void setParam6(
            String param6)
    {
        putValue(MiddlewareConstant.MW_PARAM_6, param6);
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

    public void setCarrierDeliveryStatus(
            String aCarrierDeliveryStatus)
    {
        putValue(MiddlewareConstant.MW_CARRIER_DELIVERY_STATUS, aCarrierDeliveryStatus);
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

    public void setCarrierStatusCode(
            String aCarrierStatusCode)
    {
        putValue(MiddlewareConstant.MW_CARRIER_STATUS_CODE, aCarrierStatusCode);
    }

    public void setCarrierStatusDesc(
            String aCarrierStatusDesc)
    {
        putValue(MiddlewareConstant.MW_CARRIER_STATUS_DESC, aCarrierStatusDesc);
    }

    public void setCarrierSubmitTime(
            Date aCarrierSubmitTime)
    {
        if (aCarrierSubmitTime != null)
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

    public void setClientMessageId(
            String aClientMessageId)
    {
        putValue(MiddlewareConstant.MW_CLIENT_MESSAGE_ID, aClientMessageId);
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

    public void setCurrent(
            boolean aCurrent)
    {
        putValue(MiddlewareConstant.MW_IS_CURRENT, MessageUtil.getStringFromBoolean(aCurrent));
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

    public void setDeliveryTime(
            Date aDeliveryTime)
    {
        putValue(MiddlewareConstant.MW_DELIVERY_TIME, DateTimeUtility.getFormattedDateTime(aDeliveryTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
    }

    public void setDlrFromInternal(
            String aDlrFromInternal)
    {
        putValue(MiddlewareConstant.MW_DLR_FROM_INTERNAL, aDlrFromInternal);
    }

    public void setDlrRequestFromClient(
            boolean aDlrRequestFromClient)
    {
        putValue(MiddlewareConstant.MW_DLR_REQ_FROM_CLI, MessageUtil.getStringFromBoolean(aDlrRequestFromClient));
    }

    public void setDltEntityId(
            String aDltEntityId)
    {
        putValue(MiddlewareConstant.MW_DLT_ENTITY_ID, aDltEntityId);
    }

    public void setDltRate(
            double aDltRate)
    {
        putValue(MiddlewareConstant.MW_DLT_RATE, MessageUtil.getStringFromDouble(aDltRate));
    }

    public void setDltTemplateId(
            String aDltTemplateId)
    {
        putValue(MiddlewareConstant.MW_DLT_TEMPLATE_ID, aDltTemplateId);
    }

    public void setDnClientStatusCode(
            String aDnClientStatusCode)
    {
        putValue(MiddlewareConstant.MW_DN_CLI_STATUS_CODE, aDnClientStatusCode);
    }

    public void setDnClientStatusDesc(
            String aDnClientStatusDesc)
    {
        putValue(MiddlewareConstant.MW_DN_CLI_STATUS_DESC, aDnClientStatusDesc);
    }

    public void setDnFilureType(
            String aDnFailureType)
    {
        putValue(MiddlewareConstant.MW_DN_FAILURE_TYPE, aDnFailureType);
    }

    public void setDnLatencyOrigianlInMillis(
            long aDnLatencyOrigianlInMillis)
    {
        putValue(MiddlewareConstant.MW_DELV_LATENCY_ORG_IN_MILLIS, Long.toString(aDnLatencyOrigianlInMillis));
    }

    public void setDnLatencySlaInMillis(
            long aDnLatencySlaInMillis)
    {
        putValue(MiddlewareConstant.MW_DELV_LATENCY_SLA_IN_MILLIS, Long.toString(aDnLatencySlaInMillis));
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

    public void setHeader(
            String aHeader)
    {
        putValue(MiddlewareConstant.MW_DELIVERY_HEADER, aHeader);
    }

    public void setHeaderMasked(
            String aHeaderMasked)
    {
        putValue(MiddlewareConstant.MW_IS_HEADER_MASKED, aHeaderMasked);
    }

    public void setIndicateDnFinal(
            int aIndicateDnFinal)
    {
        putValue(MiddlewareConstant.MW_INDICATE_DN_FINAL, MessageUtil.getStringFromInt(aIndicateDnFinal));
    }

    public void setInterfaceRejected(
            boolean aInterfaceRejected)
    {
        putValue(MiddlewareConstant.MW_INTERFACE_REJECTED, MessageUtil.getStringFromBoolean(aInterfaceRejected));
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

    public void setMsgReceivedTime(
            Date aMsgReceivedTime)
    {
        putValue(MiddlewareConstant.MW_MSG_RECEIVED_TIME, DateTimeUtility.getFormattedDateTime(aMsgReceivedTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
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

    public void setOtpRetyChannel(
            String aOtpRetyChannel)
    {
        putValue(MiddlewareConstant.MW_OTP_RETRY_CHANNEL, aOtpRetyChannel);
    }

    public void setPayloadExpiry(
            Date aPayloadExpiry)
    {
        putValue(MiddlewareConstant.MW_PAYLOAD_EXPIRY, DateTimeUtility.getFormattedDateTime(aPayloadExpiry, DateTimeFormat.DEFAULT));
    }

    public void setPayloadRedisId(
            String aPayloadRedisId)
    {
        putValue(MiddlewareConstant.MW_PAYLOAD_REDIS_ID, aPayloadRedisId);
    }

    public void setPlatformRejected(
            boolean aPlatformRejected)
    {
        putValue(MiddlewareConstant.MW_PLATFROM_REJECTED, MessageUtil.getStringFromBoolean(aPlatformRejected));
    }

    public void setPlatfromRejected(
            boolean aPlatfromReject)
    {
        putValue(MiddlewareConstant.MW_PLATFROM_REJECTED, MessageUtil.getStringFromBoolean(aPlatfromReject));
    }

    public void setRetryAlternateRouteId(
            String aRetryAlternateRouteId)
    {
        putValue(MiddlewareConstant.MW_RETRY_ALE_ROUTE_ID, aRetryAlternateRouteId);
    }

    public void setRetryAttempt(
            int aRetryAttempt)
    {
        putValue(MiddlewareConstant.MW_RETRY_ATTEMPT, MessageUtil.getStringFromInt(aRetryAttempt));
    }

    public void setRetryCurrentTime(
            Date aRetryCurrentTime)
    {
        putValue(MiddlewareConstant.MW_RETRY_CURRENT_TIME, DateTimeUtility.getFormattedDateTime(aRetryCurrentTime, DateTimeFormat.DEFAULT));
    }

    public void setRetryInterval(
            int aRetryInterval)
    {
        putValue(MiddlewareConstant.MW_RETRY_INTERVAL, MessageUtil.getStringFromInt(aRetryInterval));
    }

    public void setRetryMsgReject(
            String aRetryMsgReject)
    {
        putValue(MiddlewareConstant.MW_RETRY_MSG_REJECT, aRetryMsgReject);
    }

    public void setRetryOriginalRouteId(
            String aRetryOriginalRouteId)
    {
        putValue(MiddlewareConstant.MW_RETRY_ORG_ROUTE_ID, aRetryOriginalRouteId);
    }

    public void setRetryTime(
            Date aRetryTime)
    {
        putValue(MiddlewareConstant.MW_RETRY_TIME, DateTimeUtility.getFormattedDateTime(aRetryTime, DateTimeFormat.DEFAULT));
    }

    public void setRouteId(
            String aRouteId)
    {
        putValue(MiddlewareConstant.MW_ROUTE_ID, aRouteId);
    }

    public void setRouteLogicId(
            String aRouteLogicId)
    {
        putValue(MiddlewareConstant.MW_ROUTE_LOGIC_ID, aRouteLogicId);
    }

    public void setRouteType(
            String aRouteType)
    {
        putValue(MiddlewareConstant.MW_ROUTE_TYPE, aRouteType);
    }

    public void setScheduleDateTime(
            Date aScheduleDateTime)
    {
        if (aScheduleDateTime != null)
            putValue(MiddlewareConstant.MW_SCHE_DATE_TIME, DateTimeUtility.getFormattedDateTime(aScheduleDateTime, DateTimeFormat.DEFAULT));
    }

    public void setSingleDnInsertRedisKey(
            String aSingleDnInsertRedisKey)
    {
        putValue(MiddlewareConstant.MW_SINGLE_DN_INSERT_REDIS_KEY, aSingleDnInsertRedisKey);
    }

    public void setSmppDestAddrNpi(
            String aSmppDestAddrNpi)
    {
        putValue(MiddlewareConstant.MW_SMPP_DEST_ADDR_NPI, aSmppDestAddrNpi);
    }

    public void setSmppDesteAddrTon(
            String aSmppDestAddrTon)
    {
        putValue(MiddlewareConstant.MW_SMPP_DEST_ADDR_TON, aSmppDestAddrTon);
    }

    public void setSmppEsmClass(
            String aSmppEsmClass)
    {
        putValue(MiddlewareConstant.MW_SMPP_ESM_CLASS, aSmppEsmClass);
    }

    public void setSmppLastSent(
            String aSmppLastSent)
    {
        putValue(MiddlewareConstant.MW_SMPP_LAST_SENT, aSmppLastSent);
    }

    public void setSmppServiceType(
            String aSmppServiceType)
    {
        putValue(MiddlewareConstant.MW_SMPP_SERVICE_TYPE, aSmppServiceType);
    }

    public void setSmppSourceAddrNpi(
            String aSmppSourceAddrNpi)
    {
        putValue(MiddlewareConstant.MW_SMPP_SOURCE_ADDR_NPI, aSmppSourceAddrNpi);
    }

    public void setSmppSourceAddrTon(
            String aSmppSourceAddrTon)
    {
        putValue(MiddlewareConstant.MW_SMPP_SOURCE_ADDR_TON, aSmppSourceAddrTon);
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

    public void setSubOriStatusCode(
            String aSubOriStatusCode)
    {
        putValue(MiddlewareConstant.MW_SUB_ORI_STATUS_CODE, aSubOriStatusCode);
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

    public void setVoiceConfigId(
            String aVoiceConfigId)
    {
        putValue(MiddlewareConstant.MW_VOICE_CONFIG_ID, aVoiceConfigId);
    }

    public void setVoiceDlr(
            int aVoiceDlr)
    {
        putValue(MiddlewareConstant.MW_IS_VOICE_DLR, MessageUtil.getStringFromInt(aVoiceDlr));
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

    public void setUser(
            String aUser)
    {
        putValue(MiddlewareConstant.MW_USER, aUser);
    }

    public String getUser()
    {
        return getValue(MiddlewareConstant.MW_USER);
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

    public double getRefAddFixedSmsRate()
    {
        return CommonUtility.getDouble(getValue(MiddlewareConstant.MW_REF_ADD_FIXED_RATE));
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