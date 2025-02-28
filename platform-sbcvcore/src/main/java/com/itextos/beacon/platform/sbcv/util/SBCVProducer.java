package com.itextos.beacon.platform.sbcv.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.platform.dltvc.process.DltProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;
import com.itextos.beacon.platform.prc.process.RejectionProcess;
import com.itextos.beacon.platform.sbc.processor.SBConsumer;

public class SBCVProducer
{

    private SBCVProducer()
    {}

    private static final Log log = LogFactory.getLog(SBCVProducer.class);

    public static void sendToPlatformRejection(
            MessageRequest aMessageRequest)
    {

        try
        {
            aMessageRequest.setPlatfromRejected(true);
   //         MessageProcessor.writeMessage(Component.SBCV, Component.PRC, aMessageRequest);
            
            aMessageRequest.setFromComponent(Component.SBCV.getKey());
            aMessageRequest.setNextComponent(Component.PRC.getKey());
            RejectionProcess.forPRC(aMessageRequest);

        }
        catch (final Exception e)
        {
            log.error("Exception while sending the message to Platform Reject topic.", e);
            aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" : Exception while sending the message to Platform Reject topic. :  "+ErrorMessage.getStackTraceAsString(e) );

            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToVerifyConsumerTopic(
            MessageRequest aMessageRequest)
    {

        try
        {
            if (aMessageRequest.isBypassDltCheck() || aMessageRequest.isIsIntl()) {
              
            //	MessageProcessor.writeMessage(Component.SBCV, Component.VC, aMessageRequest);
              	aMessageRequest.setFromComponent(Component.SBCV.getKey());
            	aMessageRequest.setNextComponent(Component.VC.getKey());
            	com.itextos.beacon.platform.vc.process.MessageProcessor.forVC(Component.VC, aMessageRequest);
   
            }else {
            
            	if(aMessageRequest.isIldo()) {
               //     MessageProcessor.writeMessage(Component.SBCV, Component.VC, aMessageRequest);
                  	aMessageRequest.setFromComponent(Component.SBCV.getKey());
                	aMessageRequest.setNextComponent(Component.VC.getKey());
                	com.itextos.beacon.platform.vc.process.MessageProcessor.forVC(Component.VC, aMessageRequest);
       
            	}else {
            	//	MessageProcessor.writeMessage(Component.SBCV, Component.DLTVC, aMessageRequest);
            	
                   	aMessageRequest.setFromComponent(Component.SBCV.getKey());
                	aMessageRequest.setNextComponent(Component.DLTVC.getKey());

                    DltProcessor.forDLT(aMessageRequest, Component.DLTVC);
     
            	}
            }
            	
        }
        catch (final Exception e)
        {
        	
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" : Exception while sending the message to Verify Consumer topic :  "+ErrorMessage.getStackTraceAsString(e) );

            log.error("Exception while sending the message to Verify Consumer topic.", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToScheduleTopic(
            MessageRequest aMessageRequest)
    {

        try
        {
            MessageProcessor.writeMessage(Component.SBCV, Component.SBC, aMessageRequest);
          /*  
            aMessageRequest.setFromComponent(Component.SBCV.getKey());
            aMessageRequest.setNextComponent(Component.SBC.getKey());
            SBConsumer.forSPC(aMessageRequest);
 			*/
        }
        catch (final Exception e)
        {
            log.error("Exception while sending the message to Schedule topic.", e);
            aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" : Exception while sending the message to Schedule topic :  "+ErrorMessage.getStackTraceAsString(e) );

            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToBlockoutTopic(
            MessageRequest aMessageRequest)
    {

        try
        {
            MessageProcessor.writeMessage(Component.SBCV, Component.SBC, aMessageRequest);
        	/*
            aMessageRequest.setFromComponent(Component.SBCV.getKey());
            aMessageRequest.setNextComponent(Component.SBC.getKey());
            SBConsumer.forSPC(aMessageRequest);
  			*/
        }
        catch (final Exception e)
        {
            log.error("Exception while sending the message to Blockout topic.", e);
            aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" : Exception while sending the message to Blockout topic :  "+ErrorMessage.getStackTraceAsString(e) );

            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToErrorLog(
            MessageRequest aMessageRequest,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.SBCV, aMessageRequest, aErrorMsg);
        }
        catch (final Exception e)
        {
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" : Exception while sending the message to error log topic :  "+ErrorMessage.getStackTraceAsString(e) );

            log.error("Exception while sending request to error log. " + aMessageRequest, e);
        }
    }

}
