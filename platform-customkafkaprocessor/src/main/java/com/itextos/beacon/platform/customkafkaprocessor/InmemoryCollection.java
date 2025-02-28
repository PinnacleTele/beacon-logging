package com.itextos.beacon.platform.customkafkaprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.itextos.beacon.commonlib.message.IMessage;

public class InmemoryCollection
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final InmemoryCollection INSTANCE = new InmemoryCollection();

    }

    public static InmemoryCollection getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final BlockingQueue<IMessage> mQueue = new LinkedBlockingQueue<>(5000);

    private InmemoryCollection()
    {}

    public void add(
            IMessage aMessage)
            throws InterruptedException
    {
        mQueue.put(aMessage);
    }

    public List<IMessage> getList(
            int aSize)
    {
        final int            size        = aSize <= mQueue.size() ? aSize : mQueue.size();
        final List<IMessage> returnValue = new ArrayList<>(size);
        mQueue.drainTo(returnValue, size);
        return returnValue;
    }

    public int size()
    {
        return mQueue.size();
    }

    public boolean isEmpty()
    {
        return mQueue.isEmpty();
    }

}