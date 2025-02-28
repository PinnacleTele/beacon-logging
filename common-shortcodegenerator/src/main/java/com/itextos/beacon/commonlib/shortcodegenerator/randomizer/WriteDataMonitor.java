package com.itextos.beacon.commonlib.shortcodegenerator.randomizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itextos.beacon.commonlib.shortcodegenerator.randomizer.util.PropertyReader;

public class WriteDataMonitor
        implements
        Runnable
{

    private static final Logger log         = LogManager.getLogger(WriteDataMonitor.class);
    private boolean             canContinue = true;
    private boolean             stopped     = false;

    @Override
    public void run()
    {
        int count = 0;

        while (true)
        {
            final int lWritingDataSize = InformationHolder.getInstance().getWritingDataSize();

            if (!canContinue)
            {
                count++;
                log.debug("Stop requested. count " + count + " Size in Writting data " + lWritingDataSize);
            }

            if (lWritingDataSize < PropertyReader.getInstance().getMaxRecordsPerFile())
            {
                // log.debug("Writting data size " + lWritingDataSize);

                try
                {
                    Thread.sleep(10);
                }
                catch (final InterruptedException e)
                {}

                if (!canContinue)
                {
                    log.debug("Hope all the messages has been processed and stopping this");
                    stopped = true;
                    break;
                }
            }
            else
                InformationHolder.getInstance().checkAndWriteToFile(false);
        }
    }

    public void stopMe()
    {
        canContinue = false;
    }

    public boolean isStopped()
    {
        return stopped;
    }

}