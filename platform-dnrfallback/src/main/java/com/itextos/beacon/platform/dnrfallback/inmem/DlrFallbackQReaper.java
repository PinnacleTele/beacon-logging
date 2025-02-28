package com.itextos.beacon.platform.dnrfallback.inmem;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.platform.dnrfallback.dao.DlrFallBackDao;

public class DlrFallbackQReaper
        implements
        ITimedProcess,Runnable
{

    private static final Log log = LogFactory.getLog(DlrFallbackQReaper.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final DlrFallbackQReaper INSTANCE = new DlrFallbackQReaper();

    }

    public static DlrFallbackQReaper getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private boolean              canContinue = true;
    private final TimedProcessor mTimedProcessor;

    private DlrFallbackQReaper()
    {
    	
        mTimedProcessor = new TimedProcessor("DlrFallbackTableInserter", this, TimerIntervalConstant.INTERFACE_FALLBACK_TABLE_INSERTER);

        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "DlrFallbackTableInserter");
    
    }

    private static boolean process()
    {
        boolean hasRecord = false;

        try
        {
            final List<IMessage> lRecords      = DlrFallbackQ.getInstance().getMessage();
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
            log.error("ERROR >>>> Exception in FallbackQReaper Reaper thread \n", e);
        }
        return hasRecord;
    }

    private static boolean insertData(
            List<IMessage> aRecords)
    {

        try
        {
            DlrFallBackDao.storeFalbackData(aRecords);

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
    
    
    public void run() {
    	
    	long startTime=System.currentTimeMillis();
    	int loopcount=0;
    	while(true) {
    		loopcount++;
    
    		boolean status=processNow();
    		
    		if(status) {
    			
    			if((System.currentTimeMillis()-startTime)>500||loopcount>10) {
    				
    				break;
    			}
    			
    		}else {
    			
    			break;
    			
    		}
    	}
    }

}