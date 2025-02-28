package com.itextos.beacon.smpp.redisoperations;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.smpputil.ISmppInfo;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class RedisBindOperation
{

    private static final Log log = LogFactory.getLog(RedisBindOperation.class);

    private RedisBindOperation()
    {}

    public static BindCounter increaseBindCount(
            String aClientId,
            String aInstanceid)
    {
        if (log.isDebugEnabled())
            log.debug("Increment Bind Count for the client :'" + aClientId + "', InstanceId : '" + aInstanceid + "'");
        return incrementOrDecrement(aClientId, aInstanceid, 1);
    }

    public static BindCounter decreaseBindCount(
            String aClientId,
            String aInstanceid)
    {
        if (log.isDebugEnabled())
            log.debug("Decrement Bind Count for the client :'" + aClientId + "', InstanceId : '" + aInstanceid + "'");
        return incrementOrDecrement(aClientId, aInstanceid, -1);
    }

    private static BindCounter incrementOrDecrement(
            String aClientId,
            String aInstanceid,
            int aCount)
    {
        final BindCounter bindCounter = new BindCounter();

        try (
                Jedis jedis = SmppRedisConnectionProvider.getRedis();
                Pipeline pipe = jedis.pipelined();)
        {
            final Response<Long> clientTotalCount    = pipe.hincrBy(RedisKeyConstants.SESSION_BIND_INFO, aClientId, aCount);
            final Response<Long> instanceClientCount = pipe.hincrBy(RedisKeyConstants.SESSION_BIND_INFO + aInstanceid, aClientId, aCount);
            pipe.sync();

            bindCounter.setClientsTotalCount(clientTotalCount.get().intValue());
            bindCounter.setInstanceWiseClientCount(instanceClientCount.get().intValue());
        }
        catch (final Exception e)
        {
            log.error("Exception while increament / decrement the bind count", e);
            throw e;
        }
        return bindCounter;
    }

    public static int getBindCountForClient(
            String aClientId)
    {

        try (
                Jedis jedis = SmppRedisConnectionProvider.getRedis();)
        {
            final String clientCount = jedis.hget(RedisKeyConstants.SESSION_BIND_INFO, aClientId);
            return CommonUtility.getInteger(clientCount);
        }
        catch (final Exception e)
        {
            log.error("Exception while increament / decrement the bind count", e);
            throw e;
        }
    }

    public static int getTotalBindCount(
            String aInstanceId)
    {
        int totalBindCnt = 0;

        try (
                Jedis jedis = SmppRedisConnectionProvider.getRedis();)
        {
            final Map<String, String> instBindCounter = jedis.hgetAll(RedisKeyConstants.SESSION_BIND_INFO + aInstanceId);

            for (final Map.Entry<String, String> anEntry : instBindCounter.entrySet())
                totalBindCnt += Integer.parseInt(anEntry.getValue());
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the bind counts.", e);
            throw e;
        }

        return totalBindCnt;
    }

    public static void removeAllBindInfo(
            String aInstanceId)
    {

        try (
                Jedis jedis = SmppRedisConnectionProvider.getRedis();
                Pipeline pipe = jedis.pipelined();)
        {
            final Map<String, String> instBindCounter = jedis.hgetAll(RedisKeyConstants.SESSION_BIND_INFO + aInstanceId);

            for (final Map.Entry<String, String> anEntry : instBindCounter.entrySet())
                pipe.hincrBy(RedisKeyConstants.SESSION_BIND_INFO, anEntry.getKey(), -1L * Integer.parseInt(anEntry.getValue()));
            pipe.sync();

            jedis.del(RedisKeyConstants.SESSION_BIND_INFO + aInstanceId);
        }
        catch (final Exception e)
        {
            log.error("Exception while removing all the bind counters.", e);
            throw e;
        }
    }

    public static void removeUnBindInfo(
            List<ISmppInfo> aList)
    {
        // TODO Auto-generated method stub
    }

}
