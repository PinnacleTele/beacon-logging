package com.itextos.beacon.commonlib.shortcodegenerator.randomizer;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itextos.beacon.commonlib.shortcodegenerator.randomizer.util.RedisConnectionPool;

public class InmemToRedisPusher
        implements
        Runnable
{

    private static final Logger       log = LogManager.getLogger(InmemToRedisPusher.class);
    private final String              mThreadName;
    private final InMemorySizePrinter lInMemorySizePrinter;

    public InmemToRedisPusher(
            String aThreadName,
            InMemorySizePrinter aInMemorySizePrinter)
    {
        mThreadName          = aThreadName;
        lInMemorySizePrinter = aInMemorySizePrinter;
    }

    @Override
    public void run()
    {
        int count = 0;
        while (true)
            if (lInMemorySizePrinter.canAddToRedis())
            {
                final boolean lReadAndPushToRedis = readAndPushToRedis();

                if (!lReadAndPushToRedis)
                {

                    if (!InformationHolder.getInstance().isAllFilesRead())
                        log.fatal("Not all files has been read. Total Files " + InformationHolder.getInstance().getTotalFileCount() + " Files read "
                                + InformationHolder.getInstance().getReadFileCount());
                    else
                    {
                        count++;

                        if (count > 11)
                        {
                            log.fatal("No data in inmemory to push to redis for more than 10 time. Exiting thread '" + mThreadName + "'");

                            break;
                        }
                    }
                    sleepForWhile(1000);
                }
            }
            else
            {
                log.info("Redis is overloaded. Will wait for 10 seconds to get reduced.");
                sleepForWhile(10 * 1000L);
            }
    }

    private static boolean readAndPushToRedis()
    {
        boolean canContinue = true;

        try
        {
            final List<String> lReadData = InformationHolder.getInstance().getReadData(200000);

            if (!lReadData.isEmpty())
                RedisConnectionPool.getInstance().pushToRedis(lReadData);
            // log.debug("Read Data Size " + lReadData.size());
            else
                canContinue = false;
        }
        catch (final Throwable e)
        {
            log.error("Exception while pushing to Redis. Cannot continue.", e);
            log.error("Problem while rading data from inmem to push to redis.", e);
            System.exit(-1);
        }
        return canContinue;
    }

    private static void invokeGc()
    {
        // Utility.printMemoryInfo();

        // Utility.callGC();

        sleepForWhile(1000);
        // Utility.printMemoryInfo();
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