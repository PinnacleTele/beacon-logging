package com.itextos.beacon.platform.voice.redis;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

import redis.clients.jedis.Jedis;

public class VoiceRedis
{

    private static final Log log = LogFactory.getLog(VoiceRedis.class);

    private VoiceRedis()
    {}

    /*
     * Using this
     * method to
     * verify the
     * Aging DN
     * Exists Check.**
     * @param aMNumber
     * @param aSDate
     * @param aClientId
     * @param aMid
     * @return
     * @throws Exception
     */
    public static boolean isAgingDnExists(
            String aMNumber,
            Date aSDate,
            String aClientId,
            String aMid)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("Calling Redis Aging DN Exists Check ...");

        boolean isAgingDNExistsStatus = false;
        Jedis   lJedisCon             = null;

        try
        {
            if (log.isDebugEnabled())
                log.debug("SDate - " + aSDate);

            final String lSDate        = DateTimeUtility.getFormattedDateTime(aSDate, DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD);

            final int    lRedisPoolCnt = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.AGING_DN);
            final int    lModValue     = (int) (Long.valueOf(aMNumber) % lRedisPoolCnt);

            if (log.isDebugEnabled())
                log.debug(" Redis modValue - " + (lModValue + 1));

            lJedisCon = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.AGING_DN, (lModValue + 1));

            final String lKey = "aging_dn_status:" + lSDate + ":" + aClientId;

            isAgingDNExistsStatus = lJedisCon.sismember(lKey, aMid);

            if (log.isDebugEnabled())
                log.debug("isAgingDnExists Status :" + isAgingDNExistsStatus);
        }
        catch (final Exception e)
        {
            log.error("problem adding to isAgingDnExists DN check ", e);
            throw e;
        }
        finally
        {

            try
            {
                if (lJedisCon != null)
                    lJedisCon.close();
            }
            catch (final Exception ignore)
            {}
        }

        return isAgingDNExistsStatus;
    }

    /**
     * @param aMid
     * @param aMNumber
     * @param aClientId
     * @param aSDate
     * @param aReqMessage
     *
     * @return
     *
     * @throws Exception
     */
    public static boolean processVoiceRequest(
            String aMid,
            String aMNumber,
            String aClientId,
            Date aSDate,
            String aReqMessage)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("Calling Redis Voice DN Check ...");

        boolean isVoiceDNHandoverStatus = false;
        Jedis   lJedisCon               = null;

        try
        {
            final String sDate           = DateTimeUtility.getFormattedDateTime(aSDate, DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD);

            final int    lRedisPoolIndex = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.AGING_PROCESS);

            final int    modValue        = (int) (Long.valueOf(aMNumber) % lRedisPoolIndex);

            if (log.isDebugEnabled())
                log.debug("VOICE_MT - Redis modValue - " + (modValue + 1));

            lJedisCon = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.AGING_PROCESS, (modValue + 1));

            final String key   = "voice_mt_status:" + sDate + ":" + aClientId;

            final String field = aMid;

            final String value = aReqMessage;

            final long   count = lJedisCon.hset(key, field, value);

            if (log.isDebugEnabled())
                log.debug("VOICE_MT status  : " + count);

            isVoiceDNHandoverStatus = (count == 0) == true;

            if (log.isDebugEnabled())
                log.debug("VOICE_MT  status : " + count + " :: Status :" + isVoiceDNHandoverStatus);
        }
        catch (final Exception e)
        {
            log.error("Problem adding to Voice DN to redis.. ", e);
            throw e;
        }
        finally
        {

            try
            {
                if (lJedisCon != null)
                    lJedisCon.close();
            }
            catch (final Exception ignore)
            {}
        }

        return isVoiceDNHandoverStatus;
    }

    /**
     * This method use to delete the Voice DN from redis.. Once we receive the DN
     * from Voice platform and successfully hand-over to middleware.
     *
     * @param aMNumber
     * @param aMid
     * @param aSDate
     * @param aClientId
     *
     * @return
     */
    public static boolean deleteVoiceDnFromRedis(
            String aMNumber,
            String aMid,
            Date aSDate,
            String aClientId)
            throws Exception
    {
        boolean isRecordDelStatus = false;
        Jedis   lJedisCon         = null;

        try
        {
            final String lSate         = DateTimeUtility.getFormattedDateTime(aSDate, DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD);
            final int    lRedisPollCnt = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.ADNG);
            final int    lModValue     = (int) (Long.valueOf(aMNumber) % lRedisPollCnt);

            if (log.isDebugEnabled())
                log.debug("VOICE_MT Redis modValue - " + (lModValue + 1));

            lJedisCon = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.ADNG, (lModValue + 1));

            if (log.isDebugEnabled())
                log.debug("VOICE_MT Redis connection - " + lJedisCon);

            final String key       = "voice_mt_status:" + lSate + ":" + aClientId;

            final Long   delStatus = lJedisCon.hdel(key, aMid);

            if (log.isDebugEnabled())
                log.debug("VOICE_MT Response on delete for the inner key : '" + key + "' & '" + aMid + "' is '" + delStatus + "'");

            isRecordDelStatus = true;
        }
        catch (final Exception e)
        {
            log.debug("Exception occer while deleting the record from redis ..", e);
            throw e;
        }
        finally
        {

            try
            {
                if (lJedisCon != null)
                    lJedisCon.close();
            }
            catch (final Exception ignore)
            {}
        }
        return isRecordDelStatus;
    }

}
