package com.itextos.beacon.platform.dnpcore.process;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.errorlog.SMSLog;
import com.itextos.beacon.platform.dnpcore.log.DNPLog;
import com.itextos.beacon.platform.dnpcore.util.DNPProducer;

public class DlrInternalProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(DlrInternalProcessor.class);

    public DlrInternalProcessor(
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
            log.debug(" Message received : " + aBaseMessage);

        final DeliveryObject lDeliveryObject = (DeliveryObject) aBaseMessage;

        forDLRInternal(lDeliveryObject,SMSLog.getInstance());
    }

    public static void forDLRInternal(final DeliveryObject lDeliveryObject,SMSLog sb) {
    	
    	 try
         {
             lDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER).append("\n").append(" LOG START");

             DNPLog.getInstance(lDeliveryObject.getClientId()).log(lDeliveryObject.getClientId(),lDeliveryObject.getFileId()+ " : "+ lDeliveryObject.getMessageId() + " enter forDLRInternal : ");
        
             final Map<Component, DeliveryObject> processInternalDNQ = DlrProcessUtil.processDnReceiverQ(lDeliveryObject);

             DNPProducer.sendToNextComponents(processInternalDNQ,sb);
         }
         catch (final Exception e)
         {
             log.error("Exception occer while processing the  Internal Processor Dlr : ", e);

             DNPProducer.sendToErrorLog(lDeliveryObject, e);
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
