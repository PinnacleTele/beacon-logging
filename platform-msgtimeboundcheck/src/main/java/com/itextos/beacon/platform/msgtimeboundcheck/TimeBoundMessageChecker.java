package com.itextos.beacon.platform.msgtimeboundcheck;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.customfeatures.InmemCustomFeatures;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class TimeBoundMessageChecker
{

    private static Log log = LogFactory.getLog(TimeBoundMessageChecker.class);

    static
    {
        final int lRedispoolcnt = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.TIMEBOUND_CHK);

        if (log.isDebugEnabled())
            log.debug("Total number of redis configured : " + lRedispoolcnt);

        for (int index = 1; index <= lRedispoolcnt; index++)
        {
            if (log.isDebugEnabled())
                log.debug("Redis index pool : " + index);

            new TimeBoundMessageReaper(index);
        }
    }

    public static boolean increaseMsgCounter(
            String aClientId,
            long aMobileNumber,
            int aIntervalTime,
            int aMaxCount)
    {
        if (log.isDebugEnabled())
            log.debug("Checking for the time bound constraints enabled for client : '" + aClientId + "' FeatureEnabled : 'true'");
        if (log.isDebugEnabled())
            log.debug("Time bound details from database. Time bound : '" + aIntervalTime + "' Max Count : '" + aMaxCount + "'");

        return processIncMsgCounter(aClientId, aMobileNumber, aMaxCount, aIntervalTime);
    }

    private static boolean processIncMsgCounter(
            String aClientId,
            long aMobileNumber,
            int aMaxMsgCount,
            int aTimeDurationInSec)
    {
        final int lRedisCnt    = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.TIMEBOUND_CHK);
        final int lModByMobile = (int) (aMobileNumber % lRedisCnt);

        try (
                Jedis lJedisCon = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.TIMEBOUND_CHK, (lModByMobile + 1));)
        {
            final String lRedisOuterKey   = TimeBoundConstants.REDIS_KEY_TIMECHECK + aClientId;
            final String lRedisCounterKey = aMobileNumber + TimeBoundConstants.REDIS_KEY_COUNT;

            final Long   lIncrementCount  = lJedisCon.hincrBy(lRedisOuterKey, lRedisCounterKey, 1);

            if (log.isDebugEnabled())
                log.debug("Redis value after increment for outerkey :'" + lRedisOuterKey + "' and counterKey : '" + lRedisCounterKey + "' is : '" + lIncrementCount + "'");

            final long lRedisCount = lIncrementCount == null ? -1 : lIncrementCount;

            if (lRedisCount == -1)
            {
                log.error("Something went wrong here. Please check for outerKey :'" + lRedisOuterKey + "' and counterKey :'" + lRedisCounterKey + "'");
                return true;
            }

            if (lRedisCount == 1)
                processInsertOrUpdateExpiryTime(lJedisCon, aClientId, aMobileNumber, aTimeDurationInSec);
            else
                if (aMaxMsgCount < lRedisCount)
                {
                    final String lRedisExpiryKey  = aMobileNumber + TimeBoundConstants.REDIS_KEY_EXP_TIME;
                    final String lRedisExpiryTime = lJedisCon.hget(lRedisOuterKey, lRedisExpiryKey);
                    final Date   lTempExpDt       = DateTimeUtility.getDateFromString(lRedisExpiryTime, DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS);
                    final long   lFinalExpTime    = lTempExpDt.getTime();

                    if (log.isInfoEnabled())
                        log.info("Since the message count is more then the max value getting the expiry time and check the time bound '" + new Date(lFinalExpTime) + "' - long value : '"
                                + lRedisExpiryTime + "'");

                    if (lFinalExpTime > System.currentTimeMillis())
                        return false;

                    if (log.isInfoEnabled())
                        log.info("This can occur in case for some reason, if the cleanup pollar missed to remove this existing entry. In this case we need to update the new time for this key");

                    processInsertOrUpdateExpiryTime(lJedisCon, aClientId, aMobileNumber, aTimeDurationInSec, true, lRedisOuterKey, lRedisCounterKey);
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
            long aMobileNumber,
            int aTimeDurationInSec)
    {
        processInsertOrUpdateExpiryTime(aCon, aClientId, aMobileNumber, aTimeDurationInSec, false, null, null);
    }

    private static void processInsertOrUpdateExpiryTime(
            Jedis aCon,
            String aClientId,
            long aMobileNumber,
            int aTimeDurationInSec,
            boolean aResetValue,
            String aRedisOuterKey,
            String aRedisCounterKey)
    {

        try (
                Pipeline lRedisPipe = aCon.pipelined();)
        {
            final long   lExpiryTime    = getExpiryTime(aTimeDurationInSec);
            final String lStrExpiryTime = DateTimeUtility.getFormattedDateTime(lExpiryTime, DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS);
            String       lOuterKey      = TimeBoundConstants.REDIS_KEY_TIMECHECK + aClientId;
            final String lExpiryKey     = aMobileNumber + TimeBoundConstants.REDIS_KEY_EXP_TIME;

            if (log.isDebugEnabled())
                log.debug("Expiry time to set for client :'" + aClientId + "' and mobile : '" + aMobileNumber + "' is : '" + lStrExpiryTime + "'");

            lRedisPipe.hset(lOuterKey, lExpiryKey, lStrExpiryTime);
            lOuterKey = TimeBoundConstants.REDIS_KEY_TIMECHECK_EXP + lStrExpiryTime;
            lRedisPipe.lpush(lOuterKey, aClientId + TimeBoundConstants.SEPERATOR + aMobileNumber);

            if (aResetValue)
                // Reset the count to 1.
                lRedisPipe.hset(aRedisOuterKey, aRedisCounterKey, "1");

            lRedisPipe.sync();

            if (log.isDebugEnabled())
                log.debug("Expiry time for client :'" + aClientId + "' and mobile :'" + aMobileNumber + "' are insert/updated to redis with expiry time :'" + lStrExpiryTime + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while insert / update the redis info..", e);
        }
    }

    private static long getExpiryTime(
            long aTimeDurationInSec)
    {
        return ((aTimeDurationInSec * 1000) + System.currentTimeMillis());
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    public static String getCutomFeatureValue(
            String aClientId,
            CustomFeatures aCustomFeature)
    {
        final InmemCustomFeatures lCustomFeatures = (InmemCustomFeatures) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_FEATURES);
        return lCustomFeatures.getValueOfCustomFeature(aClientId, aCustomFeature.getKey());
    }

}