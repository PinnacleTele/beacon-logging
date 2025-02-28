package com.itextos.beacon.platform.promokannelrediscleaner.process;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class RemovePromoKeys
{

    private static String[] fields =
    { "service", "destination", "url", "smsc", "mask", "source", "boxc", "ts", "status" };

    public static void main(
            String[] args)
    {

        try (
                Jedis jedis = getConnection();)
        {
            jedis.auth("itextos@202110");
            jedis.select(1);

            final Set<String> lKeys = jedis.keys("dlr:ATPOAA-1:*");
            System.out.println(lKeys != null ? lKeys.size() : "NULL");

            int                               count      = 0;
            final Map<String, Response<Long>> delResults = new LinkedHashMap<>();
            final List<String>                entries    = new ArrayList<>();

            for (final String key : lKeys)
            {
                count++;
                entries.add(key);

                if ((count % 1000) == 0)
                {
                    System.out.println(count);
                    delResults.putAll(getData(jedis, entries));
                    entries.clear();
                }
            }

            if ((count % 1000) != 0)
            {
                delResults.putAll(getData(jedis, entries));
                entries.clear();
            }

            count = 0;
            for (final Entry<String, Response<Long>> entry : delResults.entrySet())
                System.out.println((++count) + " " + entry.getKey() + " : " + entry.getValue().get());
        }
    }

    private static Map<String, Response<Long>> getData(
            Jedis aJedis,
            List<String> aEntries)
    {
        final Map<String, Response<Long>> delResults = new LinkedHashMap<>();

        try (
                Pipeline pipeline = aJedis.pipelined())
        {

            for (final String key : aEntries)
            {
                final Response<Long> lHdel = pipeline.hdel(key, fields);
                delResults.put(key, lHdel);
                // final Response<Long> lHlen = pipeline.hlen(key);
                // delResults.put(key, lHlen);
            }
            pipeline.sync();
        }
        return delResults;
    }

    private static Jedis getConnection()
    {
        return new Jedis("192.168.1.183", 7001, 30000, 30000);
    }

}
