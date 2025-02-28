package com.itextos.beacon.commonlib.scheduler.logging;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.itextos.beacon.commonlib.scheduler.logging.db.DatabaseSchedulerLogging;
import com.itextos.beacon.commonlib.scheduler.logging.db.DatabaseTriggerLogging;
import com.itextos.beacon.commonlib.scheduler.logging.redis.RedisSchedulerLogging;
import com.itextos.beacon.commonlib.scheduler.logging.redis.RedisTriggerLogging;
import com.itextos.beacon.commonlib.scheduler.util.SchedulerUtility;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class LoggingFactory
{

    private static final String PROP_KEY_REDIS_ENABLED    = "logging.redis.enabled";
    private static final String PROP_KEY_DATABASE_ENABLED = "logging.database.enabled";
    private static final String DEFAULT_ENABLED           = "0";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final LoggingFactory INSTANCE = new LoggingFactory();

    }

    public static LoggingFactory getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<LoggingType, ISchedulerLogging> mSchedulerLoggingMap = new EnumMap<>(LoggingType.class);
    private final Map<LoggingType, ITriggerLogging>   mTriggerLoggingMap   = new EnumMap<>(LoggingType.class);

    private final BlockingQueue<JobData>              mJobToBeExecuted     = new LinkedBlockingQueue<>();
    private final BlockingQueue<JobExecutedData>      mJobWasExecuted      = new LinkedBlockingQueue<>();

    private final Properties                          mQuartzProperties    = SchedulerUtility.getProperties();

    private LoggingFactory()
    {
        mSchedulerLoggingMap.put(LoggingType.DB, new DatabaseSchedulerLogging());
        mTriggerLoggingMap.put(LoggingType.DB, new DatabaseTriggerLogging());

        mSchedulerLoggingMap.put(LoggingType.REDIS, new RedisSchedulerLogging());
        mTriggerLoggingMap.put(LoggingType.REDIS, new RedisTriggerLogging());
    }

    public ISchedulerLogging getSchedulerLogging(
            LoggingType aLoggingType)
    {
        return mSchedulerLoggingMap.get(aLoggingType);
    }

    public ITriggerLogging getTriggerLogging(
            LoggingType aLoggingType)
    {
        return mTriggerLoggingMap.get(aLoggingType);
    }

    public boolean isRedisLoggingEnabled()
    {
        if (mQuartzProperties == null)
            return false;
        return CommonUtility.isEnabled(CommonUtility.nullCheck(mQuartzProperties.getOrDefault(PROP_KEY_REDIS_ENABLED, DEFAULT_ENABLED), true));
    }

    public boolean isDatabaseLoggingEnabled()
    {
        if (mQuartzProperties == null)
            return false;
        return CommonUtility.isEnabled(CommonUtility.nullCheck(mQuartzProperties.getOrDefault(PROP_KEY_DATABASE_ENABLED, DEFAULT_ENABLED), true));
    }

    public void addJobTobeExecuted(
            JobData aJobData)
    {
        mJobToBeExecuted.add(aJobData);

        System.out.println("mJobToBeExecuted  " + aJobData);
    }

    public void addJobWasExecuted(
            JobExecutedData aJobExecutedData)
    {
        mJobWasExecuted.add(aJobExecutedData);

        System.out.println("mJobWasExecuted   " + aJobExecutedData);
    }

    public List<JobData> getJobTobeExecuted()
    {
        return getJobTobeExecuted(1000);
    }

    public List<JobData> getJobTobeExecuted(
            int aCount)
    {
        int size = mJobToBeExecuted.size();
        size = size > aCount ? size : aCount;

        final List<JobData> returnValue = new ArrayList<>(size);
        mJobToBeExecuted.drainTo(returnValue, size);
        return returnValue;
    }

    public List<JobExecutedData> getJobWasExecuted()
    {
        return getJobWasExecuted(1000);
    }

    public List<JobExecutedData> getJobWasExecuted(
            int aCount)
    {
        int size = mJobWasExecuted.size();
        size = size > aCount ? size : aCount;

        final List<JobExecutedData> returnValue = new ArrayList<>(size);
        mJobWasExecuted.drainTo(returnValue, size);
        return returnValue;
    }

}