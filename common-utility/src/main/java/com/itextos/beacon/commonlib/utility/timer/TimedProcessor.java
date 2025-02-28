package com.itextos.beacon.commonlib.utility.timer;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class TimedProcessor
        extends
        Thread
{

    private static final Log    log                       = LogFactory.getLog(TimedProcessor.class);

    private static final long   DEFAULT_SLEEP_TIME_MILLIS = 1000;

    private final String        mThreadName;
    private final ITimedProcess mTimedProcess;
    private long                mSleepTimeInMilliSeconds  = 1000;
    private boolean             mStoppedExternally        = false;

    public TimedProcessor(
            String aThreadName,
            ITimedProcess aTimedProcess,
            TimerIntervalConstant aTimerIntervalConstant)
    {
        mThreadName   = aThreadName;
        mTimedProcess = aTimedProcess;

        final long sleepTimeSecs = TimerProcesorIntervalProvider.getInstance().getTimerIntervalInSecs(aTimerIntervalConstant);
        mSleepTimeInMilliSeconds = sleepTimeSecs <= 0 ? DEFAULT_SLEEP_TIME_MILLIS : (sleepTimeSecs * 1000L);

        setName(aThreadName);
    }

    @Deprecated
    public TimedProcessor(
            String aThreadName,
            ITimedProcess aTimedProcess,
            int aTimerIntervalInSecs)
    {
        mThreadName   = aThreadName;
        mTimedProcess = aTimedProcess;
        final int sleepSecs = aTimerIntervalInSecs <= 0 ? 30 : aTimerIntervalInSecs;
        mSleepTimeInMilliSeconds = sleepSecs * 1000L;
        setName(aThreadName);
    }

    @Override
    public void run()
    {
        if (log.isInfoEnabled())
            log.info("TimedProcessor for " + mThreadName + " is started with the sleep time " + mSleepTimeInMilliSeconds + " milliseconds.");

        boolean canContinue                        = true;
        boolean continueNextExecutuionWithoutSleep = false;

        while (canContinue)
        {
            // if (log.isDebugEnabled())
            // log.debug(mTimedProcess.getClass() + " Invoking processNow");


        	synchronized (mTimedProcess)
            {
                continueNextExecutuionWithoutSleep = mTimedProcess.processNow();
            }

            canContinue = mTimedProcess.canContinue() && (!mStoppedExternally);

            // if (log.isDebugEnabled())
            // log.debug(mTimedProcess.getClass() + " canContinue " + canContinue);

            if (!canContinue)
                break;

            if (!continueNextExecutuionWithoutSleep)
            {
                // if (log.isDebugEnabled())
                // log.debug(mTimedProcess.getClass() + " Going on sleep for " +
                // mSleepTimeInMilliSeconds + " millis");


                CommonUtility.sleepForAWhile(mSleepTimeInMilliSeconds);

                // if (log.isDebugEnabled())
                // log.debug(mTimedProcess.getClass() + " Gotup after sleep for " +
                // mSleepTimeInMilliSeconds + " millis");

                canContinue = mTimedProcess.canContinue() && (!mStoppedExternally);
            }

            if (!canContinue)
                break;
        }
    }

    public void stopReaper()
    {
        mStoppedExternally = true;
        interrupt();
    }

}