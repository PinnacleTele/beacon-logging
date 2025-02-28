package com.itextos.beacon.commonlib.messageidentifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;

import redis.clients.jedis.Jedis;

abstract class RedisConnection
{

    private static final Log log = LogFactory.getLog(RedisConnection.class);

    private RedisConnection()
    {}

    static Jedis getConnection()
    {
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.MESSAGE_IDENTIFIER, 1);
    }

}