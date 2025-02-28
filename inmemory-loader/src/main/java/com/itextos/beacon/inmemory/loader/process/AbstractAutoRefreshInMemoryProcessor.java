package com.itextos.beacon.inmemory.loader.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;

public abstract class AbstractAutoRefreshInMemoryProcessor
        extends
        InmemoryProcessor
        implements
        ITimedProcess
{

    private static final Log     log          = LogFactory.getLog(AbstractAutoRefreshInMemoryProcessor.class);

    private final TimedProcessor mTimedProcessor;
    private boolean              mCanContinue = true;

    protected AbstractAutoRefreshInMemoryProcessor(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    
        mTimedProcessor = new TimedProcessor("TimerThread-" + mInmemoryInput.getInmemoryId(), this, mInmemoryInput.getSleepSec());
       
        mCanContinue = mInmemoryInput.isAutoRefreshRequired();
        ExecutorSheduler.getInstance().addTask(mTimedProcessor,  ""+mInmemoryInput.getInmemoryId());
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {
    	String module=System.getenv("module");
    	
    	if(module==null||module.equals("dbgwejb")) {
    		
    		 getDataFromDB();
    		 
    	}else {
    		
        	String dbgw=System.getenv("dbgw");

    		if(dbgw!=null&&dbgw.equals("ejb")) {
    			
    			getDataFromEJBServer();

    		}else {
    			
    			getDataFromDB();

    		}
    		
    	}
       
        return false;
    }

    @Override
    public void stopMe()
    {
        if (log.isDebugEnabled())
            log.debug("Inmemory process " + mInmemoryInput.getInmemoryId() + " stopped externaly.");
        mCanContinue = false;

       
        if (mTimedProcessor != null)
            mTimedProcessor.stopReaper();
		

    }

}
