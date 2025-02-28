package com.itextos.beacon.commonlib.daemonprocess;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShutdownHandler
{

    private static final Log    log                  = LogFactory.getLog(ShutdownHandler.class);

    private static final String SHUTDOWN_IN_PROGRESS = "Shutdown in progress. Request ignored.";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final ShutdownHandler INSTANCE = new ShutdownHandler();

    }

    public static ShutdownHandler getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final List<String> mHookMap = new ArrayList<>();

    public void addHook(
            String aProcessName,
            final ShutdownHook hook)
    {

        if (mHookMap.contains(aProcessName))
        {
            log.fatal("ShutDownHandler: Hook already added for '" + aProcessName + "' " + SHUTDOWN_IN_PROGRESS);
            return;
        }

        if (log.isDebugEnabled())
            log.debug("Adding shutdown hook for the object '" + aProcessName + "'");

        
       
        final Thread shutdownThread =  new Thread(() -> {

            try
            {
                log.fatal("Calling the shutdown process of '" + aProcessName + "'");
                hook.shutdown();
            }
            catch (final Exception e)
            {}
        });

        Runtime.getRuntime().addShutdownHook(shutdownThread);
        mHookMap.add(aProcessName);
    }

}