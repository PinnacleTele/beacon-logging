package com.itextos.beacon.httpclienthandover;

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
            final ProcessorInfo lDnProcessor = new ProcessorInfo(Component.HTTP_DLR);
            lDnProcessor.process();
        }
        catch (final Exception e)
        {
            log.error("Exception while starting the HTTP DLR application.", e);
        }
    }

}