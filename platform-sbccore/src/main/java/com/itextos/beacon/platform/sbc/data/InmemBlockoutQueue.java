package com.itextos.beacon.platform.sbc.data;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.itextos.beacon.commonlib.message.MessageRequest;

public class InmemBlockoutQueue
        extends
        InmemoryQueue
{

    BlockingQueue<MessageRequest> mQ = new LinkedBlockingQueue<>();

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final InmemBlockoutQueue INSTANCE = new InmemBlockoutQueue();

    }

    public static InmemBlockoutQueue getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

}