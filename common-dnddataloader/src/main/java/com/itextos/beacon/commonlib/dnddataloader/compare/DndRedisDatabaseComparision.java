package com.itextos.beacon.commonlib.dnddataloader.compare;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.dnddataloader.common.CountHolder;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler2;

public class DndRedisDatabaseComparision
{

    private static final Log log = LogFactory.getLog(DndRedisDatabaseComparision.class);

    public static void compare()
    {
        final long startTime = System.currentTimeMillis();

        RedisConnectionProvider.getInstance();

        try
        {
            final HashMap<String, Db2RedisWithRange> allThreads = new HashMap<>();
            long                                     start      = 910000000000L;

            while (start < 920000000000L)
            {
                final long              startNumber       = start;
                final long              endNumber         = startNumber + 999999;
                final String            threadName        = "[" + startNumber + "-" + endNumber + "]";

                final Db2RedisWithRange db2RedisWithRange = new Db2RedisWithRange(startNumber, endNumber);
                allThreads.put(threadName, db2RedisWithRange);

                start = endNumber + 1;
            }

            // startThreads(allThreads);
            final ExecutorService tpe = Executors.newFixedThreadPool(30);

            for (final Db2RedisWithRange db2RedisWithRange : allThreads.values())
                tpe.execute(db2RedisWithRange);

            waitForThreadToClose(allThreads);
            final long endTime = System.currentTimeMillis();
            printStats(startTime, endTime);

            log.fatal("Completed Printing statistics");

            System.out.println("COMPLETED. See log for more info >>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            tpe.shutdown();
        }
        catch (final Exception e)
        {
            log.error("Exception while processing DND load from Database to Redis", e);
        }
        finally
        {}
    }

    private static void printStats(
            long aStartTime,
            long aEndTime)
    {
        log.fatal("Redis and DB Compare Stats");
        log.fatal("Redis and DB Compare Start Time           : " + new Date(aStartTime));
        log.fatal("Redis and DB Compare End Time             : " + new Date(aEndTime));
        log.fatal("Redis and DB Compare Time taken (Sec)     : " + ((aEndTime - aStartTime) / (1000.0)));
        log.fatal("********************************************");
        log.fatal("Records Compare No Mismatch               : " + CountHolder.getInstance().getCompareNoMismatch());
        log.fatal("Records Compare Preferences Mismatch      : " + CountHolder.getInstance().getComparePreferencesMismatch());
        log.fatal("Records Compare Not Available in Database : " + CountHolder.getInstance().getCompareNotAvailableInDatabase());
        log.fatal("Records Compare Not Available in Redis    : " + CountHolder.getInstance().getCompareNotAvailableInRedis());
        log.fatal("********************************************");
    }

    private static void startThreads(
            HashMap<String, Db2RedisWithRange> aAllThreads)
    {
        if (log.isInfoEnabled())
            log.info("Start the threads ");
        String                 threadName;
        final Iterator<String> allThreadNames = aAllThreads.keySet().iterator();

        while (allThreadNames.hasNext())
        {
            threadName = allThreadNames.next();
            final Db2RedisWithRange withRange = aAllThreads.get(threadName);

            log.fatal("Starting the thread ..." + withRange);

            ExecutorSheduler2.getInstance().addTask(withRange, threadName);
          
        }
    }

    private static void waitForThreadToClose(
            HashMap<String, Db2RedisWithRange> aAllThreads)
    {
        boolean hasAllThreadsCompleted = false;

        while (!hasAllThreadsCompleted)
        {
            final Iterator<Db2RedisWithRange> allThs = aAllThreads.values().iterator();
            Db2RedisWithRange                 db2RedisWithRange;

            while (allThs.hasNext())
            {
                db2RedisWithRange      = allThs.next();
                hasAllThreadsCompleted = db2RedisWithRange.isCompleted();

                if (!hasAllThreadsCompleted)
                {
                    if (log.isDebugEnabled())
                        log.debug("Waiting for the thread " + db2RedisWithRange + " to close.");
                    break;
                }
            }

            try
            {
                // Wait before checking the threads completed status
                Thread.sleep(1 * 100L);
            }
            catch (final Exception e)
            {
                //
            }
        }

        if (log.isInfoEnabled())
            log.info("Completed Process");

        try
        {
            // To double check all the threads are complete its work
            if (log.isDebugEnabled())
                log.debug("Waiting for 10 more seconds for all threads to come out");
            Thread.sleep(10 * 1000L);
        }
        catch (final Exception e)
        {
            //
        }
    }

}
