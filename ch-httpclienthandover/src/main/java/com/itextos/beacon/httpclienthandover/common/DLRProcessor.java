package com.itextos.beacon.httpclienthandover.common;

import java.util.List;

import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.httpclienthandover.process.IDLRProcess;

public class DLRProcessor
        extends
        AbstractHandoverProcessor

{

    public DLRProcessor(
            List<BaseMessage> aMessageList,
            boolean aIsClientSpecific,
            String aCustomerId)
    {
        super(aMessageList, aIsClientSpecific, aCustomerId);
    }

    @Override
    public void process()
    {
        // Assuming that the message is list is already received as batch
        final IDLRProcess process = getProcess();
        process.processDLR(messageList);
    }

}