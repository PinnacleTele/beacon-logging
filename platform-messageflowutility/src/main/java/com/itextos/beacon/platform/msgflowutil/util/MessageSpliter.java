package com.itextos.beacon.platform.msgflowutil.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.MessagePart;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class MessageSpliter
{

    private static final Log     log = LogFactory.getLog(MessageSpliter.class);

    private final MessageRequest mMessageRequest;

    public MessageSpliter(
            MessageRequest aMessageRequest)
    {
        this.mMessageRequest = aMessageRequest;
    }

    public void setMessageList(
            List<String> aMessageList)
    {

        if (log.isDebugEnabled())
        {
            log.debug("Split the Message : " + mMessageRequest);

            log.debug("Splitted Messages        : " + aMessageList);
        }

        final String  lMessageId    = mMessageRequest.getBaseMessageId();
        final String  lMessage      = CommonUtility.nullCheck(mMessageRequest.getLongMessage());
        final boolean lIsHexMessage = mMessageRequest.isHexMessage();

        if (!lIsHexMessage && (lMessage.length() > 0))
        {
            if (log.isDebugEnabled())
                log.debug("Split Logic for English Messages");

            if (aMessageList.size() > 1)
                createPlainEnglishMultipartMessages(lMessageId, aMessageList);
            else
                createPlainEnglishSinglePartMessage(aMessageList);
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("Split logic for Non English");

            final MessagePart lChildMessageObject = mMessageRequest.getMessageParts().get(0);
            final String      udh                 = CommonUtility.nullCheck(lChildMessageObject.getUdh(), true);

            if (lChildMessageObject.getUdhi() == 1)
                createNonEnglishUdhPrefixedInMessage(lChildMessageObject);
            else
                if (udh.length() > 0)
                    createNonEnglishUdhAsSeparateParameter(lChildMessageObject, udh);
                else
                    if (aMessageList.size() == 1)
                        createNonEnglishSinglePartMessage(lChildMessageObject, aMessageList);
                    else
                        createMonEnglishMultipartMessages(lMessageId, aMessageList);
        }
    }

    private void createMonEnglishMultipartMessages(
            String aMessageId,
            List<String> aSplitMessagesList)
    {
        final int ie_ref = getSmsRefNumber();

        if (log.isDebugEnabled())
            log.debug("Message consider as Non English Multi part message");

        mMessageRequest.setMessageTotalParts(aSplitMessagesList.size());

        List<String> childMids = new ArrayList<>();

        try
        {
            childMids = CommonUtility.getSplitMessageId(aMessageId, aSplitMessagesList.size());
        }
        catch (final ItextosException e)
        {
            log.error("Exception while processing split Message process generate Message Id ..", e);
        }

        final MessagePart lMessageObject = mMessageRequest.getMessageParts().get(0);
        mMessageRequest.removeMessageObject(lMessageObject);

        for (int msgIndex = 0; msgIndex < aSplitMessagesList.size(); msgIndex++)
        {
            final MessagePart lChildMessageObject = new MessagePart(childMids.get(msgIndex));

            mMessageRequest.setIsHexMessage(true);
            lChildMessageObject.setMessageReceivedTime(lMessageObject.getMessageReceivedTime());
            lChildMessageObject.setMessageReceivedDate(lMessageObject.getMessageReceivedDate());
            lChildMessageObject.setMessageActualReceivedTime(lMessageObject.getMessageActualReceivedTime());
            lChildMessageObject.setMessageActualReceivedDate(lMessageObject.getMessageActualReceivedDate());
            lChildMessageObject.setConcatnateReferenceNumber(ie_ref);
            lChildMessageObject.setMessagePartNumber((msgIndex + 1));
            lChildMessageObject.setMessage(aSplitMessagesList.get(msgIndex));

            mMessageRequest.addMessagePart(lChildMessageObject);
        }
    }

    private void createNonEnglishSinglePartMessage(
            MessagePart lChildMessageObject,
            List<String> aSplitMessagesList)
    {
        if (log.isDebugEnabled())
            log.debug("Message consider as Non English Single part message");

        mMessageRequest.setMessageTotalParts(0);
        lChildMessageObject.setConcatnateReferenceNumber(0);
        lChildMessageObject.setMessagePartNumber(0);
        lChildMessageObject.setMessage(aSplitMessagesList.get(0));
    }

    private void createNonEnglishUdhAsSeparateParameter(
            MessagePart aMessageObject,
            String aUdh)
    {
        if (log.isDebugEnabled())
            log.debug("Message consider as Non English udh as a parameter, udh : " + aUdh);

        final Map<MiddlewareConstant, String> param = UdhExtractor.extractParams(aUdh, true);

        mMessageRequest.setMessageTotalParts(CommonUtility.getInteger(param.get(MiddlewareConstant.MW_MSG_TOTAL_PARTS)));
        aMessageObject.setConcatnateReferenceNumber(CommonUtility.getInteger(param.get(MiddlewareConstant.MW_CONCAT_REF_NUM)));
        aMessageObject.setMessagePartNumber(CommonUtility.getInteger(param.get(MiddlewareConstant.MW_MSG_PART_NUMBER)));

        if (param.get(MiddlewareConstant.MW_DESTINATION_PORT) != null)
            mMessageRequest.setDestinationPort(CommonUtility.getInteger(param.get(MiddlewareConstant.MW_DESTINATION_PORT).toString()));
    }

    private void createNonEnglishUdhPrefixedInMessage(
            MessagePart aChildMessageObj)
    {
        if (log.isDebugEnabled())
            log.debug("Message consider as Non English udh prefixed in front of Hex Message");

        final String lMessage     = aChildMessageObj.getMessage();

        final String udhlengthHex = lMessage.substring(0, 2);
        final int    i            = Integer.parseInt(udhlengthHex, 16);
        final int    udhlengthInt = (i * 2) + 2;

        final String aUdh         = lMessage.substring(0, udhlengthInt);

        if (log.isDebugEnabled())
            log.debug("getMessageList() Message consider as Other than English udh prefixed in front of Hex Message udh : " + aUdh);

        final Map<MiddlewareConstant, String> param = UdhExtractor.extractParams(aUdh, true);

        mMessageRequest.setMessageTotalParts(CommonUtility.getInteger(param.get(MiddlewareConstant.MW_MSG_TOTAL_PARTS)));
        aChildMessageObj.setConcatnateReferenceNumber(CommonUtility.getInteger(param.get(MiddlewareConstant.MW_CONCAT_REF_NUM)));
        aChildMessageObj.setMessagePartNumber(CommonUtility.getInteger(param.get(MiddlewareConstant.MW_MSG_PART_NUMBER)));

        if (param.get(MiddlewareConstant.MW_DESTINATION_PORT) != null)
            mMessageRequest.setDestinationPort(CommonUtility.getInteger(param.get(MiddlewareConstant.MW_DESTINATION_PORT).toString()));
    }

    private void createPlainEnglishSinglePartMessage(
            List<String> aSplitMessagesLis)
    {
        final MessagePart lMessageObject = mMessageRequest.getMessageParts().get(0);
        // mMessageRequest.removeMessageObject(lMessageObject);

        final String      udh            = CommonUtility.nullCheck(lMessageObject.getUdh(), true);

        if (log.isDebugEnabled())
            log.debug("Message consider as English, UDH  =  " + udh);

        if (udh.length() > 0)
        {
            if (log.isDebugEnabled())
                log.debug("Message consider as English, Multipsrt Messages. Client udh is present");

            final Map<MiddlewareConstant, String> param = UdhExtractor.extractParams(udh, true);

            mMessageRequest.setMessageTotalParts(CommonUtility.getInteger(param.get(MiddlewareConstant.MW_MSG_TOTAL_PARTS)));
            lMessageObject.setConcatnateReferenceNumber(CommonUtility.getInteger(param.get(MiddlewareConstant.MW_CONCAT_REF_NUM)));
            lMessageObject.setMessagePartNumber(CommonUtility.getInteger(param.get(MiddlewareConstant.MW_MSG_PART_NUMBER)));
            lMessageObject.setUdhi(1);

            if (param.get(MiddlewareConstant.MW_DESTINATION_PORT) != null)
                mMessageRequest.setDestinationPort(CommonUtility.getInteger(param.get(MiddlewareConstant.MW_DESTINATION_PORT)));
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("Message consider as English, Single part");

            mMessageRequest.setMessageTotalParts(0);
            lMessageObject.setConcatnateReferenceNumber(0);
            lMessageObject.setMessagePartNumber(0);
            lMessageObject.setMessage(mMessageRequest.getLongMessage());
        }
    }

    private void createPlainEnglishMultipartMessages(
            String aMessageId,
            List<String> aSplitMessagesList)
    {
        final int smeRefNumber = getSmsRefNumber();

        if (log.isDebugEnabled())
            log.debug("Message consider as English, multipart message");

        List<String> childMids = new ArrayList<>();

        try
        {
            childMids = CommonUtility.getSplitMessageId(aMessageId, aSplitMessagesList.size());
        }
        catch (final ItextosException e)
        {
            log.error("Exception while processing split Message process generate Message Id ..", e);
        }

        if (log.isDebugEnabled())
            log.debug("Child Message Id's : " + childMids);

        final MessagePart lMessageObject = mMessageRequest.getMessageParts().get(0);

        if (log.isDebugEnabled())
            log.debug("Message Object :" + lMessageObject);

        mMessageRequest.removeMessageObject(lMessageObject);
        mMessageRequest.setMessageTotalParts(aSplitMessagesList.size());

        for (int msgIndex = 0; msgIndex < aSplitMessagesList.size(); msgIndex++)
        {
            if (log.isDebugEnabled())
                log.debug("Spelited Message Id : " + childMids.get(msgIndex));

            final MessagePart lChildMessageObject = new MessagePart(childMids.get(msgIndex));

            lChildMessageObject.setMessageReceivedTime(lMessageObject.getMessageReceivedTime());
            lChildMessageObject.setMessageReceivedDate(lMessageObject.getMessageReceivedDate());
            lChildMessageObject.setMessageActualReceivedTime(lMessageObject.getMessageActualReceivedTime());
            lChildMessageObject.setMessageActualReceivedDate(lMessageObject.getMessageActualReceivedDate());
            lChildMessageObject.setConcatnateReferenceNumber(smeRefNumber);
            lChildMessageObject.setMessage(aSplitMessagesList.get(msgIndex));
            lChildMessageObject.setMessagePartNumber((msgIndex + 1));
            lChildMessageObject.setUdhi(1);
            mMessageRequest.addMessagePart(lChildMessageObject);

            if (log.isDebugEnabled())
                log.debug("Part Message : '" + msgIndex + "' :: " + lChildMessageObject);
        }
    }

    private int getSmsRefNumber()
    {
        if (mMessageRequest.is16BitUdh())
            return GenerateUDHRefNumber.getInstance().get16BitRefNumber();
        return GenerateUDHRefNumber.getInstance().get8BitRefNumber();
    }

}