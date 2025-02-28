package com.itextos.beacon.commonlib.utility;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

public class RoundRobin
{

    private static final Log log = LogFactory.getLog(RoundRobin.class);

    private static class SingletonHolder
    {

        private static final RoundRobin INSTANCE = new RoundRobin();

    }

    @SuppressWarnings("synthetic-access")
    public static RoundRobin getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<String, AtomicInteger> roundRobinPointerMap = new ConcurrentHashMap<>();

    /** The Min Index is 1 always. */
    public synchronized int getCurrentIndex(
            String aKey,
            int aMaxValue)
    {

        if (aMaxValue <= 0)
        {

            try
            {
                throw new ItextosRuntimeException("Invalid Max value given for key '" + aKey + "'");
            }
            catch (final Exception e)
            {
                log.error("Error while getting the Roundrobin value.", e);
            }
            return 1;
        }

        if (aMaxValue == 1)
            return 1;

        final AtomicInteger currentCount = roundRobinPointerMap.computeIfAbsent(aKey, k -> new AtomicInteger(0));

        int                 nextCount    = currentCount.addAndGet(1);

        if (nextCount > aMaxValue)
        {
            nextCount = 1;
            currentCount.set(nextCount);
        }
        return nextCount;
    }

}
