package com.itextos.beacon.platform.t2e;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class StartApplication
{

    private static final Log    log          = LogFactory.getLog(StartApplication.class);

    private static final String DLR_MT       = "dlrmt";
    private static final String DLR_DN       = "dlrdn";
    private static final String AGING        = "age";
    private static final String AGING_UPDATE = "ageupdate";

    public static void main(
            String[] args)
    {

        try
        {
            final String t2eProcesses = CommonUtility.nullCheck(System.getProperty("start.t2e.process"));

            log.fatal("Startup arguments for T2E processes '" + t2eProcesses + "'");

            if (t2eProcesses.isBlank() || t2eProcesses.contains(DLR_MT))
            {
                if (log.isDebugEnabled())
                    log.debug("Starting the application " + Component.DLRQMT);

                final ProcessorInfo lSubDlrqProcessor = new ProcessorInfo(Component.DLRQMT);
                lSubDlrqProcessor.process();
            }

            if (t2eProcesses.isBlank() || t2eProcesses.contains(DLR_DN))
            {
                if (log.isDebugEnabled())
                    log.debug("Starting the application " + Component.DLRQDN);

                final ProcessorInfo lDnDlrqProcessor = new ProcessorInfo(Component.DLRQDN, false);
                lDnDlrqProcessor.process();
            }

            if (t2eProcesses.isBlank() || t2eProcesses.contains(AGING))
            {
                if (log.isDebugEnabled())
                    log.debug("Starting the application " + Component.AGIN);

                final ProcessorInfo lAgingInsertProcessor = new ProcessorInfo(Component.AGIN, false);
                lAgingInsertProcessor.process();
            }

            if (t2eProcesses.isBlank() || t2eProcesses.contains(AGING_UPDATE))
            {
                if (log.isDebugEnabled())
                    log.debug("Starting the application " + Component.UADN);

                final ProcessorInfo lAgingUpdateProcessor = new ProcessorInfo(Component.UADN, false);
                lAgingUpdateProcessor.process();
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while starting the T2E application", e);
            System.exit(-1);
        }
    }

}
