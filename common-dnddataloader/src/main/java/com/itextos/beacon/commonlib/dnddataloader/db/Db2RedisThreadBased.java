package com.itextos.beacon.commonlib.dnddataloader.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.dnddataloader.util.DndPropertyProvider;
import com.itextos.beacon.commonlib.dnddataloader.util.ThreadSpanner;

public class Db2RedisThreadBased
{

    private static final Log      log                     = LogFactory.getLog(Db2RedisThreadBased.class);

    private static final int      RECORD_FETCH_BATCH_SIZE = 10000;

    public static final int       DND_JNDI_INFO           = 3;
    protected static final String SQL_TO_SELECT           = "select * from " + DndPropertyProvider.getDnDDataTableName() + " where (dest between ? and ?) order by dest limit "
            + RECORD_FETCH_BATCH_SIZE;

    protected static final String ACTION_ADD_OR_UPDATE    = "A";
    protected static final String COLUMN_NAME_DEST        = "dest";
    protected static final String COLUMN_NAME_PREFERENCES = "preferences";

    private Db2RedisThreadBased()
    {}

    public static void processDBRecords()
    {

        try
        {
            final Map<String, Map<Integer, Long>> thredSpannerResults = ThreadSpanner.getSpannedThreads();

            final Map<Integer, Long>              threadCountsMap     = thredSpannerResults.get("THREAD_COUNTS");
            final Map<Integer, Long>              dbCountsMap         = thredSpannerResults.get("DB_COUNTS");

            final Iterator<Integer>               indicee             = threadCountsMap.keySet().iterator();
            Integer                               curIndex;
            String                                startValue;
            String                                endValue;
            long                                  startNumber;
            long                                  endNumber;
            long                                  curThreadCount;
            long                                  dbCount;
            String                                threadName;

            final Map<String, Db2RedisWithRange>  allThreads          = new HashMap<>();

            while (indicee.hasNext())
            {
                curIndex       = indicee.next();
                startValue     = "91" + curIndex.intValue() + "000000000";
                endValue       = "91" + curIndex.intValue() + "999999999";

                startNumber    = Long.parseLong(startValue);
                endNumber      = Long.parseLong(endValue);

                curThreadCount = threadCountsMap.get(curIndex);
                dbCount        = dbCountsMap.get(curIndex);

                if (log.isDebugEnabled())
                    if (curIndex > 0)
                        log.debug("Index : " + curIndex + ", Start number : " + startNumber + ", End number : " + endNumber + ", Thread Count : " + curThreadCount + ", Record Count : " + dbCount);
                    else
                        log.debug("Index : " + curIndex + ", Start number : 0, End number : " + (911000000000L - 1) + ", Thread Count : " + curThreadCount + ", Record Count : " + dbCount);

                if (curIndex == 0)
                {
                    startNumber = 0;
                    endNumber   = 911000000000L - 1;
                    threadName  = "DB2Redis-" + startNumber + "~" + endNumber;

                    if (log.isDebugEnabled())
                        log.debug("Index : " + curIndex + ", Start number : " + startNumber + ", End number : " + endNumber + ", Thread Index : 0, recPerThread : " + 1000000000L);

                    final Db2RedisWithRange db2RedisWithRange = new Db2RedisWithRange(startNumber, endNumber);
                    allThreads.put(threadName, db2RedisWithRange);
                    continue;
                }

                if (curThreadCount == 1)
                {
                    threadName = "DB2Redis-" + startNumber + "~" + endNumber;

                    if (log.isDebugEnabled())
                        log.debug("Index : " + curIndex + ", Start number : " + startNumber + ", End number : " + endNumber + ", Thread Index : 0, recPerThread : " + 1000000000L);

                    final Db2RedisWithRange db2RedisWithRange = new Db2RedisWithRange(startNumber, endNumber);
                    allThreads.put(threadName, db2RedisWithRange);
                }
                else
                {
                    long recPerThread = 1000000000L / curThreadCount;
                    if (log.isDebugEnabled())
                        log.debug("recPerThread >> " + recPerThread);

                    final int length = (recPerThread + "").length();

                    recPerThread = (long) (Integer.parseInt((recPerThread + "").substring(0, 1)) * Math.pow(10d, length - 1));

                    for (int threadIndex = 0; threadIndex < curThreadCount; threadIndex++)
                    {
                        if (threadIndex == (curThreadCount - 1))
                            endNumber = Long.parseLong(endValue);
                        else
                            if (threadIndex == 0)
                                endNumber = startNumber + recPerThread;
                            else
                                endNumber = (startNumber + recPerThread) - 1;

                        if (log.isDebugEnabled())
                            log.debug(
                                    "Index : " + curIndex + ", Start number : " + startNumber + ", End number : " + endNumber + ", Thread Index : " + threadIndex + ", recPerThread : " + recPerThread);

                        threadName = "DB2Redis-" + startNumber + "~" + endNumber;
                        final Db2RedisWithRange db2RedisWithRange = new Db2RedisWithRange(startNumber, endNumber);
                        allThreads.put(threadName, db2RedisWithRange);

                        startNumber = endNumber + 1;
                    }
                }
            }

            startAllThreads(allThreads);
        }
        catch (final Exception e)
        {
            log.error("Exception while processing DND load from Database to Redis", e);
        }
    }

    private static void startAllThreads(
            Map<String, Db2RedisWithRange> aAllThreads)
    {
        if (log.isInfoEnabled())
            log.info("Start the threads ");

        final Iterator<String> allThreadNames = aAllThreads.keySet().iterator();
        String                 threadName;

        while (allThreadNames.hasNext())
        {
            threadName = allThreadNames.next();
            final Db2RedisWithRange withRange = aAllThreads.get(threadName);

            if (log.isInfoEnabled())
                log.info("Starting the thread ..." + withRange);

            final Thread t = new Thread(withRange, threadName);
            t.start();
        }
    }

}
