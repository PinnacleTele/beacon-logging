package com.itextos.beacon.httpclienthandover.retry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverData;
import com.itextos.beacon.httpclienthandover.utils.ClientHandoverUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class RedisPusher
        implements
        ITimedProcess

{

    private static class SingletonHolder
    {

        static final RedisPusher INSTANCE = new RedisPusher();

    }

    public static RedisPusher getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final TimedProcessor             mTimedProcessor;
    private boolean                          mCanContinue               = true;
    private static final Log                 log                        = LogFactory.getLog(RedisPusher.class);

    private final BlockingQueue<BaseMessage> mMessagesQueue             = new LinkedBlockingQueue<>(10000);
    private final BlockingQueue<BaseMessage> mCustomerMessagesQueue     = new LinkedBlockingQueue<>(10000);
    private final BlockingQueue<BaseMessage> mMessagesFailQueue         = new LinkedBlockingQueue<>(1000);
    private final BlockingQueue<BaseMessage> mCustomerMessagesFailQueue = new LinkedBlockingQueue<>(1000);
    private static final String              DELIMITER                  = "~";
    private static final String              PARENT_KEY                 = "CH_HTTP_RETRY";
    private static final int                 BATCH_COUNT                = 500;

    private RedisPusher()
    {
        mTimedProcessor = new TimedProcessor("TimerThread-Redis-Pusher", this, TimerIntervalConstant.DLR_HTTP_HANDOVER_REDIS_PUSH_INTERVAL);
        ExecutorSheduler.getInstance().addTask(mTimedProcessor,"TimerThread-Redis-Pusher" );
        mCanContinue = true;
    }

    public void add(
            BaseMessage aMessage)
    {

        try
        {
            mMessagesQueue.offer(aMessage, 5, TimeUnit.SECONDS);
        }
        catch (final InterruptedException e)
        {

            try
            {
                mMessagesFailQueue.put(aMessage);
            }
            catch (final InterruptedException e1)
            {
                e1.printStackTrace();
            }
        }
    }

    public void addCustomerQueue(
            BaseMessage aMessage)
    {

        try
        {
            mCustomerMessagesQueue.offer(aMessage, 5, TimeUnit.SECONDS);
        }
        catch (final InterruptedException e)
        {

            try
            {
                mCustomerMessagesFailQueue.put(aMessage);
            }
            catch (final InterruptedException e1)
            {
                e1.printStackTrace();
            }
        }
    }

    public static void pushDataToRedis(
            List<BaseMessage> messages,
            boolean isCustomerSpecific)
    {
        if (log.isDebugEnabled())
            log.debug("Retry data sending to redis | customer specific: '" + isCustomerSpecific + "'");
        boolean isSucess = false;

        // TODO check pipeline will rollback
        while (!isSucess)
            try (
                    final Jedis jedis = getClientHandoverRedisConnection();
                    Pipeline pipeline = jedis.pipelined();)
            {

                for (final BaseMessage BaseMessage : messages)
                {
                    final ClientHandoverData clientHandoverData = ClientHandoverUtils.getClientHandoverData(BaseMessage.getValue(MiddlewareConstant.MW_CLIENT_ID));

                    updateRetryTime(BaseMessage, clientHandoverData);
                    String retryTime = BaseMessage.getValue(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_TIME);
                    retryTime = DateTimeUtility.getFormattedDateTime(DateTimeUtility.getDateFromString(retryTime, DateTimeFormat.DEFAULT), DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS);
                    final String redisKey = isCustomerSpecific ? retryTime + ":" + BaseMessage.getValue(MiddlewareConstant.MW_CLIENT_ID) : retryTime;

                    pipeline.lpush(PARENT_KEY + ":" + redisKey, getMetaData(BaseMessage));
                }
                pipeline.sync();
                isSucess = true;
            }
            catch (final Exception lE)
            {
                log.error("Redis insert fails while sending the retry meta data", lE);
                isSucess = false;

                CommonUtility.sleepForAWhile();
            }
    }

    public static void updateRetryTime(
            BaseMessage aMessage,
            ClientHandoverData aClientConfiguration)
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, aClientConfiguration.getRetrySleepTimeMills());
        final Date millsAddedDate = calendar.getTime();
        if (log.isDebugEnabled())
            log.debug("Retry Date: '" + millsAddedDate + "' | Current Date: '" + new Date() + "'");

        aMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_TIME, DateTimeUtility.getFormattedDateTime(millsAddedDate, DateTimeFormat.DEFAULT));
    }

    public static String getMetaData(
            BaseMessage aMessage)
    {
        final StringJoiner joiner = new StringJoiner(DELIMITER);
        return joiner.add(aMessage.getValue(MiddlewareConstant.MW_CLIENT_ID)).add(aMessage.getValue(MiddlewareConstant.MW_MESSAGE_ID))
                .add(aMessage.getValue(MiddlewareConstant.MW_CLIENT_HANDOVER_MAX_RETRY_COUNT)).add(aMessage.getValue(MiddlewareConstant.MW_CLIENT_HANDOVER_INITIAL_TIME)).toString();
    }

    private static Jedis getClientHandoverRedisConnection()
    {
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.HTTP_DLR, 1);
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {
        final List<BaseMessage> list = new ArrayList<>();
        mMessagesQueue.drainTo(list, BATCH_COUNT);

        if (!list.isEmpty())
            pushDataToRedis(list, false);

        list.clear();

        mCustomerMessagesQueue.drainTo(list, BATCH_COUNT);

        if (!list.isEmpty())
            pushDataToRedis(list, true);
        list.clear();

        if (!mMessagesFailQueue.isEmpty())
        {
            mMessagesFailQueue.drainTo(list);

            RetryDBHelper.insertRetryMetaDataFailure(list, false);
            list.clear();
        }

        if (!mCustomerMessagesFailQueue.isEmpty())
        {
            mCustomerMessagesFailQueue.drainTo(list);
            RetryDBHelper.insertRetryMetaDataFailure(list, true);
        }

        RetryDBHelper.getRetryMetaDataFailure();

        return (!mMessagesQueue.isEmpty()) || (!mCustomerMessagesFailQueue.isEmpty()) || (!mCustomerMessagesQueue.isEmpty()) || (!mMessagesFailQueue.isEmpty());
    }

    @Override
    public void stopMe()
    {
        if (log.isDebugEnabled())
            log.debug("Inmemory process Redis Pusher stopped externaly.");
        mCanContinue = false;

        if (mTimedProcessor != null)
            mTimedProcessor.stopReaper();
    }

}
