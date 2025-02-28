package com.itextos.beacon.commonlib.scheduler.logging;

public interface IJobLogging
{

    void storeJobTobeExecuted();

    void storeJobWasExecuted();

}