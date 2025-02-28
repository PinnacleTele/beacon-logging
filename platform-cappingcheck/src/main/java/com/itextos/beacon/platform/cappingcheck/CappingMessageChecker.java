package com.itextos.beacon.platform.cappingcheck;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class CappingMessageChecker
{

    private static Log log = LogFactory.getLog(CappingMessageChecker.class);

    static
    {
        final int lRedispoolcnt = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.CAPPING_CHK);

        if (log.isDebugEnabled())
            log.debug("Total number of redis configured : " + lRedispoolcnt);

        for (int index = 1; index <= lRedispoolcnt; index++)
        {
            if (log.isDebugEnabled())
                log.debug("Redis index pool : " + index);
            new CappingMessageReaper(index);
        }
    }

    public static boolean increaseMsgCounter(
            String aClientId,
            CappingIntervalType aIntervalType,
            int aInterval,
            int aCappingMaxCount,
            int aIncrementCount)
    {
        if (log.isDebugEnabled())
            log.debug("Capping details from database. Capping Duration Type: '" + aIntervalType + "' Capping Max Count : '" + aCappingMaxCount + "' Increment Count :'" + aIncrementCount + "'");

        try (
                Jedis lJedisCon = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.CAPPING_CHK, 1);)
        {
            final String lRedisOuterKey   = CappingConstants.REDIS_KEY_TIMECHECK + aClientId;
            final String lRedisCounterKey = CappingConstants.REDIS_KEY_COUNT;

            final Long   lIncrementCount  = lJedisCon.hincrBy(lRedisOuterKey, lRedisCounterKey, aIncrementCount);

            if (log.isDebugEnabled())
                log.debug("Redis value after increment for outerkey :'" + lRedisOuterKey + "' and counterKey : '" + lRedisCounterKey + "' is : '" + lIncrementCount + "'");

            final long lRedisCount = lIncrementCount == null ? -1 : lIncrementCount;

            if (lRedisCount == -1)
            {
                log.error("Something went wrong here. Please check for outerKey :'" + lRedisOuterKey + "' and counterKey :'" + lRedisCounterKey + "'");
                return true;
            }

            if (lRedisCount == aIncrementCount)
                processInsertOrUpdateExpiryTime(lJedisCon, aClientId, aIntervalType, aInterval);
            else
                if (aCappingMaxCount < lRedisCount)
                {
                    final String lRedisExpiryTime = lJedisCon.hget(lRedisOuterKey, CappingConstants.REDIS_KEY_EXP_TIME);
                    final Date   lTempExpDt       = DateTimeUtility.getDateFromString(lRedisExpiryTime, DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS);
                    final long   lFinalExpTime    = lTempExpDt.getTime();

                    if (log.isInfoEnabled())
                        log.info("Since the message count is more then the max value getting the expiry time and check the time bound '" + new Date(lFinalExpTime) + "' - long value : '"
                                + lRedisExpiryTime + "'");

                    if (lFinalExpTime > System.currentTimeMillis())
                        return false;

                    if (log.isInfoEnabled())
                        log.info("This can occur in case for some reason, if the cleanup pollar missed to remove this existing entry. In this case we need to update the new time for this key");

                    processInsertOrUpdateExpiryTime(lJedisCon, aClientId, aIntervalType, aInterval, true, lRedisOuterKey, lRedisCounterKey);
                }
        }
        catch (final Exception e)
        {
            log.error("Exception while increment the request counter ..", e);
        }

        return true;
    }

    private static void processInsertOrUpdateExpiryTime(
            Jedis aCon,
            String aClientId,
            CappingIntervalType aIntervalType,
            int aInterval)
    {
        processInsertOrUpdateExpiryTime(aCon, aClientId, aIntervalType, aInterval, false, null, null);
    }

    private static void processInsertOrUpdateExpiryTime(
            Jedis aCon,
            String aClientId,
            CappingIntervalType aIntervalType,
            int aInterval,
            boolean aResetValue,
            String aRedisOuterKey,
            String aRedisCounterKey)
    {

        try (
                Pipeline lRedisPipe = aCon.pipelined();)
        {
            final long   lExpiryTime    = getExpiryTime(aIntervalType, aInterval);
            final String lStrExpiryTime = DateTimeUtility.getFormattedDateTime(lExpiryTime, DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS);
            String       lOuterKey      = CappingConstants.REDIS_KEY_TIMECHECK + aClientId;
            final String lExpiryKey     = CappingConstants.REDIS_KEY_EXP_TIME;

            if (log.isDebugEnabled())
                log.debug("Expiry time to set for client :'" + aClientId + "' is : '" + lStrExpiryTime + "'");

            lRedisPipe.hset(lOuterKey, lExpiryKey, lStrExpiryTime);
            lOuterKey = CappingConstants.REDIS_KEY_TIMECHECK_EXP + lStrExpiryTime;
            lRedisPipe.lpush(lOuterKey, aClientId);

            if (aResetValue)
                // Reset the count to 1.
                lRedisPipe.hset(aRedisOuterKey, aRedisCounterKey, "1");

            lRedisPipe.sync();

            if (log.isDebugEnabled())
                log.debug("Expiry time for client :'" + aClientId + "' are insert/updated to redis with expiry time :'" + lStrExpiryTime + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while insert / update the redis info..", e);
        }
    }

    private static long getExpiryTime(
            CappingIntervalType aIntervalType,
            int aInterval)
    {
        final Calendar lCalendarObj = Calendar.getInstance();
        lCalendarObj.setLenient(false);

        if (aIntervalType == null)
            aIntervalType = CappingIntervalType.NONE;

        switch (aIntervalType)
        {
            case MINUTE:
                lCalendarObj.add(Calendar.MINUTE, aInterval);
                lCalendarObj.add(Calendar.MINUTE, -1);
                lCalendarObj.set(Calendar.SECOND, 59);
                break;

            case HOUR:
                lCalendarObj.add(Calendar.HOUR, aInterval);
                lCalendarObj.add(Calendar.HOUR, -1);
                lCalendarObj.set(Calendar.MINUTE, 59);
                lCalendarObj.set(Calendar.SECOND, 59);
                break;

            case DATE:
                lCalendarObj.add(Calendar.DATE, aInterval);
                lCalendarObj.add(Calendar.DATE, -1);
                lCalendarObj.set(Calendar.HOUR_OF_DAY, 23);
                lCalendarObj.set(Calendar.MINUTE, 59);
                lCalendarObj.set(Calendar.SECOND, 59);
                break;

            case WEEK:
                // This may not be a proper option. The week starts from the date when the first
                // message comes in.
                // It is not the Calendar weeks like
                // Calendar.WEEK_OF_MONTH or Calendar.WEEK_OF_YEAR .
                lCalendarObj.add(Calendar.DATE, aInterval * 7);
                lCalendarObj.add(Calendar.DATE, -1);
                lCalendarObj.set(Calendar.HOUR_OF_DAY, 23);
                lCalendarObj.set(Calendar.MINUTE, 59);
                lCalendarObj.set(Calendar.SECOND, 59);
                break;

            case MONTH:
                lCalendarObj.add(Calendar.MONTH, aInterval);
                lCalendarObj.set(Calendar.DATE, 1);
                lCalendarObj.add(Calendar.DATE, -1);
                lCalendarObj.set(Calendar.HOUR_OF_DAY, 23);
                lCalendarObj.set(Calendar.MINUTE, 59);
                lCalendarObj.set(Calendar.SECOND, 59);
                break;

            case YEAR:
                lCalendarObj.add(Calendar.YEAR, aInterval);
                lCalendarObj.set(Calendar.MONTH, 0);
                lCalendarObj.set(Calendar.DATE, 1);
                lCalendarObj.add(Calendar.DATE, -1);
                lCalendarObj.set(Calendar.HOUR_OF_DAY, 23);
                lCalendarObj.set(Calendar.MINUTE, 59);
                lCalendarObj.set(Calendar.SECOND, 59);
                break;

            case NONE:
            default:
                log.error("The capping interval was not specified properly.");
                break;
        }
        return lCalendarObj.getTimeInMillis();
    }

    public static boolean doCappingCheck(
            String aClientId,
            long aMaxMsgCount,
            int aMsgCount)
    {

        try (
                Jedis lJedisCon = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.CAPPING_CHK, 1);)
        {
            final String lRedisOuterKey   = CappingConstants.REDIS_KEY_TIMECHECK + aClientId;
            final String lRedisCounterKey = CappingConstants.REDIS_KEY_COUNT;

            final String lResponseCount   = lJedisCon.hget(lRedisOuterKey, lRedisCounterKey);

            final long   lRedisCount      = lResponseCount == null ? 0 : CommonUtility.getLong(lResponseCount);
            final long   lCurrentCount    = (aMsgCount + lRedisCount);

            if (aMaxMsgCount >= lCurrentCount)
                return true;
        }

        return false;
    }

}