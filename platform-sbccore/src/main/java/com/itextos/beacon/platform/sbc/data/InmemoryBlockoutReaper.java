package com.itextos.beacon.platform.sbc.data;

import com.itextos.beacon.platform.sbc.dao.DBHandler;

public class InmemoryBlockoutReaper
        extends
        InmemoryQueueReaper
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final InmemoryBlockoutReaper INSTANCE = new InmemoryBlockoutReaper();

    }

    public static InmemoryBlockoutReaper getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private InmemoryBlockoutReaper()
    {
        super(InmemBlockoutQueue.getInstance(), DBHandler.TABLE_NAME_BLOCKOUT);
    }

}