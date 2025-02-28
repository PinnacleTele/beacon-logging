package com.itextos.beacon.commonlib.dnddataloader.redis;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.dnddataloader.common.DndInfo;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class DndRedisCountChecker
{

    private static final Log log = LogFactory.getLog(DndRedisCountChecker.class);

    public static void checkCountAndPrint()
    {
        final long                                    startTime    = System.currentTimeMillis();

        final TreeMap<Integer, Long>                  serverCounts = new TreeMap<>();
        final TreeMap<Integer, TreeMap<String, Long>> detailCounts = new TreeMap<>();

        final int                                     maxCount     = RedisOperations.getDndMasterRedisCount();

        Set<String>                                   allKeySet    = null;

        long                                          curCount     = 0L;
        TreeMap<String, Long>                         curKeyValue  = null;

        for (int index = 1; index <= maxCount; index++)
        {
            long serverCount = 0L;

            curKeyValue = new TreeMap<>();
            detailCounts.put(index, curKeyValue);

            try (
                    final Jedis connection = RedisOperations.getDndMasterRedis(index);
                    final Pipeline pipelined = connection.pipelined();)
            {
                allKeySet = connection.keys(DndInfo.KEY_PREFIX + "*");
                final Map<String, Response<Long>> responses = new HashMap<>();

                for (final String curKey : allKeySet)
                    responses.put(curKey, pipelined.hlen(curKey));
                pipelined.sync();

                for (final Entry<String, Response<Long>> entry : responses.entrySet())
                {
                    curCount = entry.getValue().get();
                    curKeyValue.put(entry.getKey(), curCount);
                    serverCount += curCount;
                }
                serverCounts.put(index, serverCount);
            }
        }

        final long endTime = System.currentTimeMillis();

        printInfo(maxCount, startTime, endTime, serverCounts, detailCounts);

        log.fatal("Count and Print Process Completed");
    }

    private static void printInfo(
            int maxServerCount,
            long startTime,
            long endTime,
            TreeMap<Integer, Long> serverCounts,
            TreeMap<Integer, TreeMap<String, Long>> detailCounts)
    {
        log.fatal("Counter Process Started at  : " + new Date(startTime));
        log.fatal("Counter Process Ended at    : " + new Date(endTime));
        log.fatal("Time taken                  : " + ((endTime - startTime) / (1000.0)));
        log.fatal("********************************************");
        log.fatal("Number of Redis Instances   : " + maxServerCount);
        log.fatal("Server Level Summary Counts :");
        log.fatal("****************************");

        Long count        = null;
        long totalEntries = 0;

        for (final Entry<Integer, Long> entry : serverCounts.entrySet())
        {
            count         = entry.getValue();
            totalEntries += count;
            log.fatal("Redis Server        : " + entry.getKey() + "\t" + count);
        }

        log.fatal("Redis Total Entries : \t" + totalEntries);
        log.fatal("********************************************");

        if (log.isInfoEnabled())
        {
            log.info("Server Level Detail Counts :");
            log.info("****************************");
        }

        for (final Entry<Integer, TreeMap<String, Long>> entry : detailCounts.entrySet())
        {
            final TreeMap<String, Long> detailsMap = entry.getValue();

            if (log.isInfoEnabled())
            {
                log.info("Details for Redis Server  : " + entry.getKey());
                log.info("*******************************");
            }

            for (final Entry<String, Long> entry1 : detailsMap.entrySet())
                log.info(entry1.getKey() + "\t" + entry1.getValue());

            if (log.isInfoEnabled())
                log.info("*******************************");
        }
        if (log.isInfoEnabled())
            log.info("*******************************");
    }

}