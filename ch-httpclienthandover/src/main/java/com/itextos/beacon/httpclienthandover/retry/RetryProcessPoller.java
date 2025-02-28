package com.itextos.beacon.httpclienthandover.retry;

import java.util.List;
import java.util.stream.Collectors;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler2;
import com.itextos.beacon.httpclienthandover.utils.ClientHandoverConstatnts;

public class RetryProcessPoller
{

    // This list will only have clientID~mid, because we have to fetch customer
    // configuration from DB in-case if configuration is changed
    public RetryProcessPoller(
            boolean aIsCustSpecific,
            String aCustID)
    {
        final String           currentTime = DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS);
        final RedisRetryReaper reaper      = new RedisRetryReaper(currentTime, aIsCustSpecific, aCustID);
        
        ExecutorSheduler2.getInstance().addTask(reaper,"Retry-Process-Poller - " + (aIsCustSpecific ? aCustID : "Default"));

        processInProcessMessage(aIsCustSpecific, aCustID);
        processPastMessages(currentTime, aIsCustSpecific, aCustID);
    }

    private static void processInProcessMessage(
            boolean aIsCustSpecific,
            String aCustID)
    {
        long i          = 0;
        long fetchCount = RedisHelper.MAX_COUNT + i;

        while (true)
        {
            final List<String> data = RedisHelper.getInprocessDataFromRedis(RedisHelper.PARENT_KEY + ":" + RedisHelper.TO_PROCESSING + (aIsCustSpecific ? ":" + aCustID : ""), i, fetchCount);
            fetchCount = fetchCount + i;

            i          = i + RedisHelper.MAX_COUNT + 1;

            if ((data == null) || data.isEmpty())
                break;
            final List<String> expiredMessages = RetryUtils.getExpiredMessages(data);
            data.removeAll(expiredMessages);

            if (aIsCustSpecific)
            {
                RetryDataHolder.getInstance().addInprocessMessages(data, aCustID);
                RetryDataHolder.getInstance().addExpiredMessages(expiredMessages, aCustID);
            }
            else
            {
                RetryDataHolder.getInstance().addInprocessMessages(data, ClientHandoverConstatnts.DEFAULT_KEY);
                RetryDataHolder.getInstance().addExpiredMessages(expiredMessages, ClientHandoverConstatnts.DEFAULT_KEY);
            }
        }
    }

    private static void processPastMessages(
            String aCurrentTime,
            boolean aIsCustSpecific,
            String aCustID)
    {
        final List<String> keyList    = aIsCustSpecific ? RedisHelper.getAllKeys(aCustID) : RedisHelper.getAllKeys();

        final List<String> filterList = keyList.stream().filter(key -> {
                                          if (key.contains(RedisHelper.TO_PROCESSING))
                                              return false;

                                          final String[] keySplit = key.split(":");

                                          return (Long.parseLong(keySplit[1]) < Long.parseLong(aCurrentTime));
                                      }).collect(Collectors.toList());

        processData(filterList, aIsCustSpecific, aCustID);
    }

    private static void processData(
            List<String> filterList,
            boolean aIsCustSpecific,
            String aCustID)
    {
        for (final String filterKeys : filterList)
            while (true)
            {
                final String[]     keys = filterKeys.split(":");

                final List<String> data = RedisHelper.getPastData(keys[1], keys.length > 2 ? keys[2] : null);

                if ((data == null) || data.isEmpty())
                    break;

                final List<String> expiredMessages = RetryUtils.getExpiredMessages(data);
                data.removeAll(expiredMessages);

                if (aIsCustSpecific)
                {
                    RetryDataHolder.getInstance().addInprocessMessages(data, aCustID);
                    RetryDataHolder.getInstance().addExpiredMessages(expiredMessages, aCustID);
                }
                else
                {
                    RetryDataHolder.getInstance().addInprocessMessages(data, ClientHandoverConstatnts.DEFAULT_KEY);
                    RetryDataHolder.getInstance().addExpiredMessages(expiredMessages, ClientHandoverConstatnts.DEFAULT_KEY);
                }
            }
    }

}
