package com.itextos.beacon.commonlib.dnddataloader.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InmemoryDataPrinter
        implements
        Runnable
{

    private static final Log log = LogFactory.getLog(InmemoryDataPrinter.class);

    @Override
    public void run()
    {
        log.debug("Read completed " + InMemoryDataHolder.getInstance().getFileReadCompleted() + " File Reading " + InMemoryDataHolder.getInstance().getFilesToRead() + " Add update count "
                + InMemoryDataHolder.getInstance().getDataToAddUpdate() + " Delete count " + InMemoryDataHolder.getInstance().getDataToDelete());

        try
        {
            Thread.sleep(10 * 1000L);
        }
        catch (final InterruptedException e)
        {}
    }

}