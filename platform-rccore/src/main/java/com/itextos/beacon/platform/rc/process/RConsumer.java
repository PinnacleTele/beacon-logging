package com.itextos.beacon.platform.rc.process;

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
import com.itextos.beacon.platform.rc.util.RCProducer;
import com.itextos.beacon.platform.rc.util.RCUtil;

public class RConsumer
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(RConsumer.class);

    public RConsumer(
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
    
        lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" : Message Received ");

        RConsumer.forRC(lMessageRequest);
   }

    public static void forRC(MessageRequest lMessageRequest) {
    	
        try
        {
            RCUtil.setMessageValidityPeriod(lMessageRequest);

            if (lMessageRequest.isIsIntl())
                new RCIntlProcessor(lMessageRequest).doProcess();
            else
                new RProcessor(lMessageRequest).doRCProcess();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while processing the RC Component..", e);

            lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" : Exception occer while processing the RC Component "+ErrorMessage.getStackTraceAsString(e));

            try
            {
                RCProducer.sendToErrorLog(lMessageRequest, e);
            }
            catch (final Exception e1)
            {
                log.error("Unable to push the exception in eror log ..", e);
                lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" : Exception occer while processing the error log "+ErrorMessage.getStackTraceAsString(e));

            }
        }

  	//   log.debug(" smslog : "+lMessageRequest.getLogBuffer().toString());

    }
    @Override
    public void doCleanup()
    {}

    @Override
    protected void updateBeforeSendBack(
            IMessage aMessage)
    {
        // TODO Auto-generated method stub
    }

}
