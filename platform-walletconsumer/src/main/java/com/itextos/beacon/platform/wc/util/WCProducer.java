package com.itextos.beacon.platform.wc.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;
import com.itextos.beacon.platform.prc.process.RejectionProcess;
import com.itextos.beacon.platform.rc.process.RConsumer;

public class WCProducer
{

    private static final Log log = LogFactory.getLog(WCProducer.class);

    private WCProducer()
    {}

    public static void sendToRouterComponent(
            MessageRequest aMessageRequest)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Request sending to RC topic .. " + aMessageRequest.getBaseMessageId());

//            MessageProcessor.writeMessage(Component.WC, Component.RC, aMessageRequest);
            aMessageRequest.setFromComponent(Component.WC.getKey());
            aMessageRequest.setNextComponent(Component.RC.getKey());
            RConsumer.forRC(aMessageRequest);
        
        }
        catch (final Exception e)
        {
            log.error("Exception occer while sending to Route Consumer topic ..", e);
            sendToErrorLog(Component.WC, aMessageRequest, e);
        }
    }

    public static void sendToPlatformRejection(
            MessageRequest aMessageRequest)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Request sending to PRC topic .. " + aMessageRequest.getBaseMessageId());
            aMessageRequest.setPlatfromRejected(true);
         //   MessageProcessor.writeMessage(Component.WC, Component.PRC, aMessageRequest);
            aMessageRequest.setFromComponent(Component.WC.getKey());
            aMessageRequest.setNextComponent(Component.PRC.getKey());
            RejectionProcess.forPRC(aMessageRequest);

        }
        catch (final Exception e)
        {
            log.error("Exception occer while sending to Platfrom Rejection topic ..", e);
            sendToErrorLog(Component.WC, aMessageRequest, e);
        }
    }

    public static void sendToErrorLog(
            Component aComponent,
            MessageRequest aBaseMessage,
            Exception aErrorMsg)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Request sending to ERROR topic .. " + aBaseMessage.getBaseMessageId());

            PlatformUtil.sendToErrorLog(aComponent, aBaseMessage, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log", e);
        }
    }

}
