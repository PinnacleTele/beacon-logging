package com.itextos.smpp.client;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.commons.charset.Charset;
import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.type.Address;
import com.itextos.smpp.client.pojo.SubmitRequest;
import com.itextos.smpp.client.util.SmppClientProperties;
import com.itextos.smpp.client.util.SmppClientUtil;

public class SendMessageToServer
        implements
        Runnable
{

    private static final Log  log                                 = LogFactory.getLog(SendMessageToServer.class);
    private static final int  MAX_MULTIPART_MSG_SEGMENT_SIZE_UCS2 = 66;
    private static final int  MAX_SINGLE_MSG_SEGMENT_SIZE_UCS2    = 70;
    private static int        MAX_MULTIPART_MSG_SEGMENT_SIZE_7BIT = 153;
    private static final int  MAX_SINGLE_MSG_SEGMENT_SIZE_7BIT    = 160;

    private final SmppSession mSmppSession;

    public SendMessageToServer(
            SmppSession aSmppSession)
    {
        mSmppSession = aSmppSession;
    }

    @Override
    public void run()
    {
        final boolean       lContineousHitActual = SmppClientProperties.getInstance().isContineousHit();
        final SubmitRequest lSubmitRequest       = SmppClientUtil.getSubmitSMRequest();
        final long          lNoOfMessages        = lSubmitRequest.getNoOfMessages();
        final String        lMobileString        = lSubmitRequest.getMobileNumber();
        final String[]      lDestList            = lMobileString.split(",");

        log.debug("Mobiles       " + Arrays.asList(lDestList));
        log.debug("Messages      " + lNoOfMessages);
        log.debug("Is Contineous " + lContineousHitActual);

        long    msgCount       = 1;
        long    failCount      = 1;
        boolean lContineousHit = lContineousHitActual || (msgCount <= lNoOfMessages);

        while (lContineousHit)
        {

            for (final String lDest : lDestList)
            {
                log.debug("Sending message to " + lDest + " count " + msgCount);
                final byte[][] lByteMessageArray = getMessageByteArray(lSubmitRequest);

                for (final byte[] lMsgPart : lByteMessageArray)
                    try
                    {
                        submitMessage(mSmppSession, lMsgPart, lSubmitRequest, lDest);
                    }
                    catch (final Exception e)
                    {
                        failCount++;
                        log.error("Failed Count : " + failCount + ". Exception while sending message to the SMPP server.", e);
                    }
            }
            msgCount++;
            lContineousHit = lContineousHitActual || (msgCount <= lNoOfMessages);
            sleepForWhile();
        }
    }

    private static void sleepForWhile()
    {

        try
        {
            Thread.sleep(SmppClientProperties.getInstance().getSleepInbetweenHitsInMillis());
        }
        catch (final InterruptedException e)
        {
            log.error("Exception while sleeping.", e);
        }
    }

    private static void submitMessage(
            SmppSession aSession,
            byte[] byteMessagesArray,
            SubmitRequest aSubmitRequest,
            String aDest)
            throws Exception
    {
        System.out.println("Message Bytes: " + byteMessagesArray.length);
        final SubmitSm submitSm = new SubmitSm();
        submitSm.setSourceAddress(new Address((byte) 0x01, (byte) 0x00, aSubmitRequest.getSourceHeader()));
        submitSm.setDestAddress(new Address((byte) 0x01, (byte) 0x01, aDest));
        submitSm.setShortMessage(byteMessagesArray);
        submitSm.setRegisteredDelivery((byte) aSubmitRequest.getRegisteredDelivery());
        submitSm.setDataCoding(aSubmitRequest.getDcs());
        submitSm.setEsmClass(aSubmitRequest.getEsmClass());
        submitSm.setValidityPeriod(aSubmitRequest.getValidityPeriod());
        submitSm.setScheduleDeliveryTime(aSubmitRequest.getScheduleDeliveryTime());

        final List<Tlv> lOptionalParams = aSubmitRequest.getOptionalParams();

        if (lOptionalParams != null)
            for (final Tlv lTlv : lOptionalParams)
                submitSm.setOptionalParameter(lTlv);

        if (log.isDebugEnabled())
            log.debug("Submit SM command Id: " + submitSm.getCommandId() + " ," + submitSm);

        // final SubmitSmResp submitResp = aSession.submit(submitSm, 10000);
        final WindowFuture<Integer, PduRequest, PduResponse> future1 = aSession.sendRequestPdu(submitSm, 10000, false);
        // log.debug(submitSm.getSequenceNumber() + ", Submit_SM Response Message Id : "
        // + submitResp.getMessageId());
    }

    private static byte[][] getMessageByteArray(
            SubmitRequest aSubmitRequest)
    {
        final String lMessage                           = aSubmitRequest.getMessage();
        final String lCharset                           = aSubmitRequest.getCharset();
        final byte   lDcs                               = aSubmitRequest.getDcs();

        int          maximumSingleMessageSize           = 0;
        int          maximumMultipartMessageSegmentSize = 0;
        byte[]       byteSingleMessage                  = null;

        if (SmppClientProperties.getInstance().is16BitUdfRef())
            MAX_MULTIPART_MSG_SEGMENT_SIZE_7BIT = 152;

        Charset lCharSet = CharsetUtil.CHARSET_ISO_8859_1;

        switch (lCharset)
        {
            case "GSM":
                lCharSet = CharsetUtil.CHARSET_GSM;
                if (lDcs == 8)
                {
                    maximumSingleMessageSize           = MAX_SINGLE_MSG_SEGMENT_SIZE_UCS2;
                    maximumMultipartMessageSegmentSize = MAX_MULTIPART_MSG_SEGMENT_SIZE_UCS2;
                }
                else
                {
                    maximumSingleMessageSize           = MAX_SINGLE_MSG_SEGMENT_SIZE_7BIT;
                    maximumMultipartMessageSegmentSize = MAX_MULTIPART_MSG_SEGMENT_SIZE_7BIT;
                }
                break;

            case "GSM7":
                lCharSet = CharsetUtil.CHARSET_GSM7;
                maximumSingleMessageSize = MAX_SINGLE_MSG_SEGMENT_SIZE_7BIT;
                maximumMultipartMessageSegmentSize = MAX_MULTIPART_MSG_SEGMENT_SIZE_7BIT;
                break;

            case "GSM8":
                lCharSet = CharsetUtil.CHARSET_GSM8;
                maximumSingleMessageSize = MAX_SINGLE_MSG_SEGMENT_SIZE_7BIT;
                maximumMultipartMessageSegmentSize = MAX_MULTIPART_MSG_SEGMENT_SIZE_7BIT;
                break;

            case "UTF-8":
                lCharSet = CharsetUtil.CHARSET_UTF_8;
                maximumSingleMessageSize = MAX_SINGLE_MSG_SEGMENT_SIZE_UCS2;
                maximumMultipartMessageSegmentSize = MAX_MULTIPART_MSG_SEGMENT_SIZE_UCS2;
                break;

            case "UCS-2":
                lCharSet = CharsetUtil.CHARSET_UCS_2;
                maximumSingleMessageSize = MAX_SINGLE_MSG_SEGMENT_SIZE_UCS2;
                maximumMultipartMessageSegmentSize = MAX_MULTIPART_MSG_SEGMENT_SIZE_UCS2;
                break;

            case "ISO-8859-1":
            default:
                lCharSet = CharsetUtil.CHARSET_ISO_8859_1;
                maximumSingleMessageSize = MAX_SINGLE_MSG_SEGMENT_SIZE_7BIT;
                maximumMultipartMessageSegmentSize = MAX_MULTIPART_MSG_SEGMENT_SIZE_7BIT;
                break;
        }

        log.debug("Maximum Single Message Size :" + maximumMultipartMessageSegmentSize);
        log.debug("Message length : " + lMessage.length());

        byte[][] byteMessagesArray = null;

        if (lMessage.length() > maximumSingleMessageSize)
        {
            // split message according to the maximum length of a segment
            byteMessagesArray = MultiRequest.splitUnicodeMessage(lMessage, maximumMultipartMessageSegmentSize, lCharSet);
            // set UDHI so PDU will decode the header
            aSubmitRequest.setEsmClass(SmppConstants.ESM_CLASS_UDHI_MASK);
        }
        else
        {
            byteSingleMessage = CharsetUtil.encode(lMessage, lCharSet);
            byteMessagesArray = new byte[][]
            { byteSingleMessage };
            aSubmitRequest.setEsmClass((byte) 0x00);
        }

        log.debug("Message Total Parts : " + byteMessagesArray.length);
        return byteMessagesArray;
    }

}