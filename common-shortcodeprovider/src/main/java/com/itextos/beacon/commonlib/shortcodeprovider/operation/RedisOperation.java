package com.itextos.beacon.commonlib.shortcodeprovider.operation;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ShortcodeLength;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class RedisOperation
{

    private static final Log    log                   = LogFactory.getLog(RedisOperation.class);

    private static final String SHORTCODE_REDIS_KEY_5 = "shortcode:list:len_" + ShortcodeLength.LENGTH_5.getLength();
    private static final String SHORTCODE_REDIS_KEY_6 = "shortcode:list:len_" + ShortcodeLength.LENGTH_6.getLength();

    private RedisOperation()
    {}

    public static long checkForLength5()
    {
        return checkForLength(SHORTCODE_REDIS_KEY_5);
    }

    public static long checkForLength6()
    {
        return checkForLength(SHORTCODE_REDIS_KEY_6);
    }

    public static String getNextCodeFromRedis(
            ShortcodeLength aShortcodeLength)
    {
        if (ShortcodeLength.LENGTH_5 == aShortcodeLength)
            return getShortcodeLength(SHORTCODE_REDIS_KEY_5);
        return getShortcodeLength(SHORTCODE_REDIS_KEY_6);
    }

    private static String getShortcodeLength(
            String aShortcodeRedisKey)
    {

        try (
                Jedis jedis = getShortCodeJedis();)
        {
            return jedis.spop(aShortcodeRedisKey);
        }
        catch (final Exception e)
        {
            log.debug("Excecption while getting the shortcode from Redis.", e);
        }
        return null;
    }

    public static long loadDataIntoRedis(
            ShortcodeLength aShortcodeType,
            String aFileName)
            throws ItextosException
    {
        final String fullFileName = ShortCodeProperties.getInstance().getShortCodeFilePath(aShortcodeType) + aFileName;

        final long   startTime    = System.currentTimeMillis();

        if (log.isDebugEnabled())
            log.debug("Loading file data into redis for the lenth '" + aShortcodeType + "' Filename '" + fullFileName + "'");

        final String key = aShortcodeType == ShortcodeLength.LENGTH_5 ? SHORTCODE_REDIS_KEY_5 : SHORTCODE_REDIS_KEY_6;

        try (
                final BufferedReader lReader = getReader(fullFileName);
                final Jedis jedis = getShortCodeJedis();
                final Pipeline pipe = jedis.pipelined();)
        {
            long   count = 0;
            String s     = null;

            while ((s = lReader.readLine()) != null)
            {
                pipe.sadd(key, s);
                count++;

                if ((count % 1000) == 0)
                    pipe.sync();
            }

            if ((count % 1000) != 0)
                pipe.sync();

            final long endTime = System.currentTimeMillis();

            if (log.isDebugEnabled())
                log.debug("Total number of records updated in Redis from file '" + fullFileName + "' is '" + count + "' current count '" + jedis.scard(key) + "' Time Taken : " + (endTime - startTime)
                        + " milliseconds. ");

            return count;
        }
        catch (final Exception e)
        {
            final String s = "Exception while loading the data into Redis Length '" + aShortcodeType + "' Filename '" + fullFileName + "'";
            log.error(s, e);
            throw new ItextosException(s, e);
        }
    }

    private static BufferedReader getReader(
            String aNextFile)
            throws IOException
    {
        final File f = new File(aNextFile);

        if (log.isDebugEnabled())
            log.debug("Creating Buffered Reader for file '" + aNextFile + "'");

        final InputStream         fileIs     = new FileInputStream(new File(aNextFile));
        final BufferedInputStream bufferedIs = new BufferedInputStream(fileIs, 65535);

        if (f.getName().endsWith(".txt") || f.getName().endsWith(".log"))
            return new BufferedReader(new InputStreamReader(bufferedIs));
        return new BufferedReader(new InputStreamReader(new GZIPInputStream(bufferedIs)));
    }

    private static Jedis getShortCodeJedis()
    {
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.SHORTCODE_PROVIDER, 1);
    }

    private static long checkForLength(
            String aJedisKey)
    {

        try (
                Jedis aJedis = getShortCodeJedis())
        {
            return aJedis.scard(aJedisKey);
        }
    }

}