package com.itextos.beacon.platform.dnrfallback.inmem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.itextos.beacon.commonlib.message.IMessage;

public class DlrFallbackQ
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final DlrFallbackQ INSTANCE = new DlrFallbackQ();

    }

    public static DlrFallbackQ getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final BlockingQueue<IMessage> mDlrFallBackQ = new LinkedBlockingQueue<>();

    private DlrFallbackQ()
    {}

    public BlockingQueue<IMessage> getBlockingQueue()
    {
        return mDlrFallBackQ;
    }

    public void addMessage(
            IMessage aMessage)
            throws InterruptedException
    {
        mDlrFallBackQ.put(aMessage);
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
        int size = mDlrFallBackQ.size();

        if (size > 0)
        {
            size = size > aSize ? aSize : size;
            final List<IMessage> lRecords = new ArrayList<>(size);
            mDlrFallBackQ.drainTo(lRecords, size);
            return lRecords;
        }

        return new ArrayList<>(1);
    }

}
