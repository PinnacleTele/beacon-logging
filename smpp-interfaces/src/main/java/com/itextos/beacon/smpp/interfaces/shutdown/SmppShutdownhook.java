package com.itextos.beacon.smpp.interfaces.shutdown;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.smpp.interfaces.StartApplication;

public class SmppShutdownhook
        extends
        Thread
{

    private static final Log       log = LogFactory.getLog(SmppShutdownhook.class);

    private final StartApplication mStartApplication;
    private final String           mInstanceId;

    public SmppShutdownhook(
            StartApplication aStartApplication,
            String aInstanceId)
    {
        mStartApplication = aStartApplication;
        mInstanceId       = aInstanceId;
    }

    @Override
    public void run()
    {
        log.fatal("Shutdown hook invoked..." + mInstanceId);

        mStartApplication.shutdown();
    }

}
