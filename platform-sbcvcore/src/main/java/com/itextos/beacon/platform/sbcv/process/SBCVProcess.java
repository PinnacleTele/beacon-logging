package com.itextos.beacon.platform.sbcv.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.platform.sbcv.util.SBCVProducer;

public class SBCVProcess
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(SBCVProcess.class);


    public SBCVProcess(
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

        SBCVProcess.forSBCV(lMessageRequest);
    }

    public static void forSBCV(MessageRequest lMessageRequest) {
    	


    	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" : SBCV Receive the request");
        	

        try
        {
            final boolean canProcess = new ScheduleBlockout(lMessageRequest).validateScheduleBlockoutMsg();

            if (!canProcess)
            {
                final boolean isRejectedMessage = lMessageRequest.isPlatfromRejected();

            	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :Is Message drop based on Schedule / Blockout ? " + isRejectedMessage);


                if (isRejectedMessage)
                    SBCVProducer.sendToPlatformRejection(lMessageRequest);
                else
                    sendToNextProcess(lMessageRequest);
            }
            else
            {
            	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" :Request sending to VC topic" );
                SBCVProducer.sendToVerifyConsumerTopic(lMessageRequest);
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    
    	//   log.debug(" smslog : "+lMessageRequest.getLogBuffer().toString());

    }
    
    private static void sendToNextProcess(
            MessageRequest aMessageRequest)
    {

        final int lBlockOutScheduel = aMessageRequest.getScheduleBlockoutMessage();


    	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" : Sending to Schedule/ Blockout Topic :  "+lBlockOutScheduel );

        if (Constants.SCHEDULE_MSG == lBlockOutScheduel)
            SBCVProducer.sendToScheduleTopic(aMessageRequest);

        if (Constants.BLOCKOUT_MSG == lBlockOutScheduel)
            SBCVProducer.sendToBlockoutTopic(aMessageRequest);
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
