package com.itextos.beacon.http.cloudacceptor.common;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.http.clouddatautil.common.CloudDataConfig;
import com.itextos.beacon.http.clouddatautil.common.CloudUtility;
import com.itextos.beacon.http.clouddatautil.common.Constants;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class RedisPusher
        implements
        Runnable
{

    private final static Log log    = LogFactory.getLog(RedisPusher.class);

    private final int        redisIndex;
    private final boolean    stopMe = false;
    private final String     key;

    public RedisPusher(
            int aRedisIndex,
            String aKey)
    {
        redisIndex = aRedisIndex;
        key        = aKey;
    }

    @Override
    public void run()
    {
        log.warn("RedisPusher starting for the Redis Index : '" + redisIndex + "'");

        while (!stopMe)
            try
            {
                final CloudDataConfig cloudDataConfig = CloudUtility.getCloudDataConfig(key);
                final List<String>    requests        = TemporaryInMemoryCollection.getInstance().get(cloudDataConfig.getRedisBatchSize(), key);

                if ((requests != null) && (requests.size() > 0))
                    pushToRedis(requests, redisIndex, cloudDataConfig);
                else
                {
                    if (log.isDebugEnabled())
                        log.debug("No request available in the memory..... Sleeping for " + cloudDataConfig.getProcessWaitSecs() + " second.");

                    try
                    {
                        Thread.sleep(cloudDataConfig.getProcessWaitSecs());
                    }
                    catch (final Exception e)
                    {
                        // ignore
                    }
                }
            }
            catch (final Exception e)
            {
                log.error("Exception while pushing the data to redis.", e);
            }
    }

    public static void pushToRedis(
            List<String> aRequests,
            int aRedisIndex,
            CloudDataConfig aCloudDataConfig)
    {

        try

        (
                Jedis jedis = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.CLOUD_ACCEPTOR, aRedisIndex);
                Pipeline pipeline = jedis.pipelined();)
        {
            Response<Long> lpush = null;

            if (log.isDebugEnabled())
                log.debug("Pushing messages to Redis. Redis Index : '" + aRedisIndex + "'");
            final String key = Constants.REDIS_ENTRY_KEY_NAME + ":" + aCloudDataConfig.getAuthenticationKey();
            for (final String req : aRequests)
                lpush = pipeline.lpush(key, req);

            pipeline.sync();

            if (log.isInfoEnabled())
                log.info("Records available in the Redis : " + (lpush == null ? "0" : lpush.get()));
        }
        catch (final Exception e)
        {
            log.error("Exception while pushing into Redis", e);
            log.warn("Assuming that none of messages are pushed to the redis. Adding to the inmemory queue.");

            TemporaryInMemoryCollection.getInstance().add(aRequests, aCloudDataConfig.getAuthenticationKey());
        }
    }

}