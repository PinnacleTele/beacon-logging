package com.itextos.beacon.platform.messagetool;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.FeatureCode;
import com.itextos.beacon.commonlib.constants.MessageClass;

public class FeatureCodeFinder
        extends
        FCSupporter
{

    private static final Log log = LogFactory.getLog(FeatureCodeFinder.class);

    public FeatureCodeFinder(
            MessageSplitRequest aMessageRequest)
    {
        super(aMessageRequest);
    }

    public List<String> splitMessageProcess()
    {
        if (mMessageSplitRequest.isDltEnabled())
            applySuffixPrefixToDltMessages();

        regularMessageSuffixPrefix();

        MessageClass lMsgClass = MessageClass.getMessageClass(mMessageSplitRequest.getMessageClass());

        if (mMessageSplitRequest.getDcs() != -1)
            lMsgClass = FCUtility.getMessageClassBasedonDCS(mMessageSplitRequest.getDcs());

        final int         lPlainMessageLengh = getMessageLengthWithSpecialCharacters(lMsgClass);
        final FeatureCode lFeatureCode       = getFeatureCodeValue(lPlainMessageLengh, lMsgClass);
        lMsgClass = FCUtility.findMessageClass(lFeatureCode);
        int       lSplitMessageLength = FCUtility.findSplitMessageLength(lMsgClass, mMessageSplitRequest.is16BitUdh());
        final int lMaxMessageLength   = FCUtility.fetchMaxMessageLength(lMsgClass);

        mMessageSplitRequest.setFeatureCode(lFeatureCode.getKey());

        List<String> lMessagePartsList = null;

        if (mMessageSplitRequest.getClientMaxSplit() == 1)
            lSplitMessageLength = lMaxMessageLength;

        if (isSplitRequired(lMaxMessageLength))
            lMessagePartsList = doSplit(lSplitMessageLength);
        else
            lMessagePartsList = getSinglePartMessage();

        final int lTotalMessageCount = lMessagePartsList.size();
        mMessageSplitRequest.setTotalSplitParts(lTotalMessageCount);

        if (log.isDebugEnabled())
        {
            log.debug("Total Message Parts    : " + lTotalMessageCount);
            log.debug("Splitted Message Parts : " + lMessagePartsList);
        }
        return lMessagePartsList;
    }

    private List<String> getSinglePartMessage()
    {
        final List<String> lMessagePartsList = new ArrayList<>();
        lMessagePartsList.add(mMessageSplitRequest.getMessage());
        mMessageSplitRequest.setTotalSplitParts(1);

        int lMsgCount = FCUtility.getMessageLength(mMessageSplitRequest.getMessage());
        if (mMessageSplitRequest.isHexMessage())
            lMsgCount = FCUtility.getMessageLength(mMessageSplitRequest.getMessage()) / 2;

        mMessageSplitRequest.setCharactersCount(lMsgCount);
        return lMessagePartsList;
    }

    public void getFeatureCode()
    {
        if (mMessageSplitRequest.isDltEnabled())
            applySuffixPrefixToDltMessages();

        regularMessageSuffixPrefix();

        MessageClass lMsgClass = MessageClass.getMessageClass(mMessageSplitRequest.getMessageClass());

        if (mMessageSplitRequest.getDcs() != -1)
            lMsgClass = FCUtility.getMessageClassBasedonDCS(mMessageSplitRequest.getDcs());

        final int         lPlainMessageLengh = getMessageLengthWithSpecialCharacters(lMsgClass);
        final FeatureCode lFeatureCode       = getFeatureCodeValue(lPlainMessageLengh, lMsgClass);
        lMsgClass = FCUtility.findMessageClass(lFeatureCode);
        final int lSplitMessageLength = FCUtility.findSplitMessageLength(lMsgClass, mMessageSplitRequest.is16BitUdh());
        final int lMaxMessageLength   = FCUtility.fetchMaxMessageLength(lMsgClass);

        mMessageSplitRequest.setFeatureCode(lFeatureCode.getKey());
    }

}