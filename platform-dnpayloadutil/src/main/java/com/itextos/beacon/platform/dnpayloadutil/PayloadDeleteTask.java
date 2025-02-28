package com.itextos.beacon.platform.dnpayloadutil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.platform.dnpayloadutil.dao.PayloadInsertInDB;

public class PayloadDeleteTask
        implements
        ITimedProcess
{

    private static final Log     log          = LogFactory.getLog(PayloadDeleteTask.class);

    private final TimedProcessor mTimedProcessor;
    private boolean              mCanContinue = true;

    private PayloadDeleteTask()
    {
        start();

     
        mTimedProcessor = new TimedProcessor("PayloadDeleteTask", this, TimerIntervalConstant.PAYLOAD_DELETE_TASK_RELOAD);
       
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "PayloadDeleteTask");
    }

    public static PayloadDeleteTask getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder
    {

        static final PayloadDeleteTask INSTANCE = new PayloadDeleteTask();

    }

    public static void start()
    {

        try
        {
            if (log.isInfoEnabled())
                log.info("delete payload task invoked...");

            final int lDeletedCnt = PayloadInsertInDB.deletePayload();

            if (log.isInfoEnabled())
                log.info("deleted count=>" + lDeletedCnt);
        }
        catch (final Exception exp)
        {
            log.error("problem during delete processed payload...", exp);
        }
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {
        start();
        return false;
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
    }

}