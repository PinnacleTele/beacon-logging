package com.itextos.beacon.smpp.concatenate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.MessagePart;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.http.interfaceutil.InterfaceUtil;
import com.itextos.beacon.smpp.objects.SmppUserInfo;
import com.itextos.beacon.smpp.objects.request.SmppMessageRequest;

class ExpiryMessageProcessor
        implements
        ITimedProcess
{

    private static final Log            log                   = LogFactory.getLog(ExpiryMessageProcessor.class);

    private final ClusterType           mClusterType;
    private final int                   mRedisPoolIndex;
    private final BlockingQueue<String> mExpiryRefNumberQueue = new LinkedBlockingQueue<>(500);
    private final TimedProcessor        mTimedProcessor;
    private boolean                     mCanContinue          = true;

    public ExpiryMessageProcessor(
            ClusterType aClusterType,
            int aRedisPoolIndex)
    {
        super();
        mClusterType    = aClusterType;
        mRedisPoolIndex = aRedisPoolIndex;
        
        mTimedProcessor = new TimedProcessor("ExpiryMessageProcessor:" + mClusterType + "~" + mRedisPoolIndex, this, TimerIntervalConstant.SMPP_CONCAT_MESSAGE_EXPIRY_INTERVAL);
   
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "ExpiryMessageProcessor:" + mClusterType + "~" + mRedisPoolIndex);
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {
        processExpiredMessages();
        return false;
    }

    private void processExpiredMessages()
    {
    	
    	StringBuffer sb=new StringBuffer();
        final int count = mExpiryRefNumberQueue.size() > 1000 ? 1000 : mExpiryRefNumberQueue.size();
        if (log.isDebugEnabled())
            log.debug("Expiry Ref number count :" + count);

        final List<String> expiredRefnumbers = new ArrayList<>(count);
        mExpiryRefNumberQueue.drainTo(expiredRefnumbers, count);

        final Map<String, List<String>> toDelete = new HashMap<>();

        if (log.isDebugEnabled())
            log.debug("Expiry Ref Numbers drain :" + expiredRefnumbers.size());

        final List<List<SmppMessageRequest>> lMsgToSend = new ArrayList<>();

        for (final String refNo : expiredRefnumbers)
        {
            final Map<String, String>      lMessagesForRefNumber = RedisOperation.getMessagesForRefNumber(mClusterType, mRedisPoolIndex, refNo, false);

            final List<SmppMessageRequest> toSend                = new ArrayList<>();
            final List<String>             partNumbersToDelete   = new ArrayList<>();

            for (final Entry<String, String> entry : lMessagesForRefNumber.entrySet())
            {
                final SmppMessageRequest messageRequest = new SmppMessageRequest(entry.getValue());
                toSend.add(messageRequest);
                partNumbersToDelete.add(entry.getKey());
            }
            if (!toSend.isEmpty())
                lMsgToSend.add(toSend);

            if (!partNumbersToDelete.isEmpty())
                toDelete.put(refNo, partNumbersToDelete);
        }

        try
        {
            if (!toDelete.isEmpty())
                deleteProcessedMessaes(toDelete);

            if (!lMsgToSend.isEmpty())
                buildAndSendToKafkaExpiredTopic(lMsgToSend,sb);
        }
        catch (final Exception e)
        {
            log.error("OnExpiry -Exception occer while Handover to Kafka..., Hence Data will loss.." + lMsgToSend.size(), e);
            for (final List<SmppMessageRequest> tempReq : lMsgToSend)
                log.debug("Fail Data Msg :" + tempReq);
        }
    }

    private void deleteProcessedMessaes(
            Map<String, List<String>> aToDelete)
    {
        RedisOperation.removeProcessedMessages(mClusterType, mRedisPoolIndex, aToDelete);
    }

    private void buildAndSendToKafkaExpiredTopic(
            List<List<SmppMessageRequest>> aMessageToSend,
            StringBuffer sb)
            throws Exception
    {

        for (final List<SmppMessageRequest> tempReq : aMessageToSend)
        {
            final String lClientId = tempReq.get(0).getClientId();

            if (log.isDebugEnabled())
                log.debug("Client Id : '" + lClientId + "'");

            final SmppUserInfo      lSmppUserInfo = ConcatBuildMessageRequest.updateUserInfo(lClientId);

            final List<MessagePart> msgParts      = new ArrayList<>();

            for (final SmppMessageRequest smppRequest : tempReq)
            {
                if (log.isDebugEnabled())
                    log.debug("MessageId : '" + smppRequest.getAckid() + "', Message :" + smppRequest.getMessage());

                final MessagePart msgObj = ConcatBuildMessageRequest.getMessagePartObj(smppRequest);
                msgParts.add(msgObj);
            }

            if (log.isDebugEnabled())
            {
                log.debug("Long Message : " + tempReq.get(0).getMessage());
                log.debug("Message Parts Size : " + msgParts);
            }

            final MessageRequest lMessageRequest = ConcatBuildMessageRequest.getMessageRequest(tempReq.get(0), lSmppUserInfo, mClusterType, PlatformStatusCode.CONCAT_MESSAGE_PARTS_NOT_RECEIVED, 0);
            ConcatBuildMessageRequest.setMessageParts(lMessageRequest, msgParts, tempReq.get(0).getMessage().toString());

            if (log.isDebugEnabled())
                log.debug("MessageRequest object before sending to Kafka ..:" + lMessageRequest);

            InterfaceUtil.sendToKafka(lMessageRequest,sb);
        }
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
    }

    public void addRefNumber(
            String aRefNumber)
    {

        try
        {

            if (!mExpiryRefNumberQueue.contains(aRefNumber))
            {
                if (log.isDebugEnabled())
                    log.debug("Concat Received Refrence Key : " + aRefNumber);

                mExpiryRefNumberQueue.offer(aRefNumber, 100, TimeUnit.MILLISECONDS);
            }
        }
        catch (final InterruptedException e)
        {
            //
        }
    }

}
