package com.itextos.beacon.platform.sbcv;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.Component;

public class StartApplication
{

    private static final Log       log            = LogFactory.getLog(StartApplication.class);
    private static final Component THIS_COMPONENT = Component.SBCV;

    public static void main(
            String[] args)
    {
        if (log.isDebugEnabled())
            log.debug("Starting the application " + THIS_COMPONENT);

        try
        {
            final ProcessorInfo lProcessor = new ProcessorInfo(THIS_COMPONENT);
            lProcessor.process();
        }
        catch (final Exception e)
        {
            log.error("Exception while starting component '" + THIS_COMPONENT + "'", e);
            System.exit(-1);
        }
    }

}
