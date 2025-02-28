package com.itextos.beacon.platform.smppdlrutil.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;

import redis.clients.jedis.Jedis;

public class SmppRedisConnectionProvider
{

    private static final Log log = LogFactory.getLog(SmppRedisConnectionProvider.class);

    private SmppRedisConnectionProvider()
    {}

    public static Jedis getRedis()
    {
        final Jedis lJedis = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.SMPP_SESSION, 1);

        return lJedis;
    }

    public static Jedis getSmppDlrRedis(
            long aClientId)
    {
        final int   redisPoolIndex  = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.SMPP_CLIENT_DN);
        final int   lRedisPollIndex = (int) (aClientId % redisPoolIndex);

        log.debug("getSmppDlrRedis() aClientId : "+aClientId+" lRedisPollIndex :"+lRedisPollIndex);
     
        final Jedis lJedis          = getRedisConnection(Component.SMPP_CLIENT_DN, lRedisPollIndex);

        return lJedis;
    }

    public static Jedis getRedisConnection(
            Component aComponent,
            int lRedisPoolIndex)
    {
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, aComponent, (lRedisPoolIndex + 1));
    }

}
