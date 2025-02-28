package com.itextos.beacon.smpp.redisoperations;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;

import redis.clients.jedis.Jedis;

public class SmppRedisConnectionProvider
{

    private SmppRedisConnectionProvider()
    {}

    public static Jedis getRedis()
    {
        return getRedisConnection(Component.SMPP_SESSION);
    }

    public static Jedis getSmppDlrRedis(
            String aClientId)
    {
        final int redisPoolIndex  = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.SMPP_CLIENT_DN);
        final int lRedisPollIndex = (int) (Long.parseLong(aClientId) % redisPoolIndex);
        return getRedisConnection(Component.SMPP_CLIENT_DN, lRedisPollIndex);
    }

    public static Jedis getRedisConnection(
            Component aComponent,
            int lRedisPoolIndex)
    {
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, aComponent, (lRedisPoolIndex + 1));
    }

    public static Jedis getRedisConnection(
            Component aComponent)
    {
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, aComponent, 1);
    }

}