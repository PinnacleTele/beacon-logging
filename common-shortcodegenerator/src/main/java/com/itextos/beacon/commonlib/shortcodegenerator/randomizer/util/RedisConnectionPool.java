package com.itextos.beacon.commonlib.shortcodegenerator.randomizer.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class RedisConnectionPool
{

    private static final Logger log = LogManager.getLogger(RedisConnectionPool.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final RedisConnectionPool INSTANCE = new RedisConnectionPool();

    }

    public static RedisConnectionPool getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final List<String>              mAllIds     = new ArrayList<>();
    private final Map<String, JedisPool>    mJedisPools = new HashMap<>();
    private final Map<String, List<String>> mKeyNames   = new HashMap<>();
    private final Random                    mRandom     = new Random();

    private RedisConnectionPool()
    {
        createConnectionPool();
    }

    private void createConnectionPool()
    {
        if (log.isDebugEnabled())
            log.debug("Creating Redis Pools");

        List<Map<String, Object>> data   = null;
        final ObjectMapper        mapper = new ObjectMapper();

        try
        {
            final Stream<String> stream = Files.lines(Paths.get(PropertyReader.getInstance().getRedisConfigPath()));
            final String         json   = stream.collect(Collectors.joining());
            // convert JSON string to Map
            data = mapper.readValue(json, new TypeReference<List<Map<String, Object>>>()
            {});

            final JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(150);
            config.setMaxIdle(10);

            config.setMinIdle(8);
            config.setMaxWaitMillis(10000);

            for (final Map<String, Object> innerMap : data)
            {
                log.debug("Redis Configuration '" + innerMap + "'");

                final String host = (String) innerMap.get("host");
                final int    port = (int) innerMap.get("port");
                final String pwd  = (String) innerMap.get("auth");

                for (int dbID = 1; dbID <= PropertyReader.getInstance().getMaxDatabaseInRedis(); dbID++)
                {
                    final String temp = host + "~" + port + "~" + dbID;

                    log.debug("Creating jedispool for " + temp);

                    final JedisPool jedisPool = new JedisPool(config, host, port, 30 * 1000, 30 * 1000, pwd, dbID, temp);
                    mJedisPools.put(temp, jedisPool);

                    mAllIds.add(temp);
                    final List<String> list = new ArrayList<>();

                    for (int j = 0; j < 15; j++)
                        list.add(temp + "~" + j);

                    mKeyNames.put(temp, list);
                }
            }
        }
        catch (final IOException e)
        {
            log.error("Exception while creating the Redis Connection Pool. Exiting the application", e);
            System.exit(-1);
        }
    }

    public String getNextKey()
    {
        final int lNextInt = mRandom.nextInt(mAllIds.size());
        return mAllIds.get(lNextInt);
    }

    public Jedis getJedis(
            String aKey)
    {
        int retry = 0;
        while (retry < 3)
            try
            {
                final JedisPool pool = mJedisPools.get(aKey);

                if (pool.getNumWaiters() > 10)
                    log.debug("Key : " + aKey + " Maximum : " + 150 + " Active :" + pool.getNumActive() + " Idle " + pool.getNumIdle() + " Waiters " + pool.getNumWaiters());
                return pool.getResource();
            }
            catch (final Exception e)
            {
                log.error("Problem in getting the redis connection. Will try after 10 seconds", e);
                ++retry;
                if (retry < 3)
                    try
                    {
                        Thread.sleep(10 * 1000L);
                    }
                    catch (final InterruptedException e1)
                    {
                        e1.printStackTrace();
                    }
                else
                    throw e;
            }
        return null;
    }

    public List<String> getKeys(
            String aKey)
    {
        return mKeyNames.get(aKey);
    }

    public String getNextKeyName(
            List<String> aAllKeys)
    {
        final int lNextInt = mRandom.nextInt(aAllKeys.size());
        return aAllKeys.get(lNextInt);
    }

    public void pushToRedis(
            List<String> aShortCodes)
    {
        final Map<String, List<String>> map = new HashMap<>();

        aShortCodes.stream().forEach(value -> {
            final String       key  = getNextKey();
            final List<String> list = map.computeIfAbsent(key, k -> new ArrayList<>());
            list.add(value);
        });

        // log.debug(" Map Key : " + map.keySet());

        for (final Entry<String, List<String>> entry : map.entrySet())
        {
            final String                    redisDbkey    = entry.getKey();
            final List<String>              toPushToRedis = entry.getValue();

            final List<String>              outerKeysList = getKeys(redisDbkey);
            final Map<String, List<String>> values        = new HashMap<>();

            for (final String toInsert : toPushToRedis)
            {
                final String       lNextKeyName = getNextKeyName(outerKeysList);
                final List<String> valueList    = values.computeIfAbsent(lNextKeyName, k -> new ArrayList<>());
                valueList.add(toInsert);
            }

            try (
                    Jedis jedis = getJedis(entry.getKey());
                    Pipeline pipe = jedis.pipelined();)
            {
                // int totalCount = 0;

                for (final Entry<String, List<String>> entry1 : values.entrySet())
                {
                    int          count = 0;
                    final String key   = entry1.getKey();

                    for (final String s : entry1.getValue())
                    {
                        count++;
                        pipe.sadd(key, s);

                        if ((count % 2000) == 0)
                            pipe.sync();
                    }

                    if ((count % 5000) > 0)
                        pipe.sync();
                    // totalCount += count;
                }
                // log.debug("Total counts " + entry.getKey() + " " + totalCount);
            }
        }
    }

    public List<String> getDataFromRedis(
            int aMaxCount)
    {
        final int          minCountForRedisDb = aMaxCount / mAllIds.size();
        final List<String> returnValue        = new ArrayList<>();
        int                minCountForKey     = -1;

        for (final String s : mAllIds)
        {
            final List<String> allKeyNames = mKeyNames.get(s);
            minCountForKey = (minCountForRedisDb / allKeyNames.size()) + 1;
            // log.debug(aMaxCount + "\t" + minCountForRedisDb + "\t" + minCountForKey);

            try (
                    Jedis jedis = getJedis(s);
                    Pipeline pipe = jedis.pipelined();)
            {
                final Map<String, Response<Set<String>>> responses = new HashMap<>();

                for (final String keyName : allKeyNames)
                {
                    final Response<Set<String>> lSpop = pipe.spop(keyName, minCountForKey);
                    responses.put(keyName, lSpop);
                }

                pipe.sync();

                for (final Response<Set<String>> response : responses.values())
                    returnValue.addAll(response.get());
            }
            catch (final Exception e)
            {
                log.error("Exception while getting the Jedis Connection. Returning data " + returnValue.size() + " instead of " + aMaxCount, e);
            }
        }
        // log.debug("requested " + aMaxCount + " got " + returnValue.size() + " per db
        // " + minCountForRedisDb + " per key " + minCountForKey);
        return returnValue;
    }

    public void flushAll()
    {
        log.debug("Clearing the existing data in Redis.");
        final List<String> alreadyFlushed = new ArrayList<>();

        for (final Entry<String, JedisPool> entry : mJedisPools.entrySet())
        {
            final String key    = entry.getKey();
            final String ipPort = key.substring(0, key.lastIndexOf("~"));

            if (alreadyFlushed.contains(ipPort))
            {
                log.fatal("Already flushed for the IP and PORT " + ipPort);
                continue;
            }

            log.debug("Flushing data '" + key + "'");
            alreadyFlushed.add(ipPort);

            try (
                    Jedis jedis = entry.getValue().getResource())
            {
                jedis.flushAll();
            }
        }
        log.debug("Completed clearing the existing data in Redis.");
    }

    public List<String> getAllKeys()
    {
        return mAllIds;
    }

}