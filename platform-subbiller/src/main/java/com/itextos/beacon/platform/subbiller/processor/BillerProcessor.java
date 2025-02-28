package com.itextos.beacon.platform.subbiller.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.errorlog.SMSLog;
import com.itextos.beacon.platform.billing.SubmissionProcess;

public class BillerProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(BillerProcessor.class);

    public BillerProcessor(
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
        if (log.isDebugEnabled())
            log.debug("Biller Received Object .. " + aBaseMessage);

        BillerProcessor.forSUBPC(aBaseMessage,SMSLog.getInstance());
    }

    public static void forSUBPC(BaseMessage aBaseMessage,SMSLog sb) {
    	
    	 try
         {
    	     if (log.isDebugEnabled())
    	            log.debug("Biller Received Object .. " + aBaseMessage);

             final SubmissionProcess lSubProcessor = new SubmissionProcess((SubmissionObject) aBaseMessage);
             lSubProcessor.process(sb);
         }
         catch (final ItextosException e)
         {
             log.error("Exception occure while process the submission billing : ", e);
         }
    }
    @Override
    public void doCleanup()
    {}

    @Override
    protected void updateBeforeSendBack(
            IMessage aMessage)
    {}

}
