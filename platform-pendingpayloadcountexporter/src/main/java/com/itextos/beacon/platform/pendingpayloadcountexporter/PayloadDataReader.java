package com.itextos.beacon.platform.pendingpayloadcountexporter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class PayloadDataReader
        implements
        ITimedProcess
{

    private static final Log    log             = LogFactory.getLog(PayloadDataReader.class);

    private static final int    REFRESH_MINUTES = CommonUtility.getInteger(System.getProperty("pending.payload.refresh.interval.mins"), 15) * 60;
    private static final String REDIS_KEYS      = "dnpayload-expire:*";
    private static final int    REDIS_KEYS_LEN  = "dnpayload-expire:".length();

    private boolean             canContinue     = true;
    private TimedProcessor      mTimedProcessor = null;

    public PayloadDataReader()
    {
    	
        mTimedProcessor = new TimedProcessor("PayloadDataReader", this, REFRESH_MINUTES);
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "PayloadDataReader");
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        Prometheus.resetOldValues();

        checkforPayload(ClusterType.BULK);
        checkforPayload(ClusterType.TRANSACTION);
        checkforPayload(ClusterType.OTP);

        return false;
    }

    private static void checkforPayload(
            ClusterType aClusterType)
    {
        final int poolCount = RedisConnectionProvider.getInstance().getRedisPoolCount(aClusterType, Component.DN_PAYLOAD);

        if (log.isDebugEnabled())
            log.debug("Checking Pending Payload Messages");

        for (int index = 1; index <= poolCount; index++)
        {
            if (log.isDebugEnabled())
                log.debug("Checking for the payload messages for cluster '" + aClusterType + "' and Redis Index '" + index + "'");

            try (
                    Jedis lConnection = RedisConnectionProvider.getInstance().getConnection(aClusterType, Component.DN_PAYLOAD, index);)
            {
                checkForMessages(aClusterType, index, lConnection);
            }
        }
    }

    private static void checkForMessages(
            ClusterType aClusterType,
            int aIndex,
            Jedis aConnection)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Getting the keys from Redis for the cluster '" + aClusterType + "' and Redis Index '" + aIndex + "'");

            final Set<String> lKeys = aConnection.keys(REDIS_KEYS);

            if ((lKeys != null) && !lKeys.isEmpty())
                getCounts(aClusterType, aIndex, aConnection, lKeys);
            else
                if (log.isDebugEnabled())
                    log.debug("No Keys avaialble for the cluster '" + aClusterType + "' and Redis Index '" + aIndex + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while getting payload details from ClusterType '" + aClusterType + "' and Redis Index '" + aIndex + "'", e);
        }
    }

    private static void getCounts(
            ClusterType aClusterType,
            int aIndex,
            Jedis aConnection,
            Set<String> aKeys)
    {

        try (
                Pipeline pipe = aConnection.pipelined();)
        {
            final Map<String, Response<Long>> counts = new TreeMap<>();

            if (log.isDebugEnabled())
                log.debug("Getting the counts for the keys from Redis for the cluster '" + aClusterType + "' and Redis Index '" + aIndex + "'");

            for (final String s : aKeys)
            {
                final Response<Long> lHlen = pipe.hlen(s);
                counts.put(s, lHlen);
            }
            pipe.sync();

            exportToPrometheus(aClusterType, aIndex, counts);
        }
    }

    private static void exportToPrometheus(
            ClusterType aClusterType,
            int aIndex,
            Map<String, Response<Long>> aCounts)
    {
        if (log.isDebugEnabled())
            log.debug("Exporting Payload Counts ClusterType '" + aClusterType + "' and Index '" + aIndex + "'");

        for (final Entry<String, Response<Long>> entry : aCounts.entrySet())
        {
            final String key       = entry.getKey();
            final String dateTime  = key.substring(REDIS_KEYS_LEN);
            final long   keysCount = entry.getValue().get();
            final String year      = dateTime.substring(0, 2);
            final String month     = dateTime.substring(2, 4);
            final String date      = dateTime.substring(4, 6);
            final String hour      = dateTime.substring(6, 8);

            if (log.isDebugEnabled())
                log.debug("Exporting Payload Counts ClusterType '" + aClusterType + "' and Index '" + aIndex + "' Redis Key '" + dateTime + "' " + year + "-" + month + "-" + date + " " + hour
                        + " Counts " + keysCount);

            for (int index = 0; index < keysCount; index++)
                Prometheus.setGaugeValue(aClusterType.getKey(), aIndex + "", year, month, date, hour, keysCount);
        }
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}