package com.itextos.beacon.platform.vc.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.platform.vcprocess.MsgVerifyProcessor;
import com.itextos.beacon.platform.vcprocess.util.VCProducer;

public class MessageProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(MessageProcessor.class);

    public MessageProcessor(
            String aThreadName,
            Component aComponent,
            ClusterType aPlatformCluster,
            String aTopicName,
            ConsumerInMemCollection aConsumerInMemCollection,
            int aSleepInMillis)
    {
        super(aThreadName, aComponent, aPlatformCluster, aTopicName, aConsumerInMemCollection, aSleepInMillis);
    }

    @Override
    public void doProcess(
            BaseMessage aBaseMessage)
    {
        final MessageRequest lMessageRequest = (MessageRequest) aBaseMessage;

     
    	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" : message Received");

        MessageProcessor.forVC(mComponent,lMessageRequest);
    }

    
    public static void forVC(Component mComponent,MessageRequest lMessageRequest) {
    	
        try
        {
      
            final MsgVerifyProcessor lMsgVerifyProcessor = new MsgVerifyProcessor(mComponent, lMessageRequest);
            lMsgVerifyProcessor.process();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while processing the message in Verify Consumer ....", e);

        	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" : Exception occer while processing the message in Verify Consumer "+ErrorMessage.getStackTraceAsString(e));

            VCProducer.sendToErrorLog(mComponent, lMessageRequest, e);
        }
        
 	//   log.debug(" smslog : "+lMessageRequest.getLogBuffer().toString());

    }
    @Override
    public void doCleanup()
    {
        // TODO Auto-generated method stub
    }

    @Override
    protected void updateBeforeSendBack(
            IMessage aMessage)
    {
        // TODO Auto-generated method stub
    }

}
