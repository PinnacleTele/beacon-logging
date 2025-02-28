package com.itextos.beacon.platform.sbpcore.process;

import com.itextos.beacon.platform.sbpcore.dao.DBPoller;

public class SchedulePoller
        extends
        AbstractDataPoller
{

    public SchedulePoller(
            int aAppInstanceId)
    {
        super(aAppInstanceId, DBPoller.TABLE_NAME_SCHEDULE);
    }

}