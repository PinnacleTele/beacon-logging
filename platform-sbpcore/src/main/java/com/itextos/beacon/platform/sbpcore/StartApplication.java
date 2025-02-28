package com.itextos.beacon.platform.sbpcore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.platform.sbpcore.process.ScheduleBlockoutPollarHolder;
import com.itextos.beacon.smslog.DebugLog;

public class StartApplication
{

    private static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {
        if (log.isDebugEnabled())
            log.debug("Starting the Schedule Blockout Pollar application ");

        DebugLog.log("Starting the Schedule Blockout Pollar application ");
        try
        {
            ScheduleBlockoutPollarHolder.getInstance();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while starting the SBP...", e);
            System.exit(-1);
        }
    }

}
