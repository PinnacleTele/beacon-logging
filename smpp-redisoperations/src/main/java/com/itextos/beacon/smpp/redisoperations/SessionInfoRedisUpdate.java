package com.itextos.beacon.smpp.redisoperations;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class SessionInfoRedisUpdate
{

    private static final Log log = LogFactory.getLog(SessionInfoRedisUpdate.class);

    private SessionInfoRedisUpdate()
    {}

    public static boolean increaseTransactionBindCount(
            String aClientId,
            String aInstanceId,
            boolean isDnoperation)
    {
        return increaseDecreaseBindCount(aClientId, aInstanceId, isDnoperation, 1);
    }

    public static void decreaseTransactionBindCount(
            String aClientId,
            String aInstanceId,
            boolean isDnoperation)
    {
        increaseDecreaseBindCount(aClientId, aInstanceId, isDnoperation, -1);
    }

    private static boolean increaseDecreaseBindCount(
            String aClientId,
            String aInstanceId,
            boolean isDnoperation,
            int aCount)
    {
        boolean updated = false;

        try (
                Jedis jedis = SmppRedisConnectionProvider.getRedis();)
        {
            if (log.isDebugEnabled())
                log.debug("Is DN Operation : " + isDnoperation);
            final String redisKey = isDnoperation ? RedisKeyConstants.DN_SESSION_INFO : RedisKeyConstants.TX_SESSION_INFO;
            jedis.hincrBy(redisKey + aClientId, aInstanceId, aCount);
            updated = true;
        }
        catch (final Exception e)
        {
            log.error("Exception while increment / decrement the session counts.", e);
        }
        return updated;
    }

    public static boolean setBindCount(
            String aClientId,
            String aInstanceId,
            boolean isDnoperation,
            int aCount)
    {
        boolean updated = false;

        try (
                Jedis jedis = SmppRedisConnectionProvider.getRedis();)
        {
            if (log.isDebugEnabled())
                log.debug("Is DN Operation : " + isDnoperation);

            final String redisKey = isDnoperation ? RedisKeyConstants.DN_SESSION_INFO : RedisKeyConstants.TX_SESSION_INFO;
            jedis.hset(redisKey + aClientId, aInstanceId, Integer.toString(aCount));
            updated = true;
        }
        catch (final Exception e)
        {
            log.error("Exception while increment / decrement the session counts.", e);
        }
        return updated;
    }

    public static void removeAllBindInfo(
            String instanceId,
            boolean isDnoperation)
    {

        try (
                Jedis jedis = SmppRedisConnectionProvider.getRedis();
                Pipeline pipe = jedis.pipelined();)
        {
            if (log.isDebugEnabled())
                log.debug("Is DN Operation : " + isDnoperation);

            final String      redisKey = isDnoperation ? RedisKeyConstants.DN_SESSION_INFO : RedisKeyConstants.TX_SESSION_INFO;
            final Set<String> keySet   = jedis.keys(redisKey + "*");
            for (final String key : keySet)
                pipe.hdel(key, instanceId);
            pipe.sync();
        }
        catch (final Exception e)
        {
            log.error("Exception while removing all the Bindinfo. ", e);
            throw e;
        }
    }

}
