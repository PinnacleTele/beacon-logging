package com.itextos.beacon.inmemory.spamcheck.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler2;

public class SpamLogger
{

    private static final int REAPER_COUNT = 5;

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final SpamLogger INSTANCE = new SpamLogger();

    }

    public static SpamLogger getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final LinkedBlockingQueue<SpamCheckObject> mSpamObjectQueue = new LinkedBlockingQueue<>(5000);

    private SpamLogger()
    {
        for (int i = 0; i < REAPER_COUNT; i++) {
            ExecutorSheduler2.getInstance().addTask(new SpamLoggerReaper(), "SpamLoggerReaper : "+i);
        }
    }

    public void addSpamObject(
            SpamCheckObject aSpamCheckObject)
    {

        try
        {
            mSpamObjectQueue.put(aSpamCheckObject);
        }
        catch (final InterruptedException e)
        {
            //
        }
    }

    public List<SpamCheckObject> getSpamCheckObjects()
    {
        final List<SpamCheckObject> toReturn = new ArrayList<>();
        if (!mSpamObjectQueue.isEmpty())
            mSpamObjectQueue.drainTo(toReturn, 1000);
        return toReturn;
    }

}