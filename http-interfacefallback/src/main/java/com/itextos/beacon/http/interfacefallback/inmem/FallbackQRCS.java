package com.itextos.beacon.http.interfacefallback.inmem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.itextos.beacon.commonlib.message.IMessage;

public class FallbackQRCS
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final FallbackQRCS INSTANCE = new FallbackQRCS();

    }

    public static FallbackQRCS getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final BlockingQueue<IMessage> mFallBackQ = new LinkedBlockingQueue<>();

    private FallbackQRCS()
    {}

    public BlockingQueue<IMessage> getBlockingQueue()
    {
        return mFallBackQ;
    }

    public void addMessage(
            IMessage aMessage)
            throws InterruptedException
    {
        mFallBackQ.put(aMessage);
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
        int size = mFallBackQ.size();

        if (size > 0)
        {
            size = size > aSize ? aSize : size;
            final List<IMessage> lRecords = new ArrayList<>(size);
            mFallBackQ.drainTo(lRecords, size);
            return lRecords;
        }

        return new ArrayList<>(1);
    }

}