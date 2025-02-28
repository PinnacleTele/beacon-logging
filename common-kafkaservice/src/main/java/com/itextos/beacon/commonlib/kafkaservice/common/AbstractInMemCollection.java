package com.itextos.beacon.commonlib.kafkaservice.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.IMessage;

public abstract class AbstractInMemCollection
        implements
        IInMemCollection
{

    private static final Log                    log                            = LogFactory.getLog(AbstractInMemCollection.class);
    private static final int                    WAIT_TIME_TO_CONSUME_IN_MILLIS = 100;

    private final String                        mTopicName;
    private final KafkaType                     mKafkaType;
    private final String                        mLogTopicName;
    private final LinkedBlockingQueue<IMessage> mQueue                         = new LinkedBlockingQueue<>(KafkaCustomProperties.getInstance().getConsumerMaxInmemorySize());

    private boolean                             mCanAccept                     = true;

    protected AbstractInMemCollection(
            KafkaType aKafkaType,
            String aTopicName)
    {
        mKafkaType    = aKafkaType;
        mTopicName    = aTopicName;
        mLogTopicName = "Topic: '" + mTopicName + "' ";
    }

    @Override
    public void addMessage(
            IMessage aIMessage)
            throws ItextosException
    {

        try
        {
            if (aIMessage == null)
                return;

            if (log.isDebugEnabled())
                log.debug("mCanAccept : " + mCanAccept );

            if (!mCanAccept)
                log.fatal("Adding message after stop invoked. " + aIMessage);

            {
                boolean queueFull = false;

                if (mQueue.remainingCapacity() == 0)
                {
                    queueFull = true;
                    log.error(mKafkaType + " Topic " + mTopicName + " inmemory queue is full will wait for the meessage to add in the topic.");
                }
                mQueue.put(aIMessage);

                if (queueFull)
                    log.error(mKafkaType + " Topic " + mTopicName + " Message added into memory");
            }
            // else
            // throw new ItextosRuntimeException(mLogTopicName + "Inmem collection is
            // closing. Cannot add it to memory");
        }
        catch (final Exception e)
        {
            throw new ItextosException(mLogTopicName + "Problem while adding into InMem Queue. ITextosMessage : '" + aIMessage + "'", e);
        }

        if (log.isDebugEnabled())
            log.debug("Queue Size : " + mQueue.size());
    }

    @Override
    public IMessage getMessage()
    {
        IMessage lMessage = null;

        try
        {
            lMessage = mQueue.poll(WAIT_TIME_TO_CONSUME_IN_MILLIS, TimeUnit.MILLISECONDS);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting data from inmemory. Waited for " + WAIT_TIME_TO_CONSUME_IN_MILLIS + " Milliseconds.", e);
        }

        if ((lMessage != null) && log.isDebugEnabled())
            log.debug(mLogTopicName + "IMessage to send to caller " + lMessage);

        if ((lMessage != null) && !mCanAccept)
            log.fatal("Producing message after stop invoked. " + lMessage);

        return lMessage;
    }

    @Override
    public KafkaType getType()
    {
        return mKafkaType;
    }

    @Override
    public String getTopicName()
    {
        return mTopicName;
    }

    @Override
    public int getInMemSize()
    {
        return mQueue.size();
    }

    @Override
    public void shutdown()
    {
        mCanAccept = false;
    }

    public boolean removeMessage(
            IMessage aIMessage)
    {
        return mQueue.remove(aIMessage);
    }

    @Override
    public List<IMessage> getRemainingMessages()
    {
        final int size = mQueue.size();

        if (size > 0)
        {
            if (log.isInfoEnabled())
                log.info("Remaining messages in the Inmemory Queue is " + size);
            final List<IMessage> toSend = new ArrayList<>(size);
            mQueue.drainTo(toSend);
            return toSend;
        }
        return new ArrayList<>();
    }

}