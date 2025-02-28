package com.itextos.beacon.platform.smppdlr.fbp;

import java.util.ArrayList;
import java.util.List;

public class SmppDlrFallbackPollerHolder
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final SmppDlrFallbackPollerHolder INSTANCE = new SmppDlrFallbackPollerHolder();

    }

    public static SmppDlrFallbackPollerHolder getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final List<AbstractDataPoller> allReaders = new ArrayList<>();

    private SmppDlrFallbackPollerHolder()
    {
        startPollars();
    }

    private void startPollars()
    {
        allReaders.add(new SmppDlrFallbackPoller());
    }

    public void stopMe()
    {
        for (final AbstractDataPoller poller : allReaders)
            poller.stopMe();
    }

}
