package com.itextos.beacon.platform.dlrretry.util;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.RoundRobin;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler2;
import com.itextos.beacon.platform.dlrretry.process.DnBasedRedisPoller;

import redis.clients.jedis.Jedis;

public class DnBasedRetryRedisProcessor
{

    private static final Log   log       = LogFactory.getLog(DnBasedRetryRedisProcessor.class);
    public static final String REDIS_KEY = "dnretry";

    private static class SingletonHolder
    {

        static final DnBasedRetryRedisProcessor INSTANCE = new DnBasedRetryRedisProcessor();

    }

    public static DnBasedRetryRedisProcessor getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private DnBasedRetryRedisProcessor()
    {
        startPollers();
    }

    private static void startPollers()
    {
        final int lTotalRedises = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.DLR_WAIT_RETRY);

        for (int index = 1; index <= lTotalRedises; index++)
        {
            final String             thredName = "DnRetryPollar-" + index;
            final DnBasedRedisPoller drp       = new DnBasedRedisPoller(index, thredName);
            final Thread             th        = new Thread(drp, thredName);

            ExecutorSheduler2.getInstance().addTask(th, thredName);
        }
    }

    public static void pushToRedis(
            DeliveryObject aDeliveryObject)
    {
        if (aDeliveryObject == null)
            return;

        final Date dnDelvDate = aDeliveryObject.getActualDeliveryTime();

        if (log.isDebugEnabled())
            log.debug("DN Delivery Date : " + dnDelvDate);

        final Calendar lCalender = Calendar.getInstance();
        lCalender.setLenient(false);
        lCalender.setTime(dnDelvDate);
        lCalender.add(Calendar.SECOND, aDeliveryObject.getRetryInterval());

        final Date lRetryIterval = lCalender.getTime();

        if (log.isDebugEnabled())
            log.debug("After adding retry interval DN Delivery Date : " + lRetryIterval);

        final String dateValue = DateTimeUtility.getFormattedDateTime(lRetryIterval, DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM_SS);

        if (log.isDebugEnabled())
            log.debug("Formated DN Delivery Date : " + dateValue);

        final String redisKey = CommonUtility.combine(':', REDIS_KEY, dateValue);
        pushToRedis(redisKey, aDeliveryObject);
    }

    private static void pushToRedis(
            String aRedisKey,
            DeliveryObject aDeliveryObject)
    {

        try (
                Jedis jedis = getNextRedisConnection();)
        {
            jedis.lpush(aRedisKey, aDeliveryObject.getJsonString());
        }
    }

    private static Jedis getNextRedisConnection()
    {
        final int lCurRedisIndex = getRedisCount();

        if (log.isDebugEnabled())
            log.debug("DLR Retry Wait redis index : " + lCurRedisIndex);

        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.DLR_WAIT_RETRY, lCurRedisIndex);
    }

    private static int getRedisCount()
    {
        final int lDlrRetryWaitRedisCnt = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.DLR_WAIT_RETRY);

        return RoundRobin.getInstance().getCurrentIndex("dlrwaitretry", lDlrRetryWaitRedisCnt);
    }

}