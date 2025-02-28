package com.itextos.beacon.platform.dnsingle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.platform.dnpcore.inmem.NoPayloadRetryQReaper;
import com.itextos.beacon.platform.dnpcore.inmem.NoPayloadRetryUpdateQReaper;
import com.itextos.beacon.platform.dnpcore.poller.NoPayloadRetryPollerHolder;
import com.itextos.beacon.platform.singledn.redis.delete.SingleDnDataDelete;
import com.itextos.beacon.platform.singledn.redis.delete.SingleDnDuplicateCheckRemove;
import com.itextos.beacon.platform.singledn.redis.delete.SingleDnExpiryProcessor;
import com.itextos.beacon.platform.singledn.redis.delete.SingleDnOldDuplicateCheckRemove;

public class StartApplication
{

    public static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Starting the application " + Component.DNP);
            
            final String cluster=System.getProperty("cluster");
            
            if(cluster==null) {
            
            	if(System.getenv("cluster")!=null) {
            		System.setProperty("cluster", System.getenv("cluster"));
            	}
            }

            final String modvalue=System.getProperty("modvalue");
            
            if(modvalue==null) {
            	
            	System.setProperty("modvalue", System.getenv("modvalue"));

            }
            if (log.isDebugEnabled())
                log.debug("Starting the application " + Component.SDNP);

            final ProcessorInfo lSingleDnProcessor = new ProcessorInfo(Component.SDNP, false);
            lSingleDnProcessor.process();

          
      
            SingleDnExpiryProcessor.getInstance();
            SingleDnDataDelete.getInstance();
            SingleDnDuplicateCheckRemove.getInstance();
            SingleDnOldDuplicateCheckRemove.getInstance();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while processing the DLR Services.. Hence stop the service..", e);
            System.exit(-1);
        }
    }

}
