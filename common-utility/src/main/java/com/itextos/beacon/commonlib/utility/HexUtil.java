package com.itextos.beacon.commonlib.utility;

import java.nio.charset.StandardCharsets;

/**
 * Use {@link MessageConvertionUtility} class and its methods.
 */
@Deprecated
public class HexUtil
{

    private HexUtil()
    {}

    /**
     * Use {@link MessageConvertionUtility}{@link #convertStringIntoHex(String)}
     * method.
     *
     * @param aMessage
     *
     * @return
     *
     * @deprecated
     */
    @Deprecated
    static String convertStringIntoHex(
            String aMessage)
    {
        final StringBuilder strBuff = new StringBuilder();

        for (int i = 0; i < aMessage.length(); i++)
        {
            final String lHexStr = Integer.toHexString(aMessage.charAt(i) & 0xFFFF);

            for (int j = 0; j < (4 - lHexStr.length()); j++)
                strBuff.append("0");
            strBuff.append(lHexStr.toUpperCase());
        }
        return new String(strBuff);
    }

    /**
     * Use {@link MessageConvertionUtility}{@link #convertHex2String(String)} method
     *
     * @param aHexMessage
     *
     * @return
     *
     * @deprecated
     */
    @Deprecated
    static String convertHex2String(
            String aHexMessage)
    {
        final byte[] strMsg = toByteArray(aHexMessage);
        return new String(strMsg, StandardCharsets.UTF_16);
    }

    static boolean isHexContent(
            String aMessge)
    {
        for (final char c : aMessge.toCharArray())
            Integer.parseInt(c + "", 16);
        return true;
    }

    private static int hexCharToIntValue(
            char c)
    {
        int returnValue = -1;

        switch (c)
        {
            case '0':
                returnValue = 0;
                break;

            case '1':
                returnValue = 1;
                break;

            case '2':
                returnValue = 2;
                break;

            case '3':
                returnValue = 3;
                break;

            case '4':
                returnValue = 4;
                break;

            case '5':
                returnValue = 5;
                break;

            case '6':
                returnValue = 6;
                break;

            case '7':
                returnValue = 7;
                break;

            case '8':
                returnValue = 8;
                break;

            case '9':
                returnValue = 9;
                break;

            case 'A':
            case 'a':
                returnValue = 10;
                break;

            case 'B':
            case 'b':
                returnValue = 11;
                break;

            case 'C':
            case 'c':
                returnValue = 12;
                break;

            case 'D':
            case 'd':
                returnValue = 13;
                break;

            case 'E':
            case 'e':
                returnValue = 14;
                break;

            case 'F':
            case 'f':
                returnValue = 15;
                break;

            default:
                throw new IllegalArgumentException("The character [" + c + "] does not represent a valid hex digit");
        }
        return returnValue;
    }

    public static byte[] toByteArray(
            CharSequence hexString)
    {
        if (hexString == null)
            return null;
        return toByteArray(hexString, 0, hexString.length());
    }

    private static byte[] toByteArray(
            CharSequence hexString,
            int offset,
            int length)
    {
        if (hexString == null)
            return null;

        assertOffsetLengthValid(offset, length, hexString.length());

        // a hex string must be in increments of 2
        if ((length % 2) != 0)
            throw new IllegalArgumentException("The hex string did not contain an even number of characters [actual=" + length + "]");

        // convert hex string to byte array
        final byte[] bytes = new byte[length / 2];

        int          j     = 0;
        final int    end   = offset + length;

        for (int i = offset; i < end; i += 2)
        {
            final int highNibble = hexCharToIntValue(hexString.charAt(i));
            final int lowNibble  = hexCharToIntValue(hexString.charAt(i + 1));
            bytes[j++] = (byte) (((highNibble << 4) & 0xF0) | (lowNibble & 0x0F));
        }
        return bytes;
    }

    private static void assertOffsetLengthValid(
            int offset,
            int length,
            int arrayLength)
    {
        if (offset < 0)
            throw new IllegalArgumentException("The array offset was negative");
        if (length < 0)
            throw new IllegalArgumentException("The array length was negative");
        if ((offset + length) > arrayLength)
            throw new ArrayIndexOutOfBoundsException("The array offset+length would access past end of array");
    }

    public static void main(
            String[] args)
    {
        final String unicode               = "!!!!!!!\u0916\u0941\u0936\u0916\u092C\u0930\u0940 !!!!!!  \u092A\u094D\u0930\u093F\u092F\u0947 \u0938\u0926\u0938\u094D\u092F, {#var#} \u092E\u0947\u0902 \u092A\u094D\u0930\u0924\u094D\u092F\u0947\u0915 {#var#} {#var#} \u0915\u0940 \u0916\u0930\u0940\u0926 \u092A\u0930 , \u0918\u0930 \u0932\u0947 \u091C\u093E\u090F \u090F\u0915 \u0906\u0915\u0930\u094D\u0937\u0915 {#var#}\u0964  \"\u092A\u0939\u0932\u0947 \u0906\u090F, \u092A\u0939\u0932\u0947 \u092A\u093E\u090F \"  \u0928\u093F\u092F\u092E & \u0936\u0930\u094D\u0924\u0947 \u0932\u093E\u0917\u0941 \u0964  \u091F\u0940\u092E \u091C\u0947\u0915\u0947\u090F\u0932\u0938\u0940 \u0938\u093F\u0915\u094D\u0938\u0930";
        final String lConvertStringIntoHex = convertStringIntoHex(unicode);
        System.out.println(lConvertStringIntoHex);
        final String convertedBack = convertHex2String(lConvertStringIntoHex);
        System.out.println(convertedBack);
        System.out.println(convertedBack.contentEquals(unicode));
    }

}
