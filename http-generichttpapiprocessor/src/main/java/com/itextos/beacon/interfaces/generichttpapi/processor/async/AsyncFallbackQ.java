package com.itextos.beacon.interfaces.generichttpapi.processor.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.itextos.beacon.commonlib.message.IMessage;

public class AsyncFallbackQ
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final AsyncFallbackQ INSTANCE = new AsyncFallbackQ();

    }

    public static AsyncFallbackQ getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final BlockingQueue<IMessage> mAsyncFallBackQ = new LinkedBlockingQueue<>();

    private AsyncFallbackQ()
    {}

    public BlockingQueue<IMessage> getBlockingQueue()
    {
        return mAsyncFallBackQ;
    }

    public void addMessage(
            IMessage aMessage)
            throws InterruptedException
    {
        mAsyncFallBackQ.put(aMessage);
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
        return getMessage(100);
    }

    public List<IMessage> getMessage(
            int aSize)
    {
        int size = mAsyncFallBackQ.size();

        if (size > 0)
        {
            size = size > aSize ? aSize : size;
            final List<IMessage> lRecords = new ArrayList<>(size);
            mAsyncFallBackQ.drainTo(lRecords, size);
            return lRecords;
        }

        return new ArrayList<>(1);
    }

}
