package com.itextos.beacon.platform.dltvc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.platform.templatefinder.data.DltTemplatesDataLoader;

public class StartApplication
{

    private static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {
        if (log.isDebugEnabled())
            log.debug("Starting the application " + Component.DLTVC);

        try
        {
            loadInmem();

            final ProcessorInfo lProcessor = new ProcessorInfo(Component.DLTVC);
            lProcessor.process();
        }
        catch (final Exception e)
        {
            log.error("Exception while starting the component " + Component.DLTVC, e);
            System.exit(-1);
        }
    }

    private static void loadInmem()
    {
        PrometheusMetrics.registerGenericError();
        DltTemplatesDataLoader.getInstance();
    }

}
