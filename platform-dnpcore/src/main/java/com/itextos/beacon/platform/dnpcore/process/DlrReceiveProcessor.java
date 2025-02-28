package com.itextos.beacon.platform.dnpcore.process;

import java.util.HashMap;
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
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.dnpayloadutil.PayloadProcessor;
import com.itextos.beacon.platform.dnpcore.inmem.NoPayloadRetryQ;
import com.itextos.beacon.platform.dnpcore.util.DNPProducer;
import com.itextos.beacon.platform.dnpcore.util.DNPUtil;
import com.itextos.beacon.smslog.DNLog;

public class DlrReceiveProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(DlrReceiveProcessor.class);

    public DlrReceiveProcessor(
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

        try
        {
            DeliveryObject lDeliveryObject = (DeliveryObject) aBaseMessage;

            forDLR(lDeliveryObject);
        }
        
        catch (final Exception e)
        {
            log.error("Exception occer while processing the Carrier DN/ Internal Rejection Dlr : ", e);
            DNPProducer.sendToErrorLog(aBaseMessage, e);
        }
    }
    
    
    public static void forDLR(DeliveryObject lDeliveryObject) throws Exception {
    	
    	
        lDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER).append("\n").append(" LOG START");

 
        // Request Received from Carrier

        final String requestFromVoice = null;// aNunMessage.getValue(MiddlewareConstant.MW_IS_VOICE_DLR);

        String       lPayloadStatus   = CommonUtility.nullCheck(lDeliveryObject.getDnPayloadStatus(), true);

        if (requestFromVoice == null)
        {

            if (lPayloadStatus.isEmpty())
            {
                final String lCarrierFullDn = CommonUtility.nullCheck(lDeliveryObject.getCarrierFullDn(), true);

                if (!lCarrierFullDn.isEmpty())
                    lDeliveryObject = DNPUtil.processDR(lDeliveryObject);

       
                lDeliveryObject = PayloadProcessor.retrivePayload(lDeliveryObject);
            }

   
            lPayloadStatus = CommonUtility.nullCheck(lDeliveryObject.getDnPayloadStatus(), true);
        }

        if (lDeliveryObject.isPlatfromRejected() || "1".equals(lPayloadStatus) || "1".equals(requestFromVoice))
        {
            DNPUtil.setPlatformErrorCodeBasedOnCarrierErrorCode(lDeliveryObject);

            final Map<Component, DeliveryObject> lProcessDnReceiverQ = DlrProcessUtil.processDnReceiverQ(lDeliveryObject);

            if ((lProcessDnReceiverQ != null) && !lProcessDnReceiverQ.isEmpty())
            {
                   
                lDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER).append("\n").append(lDeliveryObject.getMessageId()+" :  Sending to " + lProcessDnReceiverQ.keySet() + " Message Obj :" + lDeliveryObject);


                // return processDnReceiverQ;
                DNPProducer.sendToNextComponents(lProcessDnReceiverQ, lDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER));
            }
        }
        else
        {
            final HashMap<Component, DeliveryObject> processDNQueues = new HashMap<>();

            // -1, 0
            if ("-1".equals(lPayloadStatus))
            {
                DNPUtil.setPlatformErrorCodeBasedOnCarrierErrorCode(lDeliveryObject);
                processDNQueues.put(Component.T2DB_NO_PAYLOAD_DN, lDeliveryObject);
            }
            else
                if ("0".equals(lPayloadStatus))
                    NoPayloadRetryQ.getInstance().addMessage(lDeliveryObject);

            if (log.isDebugEnabled())
                log.debug(" Sending to " + processDNQueues.keySet() + " Message Obj:" + lDeliveryObject);

            DNPProducer.sendToNextComponents(processDNQueues, lDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER));
        }
        
        
        DNLog.log(lDeliveryObject.getLogBuffer().toString());


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
