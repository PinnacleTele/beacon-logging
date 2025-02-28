package com.itextos.beacon.inmemdata.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;

import redis.clients.jedis.Jedis;

public abstract class RedisHandler
{

    private static final Log log = LogFactory.getLog(RedisHandler.class);

    private RedisHandler()
    {}

    public static Set<String> getKeysForKeyPattern(
            Component aComponent,
            String aKey)
    {
        if (log.isDebugEnabled())
            log.debug("Key :'" + aKey + "'");

        Set<String> lResult = null;

        try (
                Jedis lJedisConn = getRedis(aComponent))
        {
            lResult = lJedisConn.keys(aKey);

            if (log.isDebugEnabled())
                log.debug("Key :'" + aKey + "' result : " + lResult);

            return lResult;
        }
        catch (final Exception e)
        {
            log.error("Exception in getKeys(String key)", e);
        }

        return lResult;
    }

    public static Map<String, String> getMapForKey(
            Component aComponent,
            String aKey)
    {
        if (log.isDebugEnabled())
            log.debug("Key :'" + aKey + "'");

        Map<String, String> map = null;

        try (
                Jedis lJedisConn = getRedis(aComponent))
        {
            map = lJedisConn.hgetAll(aKey);

            if (log.isDebugEnabled())
                log.debug("Key :'" + aKey + "' result : " + map);

            return map;
        }
        catch (final Exception e)
        {
            log.error("Exception in getAccountMap(String key)", e);
        }

        return map;
    }

    public static List<String> getListForKey(
            Component aComponent,
            String aKey)
    {
        if (log.isDebugEnabled())
            log.debug("Key :'" + aKey + "'");

        List<String> lList = null;

        try (
                Jedis lJedisConn = getRedis(aComponent))
        {
            lList = lJedisConn.lrange(aKey, 0, -1);

            if (log.isDebugEnabled())
                log.debug("Key :'" + aKey + "' result : " + lList);
        }
        catch (final Exception e)
        {
            log.error("Exception in getAccountMap(String key)", e);
        }
        return lList;
    }

    public static boolean checkKeyValueExists(
            Component aComponent,
            String aKey,
            String aValue)
    {
        if (log.isDebugEnabled())
            log.debug("Checking in redis for key : '" + aKey + "' value : '" + aValue + "'");

        boolean isExists = false;

        try (
                Jedis lJedisConn = getRedis(aComponent))
        {
            isExists = lJedisConn.hexists(aKey, aValue);

            if (log.isDebugEnabled())
                log.debug("Checking in redis for key : '" + aKey + "' value : '" + aValue + "' Result : '" + isExists + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception in checkFiledFromMap(String key, String Dest)", e);
            throw e;
        }
        return isExists;
    }

    private static Jedis getRedis(
            Component aComponent)
    {
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, aComponent, 1);
    }

}