package com.itextos.beacon.smpp.objects.inmem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.platform.smpputil.ISmppInfo;
import com.itextos.beacon.smpp.objects.SmppObjectType;

public class InfoCollection
{

    private static final Log log            = LogFactory.getLog(InfoCollection.class);
    private static final int MAX_INMEM_SIZE = 5000;

    private static class SingletonHolder
    {

        static final InfoCollection INSTANCE = new InfoCollection();

    }

    public static InfoCollection getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<SmppObjectType, BlockingQueue<ISmppInfo>> mCollection = new EnumMap<>(SmppObjectType.class);

    private InfoCollection()
    {}

    public boolean addInfoObject(
            SmppObjectType aSmppObjectType,
            ISmppInfo aSmppInfo)
    {
        final BlockingQueue<ISmppInfo> inmemQueue = mCollection.computeIfAbsent(aSmppObjectType, k -> new LinkedBlockingQueue<>(MAX_INMEM_SIZE));
        if (log.isDebugEnabled())
            log.debug("InMem Queue - " + inmemQueue);
        return addObject(aSmppObjectType, inmemQueue, aSmppInfo);
    }

    private static boolean addObject(
            SmppObjectType aSmppObjectType,
            BlockingQueue<ISmppInfo> aInmemQueue,
            ISmppInfo aSmppInfo)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("SmppInfo Object : " + aSmppInfo);

            if (aInmemQueue.remainingCapacity() == 0)
            {
                log.error("SmppObjectType " + aSmppObjectType + " inmemory queue is full will wait for the meessage to add in the topic.");
                return false;
            }

            if (aSmppInfo != null)
                aInmemQueue.put(aSmppInfo);

            if (log.isDebugEnabled())
                log.debug("SmppObjectType " + aSmppObjectType + " Message added into memory");

            return true;
        }
        catch (final Exception e)
        {
            log.error("Exception while adding object for " + aSmppObjectType + " to inmemory.", e);
            return false;
        }
    }

    public List<ISmppInfo> getObjects(
            SmppObjectType aSmppObjectType,
            int aMaxSize)
    {
        final BlockingQueue<ISmppInfo> inmemQueue = mCollection.get(aSmppObjectType);

        if (inmemQueue != null)
        {
            final int size = inmemQueue.size();

            if (size > 0)
            {
                if (log.isInfoEnabled())
                    log.info("Remaining messages in the Inmemory Queue is " + size);

                final List<ISmppInfo> toSend = new ArrayList<>(aMaxSize > size ? size : aMaxSize);
                inmemQueue.drainTo(toSend);
                return toSend;
            }
        }
        return new ArrayList<>();
    }

    public void addInfoObject(
            SmppObjectType aSmppObjectType,
            List<ISmppInfo> aSmppInfoList)
    {
        final BlockingQueue<ISmppInfo> inmemQueue = mCollection.computeIfAbsent(aSmppObjectType, k -> new LinkedBlockingQueue<>(MAX_INMEM_SIZE));
        if (log.isDebugEnabled())
            log.debug("InMem Queue - " + inmemQueue);

        for (final ISmppInfo lSmppInfo : aSmppInfoList)
            addObject(aSmppObjectType, inmemQueue, lSmppInfo);
    }

}