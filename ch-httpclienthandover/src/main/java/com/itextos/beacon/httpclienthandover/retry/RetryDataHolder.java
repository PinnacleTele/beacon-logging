package com.itextos.beacon.httpclienthandover.retry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.itextos.beacon.httpclienthandover.utils.ClientHandoverConstatnts;

public class RetryDataHolder
{

    private static class SingletonHolder
    {

        static final RetryDataHolder INSTANCE = new RetryDataHolder();

    }

    public static RetryDataHolder getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<String, BlockingQueue<String>> inprocessMessage = new ConcurrentHashMap<>();
    private final Map<String, BlockingQueue<String>> expiredMessage   = new ConcurrentHashMap<>();

    public void addInprocessMessages(
            List<String> aList,
            String custId)
    {
        if ((aList == null) || aList.isEmpty())
            return;

        final BlockingQueue<String> inProcessMessageTemp = inprocessMessage.computeIfAbsent(custId, k -> new LinkedBlockingQueue<>());
        inProcessMessageTemp.addAll(aList);
    }

    public void addExpiredMessages(
            List<String> aList,
            String custId)
    {
        if ((aList == null) || aList.isEmpty())
            return;
        final BlockingQueue<String> expiredMessageTemp = expiredMessage.computeIfAbsent(custId, k -> new LinkedBlockingQueue<>());
        expiredMessageTemp.addAll(aList);
    }

    public int expiredMessagesCount()
    {
        return expiredMessage.size();
    }

    public boolean inProcessCount()
    {
        return inprocessMessage.entrySet().stream().anyMatch(entry -> !entry.getValue().isEmpty());
    }

    public int expiredCount()
    {
        return expiredMessage.size();
    }

    public List<String> getInProcessMessages(
            int aCount,
            String custId)
    {
        final List<String> returnList = new ArrayList<>(aCount);

        if (!inprocessMessage.isEmpty())
        {
            final String                key           = ("".equals(custId)) ? ClientHandoverConstatnts.DEFAULT_KEY : custId;
            final BlockingQueue<String> blockingQueue = inprocessMessage.get(key);

            if (blockingQueue == null)
                return returnList;

            blockingQueue.drainTo(returnList, aCount);
        }
        return returnList;
    }

    public List<String> getExpiredMessages(
            int aCount,
            String custId)
    {
        final List<String> returnList = new ArrayList<>(aCount);

        if (!expiredMessage.isEmpty())
        {
            final String                key           = ("".equals(custId)) ? ClientHandoverConstatnts.DEFAULT_KEY : custId;
            final BlockingQueue<String> blockingQueue = expiredMessage.get(key);

            if (blockingQueue == null)
                return returnList;

            blockingQueue.drainTo(returnList, aCount);
        }
        return returnList;
    }

}