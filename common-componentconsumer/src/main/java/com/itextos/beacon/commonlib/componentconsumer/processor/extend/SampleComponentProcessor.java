package com.itextos.beacon.commonlib.componentconsumer.processor.extend;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;

public class SampleComponentProcessor
        extends
        AbstractKafkaComponentProcessor
{

    protected SampleComponentProcessor(
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
            BaseMessage aMessage)
    {

        try
        {
            MessageProcessor.writeMessage(Component.IC, Component.VC, aMessage);
        }
        catch (final ItextosException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void doCleanup()
    {}

    @Override
    protected void updateBeforeSendBack(
            IMessage aMessage)
    {}

}