package com.itextos.beacon.http.clouddataprocessor.process;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;

public class TemporaryStorage
        implements
        ITimedProcess,Runnable
{

    private final static Log      log       = LogFactory.getLog(TemporaryStorage.class);
    int                           batchSize = 100;
    private BlockingQueue<String> queue     = null;
    private TimedProcessor        reaper    = null;

    static class SINGLETON_HOLDER
    {

        static final TemporaryStorage INSTANCE = new TemporaryStorage();

    }

    private TemporaryStorage()
    {
        queue  = new LinkedBlockingQueue<>();
      
        reaper = new TimedProcessor("Temporary storage", this, TimerIntervalConstant.DLR_HTTP_HANDOVER_HANDOVER_RETRY_REAPER);
  
        ExecutorSheduler.getInstance().addTask(reaper, "Temporary storage");
        
    }

    public static TemporaryStorage getInstance()
    {
        return SINGLETON_HOLDER.INSTANCE;
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
  
    public void add(
            String aData)
    {
        queue.add(aData);
    }

    public void add(
            List<String> aData)
    {
        for (final String data : aData)
            queue.add(data);
    }

    public List<String> get(
            int count)
    {
        final List<String> list = new ArrayList();
        queue.drainTo(list, count);
        return list;
    }

    private boolean canProcessNow()
    {
        if (log.isDebugEnabled())
            log.debug(" List Size : " + queue.size() + " : batch size : " + batchSize);

        if (queue.size() >= batchSize)
            return true;

        return false;
    }

    public void processRecords()
    {
        processRecords(false);
    }

    public void processRecords(
            boolean aMandatory)
    {
        final boolean sizeBased = canProcessNow();

        if (log.isDebugEnabled())
            log.debug(" Process now based on size : " + sizeBased + ". Is mandatory process :" + aMandatory);

        final boolean processNow = aMandatory || sizeBased;

        if (processNow)
            generateBulkDataRequest();
    }

    private void generateBulkDataRequest()
    {

        try
        {
            int queueSize = queue.size();

            if (queueSize > 0)
            {
                if (log.isDebugEnabled())
                    log.debug(" Initial Queue Size : " + queueSize);

                final ArrayList<String> list = new ArrayList<>();
                queue.drainTo(list, batchSize);

                queueSize = queue.size();

                if (log.isDebugEnabled())
                    log.debug(" Queue size after passed : " + queueSize + ". To be passed list size : " + list.size());

                final HttpCall call = new HttpCall();
                HttpCall.generateBulkRequest(list, null);
            }
        }
        catch (final Exception e)
        {
            log.error(" Exception while process the data ", e);
        }
    }

    public void cleanupProcess()
    {
        processRecords(true);

        if (log.isInfoEnabled())
            log.info(" All the records in the list has been inserted.");

        stopMe();
    }

    @Override
    public boolean canContinue()
    {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean processNow()
    {
        generateBulkDataRequest();
        return (queue.size() > 0);
    }

    @Override
    public void stopMe()
    {
    	/*
        if (reaper != null)
            reaper.stopReaper();
            */
    }

}
