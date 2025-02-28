package com.itextos.beacon.platform.sbc.data;

import com.itextos.beacon.platform.sbc.dao.DBHandler;

public class InmemoryScheduleReaper
        extends
        InmemoryQueueReaper
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final InmemoryScheduleReaper INSTANCE = new InmemoryScheduleReaper();

    }

    public static InmemoryScheduleReaper getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private InmemoryScheduleReaper()
    {
        super(InmemScheduleQueue.getInstance(), DBHandler.TABLE_NAME_SCHEDULE);
    }

}
