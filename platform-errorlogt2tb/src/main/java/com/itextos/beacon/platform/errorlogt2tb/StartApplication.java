package com.itextos.beacon.platform.errorlogt2tb;

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
            log.debug("Starting the application " + Component.T2DB_ERROR_LOG);

        try
        {
            final ProcessorInfo lErrorProcessor = new ProcessorInfo(Component.T2DB_ERROR_LOG);
            lErrorProcessor.process();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while starting Platform Biller Submission component ..", e);
            System.exit(-1);
        }
    }

}