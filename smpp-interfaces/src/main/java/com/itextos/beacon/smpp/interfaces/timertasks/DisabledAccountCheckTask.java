package com.itextos.beacon.smpp.interfaces.timertasks;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.smpp.interfaces.sessionhandlers.ItextosSessionManager;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;

public class DisabledAccountCheckTask
        extends
        TimerTask
{

    private static final Log logger = LogFactory.getLog(DisabledAccountCheckTask.class);
    private final Timer      timer  = new Timer("DisableAccountCheck-Thread");

    public DisabledAccountCheckTask()
    {
        final long iInterval = SmppProperties.getInstance().getDisabledAccountCheckSec() * 1000L;
        timer.schedule(this, iInterval, iInterval);
    }

    @Override
    public void run()
    {

        try
        {
            if (logger.isDebugEnabled())
                logger.debug("DisableAccountCheckTask invoked....");

            ItextosSessionManager.getInstance().checkDisabledAccounts();
        }
        catch (final Exception exp)
        {
            logger.error("Problem removing disabled account", exp);
        }
    }

    public void stopMe()
    {
        timer.cancel();
    }

}