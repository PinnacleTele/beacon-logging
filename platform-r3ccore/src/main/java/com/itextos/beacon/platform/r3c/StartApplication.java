package com.itextos.beacon.platform.r3c;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.Component;

public class StartApplication
{

    private static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {
        if (log.isDebugEnabled())
            log.debug("Starting the application " + Component.R3C);

        try
        {
            final ProcessorInfo lProcessor = new ProcessorInfo(Component.R3C);
            lProcessor.process();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while stating the R3 Component..", e);
            System.exit(-1);
        }
    }

}