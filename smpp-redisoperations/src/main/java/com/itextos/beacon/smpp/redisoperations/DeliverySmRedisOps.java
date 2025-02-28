package com.itextos.beacon.smpp.redisoperations;

import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextos.beacon.platform.smpputil.DeliverSmInfo;

import redis.clients.jedis.Jedis;

public class DeliverySmRedisOps
{

    private static final Log log = LogFactory.getLog(DeliverySmRedisOps.class);

    private DeliverySmRedisOps()
    {}

    public static List<DeliverSmInfo> lpopDeliverSm(
            String aClientId)
    {

        try (
                Jedis jedis = SmppRedisConnectionProvider.getSmppDlrRedis(aClientId);)
        {
            final byte[] lPopString = jedis.lpop((RedisKeyConstants.SMPP_DN_QUEUE + aClientId).getBytes());

            if ((lPopString != null) && (lPopString.length > 0))
            {
                final String strFromRedis = new String(lPopString);

                if (log.isDebugEnabled())
                    log.info("strFromRedis=" + strFromRedis);

                final Type                type            = new TypeToken<List<DeliverSmInfo>>()
                                                          {}.getType();
                final List<DeliverSmInfo> listOfDeliverSm = new Gson().fromJson(strFromRedis, type);

                return listOfDeliverSm;
            }
        }
        catch (final Exception exp)
        {
            log.error("problem popping dlr list...", exp);
        }

        return null;
    }

    public static boolean lpushDeliverSm(
            String aClientId,
            String aDliverySmJson)
    {
        boolean pushed = false;

        try (
                Jedis jedis = SmppRedisConnectionProvider.getSmppDlrRedis(aClientId))
        {
            pushed = jedis.lpush((RedisKeyConstants.SMPP_DN_QUEUE + aClientId), aDliverySmJson) > 0;
        }
        catch (final Exception exp)
        {
            log.error("problem pushing back dlr...", exp);
        }
        return pushed;
    }

}
