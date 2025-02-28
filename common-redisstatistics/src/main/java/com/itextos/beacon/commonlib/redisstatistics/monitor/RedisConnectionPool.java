package com.itextos.beacon.commonlib.redisstatistics.monitor;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

class RedisConnectionPool
{

    private static Log                      log                   = LogFactory.getLog(RedisConnectionPool.class);

    private final ClusterType               mClusterType;
    private final Component                 mComponent;
    private final Map<Integer, RedisConfig> mRedisConfigCollection;
    private final Map<Integer, JedisPool>   mRedisConnectionPools = new HashMap<>();
    private boolean                         mIsRedisPoolCreated   = false;
    private final String                    mType;

    RedisConnectionPool(
            ClusterType aClusterType,
            Component aComponent,
            Map<Integer, RedisConfig> aRedisConfigInfoCollection)
    {
        mClusterType           = aClusterType;
        mComponent             = aComponent;
        mRedisConfigCollection = aRedisConfigInfoCollection;
        mType                  = "[Cluster Type '" + mClusterType + "' Component '" + mComponent + "']";
    }

    private void createPools()
    {

        if (mRedisConfigCollection != null)
        {

            for (final RedisConfig redisInfo : mRedisConfigCollection.values())
            {
                if (log.isDebugEnabled())
                    log.debug("Redis Id while Creating pool " + mType + " Redis Pool Index '" + redisInfo.getRedisPoolIndex() + "'");
                mRedisConnectionPools.put(redisInfo.getRedisPoolIndex(), createRedisPool(redisInfo));
            }

            if (log.isDebugEnabled())
                log.debug("Redis connection pool size for " + mType + " is : " + mRedisConnectionPools.size());
        }
    }

    void closeAllPools()
    {
        for (final JedisPool pool : mRedisConnectionPools.values())
            try
            {
                pool.close();
            }
            catch (final Exception e)
            {
                log.error("Excception while closing the connection pool ", e);
            }
    }

    Jedis getConnection(
            int aRedisPoolIndex)
    {

        if (!mIsRedisPoolCreated)
        {
            createPools();
            mIsRedisPoolCreated = true;
        }

        if (log.isDebugEnabled())
            log.debug(mType + " pool Index : " + aRedisPoolIndex);

        final JedisPool lJedisPool = mRedisConnectionPools.get(aRedisPoolIndex);

        if (lJedisPool != null)
            try
            {

                if (log.isDebugEnabled())
                {
                    final RedisConfig lRedisConfig = mRedisConfigCollection.get(aRedisPoolIndex);

                    if (lRedisConfig.isDebugEnabled())
                        log.debug(mType + " pool Index : " + aRedisPoolIndex + " Pool Max Size Specified : " + lRedisConfig.getMaxPoolSize() + " Pool Active count : " + lJedisPool.getNumActive()
                                + " Pool Idle Count : " + lJedisPool.getNumIdle() + " Waiting threads count : " + lJedisPool.getNumWaiters());
                }
                return lJedisPool.getResource();
            }
            catch (final Exception e)
            {
                log.error("Exception while getting the redis connection.", e);
                final RedisConfig lRedisConfig = mRedisConfigCollection.get(aRedisPoolIndex);
                log.error(mType + " pool Index : " + aRedisPoolIndex + " Pool Max Size Specified : " + lRedisConfig.getMaxPoolSize() + " Pool Active count : " + lJedisPool.getNumActive()
                        + " Pool Idle Count : " + lJedisPool.getNumIdle() + " Waiting threads count : " + lJedisPool.getNumWaiters());
                throw e;
            }

        log.error("There is no Redis Pool available for the index " + mType + " pool Index : '" + aRedisPoolIndex + "'");

        return null;
    }

    private JedisPool createRedisPool(
            RedisConfig aRedisConfig)
    {
        if (aRedisConfig != null)
            try
            {
                final int                        db                = aRedisConfig.getDatabase();
                final String                     ip                = aRedisConfig.getIP();
                String                           pass              = aRedisConfig.getPassword();
                final int                        port              = aRedisConfig.getPort();
                final int                        readTimeout       = aRedisConfig.getReadTimeoutInsec();
                final int                        connectionTimeout = aRedisConfig.getConnectionTimeoutInsec();
                final int                        maxWaitTime       = aRedisConfig.getMaxWaitTimeInsec();
                final int                        maxIdle           = aRedisConfig.getMaxIdle();
                final int                        minIdle           = aRedisConfig.getMinIdle();
                final int                        maxpool           = aRedisConfig.getMaxPoolSize();
                final String                     lHostAddress      = InetAddress.getLocalHost().getHostAddress();
                final String                     clientId          = "RCP-" + mType + "-" + aRedisConfig.getRedisId() + "-" + lHostAddress;

                // if (log.isDebugEnabled())
                // {
                // log.debug("Creating pool = " + clientId);
                // }

                final GenericObjectPoolConfig<?> config            = new GenericObjectPoolConfig<>();
                config.setMaxTotal(maxpool);
                config.setMaxIdle(maxIdle);
                config.setMinIdle(minIdle);
                config.setMaxWaitMillis(maxWaitTime * 1000L);

                if ((pass != null) && (pass.trim().length() == 0))
                    pass = null;

                return new JedisPool(config, ip, port, connectionTimeout * 1000, readTimeout * 1000, pass, db, "");
            }
            catch (final Exception exp)
            {
                log.error(aRedisConfig.getComponent() + " jedis pool creation problem...", exp);
            }
        return null;
    }

    int getConnectionPoolCount()
    {
        return mRedisConfigCollection.size();
    }

}