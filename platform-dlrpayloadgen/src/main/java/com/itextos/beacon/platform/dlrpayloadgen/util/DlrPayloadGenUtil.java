package com.itextos.beacon.platform.dlrpayloadgen.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

public class DlrPayloadGenUtil
{

    private static final Log log = LogFactory.getLog(DlrPayloadGenUtil.class);

    private DlrPayloadGenUtil()
    {}

    public static List<String> getPlayloadRedisInfo(
            ClusterType aCluster)
    {
        List<String> lPayloadRedisIds = null;

        try
        {
            final int lRedisPoolCnt = RedisConnectionProvider.getInstance().getRedisPoolCount(aCluster, Component.DN_PAYLOAD);

            if (lRedisPoolCnt > 0)
            {
                lPayloadRedisIds = new ArrayList<>();
                for (int index = 1; index <= lRedisPoolCnt; index++)
                    lPayloadRedisIds.add(String.valueOf(index));
            }
        }
        catch (final Exception e)
        {
            lPayloadRedisIds = null;

            e.printStackTrace();
        }

        if ((lPayloadRedisIds == null) || (lPayloadRedisIds.size() == 0))
        {
            log.error("cluster redis id's are not configured in configuration.redis_config for cluster:" + aCluster + " - Keeping shutdown");
            System.exit(0);
        }
        lPayloadRedisIds.add("mysql");
        return lPayloadRedisIds;
    }

    public static boolean getDlrPayloadStatus(
            String aPayloadId)
    {
        Jedis   lJedis  = null;
        boolean lStatus = false;

        try
        {
            lJedis  = getKannelRedisConnection();
            lStatus = Boolean.parseBoolean(lJedis.hget("kannel:dngeneratepayloadid", aPayloadId));
        }
        catch (final Exception e)
        {
            log.error("getDNPayloadStatus()", e);
        }
        finally
        {

            try
            {
                if (lJedis != null)
                    lJedis.close();
            }
            catch (final Exception e)
            {
                log.error("jedis close()", e);
            }
        }
        return lStatus;
    }

    public static void setDlrPayloadStatus(
            String aPayloadId,
            boolean aStatus)
    {
        Jedis lJedis = null;

        try
        {
            lJedis = getKannelRedisConnection();
            lJedis.hset("kannel:dngeneratepayloadid", aPayloadId, String.valueOf(aStatus));
        }
        catch (final Exception e)
        {
            log.error("getId()", e);
        }
        finally
        {

            try
            {
                if (lJedis != null)
                    lJedis.close();
            }
            catch (final Exception e)
            {
                log.error("jedis close()", e);
            }
        }
    }

    public static Date getTimeToGenerateDn()
    {
        final int      lDlrGenIntervals = getDlrGenerateIntervalInHr();

        final Calendar lCurrDate        = Calendar.getInstance();
        final Date     lTempDate        = lCurrDate.getTime();
        lTempDate.setHours(lCurrDate.get(Calendar.HOUR_OF_DAY) - lDlrGenIntervals);

        return lTempDate;
    }

    public static void updateInMemCountToDB(
            String aPayLoadId,
            List<String> aPayloadDtLs)
    {
        for (final String payloadDt : aPayloadDtLs)
            updateInMemCountToDB(aPayLoadId, payloadDt);
    }

    public static void updateInMemCountToDB(
            String aPayLoadId,
            String aRedisKey)
    {
        final String               lDlrCountKey         = CommonUtility.combine(aPayLoadId, aRedisKey);
        final Map<String, Integer> lGeneratedCountByKey = GeneratedDlrCountCache.getInstance().getGeneratedDnCountByKey(lDlrCountKey);
        if (log.isDebugEnabled())
            log.debug("generatedDnCountByKey: " + lGeneratedCountByKey + " payLoadId: " + aPayLoadId + " inMemDnCountKey: " + lDlrCountKey);

        if (lGeneratedCountByKey != null)
        {
            UpdateGeneratedDlrCount.updateGeneratedDlrCount(lGeneratedCountByKey, aRedisKey);
            GeneratedDlrCountCache.getInstance().removeGeneratedDnCountByKey(lDlrCountKey);
            log.debug("after remove from inmem:" + GeneratedDlrCountCache.getInstance().getGeneratedDnCountByKey(lDlrCountKey));
        }
    }

    public static boolean isKeyToGenerateDNs(
            String aRedisVal)
            throws ParseException
    {
        aRedisVal = aRedisVal.replace("dnpayload-expire:", "");
        final Calendar lCurrDate         = Calendar.getInstance();

        final Date     lFormatedCurrDate = DateTimeUtility.getDateFromString(DateTimeUtility.getFormattedDateTime(lCurrDate.getTime(), DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH),
                DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH);
        final Date     lRedisKeyDate     = DateTimeUtility.getDateFromString(aRedisVal, DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH);

        if (lRedisKeyDate.compareTo(lFormatedCurrDate) <= 0)
            return true;
        return false;
    }

    private static int getDlrGenerateIntervalInHr()
    {
        int lDlrGenIntervals = 6;

        try
        {
            lDlrGenIntervals = CommonUtility.getInteger(getAppConfigValueAsString(ConfigParamConstants.DLR_GEN_INTERVAL_FROM_PAYLOAD_IN_HRS), 6);
        }
        catch (final Exception ignore)
        {
            log.error(ignore);
        }
        return lDlrGenIntervals;
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    private static Jedis getKannelRedisConnection()
    {
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.KANNEL_REDIS, 1);
    }

}
