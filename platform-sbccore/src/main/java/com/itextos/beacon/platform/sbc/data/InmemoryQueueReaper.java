package com.itextos.beacon.platform.sbc.data;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.platform.sbc.dao.DBHandler;

public abstract class InmemoryQueueReaper
        implements
        ITimedProcess
{

    private static final Log     log                       = LogFactory.getLog(InmemoryQueueReaper.class);
    private static final int     MAX_RECORDS_PER_ITERATION = 1000;
    private final InmemoryQueue  mInmemoryQueue;
    private final String         mTableName;
    private final TimedProcessor mTimedProcessor;
    private boolean              canContinue               = true;

    private long starttime=System.currentTimeMillis();
    
    protected InmemoryQueueReaper(
            InmemoryQueue aInMemoryQueue,
            String aTableName)
    {
        mInmemoryQueue  = aInMemoryQueue;
        mTableName      = aTableName;
  
        mTimedProcessor = new TimedProcessor("TimerThread-InmemoryReaper-" + aTableName, this, TimerIntervalConstant.SCHEDULE_MESSAGE_TABLE_INSERTER);
        
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "TimerThread-InmemoryReaper-" + aTableName);
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        if (log.isDebugEnabled()) {
         
        	if((System.currentTimeMillis()-starttime)>4000) {
        		starttime=System.currentTimeMillis();
        		log.debug("Queue size ......: " + mInmemoryQueue.isEmpty());
        	}
        }

        if (!mInmemoryQueue.isEmpty())
        {
            final List<MessageRequest> lRecords = mInmemoryQueue.getRecords(MAX_RECORDS_PER_ITERATION);

            try
            {
                DBHandler.insertRecords(lRecords, mTableName);
            }
            catch (final ItextosException e)
            {
                addToList(lRecords);
            }
            return true;
        }
        return false;
    }

    private void addToList(
            List<MessageRequest> aRecords)
    {
        boolean isDone     = false;
        int     retryCount = 0;

        while (!isDone)
        {

            try
            {
                mInmemoryQueue.addRecords(aRecords);
                isDone = true;
            }
            catch (final Exception e1)
            {
                retryCount++;
                log.error("Excception while adding the data in to inmemory. Retry Attempt " + retryCount, e1);
            }

            if (retryCount > 10)
                log.error("Loosing data of size " + aRecords.size());
        }
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}