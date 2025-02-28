package com.itextos.beacon.commonlib.utility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MessageConvertionUtility
{

    private static final Log log = LogFactory.getLog(MessageConvertionUtility.class);

    private MessageConvertionUtility()
    {}

    public static String convertHex2String(
            String aHexMessage)
    {
        String msg = "";

        try
        {
            msg = HexUtil.convertHex2String(aHexMessage);
        }
        catch (final Exception e)
        {
            msg = aHexMessage;
            log.error("Unable to convert HexString to String. Message : '" + aHexMessage + "'", e);
        }
        return msg;
    }

    public static String convertString2HexString(
            String aUnicodeMessage)
    {

        try
        {
            return HexUtil.convertStringIntoHex(aUnicodeMessage);
        }
        catch (final Exception e)
        {
            log.error("Unable to convert String to Hexvalue. Message '" + aUnicodeMessage + "'", e);
        }
        return aUnicodeMessage;
    }

    public static boolean isHexContent(
            String aMessge)
    {

        try
        {
            return HexUtil.isHexContent(aMessge);
        }
        catch (final Exception e)
        {
            log.error("Exception while checking for the Hexvalue. Message '" + aMessge + "'", e);
        }
        return false;
    }

}
