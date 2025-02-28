package com.itextos.beacon.smpp.concatenate;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;

import redis.clients.jedis.Jedis;

public class RedisMemoryChecker
        extends
        TimerTask
{

    private static final Log log = LogFactory.getLog(RedisMemoryChecker.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final RedisMemoryChecker INSTANCE = new RedisMemoryChecker();

    }

    static RedisMemoryChecker getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<ClusterType, Map<Integer, Long>> mRedisMemInfo       = new EnumMap<>(ClusterType.class);
    private final Map<ClusterType, List<Integer>>      mRedisPoolIndexList = new EnumMap<>(ClusterType.class);

    private RedisMemoryChecker()
    {
        final Timer timer     = new Timer("RedisMemoryDetector-Thread");
        int         iInterval = 1;

        // iInterval is in sec. convert to milli sec
        iInterval = iInterval * 1000;
        timer.schedule(this, iInterval, iInterval);
    }

    public boolean canWrite(
            ClusterType aClusterType,
            int aRedisPoolIndex)
    {
        if (log.isDebugEnabled())
            log.debug(aClusterType + "Calling canWrite ...................");
        boolean canWrite = false;

        try
        {
            final long allowedLimit = SmppProperties.getInstance().getConcatRedisAllowedMemoryBytes();
            if (log.isDebugEnabled())
                log.debug(aClusterType + "AllowedLimit :'" + allowedLimit + "'");

            final long lMemoryInfo = getMemoryInfo(aClusterType, aRedisPoolIndex);

            if (lMemoryInfo != -1)
            {
                canWrite = ((allowedLimit - lMemoryInfo) > 0);

                if (!canWrite)
                    log.error("memory exceeded=" + (allowedLimit - getMemoryInfo(aClusterType, aRedisPoolIndex)),
                            new Throwable("Memory Limit Exceeded for cluster :'" + aClusterType + "', Redis Index : '" + aRedisPoolIndex + "'"));
            }
        }
        catch (final Exception exp)
        {
            log.error("assuming redis allowedLimit=" + SmppProperties.getInstance().getDefaultAllowedLimit() + " bytes due to...", exp);
        }

        if (log.isDebugEnabled())
            log.debug(aClusterType + " Can send concat message to redis :" + canWrite);

        return canWrite;
    }

    private long getMemoryInfo(
            ClusterType aClusterType,
            int aRedisPoolIndex)
    {
        final int                lRedisPoolIndex = aRedisPoolIndex + 1;
        final Map<Integer, Long> memValue        = mRedisMemInfo.computeIfAbsent(aClusterType, k -> new HashMap<>());
        final long               memUsage        = memValue.computeIfAbsent(lRedisPoolIndex, k -> getCurrentUsage(aClusterType, aRedisPoolIndex));

        synchronized (mRedisPoolIndexList)
        {
            final List<Integer> redisIndexList = mRedisPoolIndexList.computeIfAbsent(aClusterType, k -> new ArrayList<>());
            redisIndexList.add(lRedisPoolIndex);
        }

        return memUsage;
    }

    @Override
    public void run()
    {

        synchronized (mRedisPoolIndexList)
        {
            for (final Entry<ClusterType, List<Integer>> entry : mRedisPoolIndexList.entrySet())
                for (final Integer aRedisPoolIndex : entry.getValue())
                    try
                    {
                        final Map<Integer, Long> lMap = mRedisMemInfo.get(entry.getKey());
                        lMap.put(aRedisPoolIndex, getCurrentUsage(entry.getKey(), aRedisPoolIndex));
                    }
                    catch (final Exception e)
                    {
                        log.error("problem refreshing memory usage for redis index " + aRedisPoolIndex, e);
                    }
        }
    }

    private static long getCurrentUsage(
            ClusterType aClusterType,
            int aRedisPoolIndex)
    {
        long usedMemory = -1;

        if (aRedisPoolIndex > 0)
            aRedisPoolIndex = aRedisPoolIndex - 1;

        try (
                Jedis jedis = RedisOperation.getConnection(aClusterType, aRedisPoolIndex);)
        {
            final String   lMemInfo     = jedis.info("memory");
            final String[] allInfo      = lMemInfo.split("\n");
            final String[] usedMemoryKV = allInfo[1].split(":");
            usedMemory = Long.parseLong(usedMemoryKV[1].trim());
        }
        catch (final Exception e)
        {
            log.error("problem refreshing redis memory consumption...", e);
        }

        return usedMemory;
    }

}
