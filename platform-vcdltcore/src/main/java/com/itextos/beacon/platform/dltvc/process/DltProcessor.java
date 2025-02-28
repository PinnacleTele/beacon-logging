package com.itextos.beacon.platform.dltvc.process;

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
import com.itextos.beacon.platform.templatefinder.TemplateScrubber;
import com.itextos.beacon.platform.vcprocess.DltMessageVerifyProcessor;
import com.itextos.beacon.platform.vcprocess.util.VCProducer;

public class DltProcessor
        extends
        AbstractKafkaComponentProcessor
{

   private static final Log log = LogFactory.getLog(DltProcessor.class);

    public DltProcessor(
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

        lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" : Message Received :  " );

        DltProcessor.forDLT(lMessageRequest, mComponent);
     
    }

    public static void forDLT(MessageRequest lMessageRequest,Component mComponent) {
    	
    	   try
           {
               TemplateScrubber.setComponent(mComponent);
               TemplateScrubber.setClusterType(lMessageRequest.getClusterType());

      
               final DltMessageVerifyProcessor lDltMessageVerifyProcessor = new DltMessageVerifyProcessor(mComponent, lMessageRequest);
               lDltMessageVerifyProcessor.process();
           }
           catch (final Exception e)
           {
               lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" : Exception occer while processing the message in DLT Verify Consumer :  " +ErrorMessage.getStackTraceAsString(e));

               log.error("Exception occer while processing the message in DLT Verify Consumer ....", e);

               VCProducer.sendToErrorLog(mComponent, lMessageRequest, e);
           }
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
