package com.itextos.beacon.platform.dnpcore.inmem;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.platform.dnpcore.dao.NoPayloadRetryDao;

public class NoPayloadRetryQReaper
        implements
        ITimedProcess
{

    private static final Log log = LogFactory.getLog(NoPayloadRetryQReaper.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final NoPayloadRetryQReaper INSTANCE = new NoPayloadRetryQReaper();

    }

    public static NoPayloadRetryQReaper getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private boolean              canContinue = true;
    private final TimedProcessor mTimedProcessor;

    private NoPayloadRetryQReaper()
    {
        mTimedProcessor = new TimedProcessor("NoPayloadRetryQReaper", this, TimerIntervalConstant.INTERFACE_FALLBACK_TABLE_INSERTER);
        

        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "NoPayloadRetryQReaper");
    }

    private static boolean process()
    {
        boolean hasRecord = false;

        try
        {
            final List<IMessage> lRecords      = NoPayloadRetryQ.getInstance().getMessage();
            final int            lTotalDrained = lRecords.size();
            hasRecord = lTotalDrained > 0;

            if (log.isInfoEnabled())
                log.info("Total Messages Drained - " + lTotalDrained);

            if (hasRecord)
            {
                boolean done = false;

                while (!done)
                {
                    if (log.isDebugEnabled())
                        log.debug("Inserting into Database.");

                    done = insertData(lRecords);
                }
            }
        }
        catch (final Exception e)
        {
            log.error("ERROR >>>> Exception in NoPayloadRetryQ Reaper thread \n", e);
        }
        return hasRecord;
    }

    private static boolean insertData(
            List<IMessage> aRecords)
    {

        try
        {
            NoPayloadRetryDao.storeNoPayloadRetryData(aRecords);

            if (log.isDebugEnabled())
                log.debug("DB Inserted succesfully...." + aRecords.size());

            return true;
        }
        catch (final Exception exp)
        {
            log.error("problem inserting interface input data retrying after 10 sec.....", exp);
            CommonUtility.sleepForAWhile(10 * 1000L);
        }
        return false;
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        return process();
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}