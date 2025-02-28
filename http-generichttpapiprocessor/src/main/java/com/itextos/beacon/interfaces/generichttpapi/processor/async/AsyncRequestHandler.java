package com.itextos.beacon.interfaces.generichttpapi.processor.async;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.AsyncRequestObject;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.commonlib.messageprocessor.request.ProducerKafkaRequest;

public class AsyncRequestHandler
{

    private static final InterfaceGroup  DEFULT_INTERFACE_GROUP   = InterfaceGroup.API;
    private static final Component       DEFAULT_COMPONENT        = Component.INTERFACE_ASYNC_PROCESS;
    private static final MessagePriority DEFAULT_MESSAGE_PRIORITY = MessagePriority.PRIORITY_5;

    private AsyncRequestHandler()
    {}

    public static void writeToKafka(
            ClusterType aCluster,
            String aMessageSource,
            MessageType aMessageType,
            String aInstanceId,
            String aCustomerId,
            String aMessageId,
            String aCustomerIp,
            String aMessageContent,
            long aRequestedTime)
            throws ItextosException
    {
        final AsyncRequestObject   lAsyncRequestObject  = new AsyncRequestObject(aCluster, InterfaceType.HTTP_JAPI, aMessageType, aInstanceId, aCustomerId, aMessageId, aCustomerIp, aMessageContent,
                aRequestedTime, aMessageSource);
        final ProducerKafkaRequest producerKafkaRequest = new ProducerKafkaRequest(Component.INTERFACES, DEFAULT_COMPONENT, aCluster, DEFULT_INTERFACE_GROUP, aMessageType, DEFAULT_MESSAGE_PRIORITY,
                false, null);

        MessageProcessor.writeMessage(producerKafkaRequest, AsyncFallbackQ.getInstance().getBlockingQueue(), lAsyncRequestObject);
    }

}