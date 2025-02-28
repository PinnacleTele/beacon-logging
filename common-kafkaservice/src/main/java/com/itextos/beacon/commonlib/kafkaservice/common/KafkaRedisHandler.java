package com.itextos.beacon.commonlib.kafkaservice.common;

import java.util.List;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.message.IMessage;

public class KafkaRedisHandler
{

    private KafkaRedisHandler()
    {}

    public static void addToProducerRedis(
            Component aComponent,
            String aTopicName,
            IMessage aMessage)
    {
        KafkaUtility.addToProducerRedis(aComponent, aTopicName, aMessage);
    }

    public static void addToProducerRedis(
            Component aComponent,
            String aTopicName,
            List<IMessage> aMessageList)
    {
        KafkaUtility.addToProducerRedis(aComponent, aTopicName, aMessageList);
    }

    public static void addToConsumerRedis(
            Component aComponent,
            String aTopicName,
            List<IMessage> aMessageList)
    {
        KafkaUtility.addToConsumerRedis(aComponent, aTopicName, aMessageList);
    }

}
