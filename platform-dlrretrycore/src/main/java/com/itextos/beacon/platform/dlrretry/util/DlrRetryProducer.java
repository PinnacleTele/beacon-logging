package com.itextos.beacon.platform.dlrretry.util;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class DlrRetryProducer
{

    private static final Log log = LogFactory.getLog(DlrRetryProducer.class);

    private DlrRetryProducer()
    {}

    public static void sendToNextComponents(
            Map<Component, DeliveryObject> aNextProcess)
    {
        aNextProcess.entrySet().stream().forEach(e -> {

            try
            {
                if (log.isDebugEnabled())
                    log.debug("Sending to Next Component : " + e.getKey());

                MessageProcessor.writeMessage(Component.DLRR, e.getKey(), e.getValue());
            }
            catch (final ItextosException e1)
            {
                sendToErrorLog(e.getValue(), e1);
            }
        });
    }

    public static void sendToErrorLog(
            BaseMessage aBaseMessage,
            Exception e2)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.DLRR, aBaseMessage, e2);
        }
        catch (final Exception e21)
        {
            log.error("Exception while sending request to error log. " + aBaseMessage, e21);
        }
    }

}
