package com.itextos.beacon.platform.elasticsearchutil.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.DocWriteRequest;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.platform.elasticsearchutil.types.EsOperation;
import com.itextos.beacon.platform.elasticsearchutil.utility.EsBulkProcessor;
import com.itextos.beacon.platform.elasticsearchutil.utility.EsUtility;

abstract class AbstractEsInmemoryCollection
        implements
        IEsInmemoryCollection,
        ITimedProcess
{

    private static final Log              log                 = LogFactory.getLog(AbstractEsInmemoryCollection.class);
    private final EsOperation             mEsTypeInsert;
    private final TimedProcessor          mTimedProcessor;
    private boolean                       mCanContinue        = true;

    private final BlockingQueue<IMessage> mInmemoryCollection = new LinkedBlockingQueue<>(5000);

    AbstractEsInmemoryCollection(
            EsOperation aEsType)
    {
        mEsTypeInsert   = aEsType;
       
        mTimedProcessor = new TimedProcessor("ESInMemCollection-" + aEsType, this, TimerIntervalConstant.ELASTIC_SEARCH_INMEMORY_PUSH);
      
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "ESInMemCollection-" + aEsType);
    }

    @Override
    public boolean add(
            IMessage aMessage)
    {
        if (mCanContinue)
            try
            {
                boolean queueFull = false;

                if (mInmemoryCollection.remainingCapacity() == 0)
                {
                    queueFull = true;
                    log.error("ElasticType " + mEsTypeInsert + " inmemory queue is full will wait for the meessage to add inmemory.");
                }

                mInmemoryCollection.put(aMessage);

                if (queueFull)
                    log.error("ElasticType " + mEsTypeInsert + " Message added into memory");

                return true;
            }
            catch (final InterruptedException e)
            {
                log.error("Exception while inserting the data into inmemory", e);
            }
        return false;
    }

    @Override
    public boolean add(
            List<IMessage> aMessageList)
    {
        if ((aMessageList == null) || aMessageList.isEmpty())
            return true;

        boolean returnValue = true;

        try
        {
            if (mCanContinue)
                for (final IMessage message : aMessageList)
                    mInmemoryCollection.put(message);
        }
        catch (final InterruptedException e)
        {
            returnValue = false;
            log.error("Exception while inserting the data into inmemory", e);
        }

        if (log.isDebugEnabled())
            log.debug(mEsTypeInsert + " inmemory sise " + mInmemoryCollection.size());

        return returnValue;
    }

    private List<IMessage> getMessage()
    {
        return getMessage(-1);
    }

    private List<IMessage> getMessage(
            int aMaxSize)
    {
        int size = mInmemoryCollection.size();

        if (size > 0)
        {
            size = (aMaxSize == -1) ? size : (size > aMaxSize ? aMaxSize : size);
            final List<IMessage> messages = new ArrayList<>(size);
            mInmemoryCollection.drainTo(messages, size);
            return messages;
        }
        return new ArrayList<>();
    }

    @Override
    public void processRemainingData()
    {
        final List<IMessage> lMessage = getMessage();

        if (!lMessage.isEmpty())
            process(lMessage);
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {
        final List<IMessage> lMessage = getMessage(1000);

        if (lMessage.isEmpty())
            return false;

        process(lMessage);

        return true;
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
    }

    public void process(
            List<IMessage> aMessage)
    {
        final Map<String, List<DocWriteRequest<?>>> indexRequestMap = new HashMap<>();

        try
        {
            if (log.isDebugEnabled())
                log.debug(mEsTypeInsert + " Messages size to process " + aMessage.size());

            final String                   esIndexName = EsUtility.getEsIndexName(mEsTypeInsert.getParent());
            final List<DocWriteRequest<?>> updateList  = indexRequestMap.computeIfAbsent(esIndexName, k -> new ArrayList<>());

            for (final IMessage temp : aMessage)
            {
                final BaseMessage message = (BaseMessage) temp;
                updateList.add(getInsertUpdateRequest(esIndexName, message));
            }

            insertOrUpdateData(mEsTypeInsert, indexRequestMap);

            if (log.isDebugEnabled())
                log.debug(mEsTypeInsert + " Process completed");
        }
        catch (final Exception e)
        {
            add(aMessage);
            log.error("Returning the messages to the collection. Return count " + aMessage.size());
        }
    }

    abstract DocWriteRequest<?> getInsertUpdateRequest(
            String aEsIndexName,
            BaseMessage aMessage);

    public static void insertOrUpdateData(
            EsOperation aEsTypeInsert,
            Map<String, List<DocWriteRequest<?>>> aIndexRequestMap)
            throws Exception
    {

        try
        {

            for (final Entry<String, List<DocWriteRequest<?>>> entry : aIndexRequestMap.entrySet())
            {
                if (log.isDebugEnabled())
                    log.debug("Index " + entry.getKey() + " IMessage count " + entry.getValue().size());

                final EsBulkProcessor lEsBulkProcessor = new EsBulkProcessor(aEsTypeInsert, entry.getValue());
                lEsBulkProcessor.process();
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while insert / update the Es Services.", e);
            throw e;
        }
    }

}