package com.itextos.beacon.commonlib.messageprocessor.service.test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import com.itextos.beacon.commonlib.utility.CommonUtility;

public class TestKafkaConsumer
        implements
        Runnable,
        ConsumerRebalanceListener
{

    private static final Log                    log                    = LogFactory.getLog(TestKafkaConsumer.class);

    private final String                        mTopicName;
    private final Properties                    mKafkaProperties;
    private final KafkaConsumer<String, String> mConsumer;
    private final Duration                      mConsumerPollInterval;
    private long                                recordCount            = 0;
    private long                                lastCommitted          = System.currentTimeMillis();
    private final long                          maxUncommitRecords     = 0;
    private final long                          maxUncommitTimeInMilis = 0;

    public TestKafkaConsumer(
            String aTopicName,
            Properties aKafkaProperties)
    {
        mTopicName       = aTopicName;
        mKafkaProperties = aKafkaProperties;
        mKafkaProperties.put("group.id", aTopicName + "-GROUP");
        mConsumer = new KafkaConsumer<>(mKafkaProperties);
        mConsumer.subscribe(Arrays.asList(mTopicName), this);
        mConsumerPollInterval = Duration.ofMillis(100);
    }

    @Override
    public void run()
    {

        try
        {

            while (true)
            {
                final long                            startTime = System.currentTimeMillis();
                final ConsumerRecords<String, String> records   = mConsumer.poll(mConsumerPollInterval);
                final int                             pollCount = records.count();
                final long                            endTime   = System.currentTimeMillis();

                if (pollCount != 0)
                    log.error("Time taken :" + (endTime - startTime) + " records " + pollCount);
                // processRecords(records);
                else
                    CommonUtility.sleepForAWhile(100);

                doCommitCheck(pollCount);
            }
        }
        catch (final Exception ex)
        {
            log.error(mTopicName + "Exception while consumeing messages.", ex);
        }
        finally
        {

            try
            {
                if (mConsumer != null)
                    mConsumer.close();
            }
            catch (final Exception exc)
            {
                //
            }
        }
    }

    private void doCommitCheck(
            int aPollCount)
    {
        final boolean isCommitRequired = addRecordsAndCheck(aPollCount);

        if (isCommitRequired)
        {
            mConsumer.commitAsync();
            resetCounter();
        }
    }

    boolean addRecordsAndCheck(
            int aRecordsCount)
    {
        recordCount += aRecordsCount;
        return checkCommitRequired();
    }

    private boolean checkCommitRequired()
    {
        return ((recordCount >= maxUncommitRecords) || ((System.currentTimeMillis() - lastCommitted) > maxUncommitTimeInMilis));
    }

    public void resetCounter()
    {
        recordCount   = 0;
        lastCommitted = System.currentTimeMillis();
    }

    @Override
    public void onPartitionsRevoked(
            Collection<TopicPartition> aPartitions)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPartitionsAssigned(
            Collection<TopicPartition> aPartitions)
    {
        // TODO Auto-generated method stub
    }

}
