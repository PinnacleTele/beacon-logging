package com.itextos.beacon.commonlib.kafkaservice.common;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;

public class KafkaCustomProperties
        implements
        ITimedProcess
{

    private static final String KAFKA_MAX_FLUSH_BATCH_SIZE              = "kafka.producer.flush.max.record.count";
    private static final String KAFKA_MAX_FLUSH_TIME_INTERVAL           = "kafka.producer.flush.max.wait.timeinterval.in.millis";
    private static final String KAFKA_CONSUMER_MAX_INMEMORY_SIZE        = "kafka.consumer.max.inmemory.size";
    private static final String KAFKA_CONSUMER_POLL_INTERVAL            = "kafka.consumer.poll.interval";
    private static final String KAFKA_CONSUMER_MAX_UNCOMMITTED_RECORDS  = "kafka.consumer.async.uncommit.max.record.count";
    private static final String KAFKA_CONSUMER_MAX_IDLE_TIME_FOR_COMMIT = "kafka.consumer.async.uncommit.max.idle.time.in.millis";
    private static final String KAFKA_CONSUMER_FINAL_SLEEP_SECONDS      = "kafka.consumer.final.sleep.seconds";
    private static final String KAFKA_PARTITION_DB_INSERT_REQ           = "kafka.partition.db.insert.req";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final KafkaCustomProperties INSTANCE = new KafkaCustomProperties();

    }

    public static KafkaCustomProperties getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private boolean                       canContinue                = true;
    private final TimedProcessor          mTimedProcessor;
    private int                           mMaxFlushSize              = 1000;
    private int                           mMaxFlushInterval          = 1000;
    private int                           mConsumerMaxInmemorySize   = 3000;
    private int                           tempPollInterval           = 100;
    private int                           maxUncommitRecords         = 1000;
    private int                           maxUncommitTimeInMilis     = 1000;
    private int                           consumerFinalSleepSeconds  = 2;
    private boolean                       isDbInsertReqForPartitions = false;

    private final PropertiesConfiguration mPropConf;

    private KafkaCustomProperties()
    {
        mPropConf       = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.KAFKA_CUSTOM_PROPERTIES, true);


        mTimedProcessor = new TimedProcessor("KafkaCustomPropertiesReload", this, TimerIntervalConstant.KAFKA_CUSTOM_PROPERTIES_RELOAD);
   
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "KafkaCustomPropertiesReload");
    }

    public int getProducerMaxFlushCount()
    {
        return mMaxFlushSize;
    }

    public int getProducerMaxFlushTimeInterval()
    {
        return mMaxFlushInterval;
    }

    public int getConsumerPollInterval()
    {
        return tempPollInterval;
    }

    public int getConsumerMaxUncommitCount()
    {
        return maxUncommitRecords;
    }

    public int getConsumerMaxUncommitIdleTime()
    {
        return maxUncommitTimeInMilis;
    }

    public int getConsumerMaxInmemorySize()
    {
        return mConsumerMaxInmemorySize;
    }

    public int getConsumerFinalSleepTime()
    {
        return consumerFinalSleepSeconds;
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        updateProperties();
        return false;
    }

    private void updateProperties()
    {
        mMaxFlushSize              = CommonUtility.getInteger(mPropConf.getString(KAFKA_MAX_FLUSH_BATCH_SIZE), 1000);
        mMaxFlushInterval          = CommonUtility.getInteger(mPropConf.getString(KAFKA_MAX_FLUSH_TIME_INTERVAL), 1000);
        mConsumerMaxInmemorySize   = CommonUtility.getInteger(mPropConf.getString(KAFKA_CONSUMER_MAX_INMEMORY_SIZE), 2000);
        tempPollInterval           = CommonUtility.getInteger(mPropConf.getString(KAFKA_CONSUMER_POLL_INTERVAL), 100);
        maxUncommitRecords         = CommonUtility.getInteger(mPropConf.getString(KAFKA_CONSUMER_MAX_UNCOMMITTED_RECORDS), 1000);
        maxUncommitTimeInMilis     = CommonUtility.getInteger(mPropConf.getString(KAFKA_CONSUMER_MAX_IDLE_TIME_FOR_COMMIT), 1000);
        consumerFinalSleepSeconds  = CommonUtility.getInteger(mPropConf.getString(KAFKA_CONSUMER_FINAL_SLEEP_SECONDS), 2);
        isDbInsertReqForPartitions = CommonUtility.isEnabled(mPropConf.getString(KAFKA_PARTITION_DB_INSERT_REQ, "0"));
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

    public boolean isDbInsertReqForPartitions()
    {
        return isDbInsertReqForPartitions;
    }

}