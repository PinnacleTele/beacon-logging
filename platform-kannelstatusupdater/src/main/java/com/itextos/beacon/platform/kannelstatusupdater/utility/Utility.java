package com.itextos.beacon.platform.kannelstatusupdater.utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.carrierhandover.KannelInfoHolder;
import com.itextos.beacon.inmemory.carrierhandover.bean.KannelInfo;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.kannelstatusupdater.process.response.KannelStatsCollector;
import com.itextos.beacon.platform.kannelstatusupdater.process.response.KannelStatsInfo;

public class Utility
        extends
        KannelRedisConstants
{

    private static final Log log = LogFactory.getLog(Utility.class);

    protected Utility()
    {
        super();
    }

    private static char[] c = new char[]
    { 'k', 'm', 'b', 't' };

    public static String getAliveTime(
            String s)
    {
        long       x       = Long.parseLong(s);
        final long seconds = x % 60;
        x /= 60;
        final long minutes = x % 60;
        x /= 60;
        final long hours = x % 24;
        x /= 24;
        final long days = x;
        return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
    }

    public static String humanReadableFormat(
            double n,
            int iteration)
    {
        if (n < 1000)
            return "" + ((long) n);

        final double  d       = ((long) n / 100) / 10.0;
        final boolean isRound = ((d * 10) % 10) == 0;// true if the decimal part is equal to 0 (then it's trimmed anyway)
        final double  ld      = d;
        return (d < 1000 ? // this determines the class, i.e. 'k', 'm' etc
                (((d > 99.9) || isRound || (!isRound && (d > 9.99)) ? // this decides whether to trim the decimals
                        (ld * 10) / 10 : ld + "" // (int) d * 10 / 10 drops the decimal
                ) + "" + c[iteration] + (!isRound ? "" : "")) : humanReadableFormat(d, iteration + 1));
    }

    public static String formatURL(
    		final String        ip,
    		final String        statusPort)
    {
               

        final StringBuilder url        = new StringBuilder();
        url.append("http://").append(ip).append(":").append(statusPort).append("/status.xml");

        return url.toString();
    }

    public static Map<String, Map<String, String>> getKannelConfig()
    {
        Map<String, Map<String, String>> lRedisKeysMap = new HashMap<>();

        try
        {
            final Set<String> lRedisKeySet = RedisProcess.getRedisKeys(KANNEL_KEY + "*");

            if (lRedisKeySet != null)
                lRedisKeysMap = RedisProcess.getResponseData(lRedisKeySet);
        }
        catch (final Exception e)
        {
            log.error("Exception:", e);
        }
        return lRedisKeysMap;
    }

    public static void calculateTimeAndUpdateRedis()
    {

        try
        {

            while (KannelStatsCollector.getInstance().isDataAvailable())
            {
                final List<KannelStatsInfo> statsInfo       = KannelStatsCollector.getInstance().getKannelStatsInfo(500);
                final Map<String, Integer>  timeTakenMap    = new HashMap<>();
                final Map<String, Integer>  successCountMap = new HashMap<>();
                final Map<String, Integer>  invalidTimeMap  = new HashMap<>();

                for (final KannelStatsInfo si : statsInfo)
                {
                    final String route     = si.getRouteID();
                    final int    timeTaken = si.getTimetaken();

                    if (timeTaken < 0)
                    {
                        final int invalidEntries = invalidTimeMap.computeIfAbsent(route, k -> 0);
                        invalidTimeMap.put(route, invalidEntries + 1);
                    }
                    else
                    {
                        final int summedTimeTaken = timeTakenMap.computeIfAbsent(route, k -> 0);
                        timeTakenMap.put(route, summedTimeTaken + timeTaken);

                        final int successCount = successCountMap.computeIfAbsent(route, k -> 0);
                        successCountMap.put(route, successCount + 1);
                    }
                }

                RedisProcess.updateCountsInRedis(timeTakenMap, successCountMap, invalidTimeMap);

                try
                {
                    Thread.sleep(1);
                }
                catch (final Exception e)
                {
                    //
                }
            }
            if (log.isInfoEnabled())
                log.info("There is no record to update in Redis");
        }
        catch (final Exception e)
        {
            log.error("Exception while updating the response time into Redis.", e);
        }
    }

    public static void removeOldEntries()
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("removing kannel latency keys");

            if (CommonUtility.isEnabled(getAppConfigValueAsString(ConfigParamConstants.KANNEL_CONNECTION_RESPONSE_STATUS_DELETE_EXPIRED_STATUS)))
            {
                final Map<String, List<String>>        toDelete      = new HashMap<>();
                final Set<String>                      keys          = RedisProcess.getRedisKeys(KannelRedisConstants.KANNEL_KEY + "*");
                final Map<String, Map<String, String>> lResponseData = RedisProcess.getResponseData(keys);

                for (final Entry<String, Map<String, String>> entry : lResponseData.entrySet())
                {
                    final String              route     = entry.getKey();
                    final Map<String, String> redisData = entry.getValue();

                    for (final Entry<String, String> entry1 : redisData.entrySet())
                    {
                        final String keyName = entry1.getKey();

                        if (!keyName.endsWith(KannelRedisConstants.LAST_UPDATED))
                            continue;
                        checkForExpiry(entry1.getValue(), route, keyName, toDelete);
                    }
                }

                if (!toDelete.isEmpty())
                    RedisProcess.deleteRedisEntries(toDelete);
            }
        }
        catch (final Exception e)
        {
            log.error("Excpetion while removing the entries from Redis", e);
        }
    }

    public static void checkForExpiry(
            String aResponseTime,
            String aRoute,
            String aKeyName,
            Map<String, List<String>> toDelete)
    {
        long expireDiff = CommonUtility.getLong(getAppConfigValueAsString(ConfigParamConstants.KANNEL_CONNECTION_RESPONSE_STATUS_EXPIRE_TIME_IN_SEC), -1);
        expireDiff = expireDiff == -1 ? KannelRedisConstants.ONE_HOUR : expireDiff;

        final Date temp = DateTimeUtility.getDateFromString(aResponseTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
        final int  diff = (int) (System.currentTimeMillis() - temp.getTime());

        if (expireDiff > diff)
            return;

        if (log.isDebugEnabled())
            log.debug("removing kannel latency keys route :" + aRoute + " keyName:" + aKeyName);

        final List<String> deleteKeyList = toDelete.computeIfAbsent(aRoute, k -> new ArrayList<>());

        if (aKeyName.endsWith(KannelRedisConstants.KANNEL_RESPONSE_TIME_UPDATED))
        {
            deleteKeyList.add(KannelRedisConstants.KANNEL_RESPONSE_TIME);
            deleteKeyList.add(KannelRedisConstants.KANNEL_RESPONSE_COUNT);
            deleteKeyList.add(KannelRedisConstants.KANNEL_RESPONSE_TIME_UPDATED);
        }
        else
        {
            deleteKeyList.add(KannelRedisConstants.KANNEL_FAILED_COUNT);
            deleteKeyList.add(KannelRedisConstants.KANNEL_FAILED_COUNT_UPDATE);
        }
    }

    public static boolean checkKannelLatency(
            String routeid,
            Map<String, String> valueMap)
    {
        final boolean lKannelConnRespCheck            = CommonUtility.isEnabled(getAppConfigValueAsString(ConfigParamConstants.KANNEL_CONN_RESP_CHK));
        final int     lKannelTolerableLatencyInMillis = CommonUtility.getInteger(getAppConfigValueAsString(ConfigParamConstants.KANNEL_CONN_RESP_TIME_IN_MILLIS), -1);

        if (log.isDebugEnabled())
            log.debug(" kannelCheck: '" + lKannelConnRespCheck + "' kannelTolerableLatencyInMillis: '" + lKannelTolerableLatencyInMillis + "'");

        if (lKannelConnRespCheck && (lKannelTolerableLatencyInMillis != -1))
        {
            final String lRouteLatency = valueMap.get(KannelRedisConstants.KANNEL_RESPONSE_TIME);
            if (lRouteLatency != null)
                try
                {
                    final int lRrouteDelay = Integer.parseInt(lRouteLatency);
                    if (log.isDebugEnabled())
                        log.debug("Route id : '" + routeid + "' RouteDelay : '" + lRrouteDelay + "' Kannel delay : '" + lKannelTolerableLatencyInMillis + "'");

                    if (lRrouteDelay > lKannelTolerableLatencyInMillis)
                    {
                        log.error("Latency is high for route:" + routeid + " RouteLatency in redis:" + lRrouteDelay + " Kannel Tolerable Latency:" + lKannelTolerableLatencyInMillis);
                        return false;
                    }
                }
                catch (final Exception e)
                {
                    log.error("looks kannelTolerableLatencyInMillis/response-time-in-millis not a proper integer");
                }
        }
        return true;
    }

    public static boolean isFailedRecordsAvailable(
            Map<String, String> aRedisDataMap)
    {
        if (log.isDebugEnabled())
            log.debug("Redis Response Data = " + aRedisDataMap);

        final int failedCount = CommonUtility.getInteger(aRedisDataMap.get(KannelRedisConstants.KANNEL_FAILED_COUNT), 0);

        if (log.isInfoEnabled())
            log.info("Kannel status false due to failedCount=" + failedCount + " Response Data " + aRedisDataMap);

        return (failedCount > 0);
    }

    public static long getKannelStoreSize(
            String aRouteId)
    {
        final KannelInfoHolder lKannelRouteInfo = (KannelInfoHolder) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.KANNEL_CONFIG_INFO);
        final KannelInfo       lRouteConfig     = lKannelRouteInfo.getRouteConfig(aRouteId);
        return lRouteConfig == null ? -1L : lRouteConfig.getStoreSize();
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

}