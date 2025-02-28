package com.itextos.beacon.smpp.concatenate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.smpp.objects.request.SmppMessageRequest;

class DbOperationInMemory
        implements
        ITimedProcess
{

    private static final Log                        log                      = LogFactory.getLog(DbOperationInMemory.class);

    private final ClusterType                       mClusterType;
    private final BlockingQueue<SmppMessageRequest> mSmppMessageRequestQueue = new LinkedBlockingQueue<>(5000);
    private final TimedProcessor                   mTimedProcessor;
    private boolean                                 mCanContinue             = true;

    DbOperationInMemory(
            ClusterType aClusterType)
    {
        mClusterType    = aClusterType;
        
        mTimedProcessor = new TimedProcessor("SmppConcateDbInserter-" + aClusterType, this, TimerIntervalConstant.SMPP_DLR_FALLBACK_TABLE_READER);
 
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "SmppConcateDbInserter-" + aClusterType);
    }

    void addMessage(
            SmppMessageRequest aSmppMessageRequest)
            throws InterruptedException
    {
        mSmppMessageRequestQueue.put(aSmppMessageRequest);
    }

    private static List<Long> addMessageToRedis(
            Map<Long, String> aMesssageFromDb)
    {
        final List<Long> toDelete = new ArrayList<>(aMesssageFromDb.size());

        if (log.isDebugEnabled())
            log.debug("To be delete the records ...'" + toDelete.size() + "'");

        for (final Entry<Long, String> entry : aMesssageFromDb.entrySet())
            try
            {
                final String             msg                 = entry.getValue();
                final SmppMessageRequest lSmppMessageRequest = new SmppMessageRequest(msg);
                final ClusterType        aCluster            = ClusterType.getCluster(lSmppMessageRequest.getCluster());
                final int                lRefNum             = Integer.parseInt(lSmppMessageRequest.getUdhReferenceNumber(), 16);
                final int                redisPoolIndex      = RedisOperation.getRedisPoolIndex(aCluster, lRefNum);

                if (log.isDebugEnabled())
                    log.debug("Redis Pool Index : '" + redisPoolIndex + "'");

                // if (RedisMemoryChecker.getInstance().canWrite(aCluster, redisPoolIndex))
                // {
                if (log.isDebugEnabled())
                    log.debug("Redis is available .. pushing to concat-redis...");
                StringBuffer sb=new StringBuffer();
                ConcatenateReceiver.addSmppMessage(aCluster, lSmppMessageRequest, false,sb);
                toDelete.add(entry.getKey());
                // }
            }
            catch (final Exception e)
            {
                log.error("", e);
            }

        if (log.isDebugEnabled())
            log.debug("Delete the records ...'" + toDelete.size() + "'");
        return toDelete;
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    private void doDbInsert()
    {
        final int                      readSize = mSmppMessageRequestQueue.size() > 1000 ? 1000 : mSmppMessageRequestQueue.size();
        final List<SmppMessageRequest> list     = new ArrayList<>(readSize);
        mSmppMessageRequestQueue.drainTo(list, readSize);

        try
        {
            DbOperation.dbInsert(list);
        }
        catch (final Exception e)
        {
            returnToInMem(list);
        }
    }

    @Override
    public boolean processNow()
    {
        doDbInsert();
        pushMessageFromDbToRedis();
        return false;
    }

    private void pushMessageFromDbToRedis()
    {
        final Map<Long, String> messsageFromDb = DbOperation.getMessageFromDb(mClusterType.getKey());

        if (log.isDebugEnabled())
            log.debug("Concat Message from DB :" + messsageFromDb.size());

        if (messsageFromDb.isEmpty())
            return;

        final List<Long> lAddedMessageSeqNo = addMessageToRedis(messsageFromDb);
        if (log.isDebugEnabled())
            log.debug("Concat Message Sequence No size :'" + lAddedMessageSeqNo + "'");

        if (lAddedMessageSeqNo.isEmpty())
            return;

        DbOperation.deleteFromDb(lAddedMessageSeqNo);
    }

    private void returnToInMem(
            List<SmppMessageRequest> aList)
    {
        mSmppMessageRequestQueue.addAll(aList);
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
    }

}