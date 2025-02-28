package com.itextos.beacon.platform.kannelstatusupdater.utility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.kannelstatusupdater.beans.KannelStatusInfo;
import com.itextos.beacon.platform.kannelstatusupdater.beans.SmscBean;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class RedisProcess
{

    private static final Log log = LogFactory.getLog(RedisProcess.class);

    private RedisProcess()
    {}

    public static void populateDataIntoRedis(
                      Map<String, KannelStatusInfo> aOutputMap)
    {

        try (
                Jedis jedis = getKannelRedisConnection();
                Pipeline pipe = jedis.pipelined();)
        {

            for (final Entry<String, KannelStatusInfo> entry : aOutputMap.entrySet())
            {
                final String           kannelId     = entry.getKey();
                final KannelStatusInfo status       = entry.getValue();

                  boolean kannelAvailability = false;
                long    storeSize          = 0;

                if (status != null)
                {
                    kannelAvailability = status.isKannelAvailable();
                    storeSize          = status.getSMS() != null ? status.getSMS().getStoreSize() : -1;
                }

                
                Set<String> smscidset=getSet(status.getSMSCList());
                
                Iterator itr=smscidset.iterator();
                
                while(itr.hasNext()) {
                	
                	String smscid=itr.next().toString();
                	
               
                final Map<String, String> toRedis = new HashMap<>();
                toRedis.put(KannelRedisConstants.KANNEL_KEY_IP_PORT, kannelId);
                toRedis.put(KannelRedisConstants.KANNEL_KEY_AVAILABLE, Boolean.toString(kannelAvailability));
                toRedis.put(KannelRedisConstants.KANNEL_KEY_STORESIZE, Long.toString(storeSize));
                toRedis.put(KannelRedisConstants.KANNEL_KEY_LAST_UPDATED, DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));

                final String kannelAvailable = KannelRedisConstants.KANNEL_KEY + smscid.toLowerCase();
                pipe.hmset(kannelAvailable, toRedis);
                }
            }
            pipe.sync();
        }
        catch (final Exception e)
        {
            log.error("Exception while pushing the Kannel Status info into Redis ", e);
        }
    }

    private static Set<String> getSet(List<SmscBean> smscList) {
		Set<String> result=new HashSet<String>();
		
		smscList.forEach((smscid)->{
			
			result.add(smscid.getId());
		});
		return result;
	}

	public static void updateCountsInRedis(
            Map<String, Integer> aTimeTakenMap,
            Map<String, Integer> aSuccessCountMap,
            Map<String, Integer> aInvalidTimeMap)
    {

        try (
                Jedis jedis = getKannelRedisConnection();
                Pipeline pipelined = jedis.pipelined();)
        {
            if (log.isInfoEnabled())
                log.info("invoking updateRedis aTimeTakenMap=" + aTimeTakenMap + " aSuccessCountMap=" + aSuccessCountMap + " aInvalidTimeMap=" + aInvalidTimeMap);

            updateSuccessCountAndResponseTime(aTimeTakenMap, aSuccessCountMap, aInvalidTimeMap, pipelined);
            updateFailedCount(aInvalidTimeMap, pipelined);

            pipelined.sync();
        }
        catch (final Exception e)
        {
            log.error("Something went wrong while updating the Kannel counts into Redis", e);
        }
    }

    private static void updateFailedCount(
            Map<String, Integer> aInvalidTimeMap,
            Pipeline aPipelined)
    {
        final Map<String, String> responseUpdate = new HashMap<>();

        for (final Entry<String, Integer> entry : aInvalidTimeMap.entrySet())
        {
            final String outerKey = KannelRedisConstants.KANNEL_KEY + entry.getKey();
            responseUpdate.put(KannelRedisConstants.KANNEL_FAILED_COUNT, entry.getValue().toString());
            responseUpdate.put(KannelRedisConstants.KANNEL_FAILED_COUNT_UPDATE, DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
            aPipelined.hmset(outerKey, responseUpdate);
        }
    }

    private static void updateSuccessCountAndResponseTime(
            Map<String, Integer> aTimeTakenMap,
            Map<String, Integer> aSuccessCountMap,
            Map<String, Integer> aInvalidTimeMap,
            Pipeline aPipelined)
    {
        int                       timeTaken;
        int                       successCount;
        int                       averageTime;
        final Map<String, String> responseUpdate = new HashMap<>();

        for (final Entry<String, Integer> entry : aTimeTakenMap.entrySet())
        {
            final String route = entry.getKey();

            // remove if the route is in the failed count.
            aInvalidTimeMap.remove(route);

            timeTaken    = aTimeTakenMap.get(route);
            successCount = aSuccessCountMap.get(route);
            averageTime  = timeTaken / successCount;

            final String outerKey = KannelRedisConstants.KANNEL_KEY + route;

            responseUpdate.clear();
            responseUpdate.put(KannelRedisConstants.KANNEL_RESPONSE_TIME, Integer.toString(averageTime));
            responseUpdate.put(KannelRedisConstants.KANNEL_RESPONSE_COUNT, Integer.toString(successCount));
            responseUpdate.put(KannelRedisConstants.KANNEL_RESPONSE_TIME_UPDATED, DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));

            if (log.isDebugEnabled())
                log.debug("update kannel status==>" + responseUpdate);

            aPipelined.hmset(outerKey, responseUpdate);

            if (log.isDebugEnabled())
                log.debug("deleting outerKey=" + outerKey + "inner key=" + KannelRedisConstants.KANNEL_FAILED_COUNT + " and " + KannelRedisConstants.KANNEL_FAILED_COUNT_UPDATE);

            aPipelined.hdel(outerKey, KannelRedisConstants.KANNEL_FAILED_COUNT, KannelRedisConstants.KANNEL_FAILED_COUNT_UPDATE);
        }
    }

    public static Set<String> getRedisKeys(
            String key)
    {
        Set<String> lAllKeys = new HashSet<>();

        try (
                Jedis jedis = getKannelRedisConnection();)
        {
            lAllKeys = jedis.keys(key);

            if (log.isDebugEnabled())
                log.debug("allKeys:" + lAllKeys);
        }
        catch (final Exception exp)
        {
            log.error("Problem getting keys from Redis.", exp);
            throw exp;
        }

        return lAllKeys;
    }

    public static Map<String, Map<String, String>> getResponseData(
            Set<String> aKeys)
    {
        final Map<String, Map<String, String>> lKannelInfo = new HashMap<>();

        try
        {
            final Map<String, Response<Map<String, String>>> redisResult = getDataFromRedis(aKeys);

            for (final Entry<String, Response<Map<String, String>>> entry : redisResult.entrySet())
            {
                final Response<Map<String, String>> value = entry.getValue();
                if (value != null)
                    lKannelInfo.put(entry.getKey(), value.get());
            }

            if (log.isDebugEnabled())
                log.debug("map:" + lKannelInfo);
        }
        catch (final Exception exp)
        {
            log.error("Problem getting response status from Redis.", exp);
        }

        return lKannelInfo;
    }

    private static Map<String, Response<Map<String, String>>> getDataFromRedis(
            Set<String> aKeys)
    {
        final Map<String, Response<Map<String, String>>> redisResult = new HashMap<>();

        try (
                Jedis jedis = getKannelRedisConnection();
                Pipeline pipe = jedis.pipelined();)
        {

            for (final String redisKey : aKeys)
            {
                final Response<Map<String, String>> lValue = pipe.hgetAll(redisKey);
                redisResult.put(redisKey.substring(redisKey.lastIndexOf(":") + 1), lValue);
            }

            pipe.sync();
        }

        return redisResult;
    }

    public static void deleteRedisEntries(
            Map<String, List<String>> aToDelete)
    {

        try (
                Jedis jedis = getKannelRedisConnection();
                Pipeline pipe = jedis.pipelined();)
        {

            for (final Entry<String, List<String>> entry : aToDelete.entrySet())
            {
                final String   outerKey     = KannelRedisConstants.KANNEL_KEY + entry.getKey();
                final String[] keysToDelete = entry.getValue().toArray(new String[0]);
                pipe.hdel(outerKey, keysToDelete);
            }
            pipe.sync();
        }
        catch (final Exception exp)
        {
            log.error("Problem deleting the expired data from redis.", exp);
            throw exp;
        }
    }

    private static Jedis getKannelRedisConnection()
    {
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.KANNEL_REDIS, 1);
    }

}