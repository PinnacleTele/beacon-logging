package com.itextos.beacon.platform.dnpcore.inmem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.itextos.beacon.commonlib.message.IMessage;

public class NoPayloadRetryUpdateQ
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final NoPayloadRetryUpdateQ INSTANCE = new NoPayloadRetryUpdateQ();

    }

    public static NoPayloadRetryUpdateQ getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final BlockingQueue<IMessage> mNoPayloadRetryUpdateQ = new LinkedBlockingQueue<>();

    private NoPayloadRetryUpdateQ()
    {}

    public BlockingQueue<IMessage> getBlockingQueue()
    {
        return mNoPayloadRetryUpdateQ;
    }

    public void addMessage(
            IMessage aMessage)
            throws InterruptedException
    {
        mNoPayloadRetryUpdateQ.put(aMessage);
    }

    public void addMessage(
            List<IMessage> aMessageLst)
            throws InterruptedException
    {
        for (final IMessage msg : aMessageLst)
            addMessage(msg);
    }

    public List<IMessage> getMessage()
    {
        return getMessage(1000);
    }

    public List<IMessage> getMessage(
            int aSize)
    {
        int size = mNoPayloadRetryUpdateQ.size();

        if (size > 0)
        {
            size = size > aSize ? aSize : size;
            final List<IMessage> lRecords = new ArrayList<>(size);
            mNoPayloadRetryUpdateQ.drainTo(lRecords, size);
            return lRecords;
        }

        return new ArrayList<>(1);
    }

}
