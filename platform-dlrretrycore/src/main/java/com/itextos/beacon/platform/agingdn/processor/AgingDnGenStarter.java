package com.itextos.beacon.platform.agingdn.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AgingDnGenStarter
{

    private static Log log = LogFactory.getLog(AgingDnGenStarter.class);

    public static void start()
    {

        try
        {
            AgingDlrGenHolder.getInstance();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while starting the SBP...", e);
        }
    }

    public static void main(
            String[] args)
    {
        AgingDnGenStarter.start();
    }

}
