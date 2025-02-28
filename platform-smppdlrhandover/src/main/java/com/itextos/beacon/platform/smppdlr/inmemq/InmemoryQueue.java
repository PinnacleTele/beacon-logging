package com.itextos.beacon.platform.smppdlr.inmemq;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.message.DeliveryObject;

public class InmemoryQueue
{

    private static final Log                    log    = LogFactory.getLog(InmemoryQueue.class);

    private final BlockingQueue<DeliveryObject> mQueue = new LinkedBlockingQueue<>(1000);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final InmemoryQueue INSTANCE = new InmemoryQueue();

    }

    public static InmemoryQueue getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    public void addRecord(
            DeliveryObject aDeliveryObject)
            throws InterruptedException
    {
        if (log.isDebugEnabled())
            log.debug("Record to be instarted into inmemory... " + aDeliveryObject.getBaseMessageId());
        mQueue.put(aDeliveryObject);

        if (log.isDebugEnabled())
            log.debug("Imemory queue size : " + mQueue.size());
    }

    public void addRecords(
            List<DeliveryObject> aDeliveryObjectList)
            throws InterruptedException
    {
        if (!aDeliveryObjectList.isEmpty())
            for (final DeliveryObject lDeliveryObject : aDeliveryObjectList)
                addRecord(lDeliveryObject);
    }

    public List<DeliveryObject> getRecords()
    {
        return getRecords(1000);
    }

    public List<DeliveryObject> getRecords(
            int aSize)
    {
        int lSize = mQueue.size();

        if (lSize > 0)
        {
            lSize = lSize > aSize ? aSize : lSize;
            final List<DeliveryObject> lRecords = new ArrayList<>(lSize);
            mQueue.drainTo(lRecords, lSize);
            return lRecords;
        }

        return new ArrayList<>(1);
    }

    public boolean isEmpty()
    {
        return mQueue.isEmpty();
    }

}