package com.itextos.beacon.platform.dnaging;

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
                log.debug("Starting the application " + Component.ADNP);

            final ProcessorInfo lAgingDnProcessor = new ProcessorInfo(Component.ADNP, false);
            lAgingDnProcessor.process();

         
        }catch (final Exception e)
        {
            log.error("Exception occer while processing the DLR Services.. Hence stop the service..", e);
            System.exit(-1);
        }
    }

}
