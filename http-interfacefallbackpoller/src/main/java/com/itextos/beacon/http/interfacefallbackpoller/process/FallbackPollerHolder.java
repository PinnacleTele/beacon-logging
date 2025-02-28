package com.itextos.beacon.http.interfacefallbackpoller.process;

import java.util.ArrayList;
import java.util.List;

public class FallbackPollerHolder
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final FallbackPollerHolder INSTANCE = new FallbackPollerHolder();

    }

    public static FallbackPollerHolder getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final List<AbstractDataPoller> allReaders = new ArrayList<>();

    private FallbackPollerHolder()
    {
        startPollars();
    }

    private void startPollars()
    {
        allReaders.add(new FallbackPoller());
    }

    public void stopMe()
    {
        for (final AbstractDataPoller poller : allReaders)
            poller.stopMe();
    }

}
