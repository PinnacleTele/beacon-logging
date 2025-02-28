package com.itextos.beacon.commonlib.redisstatistics.monitor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.utility.CommonUtility;

import redis.clients.jedis.Jedis;

class RedisConnectionProvider
{

    private static final Log log = LogFactory.getLog(RedisConnectionProvider.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final RedisConnectionProvider INSTANCE = new RedisConnectionProvider();

    }

    static RedisConnectionProvider getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<String, RedisConnectionPool> mConnectionPools = new HashMap<>();

    private RedisConnectionProvider()
    {}

    int getRedisPoolCount(
            ClusterType aClusterType,
            Component aComponent)
    {
        if (log.isDebugEnabled())
            log.debug("ClusterType : '" + aClusterType + "' Component : '" + aComponent + "'");
        final String              lKey                 = CommonUtility.combine(aClusterType.getKey(), aComponent.getKey());
        final RedisConnectionPool lRedisConnectionPool = mConnectionPools.get(lKey);

        if (lRedisConnectionPool != null)
            return lRedisConnectionPool.getConnectionPoolCount();

        final Map<Integer, RedisConfig> lRedisCongfiguration = RedisConfigLoader.getInstance().getRedisCongfiguration(aClusterType, aComponent);

        if (lRedisCongfiguration == null)
        {
            log.error("There is no Redis connection pool configuration available for the Cluster type '" + aClusterType + "' and Component '" + aComponent + "'");
            return -1;
        }
        final RedisConnectionPool lRcp = new RedisConnectionPool(aClusterType, aComponent, lRedisCongfiguration);
        mConnectionPools.put(lKey, lRcp);
        return mConnectionPools.get(lKey).getConnectionPoolCount();
    }

    Jedis getConnection(
            ClusterType aClusterType,
            Component aComponent,
            int aRedisPoolIndex)
    {
        if (log.isDebugEnabled())
            log.debug("Cluster Type : '" + aClusterType + "' Component : '" + aComponent + "' Redis pool Index '" + aRedisPoolIndex + "'");
        final String              lKey                 = CommonUtility.combine(aClusterType.getKey(), aComponent.getKey());
        final RedisConnectionPool lRedisConnectionPool = mConnectionPools.get(lKey);

        if (lRedisConnectionPool != null)
            return lRedisConnectionPool.getConnection(aRedisPoolIndex);

        log.error("There is Redis connection pool configuration available for the Cluster Type : '" + aClusterType + "' Component : '" + aComponent + "'");
        return null;
    }

    void closeAllConnectionPools()
    {
        for (final RedisConnectionPool pool : mConnectionPools.values())
            pool.closeAllPools();
    }

}