package com.itextos.beacon.commonlib.redisstatistics.monitor;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.redisstatistics.monitor.stats.RedisMonitor;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler2;

import redis.clients.jedis.Jedis;

public class RedisStatusMonitorHolder
        implements
        ITimedProcess
{

    private static final Log log = LogFactory.getLog(RedisStatusMonitorHolder.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final RedisStatusMonitorHolder INSTANCE = new RedisStatusMonitorHolder();

    }

    public static RedisStatusMonitorHolder getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final TimedProcessor                                         mTimedProcessor;
    private boolean                                                      mCanContinue            = true;
    private Map<ClusterType, Map<Component, Map<Integer, RedisMonitor>>> mRedisMonitorStatistics = null;
    private boolean                                                      initalRun               = true;

    private RedisStatusMonitorHolder()
    {
    	
        mTimedProcessor = new TimedProcessor("RedisStatisticsCollector", this, TimerIntervalConstant.REDIS_STATISTICS_READER);
  
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "RedisStatisticsCollector");
        
    }

    public Map<Component, Map<Integer, RedisMonitor>> getRedisMonitorStats(
            ClusterType aClusterType)
    {
        if (mRedisMonitorStatistics == null)
            readRedisStatistics();
        return mRedisMonitorStatistics.get(aClusterType);
    }

    public Map<Integer, RedisMonitor> getRedisMonitorStats(
            ClusterType aClusterType,
            Component aComponent)
    {
        final Map<Component, Map<Integer, RedisMonitor>> lMap = getRedisMonitorStats(aClusterType);

        if (lMap == null)
            return null;
        return lMap.get(aComponent);
    }

    public RedisMonitor getRedisMonitorStats(
            ClusterType aClusterType,
            Component aComponent,
            int aRedisPoolIndex)
    {
        final Map<Integer, RedisMonitor> lRedisMonitorStats = getRedisMonitorStats(aClusterType, aComponent);

        if (lRedisMonitorStats == null)
            return null;
        return lRedisMonitorStats.get(aRedisPoolIndex);
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {

        try
        {
            readRedisStatistics();
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Redis Statistics", e);
        }
        return false;
    }

    private void readRedisStatistics()
    {
        final Map<ClusterType, Map<Component, Map<Integer, RedisMonitor>>> tempRedisMonitorStatistics = new EnumMap<>(ClusterType.class);

        log.error("As not cluster was specified. This will look up for all the clusters");
        final Set<ClusterType> lClusterTypes = RedisConfigLoader.getInstance().getClusterTypes();

        for (final ClusterType clusterType : lClusterTypes)
            processForCluster(initalRun, clusterType, tempRedisMonitorStatistics);
        initalRun               = false;
        mRedisMonitorStatistics = tempRedisMonitorStatistics;
    }

    private static void processForCluster(
            boolean aInitalRun,
            ClusterType aClusterType,
            Map<ClusterType, Map<Component, Map<Integer, RedisMonitor>>> aTempRedisMonitorStatistics)
    {
        final List<Component> lClusterRedisTypeMap = RedisConfigLoader.getInstance().getClusterRedisTypeMap(aClusterType);

        if ((lClusterRedisTypeMap != null) && (!lClusterRedisTypeMap.isEmpty()))
        {
            final Map<Component, Map<Integer, RedisMonitor>> lMap = aTempRedisMonitorStatistics.computeIfAbsent(aClusterType, k -> new EnumMap<>(Component.class));

            for (final Component component : lClusterRedisTypeMap)
            {
                final int redisPoolCount = RedisConnectionProvider.getInstance().getRedisPoolCount(aClusterType, component);

                if (redisPoolCount > 0)
                {
                    final Map<Integer, RedisMonitor> redisMonitorMap    = lMap.computeIfAbsent(component, k -> new HashMap<>());
                    RedisMonitor                     firstMonitorObject = null;

                    for (int index = 1; index <= redisPoolCount; index++)
                        try (
                                final Jedis jedis = RedisConnectionProvider.getInstance().getConnection(aClusterType, component, index);)
                        {
                            RedisMonitor redisMonitor = null;

                            if (index == 1)
                            {
                                redisMonitor       = new RedisMonitor(jedis, true);
                                firstMonitorObject = redisMonitor;
                            }
                            else
                                redisMonitor = new RedisMonitor(firstMonitorObject, jedis, false);

                            if (aInitalRun)
                                redisMonitor.run();
                            else
                            	
                            	
                            	
                            	ExecutorSheduler2.getInstance().addTask(redisMonitor, "redisMonitor");

                            redisMonitorMap.put(index, redisMonitor);
                        }
                }
            }
        }
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
       
    }

}