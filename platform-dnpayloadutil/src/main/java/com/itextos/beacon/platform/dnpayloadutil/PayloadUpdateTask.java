package com.itextos.beacon.platform.dnpayloadutil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.platform.dnpayloadutil.dao.PayloadInsertInDB;

public class PayloadUpdateTask
        implements
        ITimedProcess
{

    private static final Log                      log            = LogFactory.getLog(PayloadUpdateTask.class);

    private final LinkedBlockingQueue<PayloadKey> mUpdatePayload = new LinkedBlockingQueue<>();
    private final TimedProcessor                  mTimedProcessor;
    private boolean                               mCanContinue   = true;

    private PayloadUpdateTask()
    {
        PayloadDeleteTask.getInstance();
        start();

        mTimedProcessor = new TimedProcessor("PayloadUpdateTask", this, TimerIntervalConstant.PAYLOAD_UPDATE_TASK_RELOAD);
   
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "PayloadUpdateTask");
     }

    public static PayloadUpdateTask getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder
    {

        static final PayloadUpdateTask INSTANCE = new PayloadUpdateTask();

    }

    public void addKey(
            PayloadKey key)
            throws ItextosException
    {
        if (!mCanContinue)
            throw new ItextosException("Already shutdown invoked...");

        if (mUpdatePayload.size() > 50000)
            throw new ItextosException("Inmemoryqueue for update full...50000");

        mUpdatePayload.add(key);
    }

    public void start()
    {
        final List<PayloadKey> list = new ArrayList<>();

        try
        {

            while (!mUpdatePayload.isEmpty())
            {
                if (log.isDebugEnabled())
                    log.debug("payload update timer task....Inmemory size =" + list.size());

                mUpdatePayload.drainTo(list, 1000);
                PayloadInsertInDB.updatePayload(list);

                list.clear();
            }
        }
        catch (final Exception exp)
        {
            log.error("problem during update task due to...adding all key to inmemory", exp);
            if (!list.isEmpty())
                mUpdatePayload.addAll(list);
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
