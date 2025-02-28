package com.itextos.beacon.platform.topic2table.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.topic2table.dbinfo.TableInserterInfo;
import com.itextos.beacon.platform.topic2table.dbinfo.TableInserterInfoCollection;
import com.itextos.beacon.platform.topic2table.es.DeliveriesK2ES;
import com.itextos.beacon.platform.topic2table.es.K2ES;
import com.itextos.beacon.platform.topic2table.es.SubmissionK2ES;
import com.itextos.beacon.platform.topic2table.inserter.DynamicFullMessageTableInserter;
import com.itextos.beacon.platform.topic2table.inserter.DynamicTableInserter;
import com.itextos.beacon.platform.topic2table.inserter.ITableInserter;
import com.itextos.beacon.platform.topic2table.inserter.StaticFullMessageTableInserter;
import com.itextos.beacon.platform.topic2table.inserter.StaticTableInserter;

public class T2DbTableWrapper
        implements
        ITimedProcess
{

    private static final Log                 log                    = LogFactory.getLog(T2DbTableWrapper.class);

    private static final int                 MAX_QUEUE_SIZE         = 10000;
    private static final int                 DEFAULT_SLEEP_TIME_SEC = 1;
    private static final int                 DEFAULT_BATCH_SIZE     = 1000;
    

    private final Component                  mComponent;
    private final Table2DBInserterId         mTableInsertId;

    private int                              mSleepTimeSecs         = DEFAULT_SLEEP_TIME_SEC;
    private int                              mBatchSize             = DEFAULT_BATCH_SIZE;
    private boolean                          isStaticTableInserter  = true;
    private boolean                          isStaticFullMessageTableInserter  = true;

    private final BlockingQueue<BaseMessage> messagesInmemQueue     = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);

    private final TimedProcessor             timedProcessor;

    private boolean                          canContinue            = true;
 
    private K2ES k2es=null;
  
    public T2DbTableWrapper(
            Component aComponent,
            Table2DBInserterId aTableInsertId)
    {
        super();
        mComponent     = aComponent;
        mTableInsertId = aTableInsertId;

        
        loadBasicInfo();
        if(mTableInsertId == Table2DBInserterId.SUBMISSION) {
        	
        	loadFullMessageBasicInfo();
        	
        	k2es=new SubmissionK2ES();
        	
        }else if(mTableInsertId == Table2DBInserterId.DELIVERIES) {
        	
        	k2es=new DeliveriesK2ES();
        }
        timedProcessor = new TimedProcessor("T2DbTableWrapper : "+mComponent.getKey(), this, mSleepTimeSecs);
 
        ExecutorSheduler.getInstance().addTask(timedProcessor, "T2DbTableWrapper : "+ mComponent.getKey());
        
    }

  

   
    private void loadBasicInfo()
    {
        final TableInserterInfoCollection lTableInserterInfoCollection = (TableInserterInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.TABLE_INSERTER_INFO);
        final TableInserterInfo           lTableInserterInfo           = lTableInserterInfoCollection.getTableInserterInfo(mTableInsertId);

        if (lTableInserterInfo == null) {
         //   throw new ItextosRuntimeException("Unable to find the table insert information for component '" + mComponent + "' and table inserter id '" + mTableInsertId + "'");
        
        	log.error("Unable to find the table insert information for component '" + mComponent + "' and table inserter id '" + mTableInsertId + "'");
        }else {
        mSleepTimeSecs        = lTableInserterInfo.getSleepSecs();
        mBatchSize            = lTableInserterInfo.getBatchSize();
        isStaticTableInserter = lTableInserterInfo.isStaticTableInserter();

        if (log.isDebugEnabled())
        {
            log.debug("Table Inserter component  " + mComponent);
            log.debug("Table Inserter Id         " + mTableInsertId);
            log.debug("Table Inserter Sleep Sec  " + mSleepTimeSecs);
            log.debug("Table Inserter Batch Size " + mBatchSize);
            log.debug("Table Inserter is Static  " + isStaticTableInserter);
        }
        }
    }

    private void loadFullMessageBasicInfo()
    {
        final TableInserterInfoCollection lTableInserterInfoCollection = (TableInserterInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.TABLE_INSERTER_INFO);
        final TableInserterInfo           lTableInserterInfo           = lTableInserterInfoCollection.getTableInserterInfo(Table2DBInserterId.FULL_MESSAGE);

        if (lTableInserterInfo == null) {
         //   throw new ItextosRuntimeException("Unable to find the table insert information for component '" + mComponent + "' and table inserter id '" + mTableInsertId + "'");
        
        	log.error("Unable to find the table insert information for component '" + mComponent + "' and table inserter id '" + Table2DBInserterId.FULL_MESSAGE + "'");
        }else {
           isStaticFullMessageTableInserter = lTableInserterInfo.isStaticTableInserter();

        if (log.isDebugEnabled())
        {
            log.debug("Table Inserter component  " + mComponent);
            log.debug("Table Inserter Id         " + Table2DBInserterId.FULL_MESSAGE);
            log.debug("Table Inserter Sleep Sec  " + mSleepTimeSecs);
            log.debug("Table Inserter Batch Size " + mBatchSize);
            log.debug("Table Inserter is Static  " + isStaticFullMessageTableInserter);
        }
        }
    }
    public boolean isQueue() {

		return messagesInmemQueue.isEmpty();
	
	}
    public void process(
            boolean aOnSize)
    {
        List<BaseMessage> toProcess = null;

        int               size      = 1;

        if (aOnSize)
        {
            toProcess = new ArrayList<>(mBatchSize);
            size      = mBatchSize;
        }
        else
        {
            final int qsize = messagesInmemQueue.size();
            size      = qsize;
            toProcess = new ArrayList<>(qsize);
        }

        messagesInmemQueue.drainTo(toProcess, size);

        if (!toProcess.isEmpty())
        {
            if (log.isDebugEnabled())
                log.debug("Messages to process " + toProcess.size());

            ITableInserter inserter = null;

            if (isStaticTableInserter)
                inserter = new StaticTableInserter(mComponent, mTableInsertId, toProcess);
            else
                inserter = new DynamicTableInserter(mComponent, mTableInsertId, toProcess);

            if (log.isDebugEnabled())
                log.debug("Calling process method in Processor '" + inserter.getClass().getName() + "'");

            inserter.process();

            if(mTableInsertId==Table2DBInserterId.SUBMISSION) {
            	
            	   ITableInserter fullmessageinserter = null;

                   if (isStaticFullMessageTableInserter)
                	   fullmessageinserter = new StaticFullMessageTableInserter(mComponent, Table2DBInserterId.SUBMISSION, toProcess);
                   else
                	   fullmessageinserter = new DynamicFullMessageTableInserter(mComponent, Table2DBInserterId.SUBMISSION, toProcess);

                   if (log.isDebugEnabled())
                       log.debug("Calling process method in Processor '" + inserter.getClass().getName() + "'");

                   fullmessageinserter.process();
                   
                   if(k2es!=null) {
                   k2es.pushtoElasticSearch(toProcess);
                   }
            }else if(mTableInsertId==Table2DBInserterId.DELIVERIES) {
            	
            	 if(k2es!=null) {
                     k2es.pushtoElasticSearch(toProcess);
                     }
            }
            if (log.isDebugEnabled())
                log.debug("Completed processing the records");
        }
        /*
         * else
         * if (log.isDebugEnabled())
         * log.debug("No messages to process");
         */
    }

    public void addMessage(
            BaseMessage aBaseMessage)
            throws InterruptedException
    {
        messagesInmemQueue.put(aBaseMessage);

        if (mBatchSize < messagesInmemQueue.size())
            process(true);
    }
 
    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
    	
    	process(false);
        return !isQueue();
        
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}