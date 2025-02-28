package com.itextos.beacon.httpclienthandover.retry;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.httpclienthandover.utils.ClientHandoverConstatnts;

public class RedisRetryReaper
        implements
        Runnable

{

    private static final Log log = LogFactory.getLog(RedisRetryReaper.class);

    private String           currentProcessTime;
    private final boolean    isCustomerSpecific;

    private final String     custId;

    private boolean          stopMe;

    public RedisRetryReaper(
            String aProcessStartedTime,
            boolean aIsCustSpecific,
            String aCustID)
    {
        currentProcessTime = aProcessStartedTime;
        isCustomerSpecific = aIsCustSpecific;
        custId             = aCustID;
    }

    public void processNow()
    {
        while (!stopMe)
            try
            {
                final List<String> dataToProcess = isCustomerSpecific ? RedisHelper.getUnprocessedData(custId, currentProcessTime) : RedisHelper.getUnprocessedData(currentProcessTime);
                if (log.isDebugEnabled())
                    log.debug("Retry poller data: '" + dataToProcess + "'| currentProcessTime: '" + currentProcessTime + "'");

                if ((dataToProcess == null) || dataToProcess.isEmpty())
                {
                    increaseTime();
                    continue;
                }

                if (log.isDebugEnabled())
                    log.debug("dataToProcess: '" + dataToProcess + "'");

                final List<String> expiredMessages = RetryUtils.getExpiredMessages(dataToProcess);
                dataToProcess.removeAll(expiredMessages);

                if (log.isDebugEnabled())
                    log.debug("After Removed Expired Message: '" + dataToProcess + "'");

                if (isCustomerSpecific)
                {
                    RetryDataHolder.getInstance().addInprocessMessages(dataToProcess, custId);
                    RetryDataHolder.getInstance().addExpiredMessages(expiredMessages, custId);
                }
                else
                {
                    RetryDataHolder.getInstance().addInprocessMessages(dataToProcess, ClientHandoverConstatnts.DEFAULT_KEY);
                    RetryDataHolder.getInstance().addExpiredMessages(expiredMessages, ClientHandoverConstatnts.DEFAULT_KEY);
                }
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
    }

    private void increaseTime()

    {
        final String   currentTime = DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS);
        final Date     currentDate = DateTimeUtility.getDateFromString(currentProcessTime, DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS);

        final Calendar cal         = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.add(Calendar.SECOND, 1);

        final String increasedTime = DateTimeUtility.getFormattedDateTime(cal.getTime(), DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS);
        if (log.isDebugEnabled())
            log.debug("currentTime : '" + Long.parseLong(currentTime) + "' || increasedTime: '" + Long.parseLong(increasedTime) + "'");

        if (Long.parseLong(currentTime) < Long.parseLong(increasedTime))
        {
            if (log.isDebugEnabled())
                log.debug("Retry sleeps foa second");

            CommonUtility.sleepForAWhile(1 * 1000L);
        }
        currentProcessTime = increasedTime;
    }

    public void stopMe()
    {
        stopMe = true;
    }

    @Override
    public void run()
    {
        processNow();
    }

}