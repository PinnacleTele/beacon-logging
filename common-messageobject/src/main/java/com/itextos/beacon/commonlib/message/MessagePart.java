package com.itextos.beacon.commonlib.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

public class MessagePart
        implements
        Serializable
{

    private static final long                     serialVersionUID = 5291603119138053228L;

    private static final List<MiddlewareConstant> CONSTANTS        = new ArrayList<>();

    static
    {
        CONSTANTS.add(MiddlewareConstant.MW_MESSAGE_ID);
        CONSTANTS.add(MiddlewareConstant.MW_ACTUAL_CARRIER_SUBMIT_TIME);
        CONSTANTS.add(MiddlewareConstant.MW_ALTER_MSG);
        CONSTANTS.add(MiddlewareConstant.MW_CALLBACK_PARAMS);
        CONSTANTS.add(MiddlewareConstant.MW_CALLBACK_URL);
        CONSTANTS.add(MiddlewareConstant.MW_CARRIER_SUBMIT_TIME);
        CONSTANTS.add(MiddlewareConstant.MW_CONCAT_REF_NUM);
        CONSTANTS.add(MiddlewareConstant.MW_MSG);
        CONSTANTS.add(MiddlewareConstant.MW_MSG_ACTUAL_RECEIVED_DATE);
        CONSTANTS.add(MiddlewareConstant.MW_MSG_ACTUAL_RECEIVED_TIME);
        CONSTANTS.add(MiddlewareConstant.MW_MSG_RECEIVED_DATE);
        CONSTANTS.add(MiddlewareConstant.MW_MSG_RECEIVED_TIME);
        CONSTANTS.add(MiddlewareConstant.MW_UDH);
        CONSTANTS.add(MiddlewareConstant.MW_MSG_PART_NUMBER);
        CONSTANTS.add(MiddlewareConstant.MW_UDHI);
        CONSTANTS.add(MiddlewareConstant.MW_CARRIER_RECEIVED_TIME);
        CONSTANTS.add(MiddlewareConstant.MW_CARRIER_ACKNOWLEDGE_ID);
        CONSTANTS.add(MiddlewareConstant.MW_CARRIER_SYSTEM_ID);
        CONSTANTS.add(MiddlewareConstant.MW_CARRIER_FULL_DN);
        CONSTANTS.add(MiddlewareConstant.MW_DELIVERY_STATUS);
        CONSTANTS.add(MiddlewareConstant.MW_SMPP_MSG_RECEIVED_TIME);
    }

    private final Map<String, String> mAttributes = new HashMap<>();

    public MessagePart(
            JSONObject aJsonObj)
    {
        super();

        for (final MiddlewareConstant mc : CONSTANTS)
        {
            final String value = (String) aJsonObj.get(mc.getKey());
            if (value != null)
                putValue(mc, value);
        }
    }

    public MessagePart(
            String aMessageId)
    {
        super();
        putValue(MiddlewareConstant.MW_MESSAGE_ID, aMessageId);
    }

    public Date getActualCarrierSubmitTime()
    {
        final String temp = getValue(MiddlewareConstant.MW_ACTUAL_CARRIER_SUBMIT_TIME);
        return temp == null ? null : DateTimeUtility.getDateFromString(temp, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    }

    public String getAlterMessage()
    {
        return getValue(MiddlewareConstant.MW_ALTER_MSG);
    }

    public String getCallBackParams()
    {
        return getValue(MiddlewareConstant.MW_CALLBACK_PARAMS);
    }

    public String getCallBackUrl()
    {
        return getValue(MiddlewareConstant.MW_CALLBACK_URL);
    }

    public String getCarrierAcknowledgeId()
    {
        return getValue(MiddlewareConstant.MW_CARRIER_ACKNOWLEDGE_ID);
    }

    public String getCarrierFullDn()
    {
        return getValue(MiddlewareConstant.MW_CARRIER_FULL_DN);
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

    public int getConcatnateReferenceNumber()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_CONCAT_REF_NUM));
    }

    public String getDeliveryStatus()
    {
        return getValue(MiddlewareConstant.MW_DELIVERY_STATUS);
    }

    JSONObject getJson()
    {
        final JSONObject jsonObj = new JSONObject();
        mAttributes.remove(MiddlewareConstant.MW_LOG_BUFFER.getKey());
        jsonObj.putAll(mAttributes);
        return jsonObj;
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

    public String getUdh()
    {
        return getValue(MiddlewareConstant.MW_UDH);
    }

    public int getUdhi()
    {
        return CommonUtility.getInteger(getValue(MiddlewareConstant.MW_UDHI));
    }

    private String getValue(
            MiddlewareConstant aMWConstant)
    {
        return mAttributes.get(aMWConstant.getKey());
    }

    private void putValue(
            MiddlewareConstant aMWConstant,
            String aValue)
    {
        if (CONSTANTS.contains(aMWConstant)) {
            mAttributes.put(aMWConstant.getKey(), aValue);
        }else {}
         //   throw new ItextosRuntimeException("Middleware Constant '" + aMWConstant + "' is not in the specified list.");
    }

    public String getValueExt(
            MiddlewareConstant aMWConstant)
    {
        return mAttributes.get(aMWConstant.getKey());
    }

    public void putValueExt(
            MiddlewareConstant aMWConstant,
            String aValue)
    {
        if (CONSTANTS.contains(aMWConstant)) {
            mAttributes.put(aMWConstant.getKey(), aValue);
        }else {}
            //throw new ItextosRuntimeException("Middleware Constant '" + aMWConstant + "' is not in the specified list.");
    }

    public void setActualCarrierSubmitTime(
            Date aActualCarrierSubmitTime)
    {
        if (aActualCarrierSubmitTime != null)
            putValue(MiddlewareConstant.MW_ACTUAL_CARRIER_SUBMIT_TIME, DateTimeUtility.getFormattedDateTime(aActualCarrierSubmitTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
    }

    public void setAlterMessage(
            String aAlterMessage)
    {
        putValue(MiddlewareConstant.MW_ALTER_MSG, aAlterMessage);
    }

    public void setCallBackParams(
            String aCallBackParams)
    {
        putValue(MiddlewareConstant.MW_CALLBACK_PARAMS, aCallBackParams);
    }

    public void setCallBackUrl(
            String aCallBackUrl)
    {
        putValue(MiddlewareConstant.MW_CALLBACK_URL, aCallBackUrl);
    }

    public void setCarrierAcknowledgeId(
            String aCarrierAcknowledgeId)
    {
        putValue(MiddlewareConstant.MW_CARRIER_ACKNOWLEDGE_ID, aCarrierAcknowledgeId);
    }

    public void setCarrierFullDn(
            String aCarrierFullDn)
    {
        putValue(MiddlewareConstant.MW_CARRIER_FULL_DN, aCarrierFullDn);
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

    public void setConcatnateReferenceNumber(
            int aConcatnateReferenceNumber)
    {
        putValue(MiddlewareConstant.MW_CONCAT_REF_NUM, Integer.toString(aConcatnateReferenceNumber));
    }

    public void setDeliveryStatus(
            String aDeliveryStatus)
    {
        putValue(MiddlewareConstant.MW_DELIVERY_STATUS, aDeliveryStatus);
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

    public void setMessagePartNumber(
            int aMessagePartNumber)
    {
        putValue(MiddlewareConstant.MW_MSG_PART_NUMBER, MessageUtil.getStringFromInt(aMessagePartNumber));
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

}