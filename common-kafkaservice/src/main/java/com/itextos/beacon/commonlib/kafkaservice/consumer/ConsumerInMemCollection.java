package com.itextos.beacon.commonlib.kafkaservice.consumer;

import com.itextos.beacon.commonlib.kafkaservice.common.AbstractInMemCollection;
import com.itextos.beacon.commonlib.kafkaservice.common.KafkaType;

public class ConsumerInMemCollection
        extends
        AbstractInMemCollection
{

    public ConsumerInMemCollection(
            String aConsumerTopicName)
    {
        super(KafkaType.CONSUMER, aConsumerTopicName);
    }

}
