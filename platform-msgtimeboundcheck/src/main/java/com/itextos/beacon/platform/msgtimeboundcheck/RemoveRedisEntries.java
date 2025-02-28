package com.itextos.beacon.platform.msgtimeboundcheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class RemoveRedisEntries
        implements
        Runnable
{

    private static Log        log = LogFactory.getLog(RemoveRedisEntries.class);
    private final int         mRedisId;
    private final Set<String> mKeys;
    private final long        mCompareTime;

    public RemoveRedisEntries(
            int aRedisId,
            Set<String> aKeys,
            long aCompareTime)
    {
        this.mRedisId     = aRedisId;
        this.mKeys        = aKeys;
        this.mCompareTime = aCompareTime;
    }

    @Override
    public void run()
    {
        if (log.isDebugEnabled())
            log.debug("Keys to be validated : '" + mKeys + "'");

        try (
                Jedis lJedisCon = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.TIMEBOUND_CHK, mRedisId);)
        {

            for (final String expKey : mKeys)
            {
                final String lExpiryTime     = expKey.substring(TimeBoundConstants.REDIS_KEY_TIMECHECK_EXP.length());
                final long   lExpTimeFormKey = CommonUtility.getLong(lExpiryTime);

                if (log.isDebugEnabled())
                    log.debug("Key Time : '" + lExpTimeFormKey + "' Time to Compare : '" + mCompareTime + "'");

                if (mCompareTime >= lExpTimeFormKey)
                    removeAllExpiredData(lJedisCon, expKey, mCompareTime);
            }
        }
    }

    private static void removeAllExpiredData(
            Jedis aJedisCon,
            String aExpiryKey,
            long aCompareTime)
    {
        final String lCompareTime = DateTimeUtility.getFormattedDateTime(aCompareTime, DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS);
        final long   lStartTime   = System.currentTimeMillis();

        if (log.isDebugEnabled())
            log.debug("Remove activity start time : '" + lStartTime + "' for the key : '" + aExpiryKey + "' for the compare time : '" + aCompareTime + "'");

        try
        {
            int          lMaxKeysFetchLen      = 0;
            final String lMaxRedisKeyConfigLen = getAppConfigValueAsString(ConfigParamConstants.MAX_REDIS_KEYS_FETCH_LEN);

            if (log.isDebugEnabled())
                log.debug("Max redis keys fetch length configured : " + lMaxRedisKeyConfigLen);

            lMaxKeysFetchLen = CommonUtility.getInteger(lMaxRedisKeyConfigLen, 5000);
            long lKeysLength = aJedisCon.llen(aExpiryKey);

            if (log.isDebugEnabled())
                log.debug("No.of.entries for the key : '" + aExpiryKey + "' is : '" + lKeysLength + "'");

            int lIncrementCounter = 0;

            while (lKeysLength > 0)
            {
                if (log.isDebugEnabled())
                    log.debug("Key length for the time of removing : '" + aExpiryKey + "' is : '" + lKeysLength + "'");
                lIncrementCounter++;

                final int lMaxLength = (int) (lKeysLength > lMaxKeysFetchLen ? lMaxKeysFetchLen : lKeysLength);

                try (
                        Pipeline lRedisPipe = aJedisCon.pipelined();)
                {
                    final List<Response<String>> lRedisPopResults = getPopResults(lRedisPipe, lMaxLength, aExpiryKey, lCompareTime, lIncrementCounter);
                    removeExpiryEntries(lRedisPopResults, lRedisPipe, lCompareTime, lIncrementCounter);
                }
                catch (final Exception e)
                {
                    log.error("Something went wrong here. Quiting the process id removing the redis entries for the key : '" + aExpiryKey + "'", e);
                    throw e;
                }

                lKeysLength = aJedisCon.llen(aExpiryKey);

                if (log.isDebugEnabled())
                    log.debug("Increment Count :'" + lIncrementCounter + "'. Number of entries for the time : '" + lKeysLength + "'");
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while removing the expire data..", e);
        }

        if (log.isDebugEnabled())
            log.debug("Remove activity completed for the expiry second '" + lCompareTime + "'. Time taken : '" + (System.currentTimeMillis() - lStartTime) + "' milliseconds.");
    }

    private static void removeExpiryEntries(
            List<Response<String>> aPopResults,
            Pipeline aPipe,
            String aCompareTime,
            int aIncrementCount)
    {
        final Map<String, Response<Long>> lDelExpiry  = new HashMap<>();
        final Map<String, Response<Long>> lDelCounter = new HashMap<>();
        final long                        lStartTime  = System.currentTimeMillis();
        int                               lIncrCount  = 0;

        for (final Response<String> result : aPopResults)
        {
            final String lTempVal = result.get();

            if (log.isDebugEnabled())
                log.debug("Removing the entries for the EsmeAddr and Mobile : '" + lTempVal + "'");

            if (lTempVal != null)
            {
                lIncrCount++;
                final String[] lAllValues = lTempVal.split(TimeBoundConstants.SEPERATOR);
                final String   lOuterKey  = TimeBoundConstants.REDIS_KEY_TIMECHECK + lAllValues[0];
                final String   lExpKey    = lAllValues[1] + TimeBoundConstants.REDIS_KEY_EXP_TIME;
                final String   lKeyCount  = lAllValues[1] + TimeBoundConstants.REDIS_KEY_COUNT;

                Response<Long> lDelResult = aPipe.hdel(lOuterKey, lExpKey);
                lDelExpiry.put(lTempVal, lDelResult);

                lDelResult = aPipe.hdel(lOuterKey, lKeyCount);
                lDelCounter.put(lTempVal, lDelResult);
            }
        }

        if (lIncrCount > 0)
            aPipe.sync();

        final long lEndTime = System.currentTimeMillis();

        if (log.isDebugEnabled())
        {
            for (final String expKey : lDelExpiry.keySet())
                log.debug("Result of removing Keys: '" + expKey + "' Expiry : '" + lDelExpiry.get(expKey).get() + "' Counter Key : '" + lDelCounter.get(expKey).get() + "'");
            log.debug(
                    "For expiry time : '" + aCompareTime + "' IncrementCount : '" + aIncrementCount + "' Records removed : '" + lIncrCount + "' Time taken : '" + (lEndTime - lStartTime) + "' millis");
        }
    }

    private static List<Response<String>> getPopResults(
            Pipeline aPipe,
            int aMaxLength,
            String aExpiryKey,
            String aCompareTime,
            int aIncrementCount)
    {
        final long                   lRedisPopStartTime = System.currentTimeMillis();
        final List<Response<String>> lPopResults        = new ArrayList<>(aMaxLength);

        for (int index = 0; index < aMaxLength; index++)
        {
            final Response<String> lRPopResult = aPipe.rpop(aExpiryKey);
            lPopResults.add(lRPopResult);
        }

        aPipe.sync();

        final long lPopEndTime = System.currentTimeMillis();

        if (log.isDebugEnabled())
            log.debug("Expiry Time : '" + aCompareTime + "' IncrementCount :'" + aIncrementCount + "' Pop Entries Count :'" + lPopResults.size() + "' Pop Time Taken :'"
                    + (lPopEndTime - lRedisPopStartTime) + "' milliseconds.");
        return lPopResults;
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

}
