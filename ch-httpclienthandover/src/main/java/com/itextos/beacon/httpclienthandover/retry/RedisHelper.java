package com.itextos.beacon.httpclienthandover.retry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class RedisHelper
{

    private static final Log    log                = LogFactory.getLog(RedisHelper.class);

    public static final String  TO_PROCESSING      = "InProcess";
    private static final String GLOBAL_KEY_PATTERN = "*";
    public static final long    MAX_COUNT          = 500;
    public static final String  PARENT_KEY         = "CH_HTTP_RETRY";

    private RedisHelper()
    {}

    private static Jedis getRedisConnection()
    {
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.HTTP_DLR, 1);
    }

    public static void deleteInProcessMessage(
            List<BaseMessage> aClonedBaseMessage,
            String aCustId)
    {

        try (
                final Jedis connection = getRedisConnection();
                final Pipeline pipeline = connection.pipelined();)
        {

            for (final BaseMessage baseMessage : aClonedBaseMessage)
            {
                final String metaData = RedisPusher.getInstance().getMetaData(baseMessage);
                final String key      = PARENT_KEY + ":" + (!StringUtils.isEmpty(aCustId) ? TO_PROCESSING + ":" + aCustId : TO_PROCESSING);

                if (log.isDebugEnabled())
                    log.debug("Delete InProcess Message | MetaData: '" + metaData + "' | Key: '" + key + "'");
                pipeline.lrem(key, 0, metaData);
            }
            pipeline.sync();
        }
    }

    private static List<String> getDataFromRedis(
            String time,
            String custId)
    {

        try (
                final Jedis connection = getRedisConnection();
                final Pipeline pipeline = connection.pipelined();)
        {
            final List<Response<String>> unProcessedData = new ArrayList<>();

            final String                 fromKey         = PARENT_KEY + ":" + (!StringUtils.isEmpty(custId) ? time + ":" + custId : time);
            final Long                   count           = connection.llen(fromKey);

            if (count > 0)
            {
                final String toKey = PARENT_KEY + ":" + (!StringUtils.isEmpty(custId) ? TO_PROCESSING + ":" + custId : TO_PROCESSING);

                if (log.isDebugEnabled())
                    log.debug("From key: '" + fromKey + "' | To Key: '" + toKey + "");

                final long fetchSize = (count > MAX_COUNT) ? MAX_COUNT : count;

                for (int i = 1; i <= fetchSize; i++)
                {
                    final Response<String> data = pipeline.rpoplpush(fromKey, toKey);
                    unProcessedData.add(data);
                }

                pipeline.sync();
            }
            return unProcessedData.stream().map(data -> data.get()).collect(Collectors.toList());
        }
    }

    public static List<String> getInprocessDataFromRedis(
            String key,
            long start,
            long end)
    {

        try (
                final Jedis connection = getRedisConnection())
        {
            List<String> data = null;

            data = connection.lrange(key, start, end);

            return data;
        }
    }

    public static List<String> getAllKeys()
    {

        try (
                final Jedis connection = getRedisConnection())
        {
            final Set<String>  keySet   = connection.keys(PARENT_KEY + ":" + GLOBAL_KEY_PATTERN);
            final List<String> sortList = new ArrayList<>(keySet);
            Collections.sort(sortList);
            return sortList;
        }
    }

    public static List<String> getAllKeys(
            String custId)
    {

        try (
                final Jedis connection = getRedisConnection())
        {
            final Set<String>  keySet   = connection.keys(PARENT_KEY + ":" + GLOBAL_KEY_PATTERN + ":" + custId);
            final List<String> sortList = new ArrayList<>(keySet);
            Collections.sort(sortList);
            return sortList;
        }
    }

    public static List<String> getPastData(
            String time,
            String custId)
    {
        return getDataFromRedis(time, custId);
    }

    public static List<String> getUnprocessedData(
            String aTime)
    {
        return getDataFromRedis(aTime, null);
    }

    public static List<String> getUnprocessedData(
            String aCustID,
            String aTime)
    {
        return getDataFromRedis(aTime, aCustID);
    }

}