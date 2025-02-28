package com.itextos.beacon.commonlib.messageprocessor.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.kafkaservice.producer.Producer;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.ErrorObject;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.messageprocessor.data.KafkaInformation;
import com.itextos.beacon.commonlib.messageprocessor.process.remove.MessageRemovePropertyReader;
import com.itextos.beacon.commonlib.messageprocessor.request.ConsumerKafkaRequest;
import com.itextos.beacon.commonlib.messageprocessor.request.ProducerKafkaRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.smslog.KafkaSender;

public class MessageProcessor
{

    private static final Log                                                log                           = LogFactory.getLog(MessageProcessor.class);
    private static final Map<ConsumerKafkaRequest, ConsumerInMemCollection> CONSUMER_INMEM_COLLECTION_MAP = new ConcurrentHashMap<>();
    private static final int                                                MAX_RETRY_COUNT               = 10; // Need to take it from config_param

    private MessageProcessor()
    {}

    public static void writeMessage(
            Component aFromComponent,
            Component aNextComponent,
            IMessage aIMessage)
            throws ItextosException
    {
        writeMessage(aFromComponent, aNextComponent, null, aIMessage, false);
    }

    public static void writeMessage(
            Component aFromComponent,
            Component aNextComponent,
            BlockingQueue<IMessage> aFallbackQueue,
            IMessage aIMessage)
            throws ItextosException
    {
        writeMessage(aFromComponent, aNextComponent, aFallbackQueue, aIMessage, false);
    }

    public static void writeMessage(
            Component aFromComponent,
            Component aNextComponent,
            IMessage aIMessage,
            boolean aClientSpecific)
            throws ItextosException
    {
        writeMessage(aFromComponent, aNextComponent, null, aIMessage, aClientSpecific);
    }

    public static void writeMessage(
            Component aFromComponent,
            Component aNextComponent,
            BlockingQueue<IMessage> aFallbackQueue,
            IMessage aIMessage,
            boolean aClientSpecific)
            throws ItextosException
    {
        final BaseMessage     message         = (BaseMessage) aIMessage;
        final ClusterType     cluster         = message.getClusterType();
        final InterfaceGroup  interfaceGroup  = message.getInterfaceGroupType();
        final MessageType     messageType     = message.getMessageType();
        final MessagePriority messagePriority = message.getMessagePriority();
        final boolean         intlFlag        = message.isIsIntl();
        String                clientId        = null;

        if (aClientSpecific)
        {
            if (log.isDebugEnabled())
                log.debug("Messsage Type " + aIMessage.getClass());

            clientId = CommonUtility.nullCheck(message.getClientId(), true);
        }

        final ProducerKafkaRequest pkr = new ProducerKafkaRequest(aFromComponent, aNextComponent, cluster, interfaceGroup, messageType, messagePriority, intlFlag, clientId);

        if (log.isDebugEnabled())
            log.debug("ProducerKafkaRequest : '" + pkr + "'");

        writeMessage(aFromComponent, aNextComponent, pkr, aFallbackQueue, aIMessage);
    }

    public static void writeMessage(
            ProducerKafkaRequest aProducerKafkaRequest,
            BlockingQueue<IMessage> aFallbackQueue,
            IMessage aIMessage)
            throws ItextosException
    {

        if (aIMessage != null)
        {
            final Producer producer = KafkaInformation.getInstance().getProducer(aProducerKafkaRequest);

            if (producer != null)
            {
                aIMessage.setNextComponent(aProducerKafkaRequest.getNextComponent().getKey());
                aIMessage.setFromComponent(aProducerKafkaRequest.getFromComponent().getKey());

                producer.sendAsync(aIMessage, aFallbackQueue);
            }
            else
            {
                log.error("Unable to get a producer for " + aIMessage, new Exception());
                throw new ItextosException("Unable to get a producer for " + aProducerKafkaRequest);
            }
        }
    }

    private static void writeMessage(
            Component aFromComponent,
            Component aNextComponent,
            ProducerKafkaRequest aProducerKafkaRequest,
            BlockingQueue<IMessage> aFallbackQueue,
            IMessage aIMessage)
            throws ItextosException
    {
    	

        if (aIMessage != null)
        {
        	
        	((BaseMessage)aIMessage).removeByConstant(null);

            if (aIMessage instanceof BaseMessage)
            {
                final boolean canContinue = checkForRetryAndRemoveMessage(aFromComponent, aNextComponent, aIMessage);


                if(aIMessage instanceof MessageRequest) {
                if (log.isDebugEnabled())
                    log.debug("Can continue " + canContinue + " for the message "+((MessageRequest)aIMessage).getBaseMessageId() );

                }else if(aIMessage instanceof SubmissionObject){
           
                    if (log.isDebugEnabled())
                        log.debug("Can continue " + canContinue + " for the message "+((SubmissionObject)aIMessage).getBaseMessageId() );

                }else if(aIMessage instanceof DeliveryObject){
           
                    if (log.isDebugEnabled())
                        log.debug("Can continue " + canContinue + " for the message "+((DeliveryObject)aIMessage).getBaseMessageId() );

                }else {
                
                    if (log.isDebugEnabled())
                        log.debug("Can continue " + canContinue + " for the message " );

                }
                if (!canContinue)
                    return;
            }

            final Producer producer = KafkaInformation.getInstance().getProducer(aProducerKafkaRequest);

            if (producer != null)
            {
            	KafkaSender.getInstance(aNextComponent.toString()).log(aNextComponent.toString(),"aFromComponent : "+aFromComponent.toString()+" \t aNextComponent : "+aNextComponent.toString()+"\t : "+aIMessage.toString());

                aIMessage.setNextComponent(aNextComponent.getKey());
                aIMessage.setFromComponent(aFromComponent.getKey());
                if (aFallbackQueue != null)
                    producer.sendAsync(aIMessage, aFallbackQueue);
                else
                    producer.sendAsync(aIMessage);
            }
            else
            {
                log.error("Unable to get a producer for " + aIMessage, new Exception());
                throw new ItextosException("Unable to get a producer for " + aProducerKafkaRequest);
            }
        }
    }

    private static boolean checkForRetryAndRemoveMessage(
            Component aFromComponent,
            Component aNextComponent,
            IMessage aIMessage)
    {
        final BaseMessage baseMessage = (BaseMessage) aIMessage;

        if (aNextComponent != aFromComponent)
        {
            removeMessageEntries(aFromComponent, baseMessage);
            resetRetryCount(baseMessage);
        }
        else
        {
            final int retryCount = incrementRetryCount(baseMessage);

            if (retryCount > MAX_RETRY_COUNT)
            {
                // send to error topic
                sendToErrorTopic(aFromComponent, baseMessage);
                return false;
            }
        }
        return true;
    }

    private static void sendToErrorTopic(
            Component aFromComponent,
            BaseMessage aBaseMessage)
    {
        // TODO IF THE MESSAGE IS COMING FROM ERROR_COMPONENT THEN WE HAVE TO LOG IT
        // SOME WHERE ELSE.

        if (aFromComponent == Component.T2DB_ERROR_LOG)
        {
            log.fatal("Problem in sending the message to the ERROR TOPIC itself", new Exception());
            writeMessageIntoFile(aBaseMessage);
            return;
        }

        sendToErrorLog(aFromComponent, aBaseMessage, new Exception("Retry attempt Exhausted by " + new Date()));
    }

    private static void writeMessageIntoFile(
            BaseMessage aBaseMessage)
    {

        try
        {
            final String fileName = getLoggingFilename(aBaseMessage);

            log.fatal("Since we are unable to write the message to any of the topics, writting into file '" + fileName + "'");

            try (
                    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName))))
            {
                bw.write(aBaseMessage.getJsonString());
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while logging message into files", e);
        }
    }

    private static String getLoggingFilename(
            BaseMessage aBaseMessage)
    {
        final String   clusterType        = aBaseMessage.getClusterType().getKey();
        final String   interfaceType      = aBaseMessage.getInterfaceType().getKey();
        final String   interfaceGroupType = aBaseMessage.getInterfaceGroupType().getKey();
        final String   messageType        = aBaseMessage.getMessageType().getKey();
        final String   messagePriority    = aBaseMessage.getMessagePriority().getKey();
        final String   messageRouteType   = aBaseMessage.getMessageRouteType().getKey();
        String         fileName           = CommonUtility.combine('_', clusterType, interfaceType, interfaceGroupType, messageType, messagePriority, messageRouteType);

        final String[] msgIds             = getMessageIds(aBaseMessage);
        fileName = appendMessageAttributes(fileName, msgIds);

        final String date = DateTimeUtility.getFormattedDateTime(new Date(), "yyyyMMdd_HHmmssSSS");
        return CommonUtility.combine('_', fileName, date);
    }

    private static String appendMessageAttributes(
            String aFileName,
            String[] aMsgIds)
    {
        String temp = aFileName;
        for (final String s : aMsgIds)
            if (s != null)
                temp = CommonUtility.combine('_', temp, s);
        return temp;
    }

    private static String[] getMessageIds(
            BaseMessage aBaseMessage)
    {
        String type      = "generic";
        String clientId  = null;
        String baseId    = null;
        String messageId = null;

        if (aBaseMessage instanceof MessageRequest)
        {
            final MessageRequest msgRequest = (MessageRequest) aBaseMessage;
            clientId = CommonUtility.nullCheck(msgRequest.getClientId(), true);
            baseId   = CommonUtility.nullCheck(msgRequest.getBaseMessageId(), true);
            type     = "MessageRequest";
        }
        else
            if (aBaseMessage instanceof SubmissionObject)
            {
                final SubmissionObject msgRequest = (SubmissionObject) aBaseMessage;
                clientId  = CommonUtility.nullCheck(msgRequest.getClientId(), true);
                baseId    = CommonUtility.nullCheck(msgRequest.getBaseMessageId(), true);
                messageId = CommonUtility.nullCheck(msgRequest.getMessageId(), true);
                type      = "Submission";
            }
            else
                if (aBaseMessage instanceof DeliveryObject)
                {
                    final DeliveryObject msgRequest = (DeliveryObject) aBaseMessage;
                    clientId  = CommonUtility.nullCheck(msgRequest.getClientId(), true);
                    baseId    = CommonUtility.nullCheck(msgRequest.getBaseMessageId(), true);
                    messageId = CommonUtility.nullCheck(msgRequest.getMessageId(), true);
                    type      = "Deliveries";
                }

        return new String[]
        { type, clientId, baseId, messageId };
    }

    private static int incrementRetryCount(
            BaseMessage aBaseMessage)
    {
        return aBaseMessage.incrementRetryAttempt();
    }

    private static void resetRetryCount(
            BaseMessage aBaseMessage)
    {
        aBaseMessage.resetRetryAttempt();
    }

    private static void removeMessageEntries(
            Component aFromComponent,
            BaseMessage aBaseMessage)
    {

        try
        {
            final List<MiddlewareConstant> lRemoveConstants = MessageRemovePropertyReader.getInstance().getRemoveConstants(aFromComponent);
            aBaseMessage.removeByConstant(lRemoveConstants);
        }
        catch (final Exception e)
        {
            log.error("Exception while trying to remove the unwanted entries in the Message. However this will not stop sending the message.", e);
        }
    }

    public static void sendToErrorLog(
            Component aComponent,
            BaseMessage aBaseMessage,
            Exception aException)
    {

        try
        {
            final ErrorObject errorObject = aBaseMessage.getErrorObject(aComponent, aException);
            
            try {
				MessageProcessor.writeMessage(aComponent, Component.T2DB_ERROR_LOG, errorObject);
			} catch (ItextosException e) {
				// TODO Auto-generated catch block
				 log.error("Exception while sending the message to error log topic for the component '" + aComponent + "' Message '" + aBaseMessage + "' Error [[[" + CommonUtility.getStackTrace(aException)
                 + "]]]", e);
				e.printStackTrace();
			}
        }
        catch (final ItextosRuntimeException e)
        {
            log.error("Exception while sending the message to error log topic for the component '" + aComponent + "' Message '" + aBaseMessage + "' Error [[[" + CommonUtility.getStackTrace(aException)
                    + "]]]", e);
        }
    }

}