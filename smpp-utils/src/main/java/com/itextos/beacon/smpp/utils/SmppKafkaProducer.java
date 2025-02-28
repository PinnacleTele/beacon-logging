package com.itextos.beacon.smpp.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class SmppKafkaProducer
{

    private static final Log log = LogFactory.getLog(SmppKafkaProducer.class);

    private SmppKafkaProducer()
    {}

    public static void sendToPostLog(
            DeliveryObject aDeliveryObject)
    {

        try
        {
            MessageProcessor.writeMessage(Component.SMPP_CONSUMER, Component.T2DB_SMPP_POST_LOG, aDeliveryObject);
        }
        catch (final ItextosException e)
        {
            sendToErrorLog(aDeliveryObject, e);
        }
    }

    public static void sendToErrorLog(
            DeliveryObject aDeliveryObject,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.SMPP_CONSUMER, aDeliveryObject, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log", e);
        }
    }

}
