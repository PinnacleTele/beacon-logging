package com.itextos.beacon.platform.smppdlrutil.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class SmppDlrProducer
{

    private static final Log log = LogFactory.getLog(SmppDlrProducer.class);

    private SmppDlrProducer()
    {}

    public static void sendToPostLog(
            DeliveryObject aDeliveryObject)
    {

        try
        {
            MessageProcessor.writeMessage(Component.SMPP_DLR, Component.T2DB_SMPP_POST_LOG, aDeliveryObject);
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
            PlatformUtil.sendToErrorLog(Component.SMPP_DLR, aDeliveryObject, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log", e);
        }
    }

}
