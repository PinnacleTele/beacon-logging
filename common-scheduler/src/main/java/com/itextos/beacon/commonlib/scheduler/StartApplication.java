package com.itextos.beacon.commonlib.scheduler;

import com.itextos.beacon.commonlib.scheduler.impl.ScheduleDataLoader;

public class StartApplication
{

    public static void main(
            String[] args)
    {
        ItextosScheduler.getInstance();
        ScheduleDataLoader.loadData(true);
    }

}