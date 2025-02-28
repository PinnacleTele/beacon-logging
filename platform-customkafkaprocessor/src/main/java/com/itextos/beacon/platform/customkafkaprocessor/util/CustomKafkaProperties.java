package com.itextos.beacon.platform.customkafkaprocessor.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class CustomKafkaProperties
{

    private static final String PROP_FILENAME                              = "custom.kafka.consumer.properties.filename";
    private static final String PROP_KEY_POLL_INTERVAL                     = "poll.interval";
    private static final String PROP_KEY_KAFKA_CONSUMER_PROPERTIES         = "kafka.consumer.properties";
    private static final String PROP_KEY_ASYNC_UNNCOMMIT_IDLE_TIME_MILLIS  = "async.uncommit.max.idle.time.in.millis";
    private static final String PROP_KEY_ASYNC_UNNCOMMIT_IDLE_RECORD_COUNT = "async.uncommit.max.record.count";
    private static final String PROP_KEY_KAFKA_TOPIC_NAMES                 = "kafka.topic.names.consumer.count";
    private static final String PROP_KEY_PROCESS_THREAD_COUNT              = "process.threads.count";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final CustomKafkaProperties INSTANCE = new CustomKafkaProperties();

    }

    public static CustomKafkaProperties getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Properties mKafkaConsumerProperties;
    private final Properties mCustomProperties;

    private CustomKafkaProperties()
    {
        mCustomProperties        = PropertyLoader.getInstance().getPropertiesByFileName(System.getProperty(PROP_FILENAME));
        mKafkaConsumerProperties = PropertyLoader.getInstance().getPropertiesByFileName(mCustomProperties.getProperty(PROP_KEY_KAFKA_CONSUMER_PROPERTIES));
    }

    public Properties getConsumerProperties()
    {
        return mKafkaConsumerProperties;
    }

    public long getConsumerPollInterval()
    {
        return CommonUtility.getLong(mCustomProperties.getProperty(PROP_KEY_POLL_INTERVAL, "10000"));
    }

    public long getConsumerMaxUncommitIdleTime()
    {
        return CommonUtility.getLong(mCustomProperties.getProperty(PROP_KEY_ASYNC_UNNCOMMIT_IDLE_TIME_MILLIS, "1000"));
    }

    public long getConsumerMaxUncommitCount()
    {
        return CommonUtility.getLong(mCustomProperties.getProperty(PROP_KEY_ASYNC_UNNCOMMIT_IDLE_RECORD_COUNT, "1000"));
    }

    public int getProcessThreadsCount()
    {
        return CommonUtility.getInteger(mCustomProperties.getProperty(PROP_KEY_PROCESS_THREAD_COUNT), 1);
    }

    public Map<String, Integer> getTopicList()
    {
        final String               topicNameList = mCustomProperties.getProperty(PROP_KEY_KAFKA_TOPIC_NAMES, "");
        final Map<String, Integer> returnValue   = new HashMap<>();

        try
        {
            final String[] allTopicNames = topicNameList.split(",");

            if ((allTopicNames != null) && (allTopicNames.length > 0))
                for (final String s : allTopicNames)
                {
                    final String[] sd            = s.split(":");
                    int            consumerCount = CommonUtility.getInteger(sd[1]);
                    consumerCount = consumerCount <= 0 ? 1 : consumerCount;
                    returnValue.put(sd[0], consumerCount);
                }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return returnValue;
    }

    public static String getServerIP()
    {

        try
        {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (final UnknownHostException e)
        {
            e.printStackTrace();
        }
        return "<<UnknownHoust>>";
    }

}