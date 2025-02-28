package com.itextos.beacon.platform.dch.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.errorlog.SMSLog;
import com.itextos.beacon.platform.dch.util.DCHUtil;
import com.itextos.beacon.platform.dnpcore.process.DlrInternalProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class DummyCarrierHandoverProcess
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(DummyCarrierHandoverProcess.class);

    public DummyCarrierHandoverProcess(
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
        final SubmissionObject lSubmissionObject = (SubmissionObject) aBaseMessage;

        if (log.isDebugEnabled())
            log.debug("Dummy Carrier Handover receiver  : " + lSubmissionObject.getJsonString());

        DummyCarrierHandoverProcess.forDCH(lSubmissionObject,SMSLog.getInstance());
    }

    
    public static void forDCH(SubmissionObject lSubmissionObject,SMSLog sb) {
    	
    	 try
         {
      
             final DeliveryObject lDeliveryObject = DCHUtil.processDR(lSubmissionObject);

             if (log.isDebugEnabled())
                 log.debug("Before handover to Dlr Internal process : " + lSubmissionObject.getJsonString());

             sendToDlrReceiver(lDeliveryObject,sb);
         }
         catch (final Exception e)
         {
             e.printStackTrace();
         }
    }
    public static void sendToDlrReceiver(
            DeliveryObject aDeliveryObject,SMSLog sb)
    {

        try
        {
        //    MessageProcessor.writeMessage(Component.DCH, Component.DLRINTLP, aDeliveryObject);
        	
            aDeliveryObject.setNextComponent(Component.DLRINTLP.getKey());
            aDeliveryObject.setFromComponent(Component.DCH.getKey());
       
        	DlrInternalProcessor.forDLRInternal(aDeliveryObject,sb);
        }
        catch (final Exception e)
        {
            log.error("Exception occer while sending to Dlr Internal Processor topic ..", e);
            sendToErrorLog(aDeliveryObject, e);
        }
    }

    public static void sendToErrorLog(
            BaseMessage aBaseMessage,
            Exception aErrorMsg)
    {
        PlatformUtil.sendToErrorLog(Component.DCH, aBaseMessage, aErrorMsg);
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
