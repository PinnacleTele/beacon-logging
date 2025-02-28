package com.itextos.beacon.platform.promokannelrediscleaner.process.redisprocess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class RedisCleanerProcessor
{

    private static final Log         log                                           = LogFactory.getLog(RedisCleanerProcessor.class);

    private static final String      PROP_CLUSTER_NAME                             = "cluster.name";
    private static final String      CONFIG_PARAM_PROMO_KANNEL_REDIS_CLEANDER_TYPE = "promo.kannel.redis.cleaner.type";
    protected static final Component THIS_COMPONENT                                = Component.PROMO_KANNEL_REDIS_CLEANER;

    private ClusterType              mClusterType;
    private RedisProcessType         mRedisProcessType;

    public void start()
            throws ItextosException
    {
        mClusterType      = validateCluster();
        mRedisProcessType = validateRedisProcessType();

        startProcessThreads();
    }

    private void startProcessThreads()
    {
        final int lRedisPoolCount = RedisConnectionProvider.getInstance().getRedisPoolCount(mClusterType, THIS_COMPONENT);

        if (log.isDebugEnabled())
            log.debug("Total Redis Pool count '" + lRedisPoolCount + "' Process Type '" + mRedisProcessType + "'");

        for (int index = 0; index < lRedisPoolCount; index++)
        {
            final int redisIndex = index + 1;

            switch (mRedisProcessType)
            {
                case FLUSH_DB:
                    new FlushDbProcess(mClusterType, redisIndex);
                    break;

                case INSERT_ALL_RECORDS:
                    new InsertAllRecordsProcess(mClusterType, redisIndex);
                    break;

                case INSERT_FOR_SEPECIFIC_CLIENTS:
                    new InsertClientSpecificRecords(mClusterType, redisIndex);
                    break;

                case NO_ACTION:
                    log.fatal("No Action required, as the Config value is set to 0");
                    break;

                default:
                    log.fatal("No Action required, as the No Config value is available");
                    break;
            }
        }
    }

    private static RedisProcessType validateRedisProcessType()
            throws ItextosException
    {
        final String redisProcessType = CommonUtility.nullCheck(
                ((ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG)).getConfigValue(CONFIG_PARAM_PROMO_KANNEL_REDIS_CLEANDER_TYPE),
                true);

        if (redisProcessType.isBlank())
            throw new ItextosException("Empty Promo Kannel clean process specified in the config params.");

        final RedisProcessType temp = RedisProcessType.getRedisProcess(redisProcessType);

        if (temp == null)
            throw new ItextosException("Invalid RedisProcessType specified. RedisProcessType '" + redisProcessType + "'");
        return temp;
    }

    private static ClusterType validateCluster()
            throws ItextosException
    {
        final String clusterName = CommonUtility.nullCheck(System.getProperty(PROP_CLUSTER_NAME), true);

        if (clusterName.isBlank())
            throw new ItextosException("Empty cluster name specified.");

        final ClusterType temp = ClusterType.getCluster(clusterName);

        if (temp == null)
            throw new ItextosException("Invalid cluster name specified. Cluster '" + clusterName + "'");
        return temp;
    }

}
