package com.itextos.beacon.commonlib.kafkaservice.producer;

import com.itextos.beacon.commonlib.kafkaservice.common.AbstractInMemCollection;
import com.itextos.beacon.commonlib.kafkaservice.common.KafkaType;

public class ProducerInMemCollection
        extends
        AbstractInMemCollection
{

    public ProducerInMemCollection(
            String aProducerTopicName)
    {
        super(KafkaType.PRODUCER, aProducerTopicName);
    }

}