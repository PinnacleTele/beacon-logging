package com.itextos.beacon.platform.cappingcheck;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler2;

import redis.clients.jedis.Jedis;

public class CappingMessageReaper
        implements
        ITimedProcess
{

    private static final Log log             = LogFactory.getLog(CappingMessageReaper.class);

    private int              mIterateCounter = 0;
    private int              mRedisIndex     = 0;
    private TimedProcessor   mTimedProcessor = null;
    private boolean          mCanPrrocess    = true;

    public CappingMessageReaper(
            int aRedisIndex)
    {
        this.mRedisIndex = aRedisIndex;

        
        mTimedProcessor  = new TimedProcessor("CappingMessageReaper-RedisIndex:" + mRedisIndex, this, TimerIntervalConstant.TIMEBOUND_MESSAGE_REAPER);
        
        ExecutorSheduler.getInstance().addTask(mTimedProcessor,"CappingMessageReaper-RedisIndex:");
        checkForPreviousHour(mRedisIndex);
    }

    @Override
    public boolean canContinue()
    {
        return mCanPrrocess;
    }

  
    @Override
    public boolean processNow()
    {
        if (log.isDebugEnabled())
            log.debug("Checking the expiry for the current date..");

        final long lCurrStartTime = System.currentTimeMillis();

        checkAndRemoveForExpiry(true, mRedisIndex);
        mIterateCounter++;

        if (mIterateCounter >= 1000)
        {
            if (log.isDebugEnabled())
                log.debug("Checking the expiry for previous hour..");

            checkForPreviousHour(mRedisIndex);
            mIterateCounter = 0;
        }
        final long lEndTime = System.currentTimeMillis();

        try
        {
            final long lTimeDifference = lEndTime - lCurrStartTime;

            if (log.isDebugEnabled())
                log.debug("Process start Time : '" + lCurrStartTime + "' EndTime : '" + lEndTime + "' time difference : '" + lTimeDifference + "'");

            if (lTimeDifference > 0)
            {
                if ((lTimeDifference > 1000) && (lTimeDifference < 2000))
                    return true;

                if (lTimeDifference >= 2000)
                {
                    processMissedTimes(lCurrStartTime, mRedisIndex);
                    return true;
                }

                CommonUtility.sleepForAWhile(1000 - lTimeDifference);
                return true;
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while processing removing the redis entries...", e);
        }

        return false;
    }

    private static void checkForPreviousHour(
            int aRedisIndex)
    {
        checkAndRemoveForExpiry(false, aRedisIndex);
    }

    private static void checkAndRemoveForExpiry(
            boolean aCheckForCurrentTime,
            int aRedisId)
    {

        try (
                Jedis lJedisCon = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.CAPPING_CHK, aRedisId);)
        {
            if (log.isDebugEnabled())
                log.debug("Working for the connection pool '" + aRedisId + "' and for the '" + (aCheckForCurrentTime ? "Current Time" : "Previous Time"));

            Set<String> lKeys        = null;

            String      lPattern     = DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS);
            long        lCompareTime = Long.parseLong(lPattern);

            if (aCheckForCurrentTime)
            {
                lPattern = CappingConstants.REDIS_KEY_TIMECHECK_EXP + lPattern;

                if (log.isDebugEnabled())
                    log.debug("Pattern List :'" + lPattern + "' and Current Time : '" + lCompareTime + "'");

                lKeys = lJedisCon.keys(lPattern);
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Looking for all the keys to remove greater then one hour entries..");

                final Calendar lCalendarObj = Calendar.getInstance();
                lCalendarObj.setLenient(false);
                lCalendarObj.add(Calendar.HOUR, -1);

                lCompareTime = Long.parseLong(DateTimeUtility.getFormattedDateTime(lCalendarObj.getTime(), DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS));

                lKeys        = lJedisCon.keys(CappingConstants.REDIS_KEY_TIMECHECK_EXP + "*");
            }

            if ((lKeys != null) && !lKeys.isEmpty())
            {
                final RemoveRedisEntries lRemoveEntries = new RemoveRedisEntries(aRedisId, lKeys, lCompareTime);
           
                
                ExecutorSheduler2.getInstance().addTask(lRemoveEntries,  "RemoveEntries-" + lPattern);
                
      
               
            }
        }
        catch (final Exception e)
        {
            log.error("Exception occer while remove the expiry data form redis ..", e);
        }
    }

    private static void processMissedTimes(
            long aStartTime,
            int aRedisIndex)
    {

        try
        {
            final Date     parse     = DateTimeUtility.getDateFromString(DateTimeUtility.getFormattedDateTime(new Date(aStartTime), DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS),
                    DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS);
            final long     lEndDt    = DateTimeUtility
                    .getDateFromString(DateTimeUtility.getFormattedDateTime(new Date(aStartTime), DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS), DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS)
                    .getTime();

            final Calendar lFromTime = Calendar.getInstance();
            lFromTime.setLenient(false);
            lFromTime.setTime(parse);
            lFromTime.add(Calendar.SECOND, 1);

            long lTotRecords = 0;

            while (lFromTime.getTimeInMillis() < lEndDt)
            {
                final String lPattern = DateTimeUtility.getFormattedDateTime(lFromTime.getTime(), DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD_HH_MM_SS);

                try (
                        Jedis lRedisCon = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.CAPPING_CHK, aRedisIndex);)
                {
                    final Set<String> lKeys = lRedisCon.keys(lPattern);

                    if ((lKeys != null) && !lKeys.isEmpty())
                    {
                        lTotRecords = lTotRecords + lKeys.size();
                        final RemoveRedisEntries lRemoveEntries = new RemoveRedisEntries(aRedisIndex, lKeys, lFromTime.getTimeInMillis());
              
                        
                        ExecutorSheduler2.getInstance().addTask(lRemoveEntries,  "MissedRemovedEntries-" + lPattern);
                                  }
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }

                lFromTime.add(Calendar.SECOND, 1);
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void stopMe()
    {
        mCanPrrocess = false;
    }

}
