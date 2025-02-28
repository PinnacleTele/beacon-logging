package com.itextos.beacon.platform.sbpcore.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.platform.sbpcore.dao.DBPoller;
import com.itextos.beacon.smslog.DebugLog;
import com.itextos.beacon.smslog.SchedulePollerLog;

public class ScheduleBlockoutPollarHolder
        implements
        ITimedProcess
{

    private static final Log log = LogFactory.getLog(ScheduleBlockoutPollarHolder.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final ScheduleBlockoutPollarHolder INSTANCE = new ScheduleBlockoutPollarHolder();

    }

    public static ScheduleBlockoutPollarHolder getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private static final String                                 SCHEDULE    = "schedule";
    private static final String                                 BLOCOUT     = "blockout";
    private final Map<String, Map<Integer, AbstractDataPoller>> mPollers    = new HashMap<>();
    private boolean                                             canContinue = true;
    private final TimedProcessor                                mTimedProcessor;

    private ScheduleBlockoutPollarHolder()
    {
        startPollars();
       
        mTimedProcessor = new TimedProcessor("TimerThread-SchedulePollerStarter", this, TimerIntervalConstant.SCHEDULE_MESSAGE_TABLE_READER);
    
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "TimerThread-SchedulePollerStarter");
     }

    private void startPollars()
    {
        final Map<String, List<String>> appInstanceIds = DBPoller.getAllInstances();

        if (appInstanceIds != null)
        {
            if (log.isDebugEnabled())
                log.debug("Get Instance ids from Runtime : " + appInstanceIds);
            
            DebugLog.log("Get Instance ids from Runtime : " + appInstanceIds);

            final List<String> aInstanceIds = appInstanceIds.get("all");

            for (final String lInstanceId : aInstanceIds)
            {
                final Integer instanceId = CommonUtility.getInteger(lInstanceId);
                addToList(SCHEDULE, instanceId, new SchedulePoller(instanceId));
                addToList(BLOCOUT, instanceId, new BlockoutPoller(instanceId));
            }
        }
        else
            loadDynamicPollersBasedOnInstances();
    }

    private void loadDynamicPollersBasedOnInstances()
    {
        final Map<String, List<String>> appInstanceIds = DBPoller.getInstanceIds();
        if (log.isDebugEnabled())
            log.debug("Get Instance ids from DB : " + appInstanceIds);
        
        SchedulePollerLog.log("Get Instance ids from DB : " + appInstanceIds);

        if (!appInstanceIds.isEmpty())
        {

            if (appInstanceIds.containsKey(SCHEDULE))
            {
                final List<String> aInstanceIds = appInstanceIds.get(SCHEDULE);

                for (final String lInstanceId : aInstanceIds)
                    startPoller(SCHEDULE, lInstanceId);
            }

            if (appInstanceIds.containsKey(BLOCOUT))
            {
                final List<String> aInstanceIds = appInstanceIds.get(BLOCOUT);

                for (final String lInstanceId : aInstanceIds)
                    startPoller(BLOCOUT, lInstanceId);
            }
        }
    }

    private void startPoller(
            String aType,
            String aInstanceId)
    {
        final Integer instanceId         = CommonUtility.getInteger(aInstanceId);
        final boolean lNeedToStartPoller = needToStartPoller(aType, instanceId);

        if (log.isDebugEnabled())
            log.debug("Need to start Poller for " + aType + " instanceid " + instanceId + " status " + lNeedToStartPoller);

        if (lNeedToStartPoller)
        {
            if (SCHEDULE.equalsIgnoreCase(aType))
                addToList(SCHEDULE, instanceId, new SchedulePoller(instanceId));
            else
                if (BLOCOUT.equalsIgnoreCase(aType))
                    addToList(BLOCOUT, instanceId, new BlockoutPoller(instanceId));
        }
        else
            log.info("Poller already running for " + aType + " and instance id " + aInstanceId);
    }

    private boolean needToStartPoller(
            String aType,
            Integer aInstanceId)
    {
        final Map<Integer, AbstractDataPoller> lMap = mPollers.get(aType);
        if (lMap != null)
            return !lMap.containsKey(aInstanceId);
        return true;
    }

    private void addToList(
            String aType,
            Integer aInstanceId,
            AbstractDataPoller aAbstractPoller)
    {
        final Map<Integer, AbstractDataPoller> pollerList = mPollers.computeIfAbsent(aType, k -> new HashMap<>());
        pollerList.put(aInstanceId, aAbstractPoller);
        log.info("Poller started for " + aType + " and instance id " + aInstanceId);
    }

    @Override
    public void stopMe()
    {

        for (final Entry<String, Map<Integer, AbstractDataPoller>> entry : mPollers.entrySet())
        {
            final Map<Integer, AbstractDataPoller> values = entry.getValue();
            for (final AbstractDataPoller poller : values.values())
                poller.stopMe();
        }
        canContinue = false;
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        loadDynamicPollersBasedOnInstances();
        return false;
    }

}