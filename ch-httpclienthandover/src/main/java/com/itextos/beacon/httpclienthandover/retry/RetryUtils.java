package com.itextos.beacon.httpclienthandover.retry;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.httpclienthandover.data.ClientHandoverData;
import com.itextos.beacon.httpclienthandover.utils.ClientHandoverUtils;

public class RetryUtils
{

    private static final Log log                  = LogFactory.getLog(RetryUtils.class);

    private static final int RETRY_DISABLED       = 0;
    private static final int TIME_BASED           = 1;
    private static final int COUNT_BASED          = 2;
    private static final int TIME_AND_COUNT_BASED = 3;
    public static final int  DB_IN_QUERY_LIMIT    = 1000;

    private RetryUtils()
    {}

    public static boolean isExpired()
    {
        return false;
    }

    public static boolean checkIfExpired(
            String aRetryCount,
            String aLastRetryTime,
            ClientHandoverData clientConfiguration)
    {
        final int retryLogic = clientConfiguration.getRetryExpiryLogic();

        final int expiryTime = clientConfiguration.getExpiryTimeSeconds();

        switch (retryLogic)
        {
            case RETRY_DISABLED:
                return true;

            case TIME_BASED:
                return checkTimeBasedExpiry(expiryTime, aLastRetryTime);

            case COUNT_BASED:
                return checkCountBasedExpiry(aRetryCount, clientConfiguration);

            case TIME_AND_COUNT_BASED:
                return checkTimeAndCountBasedExpiry(expiryTime, aRetryCount, aLastRetryTime, clientConfiguration);

            default:
                break;
        }
        return false;
    }

    private static boolean checkCountBasedExpiry(
            String aRetryCount,
            ClientHandoverData aClientConfiguration)
    {
        final int maxRetryCount  = aClientConfiguration.getMaxRetryCount();
        final int attemptedCount = Integer.parseInt(aRetryCount);
        return attemptedCount >= maxRetryCount;
    }

    private static boolean checkTimeAndCountBasedExpiry(
            int aExpiryTime,
            String aRetryCount,
            String aLastRetryTime,
            ClientHandoverData aClientConfiguration)
    {
        final boolean isCountExpired = checkCountBasedExpiry(aRetryCount, aClientConfiguration);

        if (isCountExpired)
            return isCountExpired;
        return checkTimeBasedExpiry(aExpiryTime, aLastRetryTime);
    }

    private static boolean checkTimeBasedExpiry(
            int aExpiryTime,
            String aLastRetryTime)
    {
        long ctime = -1;

        try
        {
            ctime = Long.parseLong(aLastRetryTime);
        }
        catch (final Exception e)
        {
            ctime = System.currentTimeMillis();
        }

        final long diff            = System.currentTimeMillis() - ctime;

        final int  retryMaxTimeInt = aExpiryTime * 1000;
        return diff > retryMaxTimeInt;
    }

    public static List<String> getExpiredMessages(
            List<String> aMessageList)
    {
        final List<String> expiredMessages = new ArrayList<>();

        for (final String fromRedis : aMessageList)
        {
            final var    a              = fromRedis.split("~");
            final String clientId       = a[0];
            final String mid            = a[1];
            final String attemptedCount = a[2];
            final String lastRetryTime  = a[3];

            if (log.isDebugEnabled())
                log.debug("Retry Message | clientId: '" + clientId + "' | mid: '" + mid + "' Attempted Count: '" + attemptedCount + "' | Last Retry Time: '" + lastRetryTime + "'");
            final ClientHandoverData lClientHandoverData = ClientHandoverUtils.getClientHandoverData(clientId);
            final boolean            isExpired           = RetryUtils.checkIfExpired(attemptedCount, lastRetryTime, lClientHandoverData);

            if (log.isDebugEnabled())
                log.debug("Retry Message expired: '" + isExpired + "' | clientId: '" + clientId + "'");

            if (isExpired)
                expiredMessages.add(fromRedis);
        }
        return expiredMessages;
    }

}
