package com.itextos.beacon.commonlib.message.utility;

import java.util.Map;

import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class MessageUtil
{

    private MessageUtil()
    {}

    public static String getHeaderId(
            MessageRequest aMessageRequest)
    {
        return aMessageRequest.getHeader();
    }

    public static void setHeaderId(
            MessageRequest aMessageRequest,
            String aNewHeader)
    {
        final String temp = CommonUtility.nullCheck(aNewHeader, true);
        if (!temp.isEmpty())
            aMessageRequest.setHeader(temp);
    }

    public static void setHeaderId(
            DeliveryObject aDeliveryObject,
            String aNewHeader)
    {
        final String temp = CommonUtility.nullCheck(aNewHeader, true);
        if (!temp.isEmpty())
            aDeliveryObject.setHeader(temp);
    }

    public static String getHeaderId(
            DeliveryObject aDeliveryObject)
    {
        return aDeliveryObject.getHeader();
    }

    public static String getValueFromMap(
            Map<String, Object> aMap,
            MiddlewareConstant aMiddlewareConstant)
    {
    	/*
        final String lValue = (String) aMap.get(aMiddlewareConstant.getName());
        if (lValue == null)
            throw new ItextosRuntimeException("No valid value found for " + aMiddlewareConstant);
        aMap.remove(aMiddlewareConstant.getName());
        return lValue;
        */

        final String lValue = (String) aMap.get(aMiddlewareConstant.getName());
        
        aMap.remove(aMiddlewareConstant.getName());
        return lValue;
    }

    public static String getStringFromInt(
            int aValue)
    {
        return Integer.toString(aValue);
    }

    public static String getStringFromBoolean(
            boolean aBoolValue)
    {
        return aBoolValue ? "1" : "0";
    }

    public static String getStringFromDouble(
            double aValue)
    {
        return Double.toString(aValue);
    }

}