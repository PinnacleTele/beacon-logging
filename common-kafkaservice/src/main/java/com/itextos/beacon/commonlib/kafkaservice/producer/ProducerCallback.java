package com.itextos.beacon.commonlib.kafkaservice.producer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.kafkaservice.common.KafkaRedisHandler;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.errorlog.ErrorLog;

public class ProducerCallback
        implements
        Callback
{

    private static final Log log = LogFactory.getLog(ProducerCallback.class);

    private final Producer   mProducer;
    private final String     mTopicName;
    private final IMessage   mMessage;

    public ProducerCallback(
            Producer aProducer,
            String aTopicName,
            IMessage aMessage)
    {
        mProducer  = aProducer;
        mTopicName = aTopicName;
        mMessage   = aMessage;
    }

    @Override
    public void onCompletion(
            RecordMetadata aMetadata,
            Exception aException)
    {

        if ((aException != null) || (aMetadata == null) || !aMetadata.hasOffset())
        {
            log.error("Exception while sending to kafka. Doing the backup process. IMessage :'" + mMessage + "'", aException);
        	ErrorLog.log( "Exception while sending to kafka. Doing the backup process. IMessage :'" + mMessage + "' "+ ErrorMessage.getStackTraceAsString(aException));

            
            try
            {
                doFallbackForKafka();
            }
            catch (final Exception e)
            {
            	
            	ErrorLog.log("Exception while sending back to the same queue"+ ErrorMessage.getStackTraceAsString(e));
                log.error("Exception while sending back to the same queue", e);
            }
        }

        mProducer.removeFromInMemory(mMessage);
    }

    private void doFallbackForKafka()
    {
    	//commanded need to handle
       // KafkaRedisHandler.addToProducerRedis(mProducer.getComponent(), mTopicName, mMessage);
    }

}