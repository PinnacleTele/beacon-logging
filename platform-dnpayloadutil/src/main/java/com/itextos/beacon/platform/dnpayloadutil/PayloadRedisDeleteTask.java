package com.itextos.beacon.platform.dnpayloadutil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;

public class PayloadRedisDeleteTask
        implements
        ITimedProcess
{

    private static final Log                            log                       = LogFactory.getLog(PayloadRedisDeleteTask.class);

    public static final String                          PAYLOAD_OPERATION_SUCCESS = "1";

    private final LinkedBlockingQueue<SubmissionObject> mPayloadRedisDeleteQ      = new LinkedBlockingQueue<>(1000);
    private TimedProcessor                              mTimedProcessor           = null;
    private boolean                                     mCanContinue              = true;

    private PayloadRedisDeleteTask()
    {
        start();

        mTimedProcessor = new TimedProcessor("PayloadRedisDeleteTask", this, TimerIntervalConstant.PAYLOAD_DELETE_TASK_RELOAD);
 
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "PayloadRedisDeleteTask");
     }

    public static PayloadRedisDeleteTask getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder
    {

        static final PayloadRedisDeleteTask INSTANCE = new PayloadRedisDeleteTask();

    }

    public void addToInmemQueue(
            SubmissionObject aSubmissionObject)
    {
        if (!mPayloadRedisDeleteQ.add(aSubmissionObject))
            log.error("PayloadRedisDeleteTask QUEUEFULL failed adding object-->" + aSubmissionObject);
    }

    public void start()
    {

        try
        {
            final Iterator<SubmissionObject> lIterator      = mPayloadRedisDeleteQ.iterator();
            final List<SubmissionObject>     lRemoveInRedis = new ArrayList<>();

            while (lIterator.hasNext())
            {
                final SubmissionObject anObj = lIterator.next();

                PayloadProcessor.deletePayload(anObj);

                if (PAYLOAD_OPERATION_SUCCESS.equals(anObj.getDnPayloadStatus()))
                {
                    if (log.isInfoEnabled())
                        log.info("removed payload successfully..." + anObj);

                    lRemoveInRedis.add(anObj);
                }
                else
                    if (log.isInfoEnabled())
                        log.info("remove attempt failed for payload..." + anObj);
            }

            mPayloadRedisDeleteQ.removeAll(lRemoveInRedis);
        }
        catch (final Exception exp)
        {
            log.error("problem delete payload delete task...", exp);
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