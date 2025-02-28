package com.itextos.beacon.platform.prc.process;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.platform.prc.util.PRCUtil;
import com.itextos.beacon.platform.prc.util.PRProducer;

public class RejectionProcess
        extends
        AbstractKafkaComponentProcessor
{

  //  private static final Log log = LogFactory.getLog(RejectionProcess.class);

   
    public RejectionProcess(
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
     
        RejectionProcess.forPRC(aBaseMessage);
    }

    public static void forPRC(BaseMessage aBaseMessage) {
    	
        if (aBaseMessage instanceof MessageRequest) {
        	aBaseMessage.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(((MessageRequest)aBaseMessage).getBaseMessageId()+" :: Message received from PRC");

            processMessageRequest((MessageRequest) aBaseMessage);
        }

        if (aBaseMessage instanceof SubmissionObject) {
        	aBaseMessage.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(((SubmissionObject)aBaseMessage).getBaseMessageId()+" :: Message received from PRC");

            processSubmissionRequest((SubmissionObject) aBaseMessage);
        }
 
    }
    
    private static void processMessageRequest(
            MessageRequest aMessageRequest)
    {

        try
        {
            boolean       canProcessMultiple = false;
            final boolean lprocessDNCarrier  = PRCUtil.processDNDToCarrier(aMessageRequest);

            aMessageRequest.setBaseSmsRate(0);
            aMessageRequest.setBaseAddFixedRate(0);
            
            aMessageRequest.setBillingAddFixedRate(0);
            aMessageRequest.setBillingSmsRate(0);
            
            aMessageRequest.setRefAddFixedRate(0);
            aMessageRequest.setRefSmsRate(0);
         
            aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(((MessageRequest)aMessageRequest).getBaseMessageId()+" :: Is Process DN Carrier for DND Fail :" + lprocessDNCarrier);

            final List<BaseMessage> lSubmissionRequestLst = aMessageRequest.getSubmissions();

          

            aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(((MessageRequest)aMessageRequest).getBaseMessageId()+" : msglist size :" + lSubmissionRequestLst.size());

            if (PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED.getStatusCode().equals(aMessageRequest.getSubOriginalStatusCode()) || lprocessDNCarrier)
                canProcessMultiple = true;

            if (InterfaceType.SMPP == aMessageRequest.getInterfaceType())
                canProcessMultiple = true;

            if ((lSubmissionRequestLst.size() > 1) && canProcessMultiple)
            {
            
                aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(((MessageRequest)aMessageRequest).getBaseMessageId()+" :: Multipart Message request... ");

                PRCUtil.processReq(aMessageRequest, lprocessDNCarrier, true);
            }
            else
                PRCUtil.processReq(aMessageRequest, lprocessDNCarrier, false);
        }
        catch (final Exception e)
        {
            PRProducer.sendToErrorLog(aMessageRequest, e);
        }
    }

    private static void processSubmissionRequest(
            SubmissionObject aSubmissionObject)
    {
        
        aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append((aSubmissionObject).getBaseMessageId()+" :: Processing Submission Object : " );


        try
        {
            PRCUtil.processReq(aSubmissionObject, false);
        }
        catch (final Exception e)
        {
            PRProducer.sendToErrorLog(aSubmissionObject, e);
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
