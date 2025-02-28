package com.itextos.beacon.platform.r3c.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.dltvc.process.DltProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;
import com.itextos.beacon.platform.prc.process.RejectionProcess;
import com.itextos.beacon.platform.sbcv.process.SBCVProcess;

public class R3CProducer
{

    private static final Log log = LogFactory.getLog(R3CProducer.class);

    private R3CProducer()
    {}

    public static void sendToSBVCComponent(
            MessageRequest aMessageRequest)
    {

        try
        {

            if (ClusterType.OTP == aMessageRequest.getClusterType())
            {
                if (log.isDebugEnabled())
                    log.debug("Bypassing the Schedule/Blockout funcationality for OTP request.., Hence request sending to VC");

                sendToVerifyConsumerTopic(aMessageRequest);
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Request sending to SBCV..");
                
         //       MessageProcessor.writeMessage(Component.R3C, Component.SBCV, aMessageRequest);
           
                aMessageRequest.setFromComponent(Component.R3C.getKey());
                aMessageRequest.setNextComponent(Component.SBCV.getKey());
                SBCVProcess.forSBCV(aMessageRequest);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while sending the message to Platform Reject topic.", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToVerifyConsumerTopic(
            MessageRequest aMessageRequest)
    {

        try
        {
            if (aMessageRequest.isBypassDltCheck() || aMessageRequest.isIsIntl()) {
         //       MessageProcessor.writeMessage(Component.R3C, Component.VC, aMessageRequest);
              	aMessageRequest.setFromComponent(Component.R3C.getKey());
            	aMessageRequest.setNextComponent(Component.VC.getKey());
            	com.itextos.beacon.platform.vc.process.MessageProcessor.forVC(Component.VC, aMessageRequest);
            	
            }else {
              //  MessageProcessor.writeMessage(Component.R3C, Component.DLTVC, aMessageRequest);
            
             	aMessageRequest.setFromComponent(Component.R3C.getKey());
            	aMessageRequest.setNextComponent(Component.DLTVC.getKey());

                DltProcessor.forDLT(aMessageRequest, Component.DLTVC);
 
            }
            
        }    
        catch (final Exception e)
        {
            log.error("Exception while sending the message to Verify Consumer topic.", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToPrc(
            MessageRequest aMessageRequest)
    {
        String lMessage = CommonUtility.nullCheck(aMessageRequest.getLongMessage(), true);

        if (!lMessage.isEmpty())
        {
            lMessage = R3CUtil.rplSplCharinMessage(lMessage);
            aMessageRequest.setLongMessage(lMessage);
        }

        if (log.isDebugEnabled())
            log.debug("Reqiest sending to PRC component :: " + aMessageRequest);

        try
        {
            aMessageRequest.setPlatfromRejected(true);
         //   MessageProcessor.writeMessage(Component.R3C, Component.PRC, aMessageRequest);
            aMessageRequest.setFromComponent(Component.R3C.getKey());
            aMessageRequest.setNextComponent(Component.PRC.getKey());
            RejectionProcess.forPRC(aMessageRequest);

        }
        catch (final Exception e)
        {
            log.error("Exception while sending the message to Platform Reject topic.", e);
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToErrorLog(
            BaseMessage aBaseMessage,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.R3C, aBaseMessage, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log. " + aBaseMessage, e);
        }
    }

}
