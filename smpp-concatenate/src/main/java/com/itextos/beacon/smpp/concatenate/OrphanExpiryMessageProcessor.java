package com.itextos.beacon.smpp.concatenate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

public class OrphanExpiryMessageProcessor
        implements
        ITimedProcess
{

    private static final Log     log          = LogFactory.getLog(OrphanExpiryMessageProcessor.class);

    private final ClusterType    mClusterType;
    private final int            mRedisPoolIndex;
    private final TimedProcessor mTimedProcessor;
    private boolean              mCanContinue = true;

    public OrphanExpiryMessageProcessor(
            ClusterType aClusterType,
            int aRedisPoolIndex)
    {
        super();
        mClusterType    = aClusterType;
        mRedisPoolIndex = aRedisPoolIndex;
        
        mTimedProcessor = new TimedProcessor("OrphanExpiryMessageProcessor:" + mClusterType + "~" + mRedisPoolIndex, this, TimerIntervalConstant.SMPP_CONCAT_ORPHAN_MESSAGE_EXPIRY_INTERVAL);
    
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "OrphanExpiryMessageProcessor:" + mClusterType + "~" + mRedisPoolIndex);
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
        boolean      isRun  = false;
        final String cursor = "0";

        try
        {
        	StringBuffer sb=new StringBuffer();
            if (log.isDebugEnabled())
                log.debug( ", Orphan Expiry Poller running for cluster '" + mClusterType + ", RedisIndex:'" + mRedisPoolIndex + "'");

            while (!isRun)
            {
                final List<SmppMessageRequest> toExpiredSend = new ArrayList<>();
                final List<String>             toDelete      = new ArrayList<>();

                final Map<String, Object>      lResult       = RedisOperation.getAll(mClusterType, mRedisPoolIndex, cursor, 1000);
                final Map<String, Object>      payLoad       = (HashMap<String, Object>) lResult.get("payload");

                if ((payLoad != null) && (payLoad.size() > 0))
                {
                    final Iterator<String> keyIterator = payLoad.keySet().iterator();

                    while (keyIterator.hasNext())
                    {
                        final String  key       = keyIterator.next();

                        final String  finalKey  = key.substring(0, key.lastIndexOf(":"));

                        final String  aPayload  = (String) payLoad.get(key);

                        final boolean isExpired = ConcatExpiryUtil.checkOrphanExpiry(mClusterType, mRedisPoolIndex, finalKey, false, aPayload);

                        if (log.isDebugEnabled())
                            log.debug("Key:'" + key + "', Concat Expiry for Incorrect UDH data..:" + isExpired);

                        if (isExpired)
                        {
                            final SmppMessageRequest messageRequest = new SmppMessageRequest(aPayload);

                            toExpiredSend.add(messageRequest);
                            toDelete.add(key);
                        }
                    }

                    try
                    {
                        if (!toDelete.isEmpty())
                            deleteProcessedMessaes(toDelete);

                        if (!toExpiredSend.isEmpty())
                            buildAndSendToKafkaExpiredTopic(toExpiredSend,sb);
                    }
                    catch (final Exception e)
                    {
                        log.error("OrphanExpiry -Exception occer while Handover to Kafka..., Hence Data will loss.." + toExpiredSend.size(), e);
                        for (final SmppMessageRequest tempReq : toExpiredSend)
                            log.debug("Fail Data Msg :" + tempReq);
                    }
                }

                if (cursor.equals("0"))
                    isRun = true;
            }
        }
        catch (final Exception e)
        {
            log.error("Unexpected error in often expiry message processor....", e);
        }
    }

    private void deleteProcessedMessaes(
            List<String> aToDelete)
    {
        RedisOperation.removeProcessedMessages(mClusterType, mRedisPoolIndex, aToDelete);
    }

    private void buildAndSendToKafkaExpiredTopic(
            List<SmppMessageRequest> aMessageToSend,
            StringBuffer sb)
            throws Exception
    {

        for (final SmppMessageRequest tempReq : aMessageToSend)
        {
            final String lClientId = tempReq.getClientId();

            if (log.isDebugEnabled())
                log.debug("Client Id : '" + lClientId + "'");

            final SmppUserInfo      lSmppUserInfo = ConcatBuildMessageRequest.updateUserInfo(lClientId);

            final List<MessagePart> msgParts      = new ArrayList<>();

            if (log.isDebugEnabled())
                log.debug("MessageId : '" + tempReq.getAckid() + "', Message :" + tempReq.getMessage());

            final MessagePart msgObj = ConcatBuildMessageRequest.getMessagePartObj(tempReq);
            msgParts.add(msgObj);

            if (log.isDebugEnabled())
            {
                log.debug("Long Message : " + tempReq.getMessage());
                log.debug("Message Parts Size : " + msgParts);
            }

            final MessageRequest lMessageRequest = ConcatBuildMessageRequest.getMessageRequest(tempReq, lSmppUserInfo, mClusterType, PlatformStatusCode.CONCATE_ORPHAN_EXPIRY, tempReq.getTotalParts());
            ConcatBuildMessageRequest.setMessageParts(lMessageRequest, msgParts, tempReq.getMessage().toString());

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

}
