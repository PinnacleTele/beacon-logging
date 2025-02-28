package com.itextos.beacon.smpp.interfaces.util.counters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BindUnbindCounter
{

    private static class SingletonHolder
    {

        public static final BindUnbindCounter INSTANCE = new BindUnbindCounter();

    }

    public static BindUnbindCounter getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private BindUnbindCounter()
    {}

    private final Map<String, AtomicInteger> bindCounterMap   = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> unBindCounterMap = new ConcurrentHashMap<>();

    public void addBind(
            String aClientId)
    {
        increment(bindCounterMap, aClientId);
    }

    public void addUnBind(
            String aClientId)
    {
        increment(unBindCounterMap, aClientId);
    }

    public int getBoundCount(
            String aClientId)
    {
        return getCount(bindCounterMap, aClientId);
    }

    public int getUnBoundCount(
            String aClientId)
    {
        return getCount(unBindCounterMap, aClientId);
    }

    private static void increment(
            Map<String, AtomicInteger> aCounterMap,
            String aClientId)
    {
        final AtomicInteger counter = aCounterMap.computeIfAbsent(aClientId, k -> new AtomicInteger());
        counter.incrementAndGet();
    }

    private static int getCount(
            Map<String, AtomicInteger> aCounterMap,
            String aClientId)
    {
        final AtomicInteger lAtomicInteger = aCounterMap.get(aClientId);
        return lAtomicInteger == null ? -1 : lAtomicInteger.intValue();
    }

    public void reset()
    {
        bindCounterMap.clear();
        unBindCounterMap.clear();
    }

}
