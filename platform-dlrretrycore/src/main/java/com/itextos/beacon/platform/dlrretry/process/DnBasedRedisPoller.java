package com.itextos.beacon.platform.dlrretry.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler2;
import com.itextos.beacon.platform.dlrretry.util.DlrWaitRetryUtil;
import com.itextos.beacon.platform.dlrretry.util.DnBasedRetryRedisProcessor;

import redis.clients.jedis.Jedis;

public class DnBasedRedisPoller
        implements
        Runnable
{

    private static final Log log            = LogFactory.getLog(DnBasedRedisPoller.class);

    private final int        mRedisIndex;
    private final String     mThreadName;
    private long             mLastValidated = System.currentTimeMillis();

    public DnBasedRedisPoller(
            int aRedisIndex,
            String aThredName)
    {
        mRedisIndex = aRedisIndex;
        mThreadName = aThredName;
        workForPastSeconds();
    }

    @Override
    public void run()
    {

        while (true)
        {
            if (log.isDebugEnabled())
                log.debug(mThreadName + "-Process DN Retry ....");

            try
            {
                processRecords();
            }
            catch (final Exception e)
            {
                log.error(mThreadName + "-Exception .............", e);
            }

            try
            {
                Thread.sleep(1000);
            }
            catch (final InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void processRecords()
    {
        final long   currTime       = (System.currentTimeMillis() / 1000) * 1000; // This will remove the milliseconds
        final String currentSeconds = DateTimeUtility.getFormattedDateTime(new Date(currTime), DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM_SS);
        final String redisKey       = CommonUtility.combine(':', DnBasedRetryRedisProcessor.REDIS_KEY, currentSeconds);

        try (
                Jedis jedis = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.DLR_WAIT_RETRY, mRedisIndex);)
        {
            processRecords(jedis, redisKey);
        }
        catch (final Exception e)
        {
            log.error(mThreadName + "-Exception occer while processing the dlr retry ...", e);
        }
    }

    private void workForPastSeconds()
    {

        try (
                Jedis jedis = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.DLR_WAIT_RETRY, mRedisIndex);)
        {
            if (log.isDebugEnabled())
                log.debug(mThreadName + "-Jedis Connection : " + jedis);

            if (jedis != null)
            {
                final Set<String>  lKeys   = jedis.keys(DnBasedRetryRedisProcessor.REDIS_KEY + "*");
                final List<String> allKeys = new ArrayList<>(lKeys);

                Collections.sort(allKeys);

                final List<String> toProcess = new ArrayList<>();

                for (final String tempKey : allKeys)
                {
                    final long   currTime       = (System.currentTimeMillis() / 1000) * 1000; // This will remove the milliseconds
                    final String currentSeconds = DateTimeUtility.getFormattedDateTime(new Date(currTime), DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM_SS);
                    final int    currSec        = CommonUtility.getInteger(currentSeconds);

                    final String timeOnly       = tempKey.substring(DnBasedRetryRedisProcessor.REDIS_KEY.length() + 1);

                    if (CommonUtility.getInteger(timeOnly) > currSec)
                    {
                        mLastValidated = currTime;
                        break;
                    }
                    toProcess.add(tempKey);
                }

                if (!toProcess.isEmpty())
                    processRecords(toProcess);
            }
        }

        final long millisToNextSecond = timeBreak();
        if (millisToNextSecond > 0)
            CommonUtility.sleepForAWhile(millisToNextSecond);
    }

    private long timeBreak()
    {
        return (mLastValidated + 1000) - System.currentTimeMillis();
    }

    private void processRecords(
            List<String> aToProcess)
    {
        final Thread lThread = new Thread(new Runnable()
        {

            @Override
            public void run()
            {

                try (
                        Jedis jedis = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.DLR_WAIT_RETRY, mRedisIndex);)
                {
                    for (final String redisKey : aToProcess)
                        processRecords(jedis, redisKey);
                }
                catch (final Exception e)
                {}
            }

        }, "");
        
        ExecutorSheduler2.getInstance().addTask(lThread, ClusterType.COMMON+" : "+Component.DLR_WAIT_RETRY+" : "+mRedisIndex);
    }

    private static void processRecords(
            Jedis jedis,
            String aRedisKey)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("Process Records for redis key : " + aRedisKey);

        String dnDataJson = jedis.rpop(aRedisKey);

        if (log.isDebugEnabled())
            log.debug("Dlr Wait record from redis : " + dnDataJson);

        while (dnDataJson != null)
        {
            final DeliveryObject deliveryObject = new DeliveryObject(dnDataJson);
            process(deliveryObject);

            dnDataJson = jedis.rpop(aRedisKey);
        }
    }

    private static void process(
            DeliveryObject aDeliveryObject)
    {

        try
        {
            DlrWaitRetryUtil.process(aDeliveryObject);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

}