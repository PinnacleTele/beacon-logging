package com.itextos.beacon.platform.customkafkaprocessor;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.customkafkaprocessor.util.CustomKafkaProperties;

public class CustomKafkaConsumer
        implements
        Runnable,
        ConsumerRebalanceListener
{

    private static final Log                      log                  = LogFactory.getLog(CustomKafkaConsumer.class);
    private static final String                   PROPERTY_KAFKA_TOPIC = "kafka.topic.name";
    private static final String                   PROPERTY_CLIENT_ID   = "client.id";

    private final String                          mTopicName;
    private final int                             mThreadIndex;
    private final KafkaConsumer<String, IMessage> mConsumer;
    private boolean                               mClosed              = false;
    private boolean                               consumerCreated      = false;

    private long                                  mLastCommitted       = System.currentTimeMillis();

    public CustomKafkaConsumer(
            String aTopicName,
            int aThreadIndex)
    {
        mTopicName   = aTopicName;
        mThreadIndex = aThreadIndex;

        mConsumer    = new KafkaConsumer<>(getKafkaConsumerProperties(aTopicName, aThreadIndex));
        mConsumer.subscribe(Arrays.asList(mTopicName), this);

        consumerCreated = true;
    }

    public boolean isConsumerCreated()
    {
        return consumerCreated;
    }

    private static Properties getKafkaConsumerProperties(
            String aTopicName,
            int aThreadIndex)
    {
        final Properties lConsumerProperties = CustomKafkaProperties.getInstance().getConsumerProperties();
        lConsumerProperties.setProperty(PROPERTY_KAFKA_TOPIC, aTopicName);
        lConsumerProperties.setProperty(PROPERTY_CLIENT_ID, aTopicName + "_" + aThreadIndex + "_" + CustomKafkaProperties.getServerIP().replace('.', '_'));

        // for (final Entry<Object, Object> entry : lConsumerProperties.entrySet())
        // System.out.println("PROPERTIES : '" + entry.getKey() + "'='" +
        // entry.getValue() + "'");
        return lConsumerProperties;
    }

    @Override
    public void onPartitionsRevoked(
            Collection<TopicPartition> aPartitions)
    {
        log.fatal("Partition Revoked on " + mTopicName);
    }

    @Override
    public void onPartitionsAssigned(
            Collection<TopicPartition> aPartitions)
    {
        log.fatal("Partition Assigned on " + mTopicName + " - " + aPartitions.size());

        for (final TopicPartition partition : aPartitions)
        {
            final int  partNo    = partition.partition();
            final long curOffset = mConsumer.position(partition);

            log.fatal("Topic '" + mTopicName + "' Partition '" + partNo + "' Curr Offset '" + curOffset + "'");
        }
    }

    @Override
    public void run()
    {
        process();
    }

    private void process()
    {

        try
        {
            log.fatal(mTopicName + "-" + mThreadIndex + " Starting " + (!mClosed));
            final long                              startTime = System.currentTimeMillis();
            int loopcount=0;
            while (!mClosed)
            {
            	loopcount++;
            	
                final ConsumerRecords<String, IMessage> records   = mConsumer.poll(Duration.ofMillis(CustomKafkaProperties.getInstance().getConsumerPollInterval()));
                final int                               pollCount = records.count();
                final long                              endTime   = System.currentTimeMillis();

                if (pollCount != 0)
                {
                    log.debug("Time taken " + (endTime - startTime) + " records " + pollCount);

                    processRecords(records);

                    checkAndCommit(pollCount);
                }
                else
                {
                    checkAndCommit(0);

                    // Goto Sleep after the commit.
                //    CommonUtility.sleepForAWhile(100);
                }
                
                if(loopcount>10||(endTime-startTime)>100) {
                	
                	break;
                }
            }

            if(mClosed) {
            waitForAllMessagesToProcess();

            // Call the commit method and send the unprocessed messages to Redis.
            updateRedisAndCommitConsumer();
            }
        }
        catch (final WakeupException we)
        {
            // Ignore exception if closing
            if (!mClosed)
                throw we;
        }
        catch (final Exception ex)
        {
            log.error("Exception while consumeing messages.", ex);
        }
        finally
        {
            waitForAllMessagesToProcess();

            updateRedisAndCommitConsumer();

            mClosed = true;

            try
            {
                if (mConsumer != null)
                    mConsumer.close();

                log.fatal("Closed Consumer " + this);
            }
            catch (final Exception exc)
            {
                //
            }

            log.fatal("#### Completely Stopped the consumer ************ ");
        }
    }

    private static void waitForAllMessagesToProcess()
    {
        log.fatal("Is All inmemory processed " + (InmemoryCollection.getInstance().size() == 0));

        while (!InmemoryCollection.getInstance().isEmpty())
        {
            log.fatal("Waiting for the messages to be processed from in memory. Messages count " + InmemoryCollection.getInstance().size());
            CommonUtility.sleepForAWhile(10);
        }

        log.fatal("AFTER WHILE Is All inmemory processed " + (InmemoryCollection.getInstance().size() == 0));
    }

    private void processRecords(
            ConsumerRecords<String, IMessage> aRecords)
            throws InterruptedException
    {

        for (final ConsumerRecord<String, IMessage> messageFromKafka : aRecords)
        {
            if (log.isDebugEnabled())
                log.debug("Consumed \t" + mTopicName + "\t" + messageFromKafka.partition() + "\t" + messageFromKafka.offset());

            // final String msg = String.format("topic =%s, partition =%s, offset = %d, key
            // = %s, value = %s", messageFromKafka.topic(), messageFromKafka.partition(),
            // messageFromKafka.offset(),
            // messageFromKafka.key(), messageFromKafka.value());

            // log.fatal(msg);

            final IMessage message = messageFromKafka.value();

            InmemoryCollection.getInstance().add(message);

            if (mClosed)
                log.fatal("Consumer In mem size " + InmemoryCollection.getInstance().size());
        }
    }

    private void checkAndCommit(
            int aPollCount)
    {
        updateRedisAndCommitConsumer();
    }

    private void updateRedisAndCommitConsumer()
    {
        if (log.isDebugEnabled())
            log.debug("Committing now. mLastCommitted " + new Date(mLastCommitted));

        mConsumer.commitAsync();
        resetCounter();
    }

    private void resetCounter()
    {
        mLastCommitted = System.currentTimeMillis();
    }

}