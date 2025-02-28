package com.itextos.beacon.platform.topic2elasticsearch.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.platform.elasticsearchutil.EsProcess;
import com.itextos.beacon.platform.elasticsearchutil.utility.ESBulkAsyncListener;
import com.itextos.beacon.platform.elasticsearchutil.utility.Kafka2ESConstants;
import com.itextos.beacon.platform.elasticsearchutil.utility.Kafka2ESJSONUtil;
import com.itextos.beacon.smslog.K2ELog;
import com.itextos.beacon.smslog.K2EPushLog;

public class K2ETableWrapper
        implements
        ITimedProcess
{

    private static final Log                 log                    = LogFactory.getLog(K2ETableWrapper.class);

    private static final int                 MAX_QUEUE_SIZE         = 10000;
    private static final int                 DEFAULT_SLEEP_TIME_SEC = 1;
    private static final int                 DEFAULT_BATCH_SIZE     = 1000;
    

    private final Component                  mComponent;

    private int                              mSleepTimeSecs         = DEFAULT_SLEEP_TIME_SEC;
    private int                              mBatchSize             = DEFAULT_BATCH_SIZE;
    private final BlockingQueue<BaseMessage> messagesInmemQueue     = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
    
    private BulkRequest                     bulkRequest     = null;
    private BulkRequest                     fmsgBulkRequest = null;

    private final TimedProcessor             timedProcessor;

    private boolean                          canContinue            = true;
 
    public K2ETableWrapper(
            Component aComponent,
            Table2DBInserterId aTableInsertId)
    {
        super();
        mComponent     = aComponent;

        timedProcessor = new TimedProcessor("T2DbTableWrapper : "+mComponent.getKey(), this, mSleepTimeSecs);
 
        ExecutorSheduler.getInstance().addTask(timedProcessor, "T2DbTableWrapper : "+ mComponent.getKey());
        
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

            try {
				processData(toProcess);
				writeData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        /*
         * else
         * if (log.isDebugEnabled())
         * log.debug("No messages to process");
         */
    }

    
    private void processData(
    		List<BaseMessage>	 pDataList)
            throws Exception
    {

        for (final BaseMessage data : pDataList)
        {
            final BaseMessage iMsg     = data;
            JSONObject     dataJSON = null;

            try
            {
                if (mComponent.getKey().equals(Component.K2E_SUBMISSION.getKey()))
                    dataJSON = Kafka2ESJSONUtil.buildSubJSON(iMsg);
                else
                    if (mComponent.getKey().equals(Component.K2E_DELIVERIES.getKey()))
                        dataJSON = Kafka2ESJSONUtil.buildDelJSON(iMsg);
            }
            catch (final Exception ex)
            {
                log.error("Error while processing Message Object", ex);
                K2ELog.log("Error while processing Message Object \n "+ErrorMessage.getStackTraceAsString(ex));
                ex.printStackTrace(System.err);
                if (log.isDebugEnabled())
                    log.debug(iMsg.toString());
            }

            if (dataJSON == null)
            {
                log.error("Unable to build JSON Object from Message Object");
                
                K2ELog.log("Unable to build JSON Object from Message Object");


                if (log.isDebugEnabled())
                    log.debug(iMsg.toString());

                continue;
            }

            final String        msgId         = CommonUtility.nullCheck(dataJSON.get(Kafka2ESConstants.ESIndexUniqueColumn), true);
            final UpdateRequest updateRequest = new UpdateRequest(Kafka2ESConstants.ESIndexName, msgId)
                    .doc(dataJSON.toJSONString(), XContentType.JSON)
                    .docAsUpsert(true);
            updateRequest.retryOnConflict(Kafka2ESConstants.ESRetryConflictCount);
            bulkRequest.add(updateRequest);

            final String baseMsgId = CommonUtility.nullCheck(dataJSON.get(Kafka2ESConstants.ESFmsgIndexUniqueColumn), true);

            if (!"".equals(baseMsgId))
            {
                JSONObject fmsgJSON = null;
                if (mComponent.getKey().equals(Component.K2E_SUBMISSION))
                    fmsgJSON = Kafka2ESJSONUtil.buildSubFMSGJSON(dataJSON, baseMsgId);
                else
                    if (mComponent.getKey().equals(Component.K2E_DELIVERIES.getKey()))
                        fmsgJSON = Kafka2ESJSONUtil.buildDelFMSGJSON(dataJSON, baseMsgId);

                if (fmsgJSON != null)
                {
                    final UpdateRequest fmsgupdateRequest = new UpdateRequest(Kafka2ESConstants.ESFmsgIndexName, baseMsgId)
                            .doc(fmsgJSON.toJSONString(), XContentType.JSON)
                            .docAsUpsert(true);
                    fmsgupdateRequest.retryOnConflict(Kafka2ESConstants.ESRetryConflictCount);
                    fmsgBulkRequest.add(fmsgupdateRequest);
                }
            }

            
        }
    }

    
    
    private void writeData()
            throws Exception
    {

       
        if (log.isDebugEnabled())
            log.debug("Executing ES BulkAsync ...");
        
        K2ELog.log("Executing ES BulkAsync ...");


        // ESClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        
        if(bulkRequest.requests().size()>0) {
        final ESBulkAsyncListener bal = new ESBulkAsyncListener(bulkRequest, false);
        EsProcess.getInstance().getKafka2ElasticSearchEsConnection().bulkAsync(bulkRequest, RequestOptions.DEFAULT, bal);
        K2EPushLog.log("Executing ES BulkAsync ...bulkRequest  size : "+bulkRequest.requests().size());

        bulkRequest = new BulkRequest();
        }

        if (fmsgBulkRequest.requests().size() > 0)
        {
            if (log.isDebugEnabled())
                log.debug("Executing ES BulkAsync for Full Message Info...");
            
            K2ELog.log("Executing ES BulkAsync for Full Message Info...");

            // ESClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            final ESBulkAsyncListener balFmsg = new ESBulkAsyncListener(fmsgBulkRequest, true);
            EsProcess.getInstance().getKafka2ElasticSearchEsConnection().bulkAsync(fmsgBulkRequest, RequestOptions.DEFAULT, balFmsg);
            K2EPushLog.log("Executing ES BulkAsync ...fmsgBulkRequest size : "+fmsgBulkRequest.requests().size());

            fmsgBulkRequest = new BulkRequest();
        }

      
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