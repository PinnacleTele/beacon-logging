package com.itextos.beacon.commonlib.urlhitterthread;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CountPrinter
        implements
        Runnable
{

    private static final Log                 log         = LogFactory.getLog(CountPrinter.class);

    private final DecimalFormat              df          = new DecimalFormat("    0");
    private final Map<Integer, HitterThread> allThreads;
    private final long                       startTime;
    private boolean                          canContinue = true;

    public CountPrinter(
            Map<Integer, HitterThread> aAllHitterThreads)
    {
        allThreads = aAllHitterThreads;
        startTime  = System.currentTimeMillis();
    }

    @Override
    public void run()
    {
        final int maxThreads   = PropertyFileReader.getInstance().getNoOfThreads();
        final int maxIteration = PropertyFileReader.getInstance().getMessagesPerThread();

        while (canContinue)
        {
            boolean canBreak = true;

            for (int index = 1; index <= maxThreads; index++)
            {
                final HitterThread hitterThread = allThreads.get(index);
                log.debug("Thread Index : " + df.format(index) + "\t" + df.format(hitterThread.getAttemptCount()) + "\t" + df.format(hitterThread.getSuccessCount()) + "\t"
                        + df.format(hitterThread.getFailuerCount()) + "\t" + df.format(hitterThread.getExceptionCount()));

                canBreak = canBreak && (maxIteration == hitterThread.getAttemptCount())
                        && (maxIteration == (hitterThread.getSuccessCount() + hitterThread.getFailuerCount() + hitterThread.getExceptionCount()));
            }

            if (canBreak)
                break;

            try
            {
                Thread.sleep(1 * 1000L);
            }
            catch (final InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        final long endTime        = System.currentTimeMillis();

        long       attemptCount   = 0;
        long       successCount   = 0;
        long       failureCount   = 0;
        long       exceptionCount = 0;

        for (int index = 1; index <= maxThreads; index++)
        {
            final HitterThread hitterThread = allThreads.get(index);
            attemptCount   += hitterThread.getAttemptCount();
            successCount   += hitterThread.getSuccessCount();
            failureCount   += hitterThread.getFailuerCount();
            exceptionCount += hitterThread.getExceptionCount();
        }

        log.error("Application start time : '" + new Date(startTime) + "'");
        log.error("Application end   time : '" + new Date(endTime) + "'");
        log.error("Total Time Taken       : '" + ((endTime - startTime) / 1000) + "' seconds");

        log.error("Total Attempted : " + attemptCount);
        log.error("Total Success   : " + successCount);
        log.error("Total Failure   : " + failureCount);
        log.error("Total Exception : " + exceptionCount);
    }

    public void stopMe()
    {
        canContinue = false;
    }

}