package com.itextos.beacon.platform.kannelstatusupdater.taskpool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolTon
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static ThreadPoolTon INSTANCE = new ThreadPoolTon();

    }

    public static ThreadPoolTon getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 50, 5000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(2000));

    private ThreadPoolTon()
    {}

    public ThreadPoolExecutor getExecutor()
    {
        return executor;
    }

}