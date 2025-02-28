package com.itextos.beacon.platform.dnpayloadutil.common;

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

public class RedisHandler
{

    private static final Log    log                          = LogFactory.getLog(RedisHandler.class);

    // TODO all the below constants needs to be moved to Constants.
    private static final char   REDIS_SEPRATOR               = ':';
    private static final String REDIS_KEY_DN_HANDOVER_STATUS = "dn_handover_status";
    private static final String REDIS_KEY_AGING_DN_STATUS    = "aging_dn_status";
    private static final String REDIS_KEY_VOICE_MT_STATUS    = "voice_mt_status";

    public static boolean checkDNHandoverStatus(
            String aMNumber,
            Date aSDate,
            String aClientId,
            String aMid)
    {
        if (log.isDebugEnabled())
            log.debug("Calling Redis DN_Handover Check ...");

        boolean isCanHandoverDn = false;

        try (
                Jedis lJedisCon = getJedisConnection(aMNumber, Component.CLIENT_HANDOVER);)
        {
            if (log.isDebugEnabled())
                log.debug("SDate - " + aSDate);

            final String lSDate = getRedisRequiredFormattedDate(aSDate);
            final String lKey   = CommonUtility.combine(REDIS_SEPRATOR, REDIS_KEY_DN_HANDOVER_STATUS, lSDate, aClientId);
            final long   lCount = lJedisCon.sadd(lKey, aMid);
            isCanHandoverDn = (lCount == 0);

            if (log.isDebugEnabled())
                log.debug("DN_Handover status : " + lCount + " :: Status :" + isCanHandoverDn);
        }
        catch (final Exception e)
        {
            log.error("problem adding to DN Handover check ", e);
            throw e;
        }

        return isCanHandoverDn;
    }

    public static boolean checkAgingDNStatus(
            String aMNumber,
            Date aSDate,
            String aClientId,
            String aMid)
    {
        if (log.isDebugEnabled())
            log.debug("Calling Redis Aging DN Check ...");

        boolean isAgingDNHandoverStatus = false;

        try (
                Jedis lJedisCon = getJedisConnection(aMNumber, Component.AGING_PROCESS);)
        {
            if (log.isDebugEnabled())
                log.debug("SDate - " + aSDate);

            final String lSDate = getRedisRequiredFormattedDate(aSDate);
            final String lKey   = CommonUtility.combine(REDIS_SEPRATOR, REDIS_KEY_AGING_DN_STATUS, lSDate, aClientId);
            final long   lCount = lJedisCon.sadd(lKey, aMid);
            isAgingDNHandoverStatus = (lCount == 0);

            if (log.isDebugEnabled())
                log.debug("Aging_DN  status : " + lCount + " :: Status :" + isAgingDNHandoverStatus);
        }
        catch (final Exception e)
        {
            log.error("problem adding to Aging DN check ", e);
            throw e;
        }

        return isAgingDNHandoverStatus;
    }

    public static boolean processVoiceRequest(
            String aMid,
            String aMNumber,
            String aClientId,
            Date aSDate,
            String aMessage)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("Calling Redis Voice DN Check ...");

        boolean isVoiceDNHandoverStatus = false;

        try (
                Jedis lJedisCon = getJedisConnection(aMNumber, Component.AGING_PROCESS);)
        {
            final String lSDate = getRedisRequiredFormattedDate(aSDate);
            final String lKey   = CommonUtility.combine(REDIS_SEPRATOR, REDIS_KEY_VOICE_MT_STATUS, lSDate, aClientId);
            final long   lCount = lJedisCon.hset(lKey, aMid, aMessage);
            isVoiceDNHandoverStatus = (lCount == 0);

            if (log.isDebugEnabled())
                log.debug("VOICE_MT  status : " + lCount + " :: Status :" + isVoiceDNHandoverStatus);
        }
        catch (final Exception e)
        {
            log.error("problem adding to Voice DN to redis.. ", e);
            throw e;
        }

        return isVoiceDNHandoverStatus;
    }

    public static String getVoiceDNFromRedis(
            String aMid,
            String aMNumber,
            String aClientId,
            Date aSDate)
    {
        if (log.isDebugEnabled())
            log.debug("Calling getVoiceDNFromRedis() ....");

        String jsonMapMsg = null;

        try (
                Jedis lJedisCon = getJedisConnection(aMNumber, Component.AGING_PROCESS);)
        {
            final String lSDate = getRedisRequiredFormattedDate(aSDate);
            final String lKey   = CommonUtility.combine(REDIS_SEPRATOR, REDIS_KEY_VOICE_MT_STATUS, lSDate, aClientId);
            jsonMapMsg = lJedisCon.hget(lKey, aMid);
        }
        catch (final Exception e)
        {
            log.error("problem while recive voice DN from redis.. ", e);
            throw e;
        }

        return jsonMapMsg;
    }

    public static boolean deleteVoiceDnFromRedis(
            String aMNumber,
            String aMid,
            Date aSDate,
            String aClientId)
    {
        boolean isRecordDelStatus = false;

        try (
                Jedis lJedisCon = getJedisConnection(aMNumber, Component.AGING_PROCESS);)
        {
            final String lSDate    = getRedisRequiredFormattedDate(aSDate);
            final String lKey      = CommonUtility.combine(REDIS_SEPRATOR, REDIS_KEY_VOICE_MT_STATUS, lSDate, aClientId);
            final Long   delStatus = lJedisCon.hdel(lKey, aMid);

            if (log.isDebugEnabled())
                log.debug("Response on delete for the inner key : '" + lKey + "' & '" + aMid + "' is '" + delStatus + "'");

            isRecordDelStatus = true;
        }
        catch (final Exception e)
        {
            log.debug("Exception occer while deleting the record from redis ..", e);
            throw e;
        }

        return isRecordDelStatus;
    }

    public static boolean isAgingDnExists(
            String aMNumber,
            Date aSDate,
            String aClientId,
            String aMid)
    {
        if (log.isDebugEnabled())
            log.debug("Calling Redis Aging DN Exists Check ...");

        boolean isAgingDNExistsStatus = false;

        try (
                Jedis lJedisCon = getJedisConnection(aMNumber, Component.AGING_PROCESS);)
        {
            if (log.isDebugEnabled())
                log.debug("SDate - " + aSDate);

            final String lSDate = getRedisRequiredFormattedDate(aSDate);
            final String lKey   = CommonUtility.combine(REDIS_SEPRATOR, REDIS_KEY_AGING_DN_STATUS, lSDate, aClientId);

            isAgingDNExistsStatus = lJedisCon.sismember(lKey, aMid);

            if (log.isDebugEnabled())
                log.debug("isAgingDnExists Status :" + isAgingDNExistsStatus);
        }
        catch (final Exception e)
        {
            log.error("problem adding to isAgingDnExists DN check ", e);
            throw e;
        }

        return isAgingDNExistsStatus;
    }

    private static String getRedisRequiredFormattedDate(
            Date aSDate)
    {
        return DateTimeUtility.getFormattedDateTime(aSDate, DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD);
    }

    private static Jedis getJedisConnection(
            String aMNumber,
            Component aComponent)
    {
        final int lRedisPoolCnt = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, aComponent);
        final int lModValue     = (int) (CommonUtility.getLong(aMNumber) % lRedisPoolCnt);
        if (log.isDebugEnabled())
            log.debug(aComponent + " - Redis modValue - " + aMNumber + " - " + (lModValue + 1));
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, aComponent, (lModValue + 1));
    }

}
