package com.itextos.beacon.smpp.redisoperations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class Throttler
{

    private static final Log log                  = LogFactory.getLog(Throttler.class);
    private static final int NO_THROTTLE_REQUIRED = 0;

    private Throttler()
    {}

    public static boolean canSend(
            String aClientId,
            long aMaxTps)
    {
        if (aMaxTps <= NO_THROTTLE_REQUIRED)
            return true;

        try (
                final Jedis jedis = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.SMPP_CONSUMER, 1);)
        {
            final String redisKey = RedisKeyConstants.THROTTLER_KEY + aClientId;
            final long   counter  = jedis.llen(redisKey);

            if (log.isDebugEnabled())
                log.debug("adding throttling key=" + redisKey + " maxtps=" + aMaxTps + " counter=" + counter + " counter>= maxtps=" + (counter > aMaxTps));

            if (counter > aMaxTps)
                return false;

            final Long listLen = jedis.rpushx(redisKey, "1");

            if (listLen.longValue() == 0)
                try (
                        final Transaction trans = jedis.multi();)
                {
                    trans.rpush(redisKey, "1");
                    trans.expire(redisKey, 1);
                    trans.exec();
                }
        }
        catch (final Exception exp)
        {
            log.error("Problem checking throttling value allowing submit...", exp);
        }
        return true;
    }

    private static Jedis getJedis()
    {
        final HostAndPort hostAndPort = new HostAndPort("192.168.1.124", 6379);
        final Jedis       jedis       = new Jedis(hostAndPort);
        jedis.auth("nunzioz");
        jedis.select(3);
        return jedis;
    }

    public static void main(
            String[] args)
    {

        for (int i = 0; i < 20; i++)
        {
            final boolean lCanSend = canSend("kp:test", 100);
            System.out.println(System.currentTimeMillis() + " -" + lCanSend);
        }
    }

}
