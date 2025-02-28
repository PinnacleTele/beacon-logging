package com.itextos.beacon.commonlib.dnddataloader.csv;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.dnddataloader.common.InMemoryDataHolder;

class CsvFileReader
        implements
        Runnable
{

    private static final Log log         = LogFactory.getLog(CsvFileReader.class);

    private boolean          canContinue = true;
    private boolean          stopped     = false;

    @Override
    public void run()
    {
        int noDataCount = 0;

        while (canContinue)
        {
            final File nextFile = InMemoryDataHolder.getInstance().getNextFileToRead();

            if (nextFile == null)
            {
                if (log.isDebugEnabled())
                    log.debug("No file to read now");
                noDataCount++;

                if (noDataCount > 10)
                {
                    canContinue = false;
                    stopped     = true;
                    log.error("No File found for last 10 iterations. Stopping reader.");
                }
                else
                    sleepForAWhile(1000);
            }
            else
            {
                noDataCount = 0;

                final CsvFileProcessor lCsvFileProcessor = new CsvFileProcessor(nextFile);
                lCsvFileProcessor.process();

                sleepForAWhile(100);
            }
        }
    }

    boolean isStopped()
    {
        return stopped;
    }

    private static void sleepForAWhile(
            int aI)
    {

        try
        {
            Thread.sleep(aI);
        }
        catch (final InterruptedException e)
        {}
    }

}