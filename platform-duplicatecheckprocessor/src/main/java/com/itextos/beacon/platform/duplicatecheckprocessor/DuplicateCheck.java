package com.itextos.beacon.platform.duplicatecheckprocessor;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

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
import redis.clients.jedis.Response;

public abstract class DuplicateCheck
{

    private static final Log    log                         = LogFactory.getLog(DuplicateCheck.class);
    private static final String REDIS_KEY_DUPCHECK          = "duplicatecheck";
    private static final String REDIS_KEY_DUPCHECK_CAMPAIGN = "campaigndupcheck";
    private static final char   REDIS_KEY_SEPARATOR         = ':';

    private DuplicateCheck()
    {}

    public static boolean isDuplicateCustRef(
            String aClientId,
            String aCustRef,
            int aExpirySeconds)
    {
        final long clientId = CommonUtility.getLong(aClientId, -1);
        if (-1 == clientId)
            log.error("Invalid client id specified. Client id : '" + aClientId + "'");
        final int    lRedisIndex = (int) (clientId % RedisConnectionProvider.getInstance().getRedisPoolCount(Component.DUPLICATE_CHK));

        final String lRedisKey   = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_DUPCHECK, Integer.toString(aExpirySeconds), aClientId);
        return checkInRedis(lRedisIndex, lRedisKey, aCustRef, aExpirySeconds);
    }

    public static boolean isDuplicateMessage(
            String aClientId,
            String aDest,
            String aMessage,
            int aExpirySeconds)
    {
        final long clientId = CommonUtility.getLong(aClientId, -1);
        if (-1 == clientId)
            log.error("Invalid client id specified. Client id : '" + aClientId + "'");
        final int    lRedisIndex = (int) (clientId % RedisConnectionProvider.getInstance().getRedisPoolCount(Component.DUPLICATE_CHK));

        final String lRedisKey   = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_DUPCHECK, Integer.toString(aExpirySeconds), aClientId);
        final String lMemberKey  = CommonUtility.combine(REDIS_KEY_SEPARATOR, aClientId, aDest, aMessage);
        return checkInRedis(lRedisIndex, lRedisKey, lMemberKey, aExpirySeconds);
    }

    public static boolean isDuplicatCampiagn(
            String aClientId,
            String aDest,
            String aCampiagnId)
    {
        final long clientId = CommonUtility.getLong(aClientId, -1);
        if (-1 == clientId)
            log.error("Invalid client id specified. Client id : '" + aClientId + "'");
        final int    lRedisIndex = (int) (clientId % RedisConnectionProvider.getInstance().getRedisPoolCount(Component.DUPLICATE_CHK));

        final String lRedisKey   = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_DUPCHECK_CAMPAIGN);

        final String lMemberKey  = aDest;
        return checkInRedis(lRedisIndex, lRedisKey, aCampiagnId, aClientId, lMemberKey);
    }

    private static boolean checkInRedis(
            int aRedisIndex,
            String aHashKey,
            String aMemberKey,
            int aExpirySeconds)
    {

        try (
                Jedis lJedisCon = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.DUPLICATE_CHK, (aRedisIndex + 1));)
        {
            final Calendar lExpiry = Calendar.getInstance();
            lExpiry.add(Calendar.SECOND, aExpirySeconds);
            final long                lExpiryTimeAsScore = lExpiry.getTimeInMillis();
            final Map<String, Double> lElement           = new TreeMap<>();
            lElement.put(aMemberKey, (double) lExpiryTimeAsScore);

            if (log.isInfoEnabled())
                log.info("trying to add key=" + aHashKey + " element=" + lElement);

            long lAddedCount = 0;

            if (lJedisCon.zscore(aHashKey, aMemberKey) == null)
                lAddedCount = lJedisCon.zadd(aHashKey, lElement);

            if (log.isDebugEnabled())
                log.debug("Duplicate Message Added Count =" + lAddedCount);

            return (lAddedCount == 0);
        }
        catch (final Exception exp)
        {
            log.error("Problem adding to set to duplicate check", exp);
        }
        return false;
    }

    private static boolean checkInRedis(
            int aRedisIndex,
            String aHashKey,
            String aCampiagnId,
            String aClientId,
            String aMemberKey)
    {

        try (
                Jedis lJedisCon = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.DUPLICATE_CHK, (aRedisIndex + 1));
                Pipeline lPipe = lJedisCon.pipelined();)
        {
            if (log.isInfoEnabled())
                log.info("Trying to add key=" + aHashKey + " MemberKey=" + aMemberKey);

            final String lDateTime = DateTimeUtility.getFormattedDateTime(new Date(), DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM);

            lPipe.hsetnx(CommonUtility.combine(aHashKey, aCampiagnId), "created_ts", lDateTime);

            final Response<Long> lAddedCount = lPipe.sadd(CommonUtility.combine(aHashKey, aCampiagnId, aClientId), aMemberKey);

            lPipe.sync();
            if (log.isDebugEnabled())
                log.debug("Duplicate Message Added Count =" + lAddedCount);

            return lAddedCount != null ? (extracted(lAddedCount) == 0) : true;
        }
        catch (final Exception exp)
        {
            log.error("Problem adding to set to duplicate check", exp);
        }
        return false;
    }

    private static Long extracted(
            final Response<Long> lAddedCount)
    {
        return lAddedCount.get();
    }

}