package com.itextos.beacon.platform.duplicatecheckremoval;

import java.util.Set;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.smslog.DuplicateRemovalLog;

import redis.clients.jedis.Jedis;

public class ExpiryRemovalTask
        implements
        ITimedProcess
{

    int                      index           = 0;
    private static final Log log             = LogFactory.getLog(ExpiryRemovalTask.class);
    PropertiesConfiguration  prop;
    private TimedProcessor   mTimedProcessor = null;
    private boolean          mCanPrrocess    = true;
    private int              mRedisIndex     = 0;

    public ExpiryRemovalTask(
            int aRedisIndex)
    {
        this.mRedisIndex = aRedisIndex;

        mTimedProcessor  = new TimedProcessor("DuplicateCheckRemovel-RedisIndex:" + mRedisIndex, this, TimerIntervalConstant.DUPLICATE_CHECK_EXPIRY_TASK_INTERVAL);
  
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "DuplicateCheckRemovel-RedisIndex:" + mRedisIndex);
    }

    @Override
    public boolean processNow()
    {
        return removeExpiredValues();
    }

    private boolean removeExpiredValues()
    {

        try (
                final Jedis lRedisCon = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.DUPLICATE_CHK, mRedisIndex);)
        {
            if (log.isInfoEnabled())
                log.info("duplicatecheck:.... available keys querying");
            
            DuplicateRemovalLog.log("duplicatecheck:.... available keys querying");
            final Set<String> lKeys = lRedisCon.keys("duplicatecheck:*");

            if (lKeys.isEmpty())
                return false;

            if (log.isInfoEnabled())
                log.info("going to remove expired records...");
            
            DuplicateRemovalLog.log("going to remove expired records...");

            long lRemovedKeysCount = 0;
            for (final String key : lKeys)
                lRemovedKeysCount = lRedisCon.zremrangeByScore(key, 0, System.currentTimeMillis());
            if (log.isInfoEnabled())
                log.info("remove expired elements of size=" + lRemovedKeysCount);

            return true;
        }
        catch (final Exception exp)
        {
            log.error("problem setting expiry " + mRedisIndex, exp);
        }

        return false;
    }

    @Override
    public boolean canContinue()
    {
        return mCanPrrocess;
    }

    @Override
    public void stopMe()
    {
        mCanPrrocess = false;
    }

}
