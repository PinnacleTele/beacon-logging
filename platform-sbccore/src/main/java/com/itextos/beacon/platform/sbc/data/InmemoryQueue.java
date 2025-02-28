package com.itextos.beacon.platform.sbc.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.message.MessageRequest;

abstract class InmemoryQueue
{

    private static final Log                    log    = LogFactory.getLog(InmemoryQueue.class);

    private final BlockingQueue<MessageRequest> mQueue = new LinkedBlockingQueue<>();

    public void addRecord(
            MessageRequest aMessageRequest)
            throws InterruptedException
    {
        if (log.isDebugEnabled())
            log.debug("Record to be instarted into inmemory... " + aMessageRequest.getBaseMessageId());
        mQueue.put(aMessageRequest);

        if (log.isDebugEnabled())
            log.debug("Imemory queue size : " + mQueue.size());
    }

    public void addRecords(
            List<MessageRequest> aMessageRequestList)
            throws InterruptedException
    {
        if (!aMessageRequestList.isEmpty())
            for (final MessageRequest lMessageRequest : aMessageRequestList)
                addRecord(lMessageRequest);
    }

    public List<MessageRequest> getRecords()
    {
        return getRecords(1000);
    }

    public List<MessageRequest> getRecords(
            int aSize)
    {
        int lSize = mQueue.size();

        if (lSize > 0)
        {
            lSize = lSize > aSize ? aSize : lSize;
            final List<MessageRequest> lRecords = new ArrayList<>(lSize);
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