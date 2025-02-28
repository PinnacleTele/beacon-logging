package com.itextos.beacon.commonlib.kafkaservice.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

class TestProducerCallback
        implements
        Callback
{

    private static final Log log = LogFactory.getLog(TestProducerCallback.class);
    private final String     mTopicName;

    TestProducerCallback(
            String aTopicName)
    {
        mTopicName = aTopicName;
    }

    @Override
    public void onCompletion(
            RecordMetadata aMetadata,
            Exception aException)
    {
        // final boolean isValidPush = updateOffsetValue(mTopicName, aMetadata);
        //
        // if (!isValidPush)
        // try
        // {
        // log.error("Exception while sending to kafka. Doing the backup process.
        // IMessage :'" + aMetadata + "'", aException);
        // doFallbackForKafka();
        // }
        // catch (final Exception e)
        // {
        // log.error("Exception while sending back to the same queue", e);
        // }
    }

    // private void doFallbackForKafka()
    // {
    // // TODO What todo for the backup process.
    // }
    //
    // public static boolean updateOffsetValue(
    // String aTopicName,
    // RecordMetadata aMetadata)
    // {
    //
    // if ((aMetadata != null) && (aMetadata.hasOffset()) &&
    // (aMetadata.hasTimestamp()))
    // {
    // final int partition = aMetadata.partition();
    // final long offSet = aMetadata.offset();
    // final long timestamp = aMetadata.timestamp();
    //
    // if (log.isDebugEnabled())
    // log.debug("KafkaType : 'PRODUCER' Topic : '" + aTopicName + "' Partition : '"
    // + partition + "' Offset : '" + offSet + "' Timestamp : '" + timestamp + "'");
    //
    // // KafkaUtility.getInstance().addProducerStatistics(aTopicName, partition,
    // // offSet, timestamp);
    // return true;
    // }
    // return false;
    // }

}