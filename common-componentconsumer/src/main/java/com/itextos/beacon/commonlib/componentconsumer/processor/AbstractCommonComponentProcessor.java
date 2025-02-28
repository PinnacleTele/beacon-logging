package com.itextos.beacon.commonlib.componentconsumer.processor;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.itextos.beacon.commonlib.componentconsumer.processor.extend.Utility;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.kafkaservice.common.KafkaUtility;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.errorlog.ErrorLog;
import com.itextos.beacon.smslog.StartupFlowLog;
import com.itextos.beacon.smslog.TopicLog;

public abstract class AbstractCommonComponentProcessor
        implements
        IComponentProcessor
{

    private static final Log              log                    = LogFactory.getLog(AbstractCommonComponentProcessor.class);
    private static final long             MAX_TIME               = 5 * 1000L;

    protected final String                mThreadName;
    protected final Component             mComponent;
    protected final ClusterType           mPlatformCluster;
    protected final String                mTopicName;
    protected int                         mSleepInMillis;

    private boolean                       mInProcess             = false;
    private boolean                       mStopped               = false;
    private boolean                       mCompleted             = false;
    private long                          mOldMessageLastChecked = 0;

    private final ConsumerInMemCollection consumerInMemCollection;

    protected AbstractCommonComponentProcessor(
            String aThreadName,
            Component aComponent,
            ClusterType aPlatformCluster,
            String aTopicName,
            ConsumerInMemCollection aConsumerInMemCollection,
            int aSleepInMillis)
    {
        mThreadName             = aThreadName;
        mComponent              = aComponent;
        mPlatformCluster        = aPlatformCluster;
        mTopicName              = aTopicName;
        mSleepInMillis          = aSleepInMillis;
        consumerInMemCollection = aConsumerInMemCollection;
        
    	StartupFlowLog.log("AbstractCommonComponentProcessor contsructer Entered");

    }

    @Override
    public void run()
    {
        int messageProcessedAfterStopped = 0;
        
    
        while (true)
        {
            boolean  isNoRecordAvailable = true;
            IMessage lReadMessage        = null;

            try
            {
                TopicLog.getInstance(mTopicName).log("mTopicName : "+mTopicName +" : "+new Date());

                loadOldDataFromRedis();
                

                lReadMessage        = consumerInMemCollection.getMessage();
                isNoRecordAvailable = (lReadMessage == null);

                if (!isNoRecordAvailable && mStopped)
                {
                    messageProcessedAfterStopped++;
                    log.fatal(mTopicName + " Although application stopped. it continue to process the messages. Inmem size " + consumerInMemCollection.getInMemSize() + " Current Message "
                            + lReadMessage);
                }

                if (!isNoRecordAvailable)
                {
                    mInProcess = true;
                    processMessage(lReadMessage);
                    mInProcess = false;
                }
                else
                    if (mStopped)
                    {
                       
                        TopicLog.getInstance(mTopicName).log("Stopped invoked and no messages to process. Exiting the while loop. Messages Processed after stop invoked '" + messageProcessedAfterStopped + "'"+"  mTopicName : "+mTopicName +" : "+new Date());

                    	log.fatal("Stopped invoked and no messages to process. Exiting the while loop. Messages Processed after stop invoked '" + messageProcessedAfterStopped + "'");
                        break;
                    }
            }
            catch (final Exception e)
            {
                log.error("Exxception whil processing the messages. Resending them to the Topics", e);
                
                ErrorLog.log("Exxception whil processing the messages. message loss ln"+ErrorMessage.getStackTraceAsString(e));
                TopicLog.getInstance(mTopicName).log("Exxception whil processing the messages. Resending them to the Topics"+"  mTopicName : "+mTopicName +" : "+new Date()+ " : error : "+ErrorMessage.getStackTraceAsString(e));

           //     sendBackToTopic(lReadMessage);
            }

           
            
        
            try
            {
                if (isNoRecordAvailable) {
                    TopicLog.getInstance(mTopicName).log("goto sleep mSleepInMillis : "+mSleepInMillis+"  mTopicName : "+mTopicName +" : "+new Date());

                    
                    if(mComponent==Component.IC) {
                    	
                        CommonUtility.sleepForAWhile(100);

                    }else {
                    	
                        CommonUtility.sleepForAWhile(1000);
                    }
                }
            }
           
            catch (final Exception e)
            {
                // ignore
            }
             
        }

        TopicLog.getInstance(mTopicName).log("come out from while loop mTopicName : "+mTopicName +" : "+new Date());

        processPendingMessages();

        mCompleted = true;

      
    }

    private void loadOldDataFromRedis()
    {

        try
        {
            final long diff = System.currentTimeMillis() - mOldMessageLastChecked;

            if (diff > MAX_TIME)
            {
                if (log.isInfoEnabled())
                    log.info("Checking for the producer messages from Redis.");

                final Map<String, List<String>> lFallbackProducerData = KafkaUtility.getFallbackProducerData(mComponent);
                handleProducerData(lFallbackProducerData);

                if (log.isInfoEnabled())
                    log.info("Checking for the consumer messages from Redis.");

                final Map<String, List<String>> lFallbackConsumerData = KafkaUtility.getFallbackConsumerData(mComponent, mTopicName);
                handleConsumerData(lFallbackConsumerData);

                mOldMessageLastChecked = System.currentTimeMillis();
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while loading old data from Redis.", e);
        }
    }

    private void handleProducerData(
            Map<String, List<String>> aFallbackProducerData)
    {
        if (aFallbackProducerData.isEmpty())
            return;

        // This may have multiple topics related messages.
        final Map<String, List<FallbackMessage>> lDataFromMap = getDataFromMap(aFallbackProducerData);

        for (final Entry<String, List<FallbackMessage>> entry : lDataFromMap.entrySet())
        {
            final String                topicName = entry.getKey();
            final List<FallbackMessage> lValue    = entry.getValue();

            for (final FallbackMessage msg : lValue)
                try
                {
                    MessageProcessor.writeMessage(mComponent, msg.getNextComponent(), msg.getMessage());
                }
                catch (final Exception e)
                {
                    log.error("Exception while producing the message. Topic '" + topicName + "'. Message '" + msg + "'", e);
                }
        }
    }

    private void handleConsumerData(
            Map<String, List<String>> aFallbackConsumerData)
    {
        if (aFallbackConsumerData.isEmpty())
            return;

        // This will be always for this specific topic
        final Map<String, List<FallbackMessage>> lDataFromMap = getDataFromMap(aFallbackConsumerData);

        final List<FallbackMessage>              lList        = lDataFromMap.get(mTopicName);
        for (final FallbackMessage message : lList)
            try
            {
                consumerInMemCollection.addMessage(message.getMessage());
            }
            catch (final ItextosException e)
            {
                log.error("exception while adding to the inmem collection.", e);
            }
    }

    private Map<String, List<FallbackMessage>> getDataFromMap(
            Map<String, List<String>> aFallbackData)
    {
        final Map<String, List<FallbackMessage>> data = new HashMap<>();

        for (final Entry<String, List<String>> entry : aFallbackData.entrySet())
        {
            final String       topicName   = entry.getKey();
            final List<String> messageList = entry.getValue();

            if (messageList.isEmpty())
                continue;

            int                         count = 0;
            final List<FallbackMessage> list  = data.computeIfAbsent(topicName, k -> new ArrayList<>());
            for (final String message : messageList)
                try
                {
                    final FallbackMessage messageToPush = getMessage(message);
                    list.add(messageToPush);
                    count++;
                }
                catch (final Exception e)
                {
                    log.fatal("Cannot continue with the messages in this topic '" + topicName + "' Check the 'configuration.kafak_topic_class_reference' table."
                            + " Somthing went wrong while converting the Json to Java Objects." + " Json String '" + message + "'", e);
                }

            if (log.isDebugEnabled())
                log.debug("Component '" + mComponent + "' Topic Name '" + topicName + "' total records loaded '" + count + "'");
        }
        return data;
    }

    private static FallbackMessage getMessage(
            String aMessage)
            throws Exception
    {
        final JSONParser     parser             = new JSONParser();
        final JSONObject     jsonObj            = (JSONObject) parser.parse(aMessage);
        final String         programMessageType = (String) jsonObj.get(MiddlewareConstant.MW_PROGRAM_MESSAGE_TYPE.getKey());
        final Component      nextComponent      = Component.getComponent((String) jsonObj.get(MiddlewareConstant.MW_NEXT_COMPONENT.getKey()));
        final String         className          = Utility.getClassName(programMessageType);
        final Class<?>       cls                = Class.forName(className);
        final Constructor<?> constructor        = cls.getDeclaredConstructor(String.class);
        final IMessage       iMessage           = (IMessage) constructor.newInstance(aMessage);
        return new FallbackMessage(iMessage, nextComponent);
    }

    private void processPendingMessages()
    {
        log.fatal("Shutdown invoked. Processing pending messages.");
        IMessage lReadMessage = null;

        int      count        = 0;

        while ((lReadMessage = consumerInMemCollection.getMessage()) != null)
        {
            count++;
            mInProcess = true;
            processMessage(lReadMessage);
            mInProcess = false;
        }

        log.fatal("Completed pending messages. Messages count " + count);
    }

    /*
    void sendBackToTopic(
            List<IMessage> aMessageList)
    {
        if (aMessageList != null)
            for (final IMessage message : aMessageList)
                sendBackToTopic(message);
    }

    protected abstract void sendBackToTopic(
            IMessage aMessage);

*/
    protected abstract void updateBeforeSendBack(
            IMessage aMessage);

    @Override
    public boolean isInProcess()
    {
        return mInProcess;
    }

    @Override
    public void stopProcessing()
    {
        log.fatal(mTopicName + " Messages count in memory " + consumerInMemCollection.getInMemSize());
        mStopped = true;
    }

    @Override
    public boolean isCompleted()
    {
        return mCompleted;
    }

}