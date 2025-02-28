package com.itextos.beacon.commonlib.kafkaservice.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.TopicPartition;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.errorlog.ErrorLog;
import com.itextos.beacon.smslog.ConsumerRedisLog;
import com.itextos.beacon.smslog.ProducertoRedisLog;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class KafkaUtility
{

    private static final Log    log                   = LogFactory.getLog(KafkaUtility.class);

    private static final int    REDIS_INDEX_FOR_KAFKA = 1;
    private static final char   REDIS_SEPARATOR       = ':';
    private static final String KAFKA_KEY             = "kafka_topic_offset";
    private static final String OFF_SET               = "offset";
    private static final String TIME_STAMP            = "timestamp";
    private static final String REDIS_PRODUCER_KEY    = CommonUtility.combine(REDIS_SEPARATOR, "kafka", "producerdata");
    private static final String REDIS_CONSUMER_KEY    = CommonUtility.combine(REDIS_SEPARATOR, "kafka", "consumerdata");

    private KafkaUtility()
    {}

    public static void updateRedis(
            String aTopicName,
            Map<Integer, Long> aPartitionOffset,
            boolean aPrintOffset,
            String aEvent)
    {
        if (aPrintOffset)
            log.fatal(aEvent + " :::: Updating the Topic Partition and Offset information for topic '" + aTopicName + "'. Total Topics and Partitions to update " + aPartitionOffset.size());

        if (aPartitionOffset.isEmpty())
        {
            if (aPrintOffset)
                log.fatal(aEvent + " :::: No Redis Updation Required.");
            return;
        }

        if (log.isDebugEnabled())
            log.debug("Update the processed records offset value and time in Redis. " + aPartitionOffset.size() + " List " + aPartitionOffset);

        try (
                Jedis jedis = getKafkaRedis();
                Pipeline pipe = jedis.pipelined();)
        {

            for (final Entry<Integer, Long> entry : aPartitionOffset.entrySet())
            {
                if (log.isDebugEnabled())
                    log.debug("Topic '" + aTopicName + "' Partition '" + entry.getKey() + "' Offset '" + entry.getValue() + "'");

                if (aPrintOffset)
                    log.fatal(aEvent + " :::: Topic '" + aTopicName + "' Partition '" + entry.getKey() + "' offset '" + entry.getValue() + "'");

                final Map<String, String> values = new HashMap<>();
                values.put(OFF_SET, Long.toString(entry.getValue()));
                values.put(TIME_STAMP, DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));

                pipe.hmset(CommonUtility.combine(REDIS_SEPARATOR, KAFKA_KEY, aTopicName, Integer.toString(entry.getKey())), values);
            }
            pipe.sync();

            if (aPrintOffset)
                log.fatal(aEvent + " :::: Redis Updation Completed.");
        }
        catch (final Exception e)
        {
            if (aPrintOffset)
                log.error("Exception while updating the commmit details in Redis", e);
            throw e;
        }
    }

    public static Map<Integer, Long> getPartitionOffset(
            String aTopicName,
            Collection<TopicPartition> aPartitions)
    {
        if (log.isDebugEnabled())
            log.debug("Getting Partition offset values for Topic '" + aTopicName + "'");

        final Map<Integer, Long> partitionOffset = new HashMap<>();

        try (
                Jedis jedis = getKafkaRedis();
                Pipeline pipe = jedis.pipelined();)
        {
            final Map<Integer, Response<String>> responseMap = new HashMap<>();

            for (final TopicPartition tp : aPartitions)
            {
                final int              partition    = tp.partition();
                final String           key          = CommonUtility.combine(REDIS_SEPARATOR, KAFKA_KEY, aTopicName, Integer.toString(partition));
                final Response<String> offsetString = pipe.hget(key, OFF_SET);
                responseMap.put(partition, offsetString);
            }

            pipe.sync();

            for (final Entry<Integer, Response<String>> entry : responseMap.entrySet())
            {
                final long offSet = CommonUtility.getLong(entry.getValue().get(), -1L);

                if (log.isInfoEnabled())
                    log.info("Topic '" + aTopicName + "' Partition '" + entry.getKey() + "' offset '" + offSet + "'");

                if (offSet > -1)
                    partitionOffset.put(entry.getKey(), offSet);
                else
                    log.error("Off set is not set for the topic '" + aTopicName + "' and Partition '" + entry.getKey() + "' Response from Redis '" + offSet + "'");
            }
        }
        return partitionOffset;
    }

    private static Jedis getKafkaRedis()
    {
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.KAFKA_SERVICE, REDIS_INDEX_FOR_KAFKA);
    }

    public static void printProperties(
            String aPropertyType,
            Properties aProps)
    {
        if (aProps == null)
            return;

        if (log.isDebugEnabled())
        {
            final Map<Object, Object> map = new TreeMap(aProps);

            for (final Object s : map.keySet())
                log.debug("Kafka Property >> " + aPropertyType + " : '" + s + "' = '" + map.get(s) + "'");
        }
    }

    public static void addToProducerRedis(
            Component aComponent,
            String aTopicName,
            IMessage aMessage)
    {
        String threadName = Thread.currentThread().getName();


        try (
                Jedis jedis = getKafkaRedis())
        {
            final String listKey = CommonUtility.combine(REDIS_SEPARATOR, REDIS_PRODUCER_KEY, aComponent.getKey(), aTopicName);
            jedis.lpush(listKey, aMessage.getJsonString());
            
            ProducertoRedisLog.log(threadName+" : Component Name:" + aComponent + ", Topic ='" + aTopicName + "' Successfully added producer messages in redis count -" + aMessage);

        }
        catch (final Exception e)
        {
            log.error("Ecxception while pushing message to Redis for the Producer Topic Name '" + aTopicName + "' Message '" + aMessage + "'", e);
       
            ErrorLog.log("Ecxception while pushing message to Redis for the Producer Topic Name '" + aTopicName + "' Message '" + aMessage + "'"+ErrorMessage.getStackTraceAsString(e));
        }
    }

    public static void addToProducerRedis(
            Component aComponent,
            String aTopicName,
            List<IMessage> aMessage)
    {
        String threadName = Thread.currentThread().getName();


        try (
                Jedis jedis = getKafkaRedis();
                Pipeline pipe = jedis.pipelined())
        {
            final String listKey = CommonUtility.combine(REDIS_SEPARATOR, REDIS_PRODUCER_KEY, aComponent.getKey(), aTopicName);
            for (final IMessage msg : aMessage)
                pipe.lpush(listKey, msg.getJsonString());
            pipe.sync();

            log.fatal("Component Name:" + aComponent + ", Topic ='" + aTopicName + "' Successfully added producer messages in redis count -" + aMessage.size());
        
            ProducertoRedisLog.log(threadName+" : Component Name:" + aComponent + ", Topic ='" + aTopicName + "' Successfully added producer messages in redis count -" + aMessage.size());
        }
        catch (final Exception e)
        {
            log.error("Ecxception while pushing message to Redis for the Producer Topic Name '" + aTopicName + "' Message '" + aMessage + "'", e);
            ErrorLog.log("Ecxception while pushing message to Redis for the Producer Topic Name '" + aTopicName + "' Message '" + aMessage + "' "+ErrorMessage.getStackTraceAsString(e));
        }
    }

    public static void addToConsumerRedis(
            Component aComponent,
            String aTopicName,
            List<IMessage> aMessage)
    {

        try (
                Jedis jedis = getKafkaRedis();
                Pipeline pipe = jedis.pipelined())
        {
            final String listKey = CommonUtility.combine(REDIS_SEPARATOR, REDIS_CONSUMER_KEY, aComponent.getKey(), aTopicName);
            for (final IMessage msg : aMessage)
                pipe.lpush(listKey, msg.getJsonString());
            pipe.sync();

            log.fatal("Component Name:" + aComponent + ", Topic ='" + aTopicName + "' Successfully added consumer messages in redis count -" + aMessage.size());

            ConsumerRedisLog.log("Component Name:" + aComponent + ", Topic ='" + aTopicName + "' Successfully added consumer messages in redis count -" + aMessage.size());
        }
        catch (final Exception e)
        {
            log.error("Ecxception while pushing message to Redis for the Consumer Topic Name '" + aTopicName + "' Message '" + aMessage + "'", e);
      
            ErrorLog.log("Ecxception while pushing message to Redis for the Consumer Topic Name '" + aTopicName + "' Message '" + aMessage + "' "+ ErrorMessage.getStackTraceAsString(e));
        }
    }

    public static Map<String, List<String>> getFallbackConsumerData(
            Component aComponent,
            String aTopicName)
    {
        final String listKey = CommonUtility.combine(REDIS_SEPARATOR, REDIS_CONSUMER_KEY, aComponent.getKey(), aTopicName);
        return getFallbackMessages(listKey);
    }

    public static Map<String, List<String>> getFallbackProducerData(
            Component aComponent)
    {
        final String listKey = CommonUtility.combine(REDIS_SEPARATOR, REDIS_PRODUCER_KEY, aComponent.getKey());
        return getFallbackMessages(listKey + "*");
    }

    public static Map<String, List<String>> getFallbackMessages(
            String aKeys)
    {
        if (log.isDebugEnabled())
            log.debug("Getting fallback messages for '" + aKeys + "'");

        final Map<String, List<String>> returnValue = new HashMap<>();

        try (
                Jedis jedis = getKafkaRedis();)
        {
            final Set<String> lKeys = jedis.keys(aKeys);

            for (final String s : lKeys)
            {
                if (log.isDebugEnabled())
                    log.debug("Key Name : '" + s + "'");

                final String[] splits = s.split(REDIS_SEPARATOR + "");

                if (splits.length > 3)
                {
                    final String       topicName = splits[3];
                    final List<String> msgList   = returnValue.computeIfAbsent(topicName, k -> new ArrayList<>());
                    getMessageFromRedisList(s, jedis, msgList);

                    if (log.isDebugEnabled())
                        log.debug("Messages count for the key '" + aKeys + "' Topicname '" + topicName + "' Messages Count '" + msgList.size() + "'");
                
                    ConsumerRedisLog.log("Messages count for the key '" + aKeys + "' Topicname '" + topicName + "' Messages Count '" + msgList.size() + "'");
                }
                
                
            }
        }
        catch (final Exception e)
        {
            log.error("Ecxception while getting message from Redis for the Key '" + aKeys + "''", e);
       
            ErrorLog.log("Ecxception while getting message from Redis for the Key '" + aKeys + "'' "+ErrorMessage.getStackTraceAsString(e) );
        
        }
        return returnValue;
    }

    private static void getMessageFromRedisList(
            String aTopicName,
            Jedis aJedis,
            List<String> aMsgList)
    {

        try (
                Pipeline pipe = aJedis.pipelined();)
        {
            Long lLlen = aJedis.llen(aTopicName);

            if (log.isDebugEnabled())
                log.debug("Topic Key '" + aTopicName + " Length : '" + lLlen + "'");

            while (lLlen > 0)
            {
                if (log.isDebugEnabled())
                    log.debug("Topic Key '" + aTopicName + " Length : '" + lLlen + "'");

                final int                    maxIteration = (int) (lLlen > 1000 ? 1000 : lLlen);
                final List<Response<String>> resMsgList   = new ArrayList<>(maxIteration);

                for (int index = 0; index < maxIteration; index++)
                {
                    final Response<String> lRpop = pipe.rpop(aTopicName);
                    resMsgList.add(lRpop);
                }

                pipe.sync();

                for (final Response<String> response : resMsgList)
                {
                    final String temp = response.get();
                    if (temp != null)
                        aMsgList.add(temp);
                }

                lLlen = aJedis.llen(aTopicName);
            }
        }
        catch (final Exception e)
        {
            log.error("Ecxception while getting message from Redis for the Topic '" + aTopicName + "''", e);
        }
    }

    public static String formatTopicName(
            String aTopicName)
    {
        return String.format("%-30s", aTopicName);
    }

}