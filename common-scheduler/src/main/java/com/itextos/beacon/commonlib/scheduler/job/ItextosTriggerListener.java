package com.itextos.beacon.commonlib.scheduler.job;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;

public class ItextosTriggerListener
        implements
        TriggerListener
{

    @Override
    public String getName()
    {
        return "ItextosTriggerListener";
    }

    @Override
    public void triggerFired(
            Trigger aTrigger,
            JobExecutionContext aContext)
    {}

    @Override
    public boolean vetoJobExecution(
            Trigger aTrigger,
            JobExecutionContext aContext)
    {
        return true;
    }

    @Override
    public void triggerMisfired(
            Trigger aTrigger)
    {}

    @Override
    public void triggerComplete(
            Trigger aTrigger,
            JobExecutionContext aContext,
            CompletedExecutionInstruction aTriggerInstructionCode)
    {}

}