package com.itextos.beacon.platform.clienthandovert2tb;

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

        try
        {
            if (log.isDebugEnabled())
                log.debug("Starting the application " + Component.T2DB_CLIENT_HANDOVER_RETRY_DATA);

            final ProcessorInfo lChRetryData = new ProcessorInfo(Component.T2DB_CLIENT_HANDOVER_RETRY_DATA, false);
            lChRetryData.process();

            if (log.isDebugEnabled())
                log.debug("Starting the application " + Component.T2DB_CLIENT_HANDOVER_LOG);

            final ProcessorInfo lChRetryLog = new ProcessorInfo(Component.T2DB_CLIENT_HANDOVER_LOG);
            lChRetryLog.process();

            if (log.isDebugEnabled())
                log.debug("Starting the application " + Component.T2DB_CLIENT_HANDOVER_MASTER_LOG);

            final ProcessorInfo lChRetryMasterData = new ProcessorInfo(Component.T2DB_CLIENT_HANDOVER_MASTER_LOG, false);
            lChRetryMasterData.process();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while starting Client Handover T2DB component ..", e);
            System.exit(-1);
        }
    }

}
