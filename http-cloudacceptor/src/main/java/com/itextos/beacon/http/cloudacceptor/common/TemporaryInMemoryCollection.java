package com.itextos.beacon.http.cloudacceptor.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TemporaryInMemoryCollection
{

    private final static Log log = LogFactory.getLog(TemporaryInMemoryCollection.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final TemporaryInMemoryCollection INSTANCE = new TemporaryInMemoryCollection();

    }

    public static TemporaryInMemoryCollection getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<String, LinkedBlockingQueue<String>> requestMap = new ConcurrentHashMap<>();

    private TemporaryInMemoryCollection()
    {
        log.warn("Creating " + TemporaryInMemoryCollection.class.getName());
    }

    public void add(
            String aRequest,
            String authKey)
    {
        if (log.isDebugEnabled())
            log.debug("Request to add : '" + aRequest + "'");
        LinkedBlockingQueue<String> queue = requestMap.get(authKey);

        if (queue == null)
        {
            queue = new LinkedBlockingQueue<>(1000);
            requestMap.put(authKey, queue);
        }

        try
        {
            queue.put(aRequest);
        }
        catch (final InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void add(
            List<String> aRequest,
            String authKey)
    {
        if (log.isDebugEnabled())
            log.debug("Request List size : '" + (aRequest == null ? "-1" : aRequest.size()) + "'");

        LinkedBlockingQueue<String> queue = requestMap.get(authKey);

        if (queue == null)
        {
            queue = new LinkedBlockingQueue<>(1000);
            requestMap.put(authKey, queue);
        }

        try
        {
            if (aRequest != null)
                for (final String req : aRequest)
                    queue.put(req);
        }
        catch (final InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public List<String> get(
            int aMaxSize,
            String authKey)
    {
        final LinkedBlockingQueue<String> queue = requestMap.get(authKey);
        if (queue == null)
            return null;
        final int queueSize = queue.size();

        if (log.isDebugEnabled())
            log.debug("Requested List Size : '" + aMaxSize + "'. Messages count in the Queue : '" + queueSize + "'");

        if (queueSize > 0)
        {
            final List<String> returnValue = new ArrayList<>(aMaxSize);
            queue.drainTo(returnValue, aMaxSize);

            if (log.isDebugEnabled())
                log.debug("Returning List with  size : '" + returnValue.size() + "'");
            return returnValue;
        }

        if (log.isDebugEnabled())
            log.debug("Returning null");
        return null;
    }

}
