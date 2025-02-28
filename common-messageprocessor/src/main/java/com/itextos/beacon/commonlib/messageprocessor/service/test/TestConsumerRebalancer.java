package com.itextos.beacon.commonlib.messageprocessor.service.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;


class TestConsumerRebalancer
        implements
        ConsumerRebalanceListener
{

    private static final Log log = LogFactory.getLog(TestConsumerRebalancer.class);

    private final String     mTopicName;

    TestConsumerRebalancer(
            String aTopicName)
    {
        mTopicName = aTopicName;
    }

    @Override
    public void onPartitionsRevoked(
            Collection<TopicPartition> aPartitions)
    {

        for (final TopicPartition tp : aPartitions)
        {
            tp.partition();
            tp.topic();
        }
    }

    @Override
    public void onPartitionsAssigned(
            Collection<TopicPartition> aPartitions)
    {
        final Map<String, Integer> reassignedPartitions = new HashMap<>();

        for (final TopicPartition tp : aPartitions)
        {
            final int    lPartition = tp.partition();
            final String lTopic     = tp.topic();
            reassignedPartitions.put(lTopic, lPartition);
        }

        log.error("Consumer X the topic '" + mTopicName + "'. Reassiging happened for " + reassignedPartitions);

        // KafkaUtility.getInstance().updateReassignInfo(reassignedPartitions);

        if (log.isDebugEnabled())
            log.debug("Update happened in the reassign properties");
    }

}