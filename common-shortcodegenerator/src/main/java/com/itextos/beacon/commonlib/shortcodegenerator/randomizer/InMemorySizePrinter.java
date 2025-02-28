package com.itextos.beacon.commonlib.shortcodegenerator.randomizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itextos.beacon.commonlib.shortcodegenerator.randomizer.util.RedisConnectionPool;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class InMemorySizePrinter
        implements
        Runnable
{

    private static final Logger log          = LogManager.getLogger(InMemorySizePrinter.class);
    private static final long   MAX_LIMIT    = 5000000; // 50,00,000
    private boolean             canContinue  = true;
    private long                countInRedis = 0;

    @Override
    public void run()
    {

        while (canContinue)
        {

            try
            {
                printRedisDataSize();

                log.debug("Files to Read : " + getNewFormattedText("" + InformationHolder.getInstance().getFilesWaitingFoRead(), 5) //
                        + " Read & in use : " + getNewFormattedText("" + InformationHolder.getInstance().getReadFileCount(), 5)//
                        + " Reader Size : " + getNewFormattedText("" + InformationHolder.getInstance().getReadingDataSize(), 10) //
                        + " Writer Size : " + getNewFormattedText("" + InformationHolder.getInstance().getWritingDataSize(), 10) //
                        + " Redis Counts : " + getNewFormattedText("" + countInRedis, 10));

                // printThreadInformation();
            }
            catch (final Exception e)
            {
                log.error("Exception while printing the Redis statistics. ", e);
            }

            final long sleepTimeStart = System.currentTimeMillis();

            sleepForWhile(10 * 1000L);

            final long sleepTimeEnd = System.currentTimeMillis();
            if ((sleepTimeEnd - sleepTimeStart) > 20000)
                log.debug("Wake up after 10 seconds. But time taken :" + (sleepTimeEnd - sleepTimeStart) + " millis");
        }
    }

    private static void printThreadInformation()
    {
        final Map<Thread, StackTraceElement[]> lAllStackTraces = Thread.getAllStackTraces();

        for (final Entry<Thread, StackTraceElement[]> threadEntry : lAllStackTraces.entrySet())
        {
            final Thread curThread  = threadEntry.getKey();
            final String threadName = curThread.getName();

            if (threadName.contains("File Writer -") || threadName.contains("RedisPusher-") || threadName.contains("Redis Reader ") || threadName.contains("File Reader ")
                    || threadName.contains("FileNamingAddingThread"))
                switch (curThread.getState())
                {
                    case BLOCKED:
                    case TIMED_WAITING:
                    case WAITING:
                        printStackTrace(curThread, threadEntry.getValue());
                        break;

                    case NEW:
                    case RUNNABLE:
                    case TERMINATED:
                    default:
                        break;
                }
        }
    }

    private static final String NEW_LINE             = System.getProperty("line.separator");
    private static final String TRACE_LINE_SEPARATOR = NEW_LINE + "\t@";

    private static void printStackTrace(
            Thread aCurThread,
            StackTraceElement[] aValue)
    {
        final StringJoiner sj = new StringJoiner(TRACE_LINE_SEPARATOR);

        for (final StackTraceElement ste : aValue)
        {
            final String lName = ste.getClassName();

            if (lName.startsWith("com.itextos."))
            {
                final String trace = ste.toString();
                sj.add(trace);
            }
        }

        log.debug(getFormattedText(aCurThread.getName(), 25) + getFormattedText(aCurThread.getState().name(), 15) + " Called from " + NEW_LINE + sj.toString());
    }

    public static String getFormattedText(
            String aString,
            int aLen)
    {
        final StringBuilder sj         = new StringBuilder(aString);
        final int           strLen     = aString.length();
        final int           spaceToAdd = aLen - strLen;

        for (int index = 0; index < spaceToAdd; index++)
            sj.append(" ");
        return sj.toString();
    }

    private static String getNewFormattedText(
            String aValue,
            int aLen)
    {
        final int           strLen     = aValue.length();
        final int           spaceToAdd = aLen - strLen;

        final StringBuilder sj         = new StringBuilder();
        for (int index = 0; index < spaceToAdd; index++)
            sj.append(" ");
        sj.append(aValue);
        return sj.toString();
    }

    public void stopMe()
    {
        log.fatal("Stopping the inmemory data printer");
        canContinue = false;
    }

    public boolean canAddToRedis()
    {
        return countInRedis < MAX_LIMIT;
    }

    public long getRedisCount()
    {
        return countInRedis;
    }

    public void setCountInRedis(
            long aCountInRedis)
    {
        countInRedis = aCountInRedis;
    }

    private void printRedisDataSize()
    {
        final List<String> lAllKeys   = RedisConnectionPool.getInstance().getAllKeys();
        long               redisCount = 0;

        for (final String key : lAllKeys)
        {
            final List<Response<Long>> allResponses = new ArrayList<>();

            try (
                    Jedis lJedis = RedisConnectionPool.getInstance().getJedis(key);
                    Pipeline pipe = lJedis.pipelined();)
            {
                final List<String> lKeys = RedisConnectionPool.getInstance().getKeys(key);
                lKeys.stream().forEach(redisKey -> {
                    final Response<Long> lScard = pipe.scard(redisKey);
                    allResponses.add(lScard);
                });
                pipe.sync();
            }

            long count = 0;
            for (final Response<Long> response : allResponses)
                count += response.get();

            if (count > 0)
                redisCount += count;
            // log.debug("Redis count in " + key + " is " + count);
        }

        setCountInRedis(redisCount);
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