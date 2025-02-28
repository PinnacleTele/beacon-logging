package com.itextos.beacon.commonlib.dnddataloader.redis;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.dnddataloader.common.DndInfo;
import com.itextos.beacon.commonlib.dnddataloader.enums.RedisRecordStatus;
import com.itextos.beacon.commonlib.dnddataloader.util.DndPropertyProvider;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * Will contain all the Redis operations related code
 *
 * @author kduraimanickam
 */
public class RedisOperations
{

    private static final Log log = LogFactory.getLog(RedisOperations.class);

    /**
     * Add / Update the dest in {@link DndInfo} object into Redis, based on redis
     * index.
     * Redis Index will be calculated based on the mod value of the dest.
     * <br>
     * If the retries exhausted then a runtime exception will be thrown.
     *
     * @param aAddOrUpdateList
     *                         {@link HashMap} with the Redis index and
     *                         {@link ArrayList} of {@link DndInfo} objects to be
     *                         added or updated.
     *
     * @return {@link HashMap} with the add / update based on the number already
     *         exists in the Redis before this operation.
     */
    public static Map<RedisRecordStatus, Integer> updateDestPref(
            Map<Integer, List<DndInfo>> aAddOrUpdateList)
    {
        final Map<RedisRecordStatus, Integer> result = new EnumMap<>(RedisRecordStatus.class);

        for (final Entry<Integer, List<DndInfo>> entry : aAddOrUpdateList.entrySet())
        {
            final Map<RedisRecordStatus, Integer> curResult = updateDestPref(entry.getKey(), entry.getValue(), 0);
            mergeResults(result, curResult);
        }
        return result;
    }

    private static Map<RedisRecordStatus, Integer> updateDestPref(
            int aRedisIndex,
            List<DndInfo> aAddList,
            int aRetryCount)
    {
        final Map<RedisRecordStatus, Integer> result = new EnumMap<>(RedisRecordStatus.class);

        try (
                Jedis jedis = getDndMasterRedis(aRedisIndex);
                Pipeline pipe = jedis.pipelined();)
        {
            final Map<String, Response<Long>> responseList = new HashMap<>();

            for (final DndInfo dndInfo : aAddList)
            {
                final Response<Long> response = pipe.hset(dndInfo.getHashKey(), dndInfo.getKey(), dndInfo.getPreferences());
                responseList.put(dndInfo.getOriginal(), response);
            }

            pipe.sync();

            long l;
            long addCount    = 0;
            long updateCount = 0;

            for (final Response<Long> response : responseList.values())
            {
                l = response.get();
                if (l > 0)
                    addCount++;
                else
                    updateCount++;
            }

            result.put(RedisRecordStatus.UPDATED, (int) updateCount);
            result.put(RedisRecordStatus.ADDED, (int) addCount);
        }
        catch (final Exception aSomeException)
        {
            log.error("Exception while updating the DND Preferences. Action 'Add or Update'. Retry Count :'" + aRetryCount + "'", aSomeException);

            if (aRetryCount >= DndPropertyProvider.getInstance().getRedisRetryCount())
            {
                log.error("Unable to update the DND");
                throw new RuntimeException("There is some serios problem in updating the DND details.", aSomeException);
            }

            return updateDestPref(aRedisIndex, aAddList, aRetryCount + 1);
        }
        return result;
    }

    /**
     * Delete the dest in the {@link DndInfo} object from Redis, based on redis
     * index.
     * Redis Index will be calculated based on the mod value of the dest.
     * <br>
     * If the retries exhausted then a runtime exception will be thrown.
     *
     * @param aDeleteList
     *                    {@link HashMap} with the Redis index and {@link ArrayList}
     *                    of {@link DndInfo} objects to be deleted.
     *
     * @return {@link HashMap} deleted counts and undeleted counts (in case of the
     *         number not available).
     */
    public static Map<RedisRecordStatus, Integer> deletePref(
            Map<Integer, List<DndInfo>> aDeleteList)
    {
        final Map<RedisRecordStatus, Integer> result = new EnumMap<>(RedisRecordStatus.class);

        for (final Entry<Integer, List<DndInfo>> entry : aDeleteList.entrySet())
        {
            final Map<RedisRecordStatus, Integer> curResult = deletePref(entry.getKey(), entry.getValue(), 0);
            mergeResults(result, curResult);
        }
        return result;
    }

    private static Map<RedisRecordStatus, Integer> deletePref(
            int aRedisIndex,
            List<DndInfo> aDeleteList,
            int aRetryCount)
    {
        final Map<RedisRecordStatus, Integer> result = new EnumMap<>(RedisRecordStatus.class);

        try (
                Jedis jedis = getDndMasterRedis(aRedisIndex);
                Pipeline pipe = jedis.pipelined();)
        {
            final Map<String, Response<Long>> responseList = new HashMap<>();

            for (final DndInfo dndInfo : aDeleteList)
            {
                final Response<Long> response = pipe.hdel(dndInfo.getHashKey(), dndInfo.getKey());
                responseList.put(dndInfo.getOriginal(), response);
            }

            pipe.sync();

            long l;
            long deleteCount   = 0;
            long noRecordFound = 0;

            for (final Response<Long> response : responseList.values())
            {
                l = response.get();
                if (l > 0)
                    deleteCount++;
                else
                    noRecordFound++;
            }

            result.put(RedisRecordStatus.DELETED, (int) deleteCount);
            result.put(RedisRecordStatus.NOT_AVAILABLE, (int) noRecordFound);
        }
        catch (final Exception aSomeException)
        {
            log.error("Exception while updating the DND Preferences. Action 'Add or Update'. Retry Count :'" + aRetryCount + "'", aSomeException);

            if (aRetryCount >= DndPropertyProvider.getInstance().getRedisRetryCount())
            {
                log.error("Unable to update the DND",aSomeException);
            //    throw new ItextosRuntimeException("There is some serios problem in updating the DND details.", aSomeException);
            }

            return updateDestPref(aRedisIndex, aDeleteList, aRetryCount + 1);
        }
        return result;
    }

    private static void mergeResults(
            Map<RedisRecordStatus, Integer> aOldResult,
            Map<RedisRecordStatus, Integer> aCurResult)
    {
        Integer oldCount;
        Integer curCount;

        for (final Entry<RedisRecordStatus, Integer> entry : aCurResult.entrySet())
        {
            curCount = entry.getValue();
            oldCount = aOldResult.computeIfAbsent(entry.getKey(), k -> 0);

            aOldResult.put(entry.getKey(), (oldCount.intValue() + curCount.intValue()));
        }
    }

    public static Jedis getDndMasterRedis(
            int aIndex)
    {
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.PRI_DND_CHK, aIndex);
    }

    public static int getDndMasterRedisCount()
    {
        return RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.PRI_DND_CHK);
    }

}