package com.itextos.beacon.platform.ic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.platform.ic.process.ICProcessor;
import com.itextos.beacon.smslog.DebugLog;

public class StartApplication
{

    private static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {
    	System.out.println("Starting ....");
        if (log.isDebugEnabled())
            log.debug("Starting the application " + Component.IC);

      
        try
        {
       // 	com.itextos.beacon.platform.sbc.StartApplication.startInutialParam();

        	ICProcessor.SEGMENT=System.getenv("NODE_HOSTNAME");
        	
        	if(ICProcessor.SEGMENT==null) {
        		
            	ICProcessor.SEGMENT=System.getenv("segment");

        	}
        	
        	if(ICProcessor.SEGMENT!=null) {
        		
        		if(ICProcessor.SEGMENT.trim().length()>25) {
        			
        			ICProcessor.SEGMENT=ICProcessor.SEGMENT.substring(0, 25);
        		}
        	}
        	DebugLog.log("segment : "+ICProcessor.SEGMENT);
        	
            final ProcessorInfo lProcessor = new ProcessorInfo(Component.IC);
            lProcessor.process();
        }
        catch (final Exception e)
        {
            log.error("Exception while starting the Interface Consumer.", e);
            DebugLog.log("Exception while starting the Interface Consumer.");
            DebugLog.log(ErrorMessage.getStackTraceAsString(e));
            System.exit(-1);
        }
        
       
    }

}