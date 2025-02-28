package com.itextos.beacon.platform.walletreminder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StartApplication
{

    private static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
            throws Exception
    {
        // final String fileNAme = "C:\\Users\\KS\\Desktop\\as.html";
        // final Path path = Path.of(fileNAme);
        // Files.deleteIfExists(path);
        final SchedulerProcess lSchedulerProcess = new SchedulerProcess();
        lSchedulerProcess.start();
        // DataCollector.getInstance().getData();
        // final OverallSummary lDailySummary = new OverallSummary();
        // final String summary = lDailySummary.getSummary();
        // System.out.println(summary.length());
        // System.out.println(summary);
        //
        // Files.writeString(path, summary, StandardOpenOption.CREATE_NEW);

        // if (log.isDebugEnabled())
        // log.debug(summary);
    }

}