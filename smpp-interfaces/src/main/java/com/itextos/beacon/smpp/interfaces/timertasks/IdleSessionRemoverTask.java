package com.itextos.beacon.smpp.interfaces.timertasks;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.smpp.interfaces.sessionhandlers.ItextosSessionManager;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;

public class IdleSessionRemoverTask
        extends
        TimerTask
{

    private static final Log log   = LogFactory.getLog(IdleSessionRemoverTask.class);
    private final Timer      timer = new Timer("IdleSessionRemoverTask-Thread");
    private final long       inactiveInterval;

    public IdleSessionRemoverTask()
    {
        inactiveInterval = SmppProperties.getInstance().getSmppInterfaceIdleSessionAllowedTime() * 1000;
        timer.scheduleAtFixedRate(this, inactiveInterval + 1000, inactiveInterval + 1000);
    }

    @Override
    public void run()
    {

        try
        {
            if (log.isInfoEnabled())
                log.info("Removing expired sessions...");
            if (log.isDebugEnabled())
                log.debug("Session inactiveInterval=" + inactiveInterval);

            final int removedCount = ItextosSessionManager.getInstance().removeExpiredSessions(inactiveInterval);

            if (log.isInfoEnabled())
                log.info(removedCount + " session(s) removed successfully.");
        }
        catch (final Exception e)
        {
            log.error("Problem removing idle session due to...", e);
        }
    }

    public void stopMe()
    {
        timer.cancel();
    }

}