package com.itextos.beacon.platform.topic2table.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.platform.topic2table.wrapper.T2DbTableWrapper;

public abstract class AbstractTableInserterWrapper
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log       log = LogFactory.getLog(AbstractTableInserterWrapper.class);

    private final T2DbTableWrapper mT2DbTableWrapper;


    protected AbstractTableInserterWrapper(
            String aThreadName,
            Component aComponent,
            ClusterType aPlatformCluster,
            String aTopicName,
            ConsumerInMemCollection aConsumerInMemCollection,
            int aSleepInMillis,
            Table2DBInserterId aTableInserterId)
    {
        super(aThreadName, aComponent, aPlatformCluster, aTopicName, aConsumerInMemCollection, aSleepInMillis);
        
        mT2DbTableWrapper = new T2DbTableWrapper(aComponent, aTableInserterId);
    }

    @Override
    public void doCleanup()
    {
        if (mT2DbTableWrapper != null)
            mT2DbTableWrapper.processNow();
    }

    @Override
    public void doProcess(
            BaseMessage aBaseMessage)
    {
        if (log.isDebugEnabled())
            log.debug(mComponent + " : Message Received : " + aBaseMessage);

        try
        {
        	mT2DbTableWrapper.addMessage(aBaseMessage);
        }
        catch (final Exception e)
        {
            log.error(mComponent + " : Exception while trying to insert  on insert err errorMsg  ::::", e);
            log.error(" on insert err map  " + aBaseMessage.toString(), e);

            // TODO send to error topic
        }
    }

    @Override
    protected void updateBeforeSendBack(
            IMessage aMessage)
    {}

}