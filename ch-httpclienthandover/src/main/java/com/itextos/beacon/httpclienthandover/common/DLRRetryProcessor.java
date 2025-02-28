package com.itextos.beacon.httpclienthandover.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverData;
import com.itextos.beacon.httpclienthandover.process.IDLRProcess;
import com.itextos.beacon.httpclienthandover.utils.ClientHandoverUtils;

public class DLRRetryProcessor
        extends
        AbstractHandoverProcessor
{

    private static final Log log = LogFactory.getLog(DLRRetryProcessor.class);

    public DLRRetryProcessor(
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
        removeExpiredMessage();

        if (messageList.isEmpty())
        {
            if (log.isDebugEnabled())
                log.debug("No messages to be processed");
            return;
        }

        final IDLRProcess process = getProcess(); // What are we doing with this process?
        process.processDLR(messageList);
    }

    private void removeExpiredMessage()
    {
        final List<BaseMessage>  expiredMessages     = new ArrayList<>();
        final ClientHandoverData lClientHandoverData = ClientHandoverUtils.getClientHandoverData(customerId);

        for (final BaseMessage currentMessage : messageList)
        {
            // TODO Faizul Check for expiry
            final boolean isExpired = ClientHandoverUtils.checkIfExpired(currentMessage, lClientHandoverData, false);

            if (isExpired)
                expiredMessages.add(currentMessage);
        }

        if (!expiredMessages.isEmpty())
            messageList.removeAll(expiredMessages);
    }

}
