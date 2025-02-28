package com.itextos.beacon.platform.sbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.platform.sbc.data.InmemBlockoutQReaper;
import com.itextos.beacon.platform.sbc.data.InmemoryBlockoutReaper;
import com.itextos.beacon.platform.sbc.data.InmemoryScheduleReaper;
import com.itextos.beacon.smslog.DebugLog;

public class StartApplication
{

    private static final Log       log            = LogFactory.getLog(StartApplication.class);
    private static final Component THIS_COMPONENT = Component.SBC;

    public static void main(
            String[] args)
    {
        if (log.isDebugEnabled())
            log.debug("Starting the application " + THIS_COMPONENT);
        
        DebugLog.log("Starting the application " + THIS_COMPONENT);

        try
        {
        	com.itextos.beacon.platform.sbc.StartApplication.startInutialParam();

            final ProcessorInfo lProcessor = new ProcessorInfo(THIS_COMPONENT);
            lProcessor.process();
        }
        catch (final Exception e)
        {
            log.error("Exception while starting component '" + THIS_COMPONENT + "'", e);
            
            DebugLog.log("Exception while starting component '" + THIS_COMPONENT + "'");
            
            
            DebugLog.log(ErrorMessage.getStackTraceAsString(e));
            
            System.exit(-1);
        }
    }

    public static void startInutialParam()
    {
        if (log.isDebugEnabled())
            log.debug("Starting the application InmemReapers ...");

        InmemBlockoutQReaper.getInstance();
        InmemoryBlockoutReaper.getInstance();
        InmemoryScheduleReaper.getInstance();
    }

}
