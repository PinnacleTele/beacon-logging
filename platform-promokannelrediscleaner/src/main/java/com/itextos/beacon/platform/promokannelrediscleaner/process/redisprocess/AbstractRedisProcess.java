package com.itextos.beacon.platform.promokannelrediscleaner.process.redisprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;

import redis.clients.jedis.Jedis;

abstract class AbstractRedisProcess
        implements
        IRedisProcess,
        ITimedProcess
{

    private static final Log         log                       = LogFactory.getLog(AbstractRedisProcess.class);

    protected static final Component THIS_COMPONENT            = Component.PROMO_KANNEL_REDIS_CLEANER;

    private static final String      DLR_STARTS_WITH           = "dlr*";
    private static final int         MAX_ENTRIES_PER_ITERATION = 1000;

    protected ClusterType            mClusterType;
    protected int                    mRedisIndex;
    private boolean                  mCanContinue              = true;
    private final TimedProcessor     mTimedProcessor;

    AbstractRedisProcess(
            ClusterType aClusterType,
            int aRedisIndex)
    {
        mClusterType    = aClusterType;
        mRedisIndex     = aRedisIndex;

       
        mTimedProcessor = new TimedProcessor("PromoRedisDataCleaner-" + mRedisIndex, this, TimerIntervalConstant.PROMO_KANNEL_REDIS_CLEANER_INTERVAL);
  
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "PromoRedisDataCleaner-" + mRedisIndex);
        
    }

    @Override
    public List<String> getRedisKeys()
    {
        final List<String> toReturn = new ArrayList<>(MAX_ENTRIES_PER_ITERATION);

        try (
                Jedis jedis = getRedisConnection();)
        {
            final Set<String> lKeys = jedis.keys(DLR_STARTS_WITH);

            if (!lKeys.isEmpty())
            {
                final int len   = lKeys.size() > MAX_ENTRIES_PER_ITERATION ? MAX_ENTRIES_PER_ITERATION : lKeys.size();
                int       count = 0;

                for (final String s : lKeys)
                {
                    if (log.isDebugEnabled())
                        log.debug("Key is '" + s + "'");

                    toReturn.add(s);
                    count++;
                    if (count == len)
                        break;
                }
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while getting redis keys for the redis id '" + mRedisIndex + "'", e);
        }

        if (log.isDebugEnabled())
            log.debug("Total number of redis key to process " + toReturn.size());

        return toReturn;
    }

    protected void deleteKeys(
            List<String> aKeys)
    {

        try (
                Jedis jedis = getRedisConnection();)
        {
            final String[] keysArray = aKeys.toArray(new String[0]);
            jedis.del(keysArray);
        }
        catch (final Exception e)
        {
            log.error("Exception while deletein the keys from redis.", e);
            throw e;
        }
    }

    protected Jedis getRedisConnection()
    {
        return RedisConnectionProvider.getInstance().getConnection(mClusterType, THIS_COMPONENT, mRedisIndex);
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {
        return process();
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
    }

}