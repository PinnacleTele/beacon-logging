package com.itextos.smpp.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.commons.charset.Charset;
import com.cloudhopper.commons.charset.CharsetUtil;
import com.itextos.smpp.client.util.GenerateUDHRefNumber;
import com.itextos.smpp.client.util.SmppClientProperties;
import com.itextos.smpp.client.util.SpecialCharacters;

public class MultiRequest
{

    private static final Log log = LogFactory.getLog(MultiRequest.class);

    private MultiRequest()
    {}

    public static byte[][] splitUnicodeMessage(
            String aMessage,
            int maximumMultipartMessageSegmentSize,
            Charset aCharset)
    {
        byte UDHIE_HEADER_LENGTH  = 0x05;
        byte UDHIE_IDENTIFIER_SAR = 0x00;
        byte UDHIE_SAR_LENGTH     = 0x03;

        if (SmppClientProperties.getInstance().is16BitUdfRef())
        {
            UDHIE_HEADER_LENGTH  = 0x06;
            UDHIE_IDENTIFIER_SAR = 0x08;
            UDHIE_SAR_LENGTH     = 0x04;
        }

        int messageLength    = getMessageLength(aMessage);

        // determine how many messages have to be sent
        int numberOfSegments = messageLength / maximumMultipartMessageSegmentSize;

        log.debug("No.of segments >>>>>>> " + numberOfSegments);

        if (numberOfSegments > 255)
        {
            numberOfSegments = 255;
            messageLength    = numberOfSegments * maximumMultipartMessageSegmentSize;
        }

        if ((messageLength % maximumMultipartMessageSegmentSize) > 0)
            numberOfSegments++;

        log.debug("final No.of segments >>>>>>> " + numberOfSegments);

        // prepare array for all of the msg segments
        final byte[][] segments = new byte[numberOfSegments][];

        int            lengthOfData;

        final int      lRefNum  = getSmsRefNumber();
        log.debug("Ref Num : " + lRefNum);

        // split the message adding required headers

        int startPos = 0;
        int endPos   = 0;

        for (int i = 0; i < numberOfSegments; i++)
        {
            log.debug("Segment Count >>>  " + i);

            startPos = endPos;

            if ((numberOfSegments - i) == 1)
            {
                lengthOfData = messageLength - (i * maximumMultipartMessageSegmentSize);
                endPos       = (startPos + lengthOfData);
            }
            else
            {
                lengthOfData = maximumMultipartMessageSegmentSize;
                if (startPos == 0)
                    endPos = lengthOfData;
                else
                    endPos = (startPos + lengthOfData);
            }

            System.out.println("Part Length :" + lengthOfData);

            // new array to store the header
            byte[]       lMessageBytes   = null;
            final String lSplitedMessage = aMessage.substring(startPos, endPos);

            lMessageBytes = CharsetUtil.encode(lSplitedMessage, aCharset);

            if (SmppClientProperties.getInstance().is16BitUdfRef())
                segments[i] = new byte[7 + lMessageBytes.length];
            else
                segments[i] = new byte[6 + lMessageBytes.length];

            log.debug("Length of the message :" + lengthOfData);

            // UDH header
            // doesn't include itself, its header length
            segments[i][0] = UDHIE_HEADER_LENGTH;
            // SAR identifier
            segments[i][1] = UDHIE_IDENTIFIER_SAR;
            // SAR length
            segments[i][2] = UDHIE_SAR_LENGTH;
            // reference number (same for all messages)

            if (SmppClientProperties.getInstance().is16BitUdfRef())
            {
                segments[i][3] = (byte) ((lRefNum >> 8) & 0xff);

                segments[i][4] = (byte) (lRefNum & 0xff);
                // total number of segments
                segments[i][5] = (byte) numberOfSegments;
                // segment number
                segments[i][6] = (byte) (i + 1);
            }
            else
            {
                segments[i][3] = (byte) lRefNum;
                // total number of segments
                segments[i][4] = (byte) numberOfSegments;
                // segment number
                segments[i][5] = (byte) (i + 1);
            }

            // copy the data into the array

            if (SmppClientProperties.getInstance().is16BitUdfRef())
                System.arraycopy(lMessageBytes, 0, segments[i], 7, lMessageBytes.length);
            else
                System.arraycopy(lMessageBytes, 0, segments[i], 6, lMessageBytes.length);
        }

        return segments;
    }

    /*
     * public static byte[][] splitUnicodeMessage_old(
     * byte[] aMessage,
     * int maximumMultipartMessageSegmentSize)
     * {
     * byte UDHIE_HEADER_LENGTH = 0x05;
     * byte UDHIE_IDENTIFIER_SAR = 0x00;
     * byte UDHIE_SAR_LENGTH = 0x03;
     * if (SmppClientProperties.getInstance().is16BitUdfRef())
     * {
     * UDHIE_HEADER_LENGTH = 0x06;
     * UDHIE_IDENTIFIER_SAR = 0x08;
     * UDHIE_SAR_LENGTH = 0x04;
     * }
     * // determine how many messages have to be sent
     * int numberOfSegments = aMessage.length / maximumMultipartMessageSegmentSize;
     * int messageLength = aMessage.length;
     * log.debug("No.of segments >>>>>>> " + numberOfSegments);
     * if (numberOfSegments > 255)
     * {
     * numberOfSegments = 255;
     * messageLength = numberOfSegments * maximumMultipartMessageSegmentSize;
     * }
     * if ((messageLength % maximumMultipartMessageSegmentSize) > 0)
     * numberOfSegments++;
     * log.debug("final No.of segments >>>>>>> " + numberOfSegments);
     * // prepare array for all of the msg segments
     * final byte[][] segments = new byte[numberOfSegments][];
     * int lengthOfData;
     * final int lRefNum = getSmsRefNumber();
     * log.debug("Ref Num : " + lRefNum);
     * // split the message adding required headers
     * for (int i = 0; i < numberOfSegments; i++)
     * {
     * log.debug("Segment Count >>>  " + i);
     * if ((numberOfSegments - i) == 1)
     * lengthOfData = messageLength - (i * maximumMultipartMessageSegmentSize);
     * else
     * lengthOfData = maximumMultipartMessageSegmentSize;
     * // new array to store the header
     * if (SmppClientProperties.getInstance().is16BitUdfRef())
     * segments[i] = new byte[7 + lengthOfData];
     * else
     * segments[i] = new byte[6 + lengthOfData];
     * // UDH header
     * // doesn't include itself, its header length
     * segments[i][0] = UDHIE_HEADER_LENGTH;
     * // SAR identifier
     * segments[i][1] = UDHIE_IDENTIFIER_SAR;
     * // SAR length
     * segments[i][2] = UDHIE_SAR_LENGTH;
     * // reference number (same for all messages)
     * if (SmppClientProperties.getInstance().is16BitUdfRef())
     * {
     * segments[i][3] = (byte) ((lRefNum >> 8) & 0xff);
     * segments[i][4] = (byte) (lRefNum & 0xff);
     * // total number of segments
     * segments[i][5] = (byte) numberOfSegments;
     * // segment number
     * segments[i][6] = (byte) (i + 1);
     * }
     * else
     * {
     * segments[i][3] = (byte) lRefNum;
     * // total number of segments
     * segments[i][4] = (byte) numberOfSegments;
     * // segment number
     * segments[i][5] = (byte) (i + 1);
     * }
     * // copy the data into the array
     * if (SmppClientProperties.getInstance().is16BitUdfRef())
     * System.arraycopy(aMessage, (i * maximumMultipartMessageSegmentSize),
     * segments[i], 7, lengthOfData);
     * else
     * System.arraycopy(aMessage, (i * maximumMultipartMessageSegmentSize),
     * segments[i], 6, lengthOfData);
     * }
     * return segments;
     * }
     */

    public static String pmToHexString(
            String msg)
    {
        byte[] byteArr = null;
        byteArr = msg.getBytes();

        final StringBuilder sb = new StringBuilder(byteArr.length * 2);

        for (final byte l_element : byteArr)
        {
            final int v = l_element & 0xff;

            if (v < 16)
                sb.append('0');
            sb.append(Integer.toHexString(v));
        }

        return sb.toString().toUpperCase();
    }

    private static int getSmsRefNumber()
    {
        return GenerateUDHRefNumber.getInstance().getNextRefNumber();
    }

    private static int getMessageLength(
            String aMessage)
    {
        int          curMessageLength = 0;

        final char[] lMessageChar     = aMessage.toCharArray();
        for (final char lElement : lMessageChar)
            if (SpecialCharacters.isSpecialCharacter(lElement))
                curMessageLength = curMessageLength + 2;
            else
                curMessageLength = curMessageLength + 1;

        return curMessageLength;
    }

    public static void main(
            String[] args)
    {
        final String message = "Greater Chennai Corporation EDP CELL is inviting you to a scheduled Zoom meeting. Topic: Training Program for AE /JE in COVID CARE APP Time: 20211112164033 wsd India Join Zoom Meeting Link google.";

        for (int i = 255; i < 1000; i++)
            System.out.println("i>" + i);
        // splitUnicodeMessage_old(message.getBytes(), 152);
    }

}
