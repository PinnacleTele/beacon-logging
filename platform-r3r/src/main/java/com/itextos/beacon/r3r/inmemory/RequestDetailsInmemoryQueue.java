package com.itextos.beacon.r3r.inmemory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.r3r.data.R3RObject;

public class RequestDetailsInmemoryQueue
{

    private static final Log log = LogFactory.getLog(RequestDetailsInmemoryQueue.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final RequestDetailsInmemoryQueue INSTANCE = new RequestDetailsInmemoryQueue();

    }

    public static RequestDetailsInmemoryQueue getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final BlockingQueue<R3RObject> linkedBlockingQueue = new LinkedBlockingQueue<>(5000);

    private RequestDetailsInmemoryQueue()
    {}

    public void add(
            R3RObject aR3rObject)
    {

        try
        {
            linkedBlockingQueue.add(aR3rObject);
        }
        catch (final Exception e)
        {
            log.error("Exception while put the record to Inmemory Queue ", e);
        }
    }

    public List<R3RObject> getData()
    {
        return getRecords(50);
    }

    List<R3RObject> getRecords(
            int aMaxSize)
    {
        int lSize = linkedBlockingQueue.size();
        if (log.isDebugEnabled())
            log.debug("inmemory Queue Size is " + linkedBlockingQueue.size());

        if (lSize > 0)
        {
            lSize = lSize > aMaxSize ? aMaxSize : lSize;
            final List<R3RObject> lRecords = new ArrayList<>(lSize);
            linkedBlockingQueue.drainTo(lRecords, lSize);
            return lRecords;
        }

        return null;
    }

}
