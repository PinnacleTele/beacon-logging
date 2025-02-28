package com.itextos.beacon.smpp.interfaces.timertasks;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.smpp.interfaces.sessionhandlers.ItextosSessionManager;
import com.itextos.beacon.smpp.interfaces.sessionhandlers.objects.SessionRoundRobin;
import com.itextos.beacon.smpp.redisoperations.SessionInfoRedisUpdate;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;

public class SessionCountUpdateTask
        extends
        TimerTask
{

    private static final Log log                    = LogFactory.getLog(SessionCountUpdateTask.class);

    private final Timer      timer                  = new Timer("SessionCountUpdateTask-Thread");
    private int              sessionCountUpdateTime = 2000;

    public SessionCountUpdateTask()
    {

        try
        {
            sessionCountUpdateTime = 10000;
            timer.scheduleAtFixedRate(this, sessionCountUpdateTime + 1000, sessionCountUpdateTime + 1000);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {

        try
        {
            if (log.isInfoEnabled())
                log.info("going to update session count...");
            if (log.isDebugEnabled())
                log.debug("session update time=" + sessionCountUpdateTime);

            final Map<String, SessionRoundRobin> lSessionCounts = ItextosSessionManager.getInstance().getRxTrxSessionCounts();
            final String                         instanceId     = SmppProperties.getInstance().getInstanceId();

            for (final Entry<String, SessionRoundRobin> entry : lSessionCounts.entrySet())
            {
                final SessionRoundRobin lssr = entry.getValue();
                SessionInfoRedisUpdate.setBindCount(String.valueOf(lssr.getSessionHandlers().get(0).getClientId()), instanceId, true, lssr.getHandlersCount());
            }
            if (log.isInfoEnabled())
                log.info("session/s count updated successful ");
        }
        catch (final Exception unexpected)
        {
            log.error("Problem updating session count due to...", unexpected);
        }
    }

}
