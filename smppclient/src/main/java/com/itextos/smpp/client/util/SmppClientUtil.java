package com.itextos.smpp.client.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.tlv.Tlv;
import com.itextos.smpp.client.pojo.BindRequest;
import com.itextos.smpp.client.pojo.SubmitRequest;

public class SmppClientUtil
{

    private static final Log log = LogFactory.getLog(SmppClientUtil.class);

    private SmppClientUtil()
    {}

    public static BindRequest getBindRequest()
    {
        final SmppClientProperties properties   = SmppClientProperties.getInstance();

        final BindRequest          lBindRequest = new BindRequest();
        lBindRequest.setBindType(properties.getBindType());
        lBindRequest.setHost(properties.getHost());
        lBindRequest.setPort(properties.getPort());
        lBindRequest.setSystemId(properties.getSystemId());
        lBindRequest.setPassword(properties.getPassword());
        lBindRequest.setWindowSize(properties.getWindowSize());

        return lBindRequest;
    }

    public static SubmitRequest getSubmitSMRequest()
    {
        final SmppClientProperties properties     = SmppClientProperties.getInstance();

        final SubmitRequest        lSubmitRequest = new SubmitRequest();

        lSubmitRequest.setSourceHeader(properties.getSourceAddress());
        lSubmitRequest.setMobileNumber(properties.getDestinationAddress());
        lSubmitRequest.setRegisteredDelivery(properties.getRegisteredDelivery());
        lSubmitRequest.setValidityPeriod(properties.getValidityPeriod());
        lSubmitRequest.setMessage(properties.getMessage());
        lSubmitRequest.setScheduleDeliveryTime(properties.getScheduleTime());
        lSubmitRequest.setDcs(properties.getDcs());
        lSubmitRequest.setCharset(properties.getCharSet());
        lSubmitRequest.setNoOfMessages(properties.getNumberOfMessages());

        final String lOptionalValues = properties.getOptionalParams();
        if (!lOptionalValues.isBlank())
            lSubmitRequest.setOptionalParams(getOptionalPrams(lOptionalValues));

        return lSubmitRequest;
    }

    private static List<Tlv> getOptionalPrams(
            String aOptionalValues)
    {
        final List<Tlv> lTlvList     = new ArrayList<>();
        final String[]  lTeampParams = aOptionalValues.split(",");

        for (final String temp : lTeampParams)
        {
            final String[] lTags = temp.split(":");
            final Tlv      lTlv  = new Tlv(Short.parseShort(lTags[0]), lTags[1].getBytes());

            lTlvList.add(lTlv);
        }

        return lTlvList;
    }

    public static String nullCheck(
            Object aObject)
    {
        return nullCheck(aObject, false);
    }

    public static String nullCheck(
            Object aObject,
            boolean aTrimIt)
    {
        return aObject == null ? (aTrimIt ? "" : " ") : (aTrimIt ? aObject.toString().trim() : aObject.toString());
    }

    public static int getInteger(
            String aIntegerString)
    {
        return getInteger(aIntegerString, 0);
    }

    private static int getInteger(
            String aIntegerString,
            int aDefaultValue)
    {
        int returnValue = aDefaultValue;

        try
        {
            returnValue = Integer.parseInt(aIntegerString);
        }
        catch (final Exception e)
        {}
        return returnValue;
    }

    public static long getLong(
            String aLongString)
    {
        return getLong(aLongString, 0);
    }

    private static long getLong(
            String aLongString,
            long aDefaultValue)
    {
        long returnValue = aDefaultValue;

        try
        {
            returnValue = Long.parseLong(aLongString);
        }
        catch (final Exception e)
        {}
        return returnValue;
    }

    public static SmppBindType getBindType(
            int aBindType)
    {
        SmppBindType lBindType = null;

        switch (aBindType)
        {
            case 1:
                lBindType = SmppBindType.TRANSMITTER;
                break;

            case 2:
                lBindType = SmppBindType.RECEIVER;
                break;

            case 3:
                lBindType = SmppBindType.TRANSCEIVER;
                break;

            default:
                lBindType = SmppBindType.TRANSCEIVER;
                break;
        }

        return lBindType;
    }

}
