package com.itextos.beacon.r3r.inmemory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.r3r.data.R3rRequestData;

public class RequestDataInmemoryQueue
{

    private static final Log log = LogFactory.getLog(RequestDataInmemoryQueue.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final RequestDataInmemoryQueue INSTANCE = new RequestDataInmemoryQueue();

    }

    public static RequestDataInmemoryQueue getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final BlockingQueue<R3rRequestData> linkedBlockingQueue = new LinkedBlockingQueue<>(5000);

    private RequestDataInmemoryQueue()
    {}

    public void add(
            R3rRequestData aR3rRequestData)
    {

        try
        {
            linkedBlockingQueue.put(aR3rRequestData);
        }
        catch (final InterruptedException e)
        {
            log.error("Exception while add the record to inmemory Queue ", e);
        }
    }

    public R3rRequestData getData()
    {
        return linkedBlockingQueue.poll();
    }

}