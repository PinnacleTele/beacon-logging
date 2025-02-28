package com.itextos.beacon.platform.prc.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.errorlog.SMSLog;
import com.itextos.beacon.platform.dnpcore.process.DlrInternalProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;
import com.itextos.beacon.platform.subbiller.processor.BillerProcessor;

public class PRProducer
{

    private static final Log log = LogFactory.getLog(PRProducer.class);

    private PRProducer()
    {}

    public static void sendToBillerTopic(
            SubmissionObject aSubmissionObject,SMSLog sb)
    {

        try
        {
      //      MessageProcessor.writeMessage(Component.PRC, Component.SUBBC, aSubmissionObject);
            
            aSubmissionObject.setFromComponent(Component.PRC.getKey());
            aSubmissionObject.setNextComponent(Component.SUBBC.getKey());
            BillerProcessor.forSUBPC(aSubmissionObject,sb);
        }
        catch (final Exception e)
        {
            log.error("Exception occer while Final Processor MT ..", e);
            sendToErrorLog(aSubmissionObject, e);
        }
    }

    public static void sendToDLRTopic(
            DeliveryObject aDeliveryObject,SMSLog sb)
    {

        try
        {
            aDeliveryObject.setNextComponent(Component.DLRINTLP.getKey());
            aDeliveryObject.setFromComponent(Component.PRC.getKey());
        	DlrInternalProcessor.forDLRInternal(aDeliveryObject,sb);

          //  MessageProcessor.writeMessage(Component.PRC, Component.DLRINTLP, aDeliveryObject);
        }
        catch (final Exception e)
        {
            log.error("Exception occer while Dlr internal processor ..", e);
            sendToErrorLog(aDeliveryObject, e);
        }
    }

    public static void sendToErrorLog(
            BaseMessage aBaseMessage,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.PRC, aBaseMessage, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log. " + aBaseMessage, e);
        }
    }

}
