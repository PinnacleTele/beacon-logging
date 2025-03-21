package com.itextos.beacon.subk2e;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;

public class StartApplication
{

    private static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Starting the application " + Component.K2E_SUBMISSION);

            final ProcessorInfo lSubProcessor = new ProcessorInfo(Component.K2E_SUBMISSION);
            lSubProcessor.process();

            // if (log.isDebugEnabled())
            // log.debug("Starting the application " + Component.T2DB_FULL_MESSAGE);
            //
            // final ProcessorInfo lFullMsgProcessor = new
            // ProcessorInfo(Component.T2DB_FULL_MESSAGE, false);
            // lFullMsgProcessor.process();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while starting Platform Biller Submission component ..", e);
            System.exit(-1);
        }
    }

}
