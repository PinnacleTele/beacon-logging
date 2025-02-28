package com.itextos.beacon.smpp.interfaces.util.counters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.itextos.beacon.smpp.interfaces.sessionhandlers.ItextosSessionManager;

public class LiveSessionsCounter
{

    private static class SingletonHolder
    {

        static final LiveSessionsCounter INSTANCE = new LiveSessionsCounter();

    }

    public static LiveSessionsCounter getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final ConcurrentHashMap<Long, SessionCounter> map = new ConcurrentHashMap<>();

    private LiveSessionsCounter()
    {}

    public void addCounter(
            Long sessionId,
            SessionCounter counter)
    {
        map.put(sessionId, counter);
    }

    public void removeCounter(
            Long sessionId)
    {
        map.remove(sessionId);
    }

    public void captureStat()
    {
        final Map<Long, SessionCounter> allSessions = ItextosSessionManager.getInstance().getRxTrxStatistics();
        map.putAll(allSessions);
    }

    public Map<Long, SessionCounter> collectStat()
    {
        return ItextosSessionManager.getInstance().getRxTrxStatistics(map);
    }

}
