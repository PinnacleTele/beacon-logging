package com.itextos.beacon.platform.walletreminder.quartz.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.walletreminder.data.summary.DailySummary;

public class DailyBalanceJob
        extends
        WalletBalanceJob
{

    private static final Log log = LogFactory.getLog(DailyBalanceJob.class);

    @Override
    void process(
            JobExecutionContext aContext)
    {

        try
        {
            sendAdminEmail(new DailySummary());
        }
        catch (final ItextosException e)
        {
            log.error("Exception while getting in summary and processing throug mail.", e);
        }
    }

    @Override
    void printNextFireTime(
            JobExecutionContext aContext)
    {
        log.fatal("Next scheduled time: " + DateTimeUtility.getFormattedDateTime(aContext.getNextFireTime(), DateTimeFormat.DEFAULT));
    }

}