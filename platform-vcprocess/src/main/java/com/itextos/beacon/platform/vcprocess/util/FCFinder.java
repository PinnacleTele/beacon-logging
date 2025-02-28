package com.itextos.beacon.platform.vcprocess.util;

import java.util.List;

import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.platform.messagetool.FeatureCodeFinder;
import com.itextos.beacon.platform.messagetool.MessageSplitRequest;
import com.itextos.beacon.platform.msgflowutil.util.MessageSpliter;
import com.itextos.beacon.platform.msgflowutil.util.UdhProcessor;

public class FCFinder
{


    private final MessageRequest mMessageRequest;

    public FCFinder(
            MessageRequest aMessageRequest)
    {
        mMessageRequest = aMessageRequest;
    }

    public void splitMessageProcess()
    {
        final MessageSplitRequest lMsgSplitReq         = papulateMessageSplitRequest(mMessageRequest);

        final FeatureCodeFinder   lFCFinder            = new FeatureCodeFinder(lMsgSplitReq);
        final List<String>        lSplitMessageProcess = lFCFinder.splitMessageProcess();

        mMessageRequest.setMessageTotalParts(lMsgSplitReq.getTotalSplitParts());

        mMessageRequest.setFeatureCode(lMsgSplitReq.getFeatureCode());
        mMessageRequest.setMessageClass(lMsgSplitReq.getMessageClass());

        new MessageSpliter(mMessageRequest).setMessageList(lSplitMessageProcess);

        new UdhProcessor(mMessageRequest).generateUDH();
    }

    private static MessageSplitRequest papulateMessageSplitRequest(
            MessageRequest aMessageRequest)
    {
        final MessageSplitRequest msr = new MessageSplitRequest(aMessageRequest.getClientId(), aMessageRequest.getBaseMessageId(), aMessageRequest.getLongMessage(), aMessageRequest.getMessageClass(),
                aMessageRequest.isHexMessage());

        msr.setClientMaxSplit(aMessageRequest.getClientMaxSplit());
        msr.setCountry(aMessageRequest.getCountry());
        msr.setDcs(aMessageRequest.getDcs());
        msr.setDestinationPort(aMessageRequest.getDestinationPort());
        msr.setDltEnabled(aMessageRequest.isDltCheckEnabled());
        msr.setDltTemplateType(aMessageRequest.getDltTemplateType());
        msr.setHeader(aMessageRequest.getHeader());
        msr.setIs16BitUdh(aMessageRequest.is16BitUdh());
        msr.setUdh(null);
        // msr.setUdhi(aMessageRequest.getUDH);

        return msr;
    }

}
