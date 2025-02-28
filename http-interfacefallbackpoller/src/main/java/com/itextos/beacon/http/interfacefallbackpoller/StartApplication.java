package com.itextos.beacon.http.interfacefallbackpoller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.http.interfacefallbackpoller.process.FallbackPollerHolder;
import com.itextos.beacon.smslog.DebugLog;

public class StartApplication
{

    private static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {
        if (log.isDebugEnabled())
            log.debug("Starting the Fallback Poller application.");

        DebugLog.log("Starting the Fallback Poller application.");
        try
        {
            FallbackPollerHolder.getInstance();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.error("Shutdownhook invoked for the Fallback Poller.");
                FallbackPollerHolder.getInstance().stopMe();
            }));
        }
        catch (final Exception e)
        {
            log.error("Exception while starting the Fallback Poller application.", e);
            System.exit(-1);
        }
    }

}
