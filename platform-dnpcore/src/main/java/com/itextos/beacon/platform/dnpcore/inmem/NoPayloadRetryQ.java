package com.itextos.beacon.platform.dnpcore.inmem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.itextos.beacon.commonlib.message.IMessage;

public class NoPayloadRetryQ
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final NoPayloadRetryQ INSTANCE = new NoPayloadRetryQ();

    }

    public static NoPayloadRetryQ getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final BlockingQueue<IMessage> mNoPayloadRetryQ = new LinkedBlockingQueue<>();

    private NoPayloadRetryQ()
    {}

    public BlockingQueue<IMessage> getBlockingQueue()
    {
        return mNoPayloadRetryQ;
    }

    public void addMessage(
            IMessage aMessage)
            throws InterruptedException
    {
        mNoPayloadRetryQ.put(aMessage);
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
        int size = mNoPayloadRetryQ.size();

        if (size > 0)
        {
            size = size > aSize ? aSize : size;
            final List<IMessage> lRecords = new ArrayList<>(size);
            mNoPayloadRetryQ.drainTo(lRecords, size);
            return lRecords;
        }

        return new ArrayList<>(1);
    }

}
