package com.itextos.beacon.platform.t2e.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class T2EProducer
{

    private static final Log log = LogFactory.getLog(T2EProducer.class);

    private T2EProducer()
    {}

    public static void sendIntrimDeliveriesTopic(
            BaseMessage aBaseMessage,
            Component aComponent)
    {

        try
        {
            MessageProcessor.writeMessage(aComponent, Component.T2DB_INTERIM_DELIVERIES, aBaseMessage);
        }
        catch (final Exception e1)
        {
            log.error("Unable to send to sendIntrimDeliveries topic", e1);
            sendToErrorLog(aBaseMessage, e1, aComponent);
        }
    }

    public static void sendAgingDNProcessorTopic(
            BaseMessage aBaseMessage,
            Component aComponent)
    {

        try
        {
            aBaseMessage.putValue(MiddlewareConstant.MW_INDICATE_DN_FINAL, Constants.ENABLED);
            MessageProcessor.writeMessage(aComponent, Component.AGING_PROCESS, aBaseMessage);
        }
        catch (final Exception e1)
        {
            log.error("Unable to send to Aging DN Processor topic", e1);
            sendToErrorLog(aBaseMessage, e1, aComponent);
        }
    }

    public static void sendToErrorLog(
            BaseMessage aBaseMessage,
            Exception aErrorMsg,
            Component aComponent)
    {

        try
        {
            PlatformUtil.sendToErrorLog(aComponent, aBaseMessage, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log", e);
        }
    }

}
