package com.itextos.beacon.platform.aysnprocessor;


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
    	 startConsumers();
    }
    
    

    private static void startConsumers()
    {

       
            if (log.isDebugEnabled())
                log.debug("**************Kafka consumer started*********************");

            try
            {
                final ProcessorInfo lProcessor = new ProcessorInfo(Component.INTERFACE_ASYNC_PROCESS, true);
                lProcessor.process();
            }
            catch (final Exception e)
            {
                log.error("Unable to start the Kafka Async Consumer.., Hence Stoping the Instance..", e);
                System.exit(-1);
            }

       
    }
}