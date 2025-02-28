package com.itextos.beacon.commonlib.redisconnectionprovider;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.config.RedisConfig;
import com.itextos.beacon.commonlib.redisconnectionprovider.config.RedisConfigLoader;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.errorlog.ErrorLog;

import redis.clients.jedis.Jedis;

public class RedisConnectionProvider
{

    private static final Log log = LogFactory.getLog(RedisConnectionProvider.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final RedisConnectionProvider INSTANCE = new RedisConnectionProvider();

    }

    public static RedisConnectionProvider getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<String, RedisConnectionPool> mConnectionPools = new HashMap<>();

    private RedisConnectionProvider()
    {
        RedisConfigLoader.getInstance();
    }

    public int getRedisPoolCount(
            Component aComponent)
    {
        return getRedisPoolCount(ClusterType.COMMON, aComponent);
    }

    public int getRedisPoolCount(
            ClusterType aClusterType,
            Component aComponent)
    {
        if (log.isDebugEnabled())
            log.debug("ClusterType : '" + aClusterType + "' Component : '" + aComponent + "'");
        final String              key                  = CommonUtility.combine(aClusterType.getKey(), aComponent.getKey());
        final RedisConnectionPool lRedisConnectionPool = mConnectionPools.get(key);

        if (lRedisConnectionPool != null)
            return lRedisConnectionPool.getConnectionPoolCount();

        final Map<Integer, RedisConfig> lRedisCongfiguration = RedisConfigLoader.getInstance().getRedisCongfiguration(aClusterType, aComponent);

        if (lRedisCongfiguration == null)
        {
            log.error("There is no Redis connection pool configuration available for the Cluster type '" + aClusterType + "' and Component '" + aComponent + "'");
            return -1;
        }
        final RedisConnectionPool rcp = new RedisConnectionPool(aClusterType, aComponent, lRedisCongfiguration);
        mConnectionPools.put(key, rcp);

        return rcp.getConnectionPoolCount();
    }

    public Jedis getConnection(
            ClusterType aClusterType,
            Component aComponent,
            int aRedisPoolIndex)
    {
        if (log.isDebugEnabled())
            log.debug("Cluster Type : '" + aClusterType + "' Component : '" + aComponent + "' Redis pool Index '" + aRedisPoolIndex + "'");
        final String        key                  = CommonUtility.combine(aClusterType.getKey(), aComponent.getKey());
        RedisConnectionPool lRedisConnectionPool = mConnectionPools.get(key);

        if (lRedisConnectionPool == null)
            lRedisConnectionPool = createJedisPool(aClusterType, aComponent);

        if (lRedisConnectionPool != null)
            return lRedisConnectionPool.getConnection(aRedisPoolIndex);

        log.error("There is NO Redis connection pool configuration available for the Cluster Type : '" + aClusterType + "' Component : '" + aComponent + "'");
        return null;
    }

    private RedisConnectionPool createJedisPool(
            ClusterType aClusterType,
            Component aComponent)
    {
        final Map<Integer, RedisConfig> lRedisCongfiguration = RedisConfigLoader.getInstance().getRedisCongfiguration(aClusterType, aComponent);

        if (lRedisCongfiguration == null)
        {
            log.error("There is no Redis connection pool configuration available for the Cluster type '" + aClusterType + "' and Component '" + aComponent + "'");
            ErrorLog.log("There is no Redis connection pool configuration available for the Cluster type '" + aClusterType + "' and Component '" + aComponent + "'");
            return null;
        }
        final RedisConnectionPool lRedisConnectionPool = new RedisConnectionPool(aClusterType, aComponent, lRedisCongfiguration);
        final String              key                  = CommonUtility.combine(aClusterType.getKey(), aComponent.getKey());
        mConnectionPools.put(key, lRedisConnectionPool);
        return lRedisConnectionPool;
    }

    public void closeAllConnectionPools()
    {
        for (final RedisConnectionPool pool : mConnectionPools.values())
            pool.closeAllPools();
    }

}