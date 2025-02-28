package com.itextos.beacon.platform.vcprocess.util;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;
import com.itextos.beacon.platform.prc.process.RejectionProcess;
import com.itextos.beacon.platform.rc.process.RConsumer;
import com.itextos.beacon.platform.wc.process.WalletProcessor;

public class VCProducer
{

 //   private static final Log log = LogFactory.getLog(VCProducer.class);

    private VCProducer()
    {}

    public static void sendToNextComponent(
            Component aSourceComponent,
            Component aComponent,
            MessageRequest aMessageRequest)
    {

        try
        {
            /**
             * Execute Split logic here for other interface requests except SMPP
             * For SMPP Request platform will skip the Message Split and UDH generate
             * process
             */
            if (aMessageRequest.getInterfaceType() != InterfaceType.SMPP)
                doFeatureCodeIdentifier(aMessageRequest);

            if (!VCUtil.doCappingCheck(aMessageRequest, aSourceComponent))
                return;

            switch (aComponent)
            {
                case PRC:
                    sendToPlatformRejection(aSourceComponent, aMessageRequest);
                    break;

                case WC:
                    sendToWalletComponent(aSourceComponent, aMessageRequest);
                    break;

                case RC:
                    sendToRouterComponent(aSourceComponent, aMessageRequest);
                    break;

                default:
                    break;
            }
        }
        catch (final Exception e)
        {
            sendToErrorLog(aSourceComponent, aMessageRequest, e);
        }
    }

    public static void sendToPlatformRejection(
            Component aComponent,
            MessageRequest aMessageRequest)
    {

        try
        {
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" : Request sending to PRC topic ..");

            aMessageRequest.setPlatfromRejected(true);
            aMessageRequest.setFromComponent(aComponent.getKey());
            aMessageRequest.setNextComponent(Component.PRC.getKey());
            RejectionProcess.forPRC(aMessageRequest);

//            MessageProcessor.writeMessage(aComponent, Component.PRC, aMessageRequest);
        }
        catch (final Exception e)
        {
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" : Request sending to Platfrom Rejection topic .."+ ErrorMessage.getStackTraceAsString(e));

            sendToErrorLog(aComponent, aMessageRequest, e);
        }
    }

    public static void sendToWalletComponent(
            Component aComponent,
            MessageRequest aMessageRequest)
    {

        try
        {
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" : Request sending to WC topic ..");

         //   MessageProcessor.writeMessage(aComponent, Component.WC, aMessageRequest);
            
            aMessageRequest.setFromComponent(aComponent.getKey());
            aMessageRequest.setNextComponent(Component.WC.getKey());
            WalletProcessor.forWC(aMessageRequest);
        }
        catch (final Exception e)
        {
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" : Request sending to Prepaid topic .."+ ErrorMessage.getStackTraceAsString(e));

            sendToErrorLog(aComponent, aMessageRequest, e);
        }
    }

    public static void sendToRouterComponent(
            Component aComponent,
            MessageRequest aMessageRequest)
    {

        try
        {
 
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" : Request sending to RC topic ..");

            //MessageProcessor.writeMessage(aComponent, Component.RC, aMessageRequest);
            aMessageRequest.setFromComponent(aComponent.getKey());
            aMessageRequest.setNextComponent(Component.RC.getKey());
            RConsumer.forRC(aMessageRequest);
        }
        catch (final Exception e)
        {
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" : Request sending to Route Consumer topic .."+ ErrorMessage.getStackTraceAsString(e));

            sendToErrorLog(aComponent, aMessageRequest, e);
        }
    }

    public static void sendToErrorLog(
            Component aComponent,
            MessageRequest aMessageRequest,
            Exception aErrorMsg)
    {

        try
        {
       
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" : Request sending to ERROR topic ..");

            PlatformUtil.sendToErrorLog(aComponent, aMessageRequest, aErrorMsg);
        }
        catch (final Exception e)
        {
    
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" : Request sending to ERROR topic .."+ ErrorMessage.getStackTraceAsString(e));

        }
    }

    private static void doFeatureCodeIdentifier(
            MessageRequest aMessagaeRequest)
    {
        new FCFinder(aMessagaeRequest).splitMessageProcess();
    }

}
