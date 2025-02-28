package com.itextos.beacon.platform.sbc.data;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler2;
import com.itextos.beacon.platform.sbc.dao.DBHandler;

public class InmemBlockoutQReaper
        implements
        Runnable
{

    private static final Log log = LogFactory.getLog(InmemBlockoutQReaper.class);

    private InmemBlockoutQReaper()
    {
    	         
         ExecutorSheduler2.getInstance().addTask(this,  "InmemBlockoutQReaper");
    }

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final InmemBlockoutQReaper INSTANCE = new InmemBlockoutQReaper();

    }

    public static InmemBlockoutQReaper getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    boolean mDone = false;

    public void stop()
    {
        mDone = true;
    }

    @Override
    public void run()
    {

        try
        {
            while (!mDone)
                try
                {
                    final List<MessageRequest> lRecords      = InmemBlockoutQueue.getInstance().getRecords();
                    final int                  lTotalDrained = lRecords.size();

                    if (log.isInfoEnabled() && (lTotalDrained != 0))
                        log.info("InmemBlockoutQReaper() Total Messages Drained - " + lTotalDrained);

                    if (lTotalDrained == 0)
                        goToSleep();

                    boolean isDone = lRecords.isEmpty();
                    while (!isDone)
                        try
                        {
                            DBHandler.insertRecords(lRecords, DBHandler.TABLE_NAME_BLOCKOUT);
                            isDone = true;
                            if (log.isInfoEnabled())
                                log.info("DB Inserted succesfully...." + lRecords.size());
                        }
                        catch (final Exception exp)
                        {
                            log.error("problem inserting blockout retrying after 10 sec.....", exp);
                            goToSleep();
                        }
                }
                catch (final Exception e)
                {
                    log.error("InmemBlockoutQReaper ERROR >>>> While draining the msg from the Q\n", e);
                }
        }
        catch (final Exception e)
        {
            log.error("ERROR >>>> Exception in InmemBlockoutQReaper Reaper thread \n", e);
        }
    }

    private void goToSleep()
    {

        try
        {
            Thread.sleep(10000);
        }
        catch (final InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
