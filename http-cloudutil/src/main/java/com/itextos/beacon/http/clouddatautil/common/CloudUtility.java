package com.itextos.beacon.http.clouddatautil.common;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public final class CloudUtility
{

    private final static Log   log                  = LogFactory.getLog(CloudUtility.class);

    public final static String REDIS_PROP_KEY_NAME  = "foodpanda";
    public final static String REDIS_ENTRY_KEY_NAME = REDIS_PROP_KEY_NAME + ":requests";

    public static List<String> splitIntoList(
            String aData)
    {
        return Arrays.asList(aData.split(","));
    }

    public static HashMap<String, String> stringToMap(
            String ticket)
    {
        final HashMap<String, String> map = new HashMap<>();
        if (ticket == null)
            return map;
        final String[] tockens = ticket.split("&");

        if ((tockens != null) && (tockens.length > 0))
            for (final String lTocken : tockens)
            {
                final String[] subtockens = lTocken.split("=");

                try
                {
                    final String key   = URLDecoder.decode(CommonUtility.nullCheck(subtockens[0]), StandardCharsets.UTF_8.name());
                    final String value = URLDecoder.decode(CommonUtility.nullCheck(subtockens[1]), StandardCharsets.UTF_8.name());
                    map.put(key, value);
                }
                catch (final Exception ex)
                {}
            }
        else
        {
            final String[] subtockens = ticket.split("=");

            try
            {
                final String key   = URLDecoder.decode(CommonUtility.nullCheck(subtockens[0]), StandardCharsets.UTF_8.name());
                final String value = URLDecoder.decode(CommonUtility.nullCheck(subtockens[1]), StandardCharsets.UTF_8.name());
                map.put(key, value);
            }
            catch (final Exception ex2)
            {}
        }
        return map;
    }

    public static void pushToRedis(
            List<String> aRequests,
            int aRedisIndex)
    {

        try (
                Jedis jedis = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.CLOUD_ACCEPTOR, aRedisIndex);
                Pipeline pipeline = jedis.pipelined();)
        {
            Response<Long> lpush = null;

            if (log.isDebugEnabled())
                log.debug("Pushing messages to Redis. Redis Index : '" + aRedisIndex + "'");

            for (final String req : aRequests)
                lpush = pipeline.lpush(CloudUtility.REDIS_ENTRY_KEY_NAME, req);

            pipeline.sync();

            if (log.isInfoEnabled())
                log.info("Records available in the Redis : " + (lpush == null ? "0" : lpush.get()));
        }
        catch (final Exception e)
        {
            log.error("Exception while pushing into Redis", e);
            log.warn("Assuming that none of messages are pushed to the redis. again tryong to push to the same redis");

            try
            {
                Thread.sleep(1000);
            }
            catch (final InterruptedException e1)
            {
                // ignore it
            }
            pushToRedis(aRequests, aRedisIndex);
        }
    }

    public static void setToOuterObject(
            JSONObject aJson,
            String aParameters,
            HashMap<String, String> aMap)
    {
        aJson.put(aParameters, CommonUtility.nullCheck(aMap.get(aParameters)));
    }

    public static void setToMessageObject(
            Map<String, Object> aMessageObject,
            String aParameters,
            HashMap<String, String> aMap)
    {

        try
        {
            aMessageObject.put(aParameters, CommonUtility.nullCheck(aMap.get(aParameters)));
        }
        catch (final Exception e)
        {
            log.error("Exception while parsing the querystring into Json Object ", e);
        }
    }

    public static void setToMessageObjectAsList(
            Map<String, Object> aMessageObject,
            String aParameters,
            HashMap<String, String> aMap)
    {

        try
        {
            aMessageObject.put(aParameters, CloudUtility.splitIntoList(CommonUtility.nullCheck(aMap.get(aParameters))));
        }
        catch (final Exception e)
        {
            log.error("Exception while parsing the querystring into Json Object ", e);
        }
    }

    public static CloudDataConfig getCloudDataConfig(

            String aAuthKey)
    {
        final CloudDataConfigInfo clientConfigurationInfo = (CloudDataConfigInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CLOUD_INTERFACE_CONFIGURATION);
        return clientConfigurationInfo.getCloudDataConfigUsingAuthKey(aAuthKey);
    }

}