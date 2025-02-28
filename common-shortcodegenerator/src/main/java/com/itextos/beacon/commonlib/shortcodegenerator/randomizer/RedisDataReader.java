package com.itextos.beacon.commonlib.shortcodegenerator.randomizer;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itextos.beacon.commonlib.shortcodegenerator.randomizer.util.RedisConnectionPool;

public class RedisDataReader
        implements
        Runnable
{

    private static final Logger log         = LogManager.getLogger(RedisDataReader.class);
    private final String        mThreadName;
    private boolean             isCompleted = false;

    public RedisDataReader(
            String aThreadName)
    {
        mThreadName = aThreadName;
    }

    @Override
    public void run()
    {
        int emptyCount = 0;

        while (true)
        {
            final List<String> lDataFromRedis = RedisConnectionPool.getInstance().getDataFromRedis(200000);

            if (!lDataFromRedis.isEmpty())
            {
                InformationHolder.getInstance().addWriteData(lDataFromRedis);
                emptyCount = 0;
            }
            else
            {
                emptyCount++;

                if (emptyCount == 11)
                {
                    log.fatal("No data for last 10 iterations. Stopping thread '" + mThreadName + "'");
                    break;
                }

                sleepForWhile(1000);
            }
        }
        isCompleted = true;
    }

    public boolean isCompleted()
    {
        return isCompleted;
    }

    public String getThreadName()
    {
        return mThreadName;
    }

    private static void sleepForWhile(
            long aTimeToSleepinMillies)
    {

        try
        {
            Thread.sleep(aTimeToSleepinMillies);
        }
        catch (final InterruptedException e)
        {
            log.error("InterruptedException", e);
        }
    }

}