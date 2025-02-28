package com.itextos.beacon.interfaces.generichttpapi.processor.pollers;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;

public class ThreadExecutor
{

    private static final Log log = LogFactory.getLog(ThreadExecutor.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final ThreadExecutor INSTANCE = new ThreadExecutor();

    }

    public static ThreadExecutor getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final ThreadPoolExecutor executor;

    private ThreadExecutor()
    {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(APIConstants.MAX_THREAD_POOL_COUNT);
    }

    public void addTask(
            Runnable aTask)
    {
        executor.execute(aTask);

        if (log.isDebugEnabled())
            log.debug("Success fully executed the Thread Pool size : " + executor.getPoolSize());
    }

    public int getCurrentTaskCount()
    {
        return executor.getPoolSize();
    }

    public int getPendingQueue()
    {
        return executor.getQueue().size();
    }

    public void shutdown()
    {
        executor.shutdown();
    }

}