package com.itextos.beacon.commonlib.accountsync;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

class RedisOperations
{

    private static final Log log = LogFactory.getLog(RedisOperations.class);

    private RedisOperations()
    {}

    static void updateAccount(
            Map<String, Map<String, String>> aAccountsToUpdate)
    {

        try (
                Jedis jedis = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.ACCOUNT_SYNC, 1);
                Pipeline pipe = jedis.pipelined();)
        {

            for (final Entry<String, Map<String, String>> entry : aAccountsToUpdate.entrySet())
            {
                pipe.del(entry.getKey()); // To Remove any old data available.
                pipe.hmset(entry.getKey(), entry.getValue());
                pipe.hset(entry.getKey(), "REDIS_LAST_UPDATED", DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
            }
            pipe.sync();
        }
        catch (final Exception aSomeException)
        {
            log.error("Exception while inserting / updating account into redis:", aSomeException);
            throw aSomeException;
        }
    }

    static void deleteAccount(
            List<String> aAccountToRemove)
    {

        try (
                Jedis jedis = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.ACCOUNT_SYNC, 1);)
        {
            final String[] toDelete = new String[aAccountToRemove.size()];
            jedis.del(aAccountToRemove.toArray(toDelete));
        }
        catch (final Exception aSomeException)
        {
            log.error("Exception while deleting account from redis:", aSomeException);
            throw aSomeException;
        }
    }

}