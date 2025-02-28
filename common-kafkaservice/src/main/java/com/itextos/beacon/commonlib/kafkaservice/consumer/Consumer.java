package com.itextos.beacon.commonlib.kafkaservice.consumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.kafkaservice.common.KafkaCustomProperties;
import com.itextos.beacon.commonlib.kafkaservice.common.KafkaRedisHandler;
import com.itextos.beacon.commonlib.kafkaservice.common.KafkaUtility;
import com.itextos.beacon.commonlib.kafkaservice.consumer.partitionlogger.KafkaParititionInfo;
import com.itextos.beacon.commonlib.kafkaservice.consumer.partitionlogger.PartitionEventType;
import com.itextos.beacon.commonlib.kafkaservice.consumer.partitionlogger.PartitionInfoCollection;
import com.itextos.beacon.commonlib.kafkaservice.producer.Producer;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.errorlog.ErrorLog;
import com.itextos.beacon.smslog.ConsumerLog;
import com.itextos.beacon.smslog.ConsumerTPLog;
import com.itextos.beacon.smslog.KafkaReceiver;

public class Consumer
        implements
        Runnable,
        ConsumerRebalanceListener
{

    private static final Log                      log                     = LogFactory.getLog(Consumer.class);

    private final Component                       mComponent;
    private final String                          mTopicName;
    private final Properties                      mKafkaConsumerProperties;
    private final String                          mLogTopicName;
    private final KafkaConsumer<String, IMessage> mConsumer;
    private final ConsumerInMemCollection         mConsumerInMemCollection;
    private boolean                               mClosed                 = false;
    private long                                  mRecordCount            = 0;
    private long                                  mLastCommitted          = System.currentTimeMillis();
    private boolean                               mAreRecordsInProcess    = true;
    private final Map<Long, Producer>             mProducerRelatedToTopic = new ConcurrentHashMap<>();
    private boolean                               isCompletelyStopped     = true;
    private boolean                               mIsStartup              = true;

    public Consumer(
            Component aComponent,
            String aTopicName,
            KafkaConsumerProperties aKafkaConsumerProperties,
            ConsumerInMemCollection aConsumerInMemCollection,
            int aConsumerClientIndex)
    {
        mComponent               = aComponent;
        mTopicName               = aTopicName;
        mKafkaConsumerProperties = aKafkaConsumerProperties.getProperties();
        mLogTopicName            = "Topic Name : '" + mTopicName + "-" + aConsumerClientIndex;
        mConsumerInMemCollection = aConsumerInMemCollection;

        KafkaUtility.printProperties(aTopicName + " Consumer", mKafkaConsumerProperties);

        mConsumer = new KafkaConsumer<>(mKafkaConsumerProperties);
        mConsumer.subscribe(Arrays.asList(mTopicName), this);

        if (log.isDebugEnabled())
            log.debug("Consumers created successfully for topic '" + aTopicName + "'");
    }

    private  void process()
    {
        String threadName = Thread.currentThread().getName();

        if (log.isDebugEnabled())
            log.debug("Started consuming messages from '" + mTopicName + "'");
        
        

        try
        {
            isCompletelyStopped = false;

            
            while (!mClosed)
            {
            	ConsumerTPLog.getInstance(mTopicName).log(mTopicName+" : "+new Date());
            	
            
            
                final long                              startTime = System.currentTimeMillis();
                final ConsumerRecords<String, IMessage> records   = mConsumer.poll(Duration.ofMillis(KafkaCustomProperties.getInstance().getConsumerPollInterval()));
                    
                
                final int                               pollCount = records.count();
                final long                              endTime   = System.currentTimeMillis();

                if (log.isDebugEnabled()) {
                    log.debug("Started consuming messages from '" + mTopicName + "' pollCount : "+pollCount);
                }
                
                if (pollCount != 0)
                {
                    if (log.isDebugEnabled())
                        log.debug(mLogTopicName + " Time taken " + (endTime - startTime) + " records " + pollCount);

                    ConsumerLog.log(threadName+" : "+mLogTopicName + " Time taken " + (endTime - startTime) + " records " + pollCount);
                    mAreRecordsInProcess = false;

                    PrometheusMetrics.kafkaConsumerIncrement(mTopicName, pollCount);

                    processRecords(records);

                    mAreRecordsInProcess = true;
                    checkAndCommit(pollCount);
                }
                else
                {
                
                	 checkAndCommit(0);
                    
                 	
                 	if(mComponent.getKey().equals(Component.IC.getKey())) {
                 		
                   	 CommonUtility.sleepForAWhile(100);
                   	 
                  	ConsumerTPLog.getInstance(mTopicName).log("sleepForAWhile(100); : mTopicName "+mTopicName+" : "+new Date());


                 	}else {
                 		
                 		 CommonUtility.sleepForAWhile(1000);
                       	 
                       	ConsumerTPLog.getInstance(mTopicName).log("sleepForAWhile(1000); : mTopicName "+mTopicName+" : "+new Date());

                 	}

                }
                
            }

            
            printInMemoryMessageDetails();

            waitForAllMessagesToProcess();

            // Call the commit method and send the unprocessed messages to Redis.
            flushProducers("ConsumerStopConsuming");
            updateRedisAndCommitConsumer(true, "OnExit");
            resendInMemMessages();
        }
        catch (final WakeupException we)
        {
        	
        	
            log.error(mLogTopicName + "Exception while consumeing messages.", we);
            
            ErrorLog.log(mLogTopicName + "Exception while consumeing messages. \t "+ErrorMessage.getStackTraceAsString(we));

            // Ignore exception if closing
            if (!mClosed)
                throw we;
        }
        catch (final Exception ex)
        {
            log.error(mLogTopicName + "Exception while consumeing messages.", ex);
            
            ErrorLog.log(mLogTopicName + "Exception while consumeing messages \t "+ErrorMessage.getStackTraceAsString(ex));

        }
        finally
        {
            waitForAllMessagesToProcess();

            flushProducers("ConsumerFinally");
            updateRedisAndCommitConsumer(true, "ConsumerStopConsuming");
            resendInMemMessages();
            mClosed = true;

            try
            {
                if (mConsumer != null)
                    mConsumer.close();

                log.fatal("Closed Consumer " + this);
            }
            catch (final Exception exc)
            {
                //
            	
                ErrorLog.log(mLogTopicName + "Exception while consumeing messages \t "+ErrorMessage.getStackTraceAsString(exc));

            }

            log.fatal("#### Completely Stopped the consumer ************ ");

            isCompletelyStopped = true;
            
            ConsumerLog.log(threadName+" isCompletelyStopped : "+isCompletelyStopped);
        }
    }

    private void waitForAllMessagesToProcess()
    {
        log.fatal("Is All inmemory processed " + (mConsumerInMemCollection.getInMemSize() == 0));

        while (mConsumerInMemCollection.getInMemSize() > 0)
        {
            log.fatal("Waiting for the messages to be processed from in memory. Messages count " + mConsumerInMemCollection.getInMemSize());
            CommonUtility.sleepForAWhile(10);
        }

        log.fatal("AFTER WHILE Is All inmemory processed " + (mConsumerInMemCollection.getInMemSize() == 0));
    }

    private void processRecords(
            ConsumerRecords<String, IMessage> aRecords)
            throws Exception
    {

        for (final ConsumerRecord<String, IMessage> messageFromKafka : aRecords)
        {
            if (log.isDebugEnabled())
                log.debug("Consumed \t" + mTopicName + "\t" + messageFromKafka.partition() + "\t" + messageFromKafka.offset());

            final IMessage message = messageFromKafka.value();

            KafkaReceiver.getInstance(message.getNextComponent()).log(message.getNextComponent(),"Consumed \t" + mTopicName + "\t" + messageFromKafka.partition() + "\t" + messageFromKafka.offset());

            KafkaReceiver.getInstance(message.getNextComponent()).log(message.getNextComponent(),message.getJsonString());
            
            mConsumerInMemCollection.addMessage(message);

            if (mClosed)
                log.fatal("Consumer In mem size " + mConsumerInMemCollection.getInMemSize());
        }
    }

    private void checkAndCommit(
            int aPollCount)
    {
        mRecordCount += aPollCount;

        final boolean isCommitRequired = checkCommitRequired();
        if (isCommitRequired)
            updateRedisAndCommitConsumer(false, null);
    }

    private void updateRedisAndCommitConsumer(
            boolean aPrintOffsetInfo,
            String aEvent)
    {
        if (log.isDebugEnabled())
            log.debug(mLogTopicName + " Committing now. Record count " + String.format("%8s", mRecordCount) + " mLastCommitted " + new Date(mLastCommitted));

        updateOffsetinRedis(aPrintOffsetInfo, aEvent);
        mConsumer.commitAsync();
        resetCounter();
    }

    private void updateOffsetinRedis(
            boolean aPrintOffsetInfo,
            String aEvent)
    {
        final Map<Integer, Long> partitionOffset = new HashMap<>();

        for (final TopicPartition topicPartition : mConsumer.assignment())
            partitionOffset.put(topicPartition.partition(), mConsumer.position(topicPartition));

        KafkaUtility.updateRedis(mTopicName, partitionOffset, aPrintOffsetInfo, aEvent);
    }

    private boolean checkCommitRequired()
    {
        // It is working. So no need to have a log here.

        return ((mAreRecordsInProcess)  // This is required when the time based commit request is called. When shutdown)
                // is called, the messages will not be committed as it may be not even added to
                // the in-memory.
                && (mRecordCount > 0) // Do the commit only when there are some records fetched.
                && (isCommitBasedOnCount() || isCommitBasedOnTime()));
    }

    private boolean isCommitBasedOnTime()
    {
        return (System.currentTimeMillis() - mLastCommitted) >= KafkaCustomProperties.getInstance().getConsumerMaxUncommitIdleTime();
    }

    private boolean isCommitBasedOnCount()
    {
        return mRecordCount >= KafkaCustomProperties.getInstance().getConsumerMaxUncommitCount();
    }

    private void resetCounter()
    {
        mRecordCount   = 0;
        mLastCommitted = System.currentTimeMillis();
    }

    public void stopConsuming()
    {
        log.fatal(KafkaUtility.formatTopicName(mTopicName) + " STOP INVOKED");
        mClosed = true;
        mConsumerInMemCollection.shutdown();
    }

    private void printInMemoryMessageDetails()
    {
        ConsumerLog.log(Thread.currentThread().getName()+" : Topic Name "+mLogTopicName  + ", Consumer InMem Collection Size >>>>>>>> " + mConsumerInMemCollection.getInMemSize());

        log.fatal("Topic Name : " + mTopicName + ", Consumer InMem Collection Size >>>>>>>> " + mConsumerInMemCollection.getInMemSize());
    }

    private void resendInMemMessages()
    {
        printInMemoryMessageDetails();
        final List<IMessage> lMessages = mConsumerInMemCollection.getRemainingMessages();

        if (!lMessages.isEmpty())
        {
            log.fatal("Ideally we should not have any data here from CONSUMER. Please check the handling parts....", new Exception("NEED TO CHECK THIS POINT IN CONSUMER"));
            KafkaRedisHandler.addToConsumerRedis(mComponent, mTopicName, lMessages);
        }

        printInMemoryMessageDetails();
        log.fatal("Consumer Process Completed ...................");
    }

    @Override
    public void run()
    {
        process();
    }

    @Override
    public void onPartitionsRevoked(
            Collection<TopicPartition> aPartitions)
    {
        insertIntoInmemoryForDbInsert(PartitionEventType.PARTITION_REVOKE, aPartitions);

        try
        {
            flushProducers("OnPartitionRevoked");
            updateRedisAndCommitConsumer(true, "OnPartitionRevoked");
            resendInMemMessages();
            PartitionInfoCollection.getInstance().processNow();
        }
        catch (final Exception e)
        {
            log.error("Exception on partition Revoked ...........", e);
        }
    }

    @Override
    public void onPartitionsAssigned(
            Collection<TopicPartition> aPartitions)
    {
        insertIntoInmemoryForDbInsert(PartitionEventType.PARTITION_REASSIGN, aPartitions);

        final Map<Integer, Long> lPartitionOffsets = KafkaUtility.getPartitionOffset(mTopicName, aPartitions);

        for (final TopicPartition partition : aPartitions)
        {
            final int  partNo      = partition.partition();
            final Long redisOffset = lPartitionOffsets.get(partNo);
            final long curOffset   = mConsumer.position(partition);

            log.fatal("Topic '" + mTopicName + "' Partition '" + partNo + "' Redis offSet '" + redisOffset + "' Curr Offset '" + curOffset + "'");

            if ((redisOffset == null) || (redisOffset == -1))
                continue;

            if (curOffset < redisOffset)
            {
                log.fatal("Because of the Parition Assigned the offset value was updated from '" + curOffset + "' to '" + redisOffset + "'");
                mConsumer.seek(partition, redisOffset);
            }
        }
    }

    public synchronized void addProducer(
            Long aThreadId,
            Producer aProducer)
    {
        log.fatal("Topic '" + mTopicName + "' adding Producer with Thread id '" + aThreadId + "' and Producer '" + aProducer + "' with consumer " + this);
        mProducerRelatedToTopic.put(aThreadId, aProducer);
    }

    private void flushProducers(
            String aEvent)
    {
        log.fatal("Flushing producers on <<" + aEvent + ">>. Producers count '" + mProducerRelatedToTopic.size() + "'");

        for (final Entry<Long, Producer> entry : mProducerRelatedToTopic.entrySet())
        {
            log.fatal("Flushing Producers on <<" + aEvent + ">> Thread id : '" + entry.getKey() + "' Producer '" + entry.getValue() + "'");

            try
            {
                entry.getValue().flush(aEvent);
            }
            catch (final Exception e)
            {
                log.error("Exception while flusing the Producer Thread id : '" + entry.getKey() + "' Producer '" + entry.getValue().toString() + "' on event '" + aEvent + "'", e);
            }
        }
    }

    @Override
    public String toString()
    {
        return "Consumer [mComponent=" + mComponent + ", mTopicName=" + mTopicName + ", mLastCommitted=" + mLastCommitted + "]";
    }

    public boolean isCompletelyStopped()
    {
        return isCompletelyStopped;
    }

    private void insertIntoInmemoryForDbInsert(
            PartitionEventType aPartitionEvent,
            Collection<TopicPartition> aPartitions)
    {
        final boolean lDbInsertReqForPartitions = KafkaCustomProperties.getInstance().isDbInsertReqForPartitions();

        if (lDbInsertReqForPartitions)
        {
            final Map<Integer, Long> lPartitionOffsets = KafkaUtility.getPartitionOffset(mTopicName, aPartitions);
            final int                jettyPort         = CommonUtility.getInteger("1075");

            for (final TopicPartition partition : aPartitions)
            {
                final int                 partNo      = partition.partition();
                final long                curOffset   = mConsumer.position(partition);
                final Long                redisOffset = lPartitionOffsets.get(partNo);
                final KafkaParititionInfo kpi         = new KafkaParititionInfo(mComponent, aPartitionEvent, jettyPort, mIsStartup, mTopicName, partNo, curOffset,
                        ((redisOffset == null) ? -1 : redisOffset));
                PartitionInfoCollection.getInstance().addKafkaPartition(kpi);
            }
        }
        mIsStartup = false;
    }

}