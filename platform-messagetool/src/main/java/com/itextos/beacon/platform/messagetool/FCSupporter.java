package com.itextos.beacon.platform.messagetool;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.FeatureCode;
import com.itextos.beacon.commonlib.constants.MessageClass;
import com.itextos.beacon.commonlib.constants.SpecialCharacters;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.msgutil.cache.MessageSuffixPrefix;
import com.itextos.beacon.inmemory.msgutil.util.MessageFlowUtil;
import com.itextos.beacon.inmemory.templates.DLTMsgTemplatePrefixSuffix;
import com.itextos.beacon.inmemory.templates.pojo.DLTMsgPrefixSuffixObj;

abstract class FCSupporter
{

    private static final Log      log = LogFactory.getLog(FCSupporter.class);

    protected MessageSplitRequest mMessageSplitRequest;

    protected FCSupporter(
            MessageSplitRequest aMessageRequest)
    {
        mMessageSplitRequest = aMessageRequest;
    }

    protected FeatureCode getFeatureCodeValue(
            int aMessageLengh,
            MessageClass aMsgClass)
    {
        final int    lDataCodingValue = mMessageSplitRequest.getDcs();
        final int    lDestinationPort = mMessageSplitRequest.getDestinationPort();
        final String lUdh             = CommonUtility.nullCheck(mMessageSplitRequest.getUdh(), true);
        final int    lUdhInclude      = mMessageSplitRequest.getUdhi();
        final String lMessage         = mMessageSplitRequest.getMessage();

        if (log.isDebugEnabled())
            log.debug("Message Class Value : '" + aMsgClass + "' Message Length : '" + aMessageLengh //
                    + "' DCS : '" + lDataCodingValue + "' Destination Port : '" + lDestinationPort //
                    + "' UDH : '" + lUdh + "' UDHI : '" + lUdhInclude + "' Message : '" + lMessage + "'");

        FeatureCode lFeaturecd = null;
        if (aMsgClass != null)
            lFeaturecd = FCUtility.getFeatureCodeBasedOnMessageClass(aMsgClass, aMessageLengh, lUdhInclude, lUdh, lMessage);

        if (lFeaturecd == null)
            lFeaturecd = FCUtility.getFeatureCodeBasedOnDcs(lDataCodingValue, aMessageLengh, lUdhInclude, lUdh, lMessage);

        if ((lFeaturecd == null) && (lDestinationPort != 0))
            lFeaturecd = FCUtility.getSpecialPortFeatureCode(aMessageLengh, lUdhInclude, lMessage);

        if (lFeaturecd == null)
            lFeaturecd = FeatureCode.BINARY_MSG;

        if (log.isDebugEnabled())
            log.debug("Feature code value : " + lFeaturecd);

        return lFeaturecd;
    }

    protected int getMessageLengthWithSpecialCharacters(
            MessageClass aMessageClass)
    {
        final int lClientMaxSplit = mMessageSplitRequest.getClientMaxSplit();

        if (lClientMaxSplit == 1)
            return FCUtility.getSingleMessageLength(aMessageClass);

        final boolean isHexMsg = mMessageSplitRequest.isHexMessage();

        if (isHexMsg)
            return 0;

        final String message = getFullMesssage();
        return FCUtility.getMessageLength(message);
    }

    private String getFullMesssage()
    {
        final PrefixSuffix  clientPrefixSuffix = mMessageSplitRequest.getClientPrefixSuffix();
        final PrefixSuffix  dltPrefixSuffix    = mMessageSplitRequest.getDltPrefixSuffix();

        final StringBuilder message            = new StringBuilder();

        if (dltPrefixSuffix != null)
            message.append(dltPrefixSuffix.getPrefix());

        if (clientPrefixSuffix != null)
            message.append(clientPrefixSuffix.getPrefix());

        message.append(mMessageSplitRequest.getMessage());

        if (clientPrefixSuffix != null)
            message.append(clientPrefixSuffix.getSuffix());

        if (dltPrefixSuffix != null)
            message.append(dltPrefixSuffix.getSuffix());

        return message.toString();
    }

    protected void applySuffixPrefixToDltMessages()
    {

        try
        {
            final String lDltTemplateType = CommonUtility.nullCheck(mMessageSplitRequest.getDltTemplateType(), true);

            if (lDltTemplateType.isBlank())
                return;

            final DLTMsgTemplatePrefixSuffix lDltMsgTemplatePrefixSuffix = (DLTMsgTemplatePrefixSuffix) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.DLT_MSG_PREFIX_SUFFIX);
            final DLTMsgPrefixSuffixObj      lDltMsgPrefixSuffix         = lDltMsgTemplatePrefixSuffix.getMsgPrefixSuffixVal(lDltTemplateType.toLowerCase());

            if (lDltMsgPrefixSuffix == null)
                return;

            final boolean      isHexMsg      = mMessageSplitRequest.isHexMessage();
            final String       lPrefixValue  = lDltMsgPrefixSuffix.getPrefix();
            final String       lSuffixValue  = lDltMsgPrefixSuffix.getSuffix();
            final PrefixSuffix lPrefixSuffix = new PrefixSuffix(lPrefixValue, lSuffixValue, isHexMsg);
            mMessageSplitRequest.setDltPrefixSuffix(lPrefixSuffix);
        }
        catch (final Exception e)
        {
            log.error("Exception in Adding Prefix / Suffix for the DLT message " + mMessageSplitRequest, e);
        }
    }

    protected void regularMessageSuffixPrefix()
    {

        try
        {
            final String lClientId   = mMessageSplitRequest.getClientId();
            final String lCountry    = CommonUtility.nullCheck(mMessageSplitRequest.getCountry(), true);
            final String lHeader     = mMessageSplitRequest.getHeader();
            final int    lUdhInclude = mMessageSplitRequest.getUdhi();
            final String lUdh        = CommonUtility.nullCheck(mMessageSplitRequest.getUdh(), true);

            if (log.isDebugEnabled())
                log.debug("Need to add Prefix / Suffix for Base Message Id:" + mMessageSplitRequest.getBaseMessageId() + " is " + ((lUdhInclude == 0) && ((lUdh.isBlank()) || (lUdh.length() == 0))));

            if ((lUdhInclude == 0) && lUdh.isBlank())
            {
                final MessageSuffixPrefix lMessageSuffixPrefixObj = MessageFlowUtil.getMessageSuffixPrefixInfo();
                final boolean             lIsHexMsg               = mMessageSplitRequest.isHexMessage();
                final String              lPrefixValue            = lMessageSuffixPrefixObj.getClientMsgPrefix(lClientId, lCountry, lHeader);
                final String              lSuffixValue            = lMessageSuffixPrefixObj.getClientMsgSuffix(lClientId, lCountry, lHeader);

                if (log.isDebugEnabled())
                    log.debug("Client id '" + lClientId + "' PrefixValue '" + lPrefixValue + "' SuffixValue '" + lSuffixValue + "' IshexMsg '" + lIsHexMsg + "'");

                final PrefixSuffix lPrefixSuffix = new PrefixSuffix(lPrefixValue, lSuffixValue, lIsHexMsg);
                mMessageSplitRequest.setClientPrefixSuffix(lPrefixSuffix);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception in Adding Prefix / Suffix for the message " + mMessageSplitRequest, e);
        }
    }

    protected boolean isSplitRequired(
            int aSplitMessageLength)
    {
        final String  lMessage          = getFullMesssage();
        final boolean lIsHexMsg         = mMessageSplitRequest.isHexMessage();

        int           lSpecialCharCount = 0;

        if (!lIsHexMsg)
        {
            final char[] strChar = lMessage.toCharArray();

            for (final char lElement : strChar)
                if (SpecialCharacters.isSpecialCharacter(lElement))
                    lSpecialCharCount++;
        }

        final int lFinalMessageLength = (lMessage.length() + lSpecialCharCount);

        if (log.isDebugEnabled())
            log.debug("FinalMessageLength : " + lFinalMessageLength);

        mMessageSplitRequest.setMessageLength(lFinalMessageLength);

        return (lFinalMessageLength > aSplitMessageLength);
    }

    List<String> doSplit(
            int aSplitMessageLength)
    {
        final int          lCustomerSplitCount           = mMessageSplitRequest.getClientMaxSplit();
        final List<String> lSplitMessagePartsList        = new ArrayList<>();

        int                maxPossibleSplits             = mMessageSplitRequest.getMessageLength() / aSplitMessageLength;
        final double       maxPossibleSplitsWithAddition = mMessageSplitRequest.getMessageLength() / (aSplitMessageLength * 1.0);

        if ((maxPossibleSplitsWithAddition - maxPossibleSplits) > 0)
            maxPossibleSplits++;

        String message = getFullMesssage();

        if ((lCustomerSplitCount > 0) && (maxPossibleSplits > lCustomerSplitCount))
        {
            final PrefixSuffix  clientPrefixSuffix = mMessageSplitRequest.getClientPrefixSuffix();
            final PrefixSuffix  dltPrefixSuffix    = mMessageSplitRequest.getDltPrefixSuffix();
            int                 suffixLength       = 0;

            final StringBuilder tempSb             = new StringBuilder();

            if (dltPrefixSuffix != null)
            {
                suffixLength += dltPrefixSuffix.getSuffixLength();
                tempSb.append(dltPrefixSuffix.getPrefix());
            }

            if (clientPrefixSuffix != null)
            {
                suffixLength += clientPrefixSuffix.getSuffixLength();
                tempSb.append(clientPrefixSuffix.getPrefix());
            }

            final int maxLength = (lCustomerSplitCount * aSplitMessageLength) - suffixLength;
            tempSb.append(mMessageSplitRequest.getMessage());

            String temp = tempSb.toString();

            if (maxLength > 0)
                if (maxLength <= temp.length())
                    temp = temp.substring(0, maxLength);
                else
                    temp = temp.substring(temp.length() - suffixLength);

            message = temp;
            if (clientPrefixSuffix != null)
                message += clientPrefixSuffix.getSuffix();

            if (dltPrefixSuffix != null)
                message += dltPrefixSuffix.getSuffix();
        }

        if (log.isDebugEnabled())
            log.debug("Final Message to Split '" + message + "'");

        final char[]  lMessageChar     = message.toCharArray();
        int           curMessageLength = 0;
        StringBuilder sb               = new StringBuilder();

        /** Buggy logic
         * 1. It does not split the parts at 153 because the split comparison is ==, this does not work if the
         *      curchar length is 152 and then the immediate char is spl char
         *      which takes the curlen to 154, which is bypass the == if condition
         * 2. If the cur length is not exactly 153 and if xceeds 153, then the first
         *      split should be till the prev char which is length till 152
         *
         *  Both these have been fixed in the for loop
         */

        // BUGFIX: see the comments above
        for (final char lElement : lMessageChar) {
            int charLength = SpecialCharacters.isSpecialCharacter(lElement) ? 2 : 1;

            // Check if adding this character would exceed the split length
            if (curMessageLength + charLength > aSplitMessageLength) {
                // If so, add the current part to the list and reset
                lSplitMessagePartsList.add(sb.toString());
                sb = new StringBuilder();
                curMessageLength = 0;
            }

            // Append the character and update the length
            sb.append(lElement);
            curMessageLength += charLength;
        }

        if (sb.length() > 0)
            lSplitMessagePartsList.add(sb.toString());

        mMessageSplitRequest.setTotalSplitParts(lSplitMessagePartsList.size());

        if (mMessageSplitRequest.isHexMessage())
            mMessageSplitRequest.setCharactersCount(lMessageChar.length / 2);
        else
            mMessageSplitRequest.setCharactersCount(lMessageChar.length);

        return lSplitMessagePartsList;
    }

}