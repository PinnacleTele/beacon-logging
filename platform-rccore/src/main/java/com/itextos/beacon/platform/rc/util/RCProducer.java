package com.itextos.beacon.platform.rc.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.platform.ch.processor.CarrierHandoverProcess;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;
import com.itextos.beacon.platform.prc.process.RejectionProcess;

public class RCProducer
{

    private static final Log log = LogFactory.getLog(RCProducer.class);

    private RCProducer()
    {}

    public static void sendToPlatformRejection(
            MessageRequest aMessageRequest)
    {

        try
        {
            aMessageRequest.setPlatfromRejected(true);
          //  MessageProcessor.writeMessage(Component.RC, Component.PRC, aMessageRequest);
            
            aMessageRequest.setFromComponent(Component.RC.getKey());
            aMessageRequest.setNextComponent(Component.PRC.getKey());
            RejectionProcess.forPRC(aMessageRequest);

        }
        catch (final Exception e)
        {
            log.error("Exception occer while sending the request to Platfrom Rejection topic..", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToCarrierHandover(
            MessageRequest aMessageRequest)
    {

        try
        {
            aMessageRequest.setActualRouteId(aMessageRequest.getRouteId());
         //  MessageProcessor.writeMessage(Component.RC, Component.CH, aMessageRequest);
           aMessageRequest.setFromComponent(Component.RC.getKey());
           aMessageRequest.setNextComponent(Component.CH.getKey());
           CarrierHandoverProcess.forCH(aMessageRequest, aMessageRequest.getClusterType());
        }
        catch (final Exception e)
        {
            log.error("Exception occer while sending to Carrier Handover topic..", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToErrorLog(
            MessageRequest aMessageRequest,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.RC, aMessageRequest, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log", e);
        }
    }

}
