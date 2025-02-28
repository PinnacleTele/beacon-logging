package com.itextos.beacon.platform.dnpcore.redis;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

import redis.clients.jedis.Jedis;

public class RedisProcess
{

    private static Log log = LogFactory.getLog(RedisProcess.class);

    private RedisProcess()
    {}

    /**
     * @param aMid
     * @param aMNumber
     * @param aClientId
     * @param aSDate
     *
     * @throws Exception
     */
    public static String getVoiceDNFromRedis(
            String aMid,
            String aMNumber,
            String aClientId,
            String aSDate)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("Calling getVoiceDNFromRedis() ....");

        String                 jsonMapMsg = null;

        final SimpleDateFormat lSdfSDate  = new SimpleDateFormat("yyyy-MM-dd");
        final SimpleDateFormat lSdf       = new SimpleDateFormat("yyyyMMdd");
        Jedis                  lJedisCon  = null;

        try
        {
            final Date   lDate         = lSdfSDate.parse(aSDate);

            final String lSDate        = lSdf.format(lDate);

            final int    lRedisPollCnt = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.AGING_DN);

            final int    lModValue     = (int) (Long.valueOf(aMNumber) % lRedisPollCnt);

            if (log.isDebugEnabled())
                log.debug(" VOICE_MT Redis modValue - " + (lModValue + 1));

            lJedisCon = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.AGING_DN, (lModValue + 1));

            if (log.isDebugEnabled())
                log.debug(" VOICE_MT - Redis connection - " + lJedisCon);

            final String lKey = "voice_mt_status:" + lSDate + ":" + aClientId;

            jsonMapMsg = lJedisCon.hget(lKey, aMid);
        }
        catch (final Exception e)
        {
            log.error("Problem while recive voice DN from redis.. ", e);
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

        return jsonMapMsg;
    }

    /**
     * Using this method to verify the Aging DN Exists Check.
     *
     * @param aMNumber
     * @param aSDate
     * @param aClientId
     * @param aMid
     *
     * @return
     *
     * @throws Exception
     */
    public static boolean isAgingDnExists(
            String aMNumber,
            String aSDate,
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

            final String lSDate        = DateTimeUtility.getFormattedDateTime(DateTimeUtility.getDateFromString(aSDate, DateTimeFormat.DEFAULT_DATE_ONLY), DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD);

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
     * Using this method to verify the Aging DN Handover status.
     *
     * @param aMNumber
     * @param aSDate
     * @param aClientId
     * @param aMid
     *
     * @return
     *
     * @throws Exception
     */
    public static boolean checkAgingDNStatus(
            String aMNumber,
            Date aSDate,
            String aClientId,
            String aMid)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("Calling Redis Aging DN Check ...");

        boolean isAgingDNHandoverStatus = false;
        Jedis   lJedisCon               = null;

        try
        {
            if (log.isDebugEnabled())
                log.debug("SDate - " + aSDate);

            final String lSDate        = DateTimeUtility.getFormattedDateTime(aSDate, DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD);

            final int    lRedisPoolCnt = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.AGING_DN);

            final int    lModValue     = (int) (Long.valueOf(aMNumber) % lRedisPoolCnt);

            if (log.isDebugEnabled())
                log.debug(" AGING_DN - Redis modValue - " + (lModValue + 1));

            lJedisCon = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.AGING_DN, (lModValue + 1));

            final String lKey   = "aging_dn_status:" + lSDate + ":" + aClientId;

            final long   lCount = lJedisCon.sadd(lKey, aMid);

            if (log.isDebugEnabled())
                log.debug("Aging_DN status  : " + lCount);

            isAgingDNHandoverStatus = (lCount == 0) == true;

            if (log.isDebugEnabled())
                log.debug("Aging_DN  status : " + lCount + " :: Status :" + isAgingDNHandoverStatus);
        }
        catch (final Exception e)
        {
            log.error("problem adding to Aging DN check ", e);
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

        return isAgingDNHandoverStatus;
    }

    /**
     * Using this method to verify the DN handovesr to Client.
     *
     * @param aMNumber
     * @param aSDate
     * @param aClientId
     * @param aMid
     *
     * @return
     *
     * @throws Exception
     */
    public static boolean checkDNHandoverStatus(
            String aMNumber,
            Date aSDate,
            String aClientId,
            String aMid)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("Calling Redis DN_Handover Check ...");

        boolean isDNHandover = false;
        Jedis   lJedisCon    = null;

        try
        {
            if (log.isDebugEnabled())
                log.debug("SDate - " + aSDate);

            final String lSDate        = DateTimeUtility.getFormattedDateTime(aSDate, DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD);

            final int    lRedisPoolCnt = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.HANDOVER_DN);

            final int    lModValue     = (int) (Long.valueOf(aMNumber) % lRedisPoolCnt);

            if (log.isDebugEnabled())
                log.debug(" HANDOVER_DN - Redis modValue - " + (lModValue + 1));

            lJedisCon = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.HANDOVER_DN, (lModValue + 1));

            final String lKey   = "dn_handover_status:" + lSDate + ":" + aClientId;

            final long   lCount = lJedisCon.sadd(lKey, aMid);

            if (log.isDebugEnabled())
                log.debug("DN_Handover status : " + lCount);

            isDNHandover = (lCount == 0) == true;

            if (log.isDebugEnabled())
                log.debug("DN_Handover status : " + lCount + " :: Status :" + isDNHandover);
        }
        catch (final Exception e)
        {
            log.error("problem adding to DN Handover check ", e);
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

        return isDNHandover;
    }

}
