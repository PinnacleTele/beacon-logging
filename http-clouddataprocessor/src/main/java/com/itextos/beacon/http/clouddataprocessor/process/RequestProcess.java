package com.itextos.beacon.http.clouddataprocessor.process;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.http.clouddataprocessor.DataFromRedisHolder;
import com.itextos.beacon.http.clouddatautil.common.CloudDataConfig;
import com.itextos.beacon.http.clouddatautil.common.CloudUtility;
import com.itextos.beacon.http.clouddatautil.common.Constants;

import redis.clients.jedis.Jedis;

public class RequestProcess
        implements
        Runnable
{

    private static final Log log = LogFactory.getLog(RequestProcess.class);

    private final int        redisIndex;
    private final boolean    canContinue;
    private final String     authKey;

    public RequestProcess(
            int aIndex,
            String aAuthKey)
    {
        redisIndex  = aIndex;
        canContinue = true;
        authKey     = aAuthKey;
    }

    @Override
    public void run()
    {
        if (log.isDebugEnabled())
            log.debug("Request process thread started '" + Thread.currentThread().getName() + "' and the index " + redisIndex);

        while (canContinue)
        {
            boolean               isDataAvailable = false;
            final CloudDataConfig cloudDataConfig = CloudUtility.getCloudDataConfig(authKey);

            if (log.isDebugEnabled())
                log.debug("Bulk is enable for this customer so we are parsing the request as JSON");
            isDataAvailable = popFromRedisWithBulk(cloudDataConfig);

            if (!isDataAvailable)
            {
                if (log.isDebugEnabled())
                    log.debug("No request available in the Redis..... Sleeping for " + cloudDataConfig.getProcessWaitSecs() + " second.");

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
    }

    private boolean popFromRedisWithBulk(
            CloudDataConfig aCloudDataConfig)
    {
        Jedis              jedis     = null;
        final List<String> reqString = new ArrayList<>(100);

        try
        {
            jedis = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.CLOUD_ACCEPTOR, redisIndex);

            int                       count       = 0;
            String                    data        = null;
            final DataFromRedisHolder redisHolder = new DataFromRedisHolder();
            final String              key         = Constants.REDIS_ENTRY_KEY_NAME + ":" + aCloudDataConfig.getAuthenticationKey();

            while ((count < 100) && ((data = jedis.rpop(key)) != null))
            {
                ++count;
                log.info("Before Swap Data -- '" + data + "' count - '" + count + "'");

                // data = swappingTheData(data, aCloudDataConfig.getSwapFrom(),
                // aCloudDataConfig.getSwapTo());

                log.info("After Swap Data -- '" + data + "'");
                redisHolder.addRequest(aCloudDataConfig.getClientId(), data);
            }
            if (count > 0)
                redisHolder.process();
            final Long dataCount = jedis.llen(key);
            return dataCount > 0;
        }
        catch (final Exception e)
        {
            log.error("Exception while fetching data from redis. But will propcess already poped up requests.", e);
        }
        finally
        {
            if (jedis != null)
                jedis.close();
        }
        return false;
    }

    private static String swappingTheData(
            String aData,
            String aSwapFrom,
            String aSwapTo)
    {
        aData = aData.replace(aSwapFrom, aSwapTo);
        return aData;
    }

    public static void main(
            String[] args)
    {
        System.out.println("{\"ackid\":\"11111243613848340006200\",\"time\":\"2018-12-06 18:48:34\",\"status\":{\"code\":\"200\",\"desc\":\"Request accepted\"}}".contains("Request accepted"));
    }

}