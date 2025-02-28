package com.itextos.beacon.platform.smppdlr.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.platform.smppdlr.inmemq.InmemoryQueue;
import com.itextos.beacon.platform.smppdlrutil.util.SmppDlrProducer;
import com.itextos.beacon.smslog.PromoConsumerLog;
import com.itextos.beacon.smslog.TransConsumerLog;

public class SMPPDlrProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(SMPPDlrProcessor.class);

    public SMPPDlrProcessor(
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
        final DeliveryObject lDeliveryObject = (DeliveryObject) aBaseMessage;

        if (log.isDebugEnabled())
            log.debug(lDeliveryObject.getMessageId()+ " : SMPP Dlr the request : " + lDeliveryObject);

     	String msgid="notfind";
    	
    	String msgtype="notfind";
    
    	msgtype=((DeliveryObject)lDeliveryObject).getMessageType().getKey();

		msgid =((DeliveryObject)lDeliveryObject).getMessageId()+" msgtype : "+msgtype+ " getClusterType : "+((DeliveryObject)lDeliveryObject).getClusterType().toString()+ " getSmsPriority : "+((DeliveryObject)lDeliveryObject).getSmsPriority()+ " getMessagePriority : "+((DeliveryObject)lDeliveryObject).getMessagePriority();
	
	
        try
        {
            InmemoryQueue.getInstance().addRecord(lDeliveryObject);
            
        	if(msgtype!=null&&msgtype.equals("0")) {
    			
    			PromoConsumerLog.log(msgid+ " "+ mTopicName + " Consumed successfully in Non-Trans mode (Async)");

    		}else {
    			
    			TransConsumerLog.log(msgid+ " "+ mTopicName + " Consumed successfully in Non-Trans mode (Async)");
    		}
        }
        catch (final Exception e)
        {
            SmppDlrProducer.sendToErrorLog(lDeliveryObject, e);
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
