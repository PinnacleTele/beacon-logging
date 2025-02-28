package com.itextos.beacon.inmemory.spamcheck.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler2;

public class IntlSpamLogger
{

    private static final int REAPER_COUNT = 5;

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final IntlSpamLogger INSTANCE = new IntlSpamLogger();

    }

    public static IntlSpamLogger getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final LinkedBlockingQueue<IntlSpamCheckObject> mIntlSpamObjectQueue = new LinkedBlockingQueue<>(5000);

    private IntlSpamLogger()
    {
        for (int i = 0; i < REAPER_COUNT; i++) {
           ;
            ExecutorSheduler2.getInstance().addTask( new IntlSpamLoggerReaper(), "IntlSpamLoggerReaper : "+i);
        }
    }

    public void addSpamObject(
            IntlSpamCheckObject aIntlSpamCheckObject)
    {

        try
        {
            mIntlSpamObjectQueue.put(aIntlSpamCheckObject);
        }
        catch (final InterruptedException e)
        {
            //
        }
    }

    public List<IntlSpamCheckObject> getSpamCheckObjects()
    {
        final List<IntlSpamCheckObject> toReturn = new ArrayList<>();
        if (!mIntlSpamObjectQueue.isEmpty())
            mIntlSpamObjectQueue.drainTo(toReturn, 1000);
        return toReturn;
    }

}
