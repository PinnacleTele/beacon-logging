package com.itextos.beacon.smpp.concatenate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.MessagePart;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.http.interfaceutil.InterfaceUtil;
import com.itextos.beacon.smpp.objects.SmppUserInfo;
import com.itextos.beacon.smpp.objects.request.SmppMessageRequest;
import com.itextos.beacon.smpp.utils.ItextosSmppConstants;
import com.itextos.beacon.smpp.utils.ItextosSmppUtil;
import com.itextos.beacon.smslog.ConcatePollerLog;

public class CompletedMessagePoller
        implements
        ITimedProcess
{

    private static final Log     log                        = LogFactory.getLog(CompletedMessagePoller.class);
    private static final int     MAX_MESSAGES_PER_ITERATION = 1000;

    private final ClusterType    mClusterType;
    private final int            mRedisPoolIndex;
    private final TimedProcessor mTimedProcessor;
    private boolean              mCanContinue               = true;

    public CompletedMessagePoller(
            ClusterType aClusterType,
            int aRedisPoolIndex)
    {
        super();
        mClusterType    = aClusterType;
        mRedisPoolIndex = aRedisPoolIndex;
      
        mTimedProcessor = new TimedProcessor("CompletedMessagePoller:" + mClusterType + "~" + mRedisPoolIndex, this, TimerIntervalConstant.SMPP_CONCAT_MESSAGE_CHECKER_INTERVAL);
  
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "CompletedMessagePoller:" + mClusterType + "~" + mRedisPoolIndex);
    
        if (log.isDebugEnabled())
            log.debug("CompletedMessagePoller started successfully ........." + mClusterType + "~" + aRedisPoolIndex);
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {
        return processFullyReceivedMessages();
    }

    private boolean processFullyReceivedMessages()
    {
        final Map<String, List<String>> toDelete    = new HashMap<>();
        boolean                         returnValue = false;

        try
        {
        	StringBuffer sb=new StringBuffer();
            final List<String> lCompletedMessageRefNumbers = RedisOperation.getCompletedMessageRefNumbers(mClusterType, mRedisPoolIndex, MAX_MESSAGES_PER_ITERATION);

            if (log.isDebugEnabled())
                log.debug("Concat Message Ref Numbers :" + lCompletedMessageRefNumbers);

            ConcatePollerLog.log("Concat Message Ref Numbers :" + lCompletedMessageRefNumbers);
            
            for (final String key : lCompletedMessageRefNumbers)
            {
                if (log.isDebugEnabled())
                    log.debug("Fully Received Message Key :" + key);

                returnValue = true;

                final Map<String, String> lMessagesForRefNumber = RedisOperation.getMessagesForRefNumber(mClusterType, mRedisPoolIndex, key, true);

                if (lMessagesForRefNumber == null)
                {
                    final boolean isExpired = ConcatExpiryUtil.checkExpiry(mClusterType, mRedisPoolIndex, key, false);

                    if (log.isDebugEnabled())
                        log.debug("Concat Expiry for Incorrect UDH data..:" + isExpired);

                    if (!isExpired)
                        RedisOperation.pushConcatReady(mClusterType, mRedisPoolIndex, key);

                    continue;
                }

                final List<SmppMessageRequest> toSend              = new ArrayList<>();
                final List<String>             partNumbersToDelete = new ArrayList<>();

                if (log.isDebugEnabled())
                    log.debug("MessagesForRefNumber >>>>>>: " + lMessagesForRefNumber);

                for (final Entry<String, String> entry : lMessagesForRefNumber.entrySet())
                {
                    if (log.isDebugEnabled())
                        log.debug("Entry Key :'" + entry.getKey() + "'");

                    final SmppMessageRequest messageRequest = new SmppMessageRequest(entry.getValue());

                    if (log.isDebugEnabled())
                        log.debug("Message Req '" + messageRequest + "', Client Id: '" + messageRequest.getClientId() + "'");

                    toSend.add(messageRequest);
                    partNumbersToDelete.add(entry.getKey());
                }
                log.debug("toSend.size " + toSend.size());

                try
                {
                    if (!toSend.isEmpty())
                        buildAndSendToKafka(toSend,sb);
                }
                catch (final Exception e)
                {
                    log.error("CompletePooler -Exception occer while Handover to Kafka..., Hence Data will loss.." + toSend.size(), e);
                }

                if (log.isDebugEnabled())
                    log.debug("Part Numbers to delete : " + partNumbersToDelete.size());

                if (!partNumbersToDelete.isEmpty())
                    toDelete.put(key, partNumbersToDelete);
            }

            if (log.isDebugEnabled())
                log.debug("Final Delete Keys : " + toDelete.size());

            if (!toDelete.isEmpty())
                deleteProcessedMessaes(toDelete);
        }
        catch (final Exception e)
        {
            log.error("ERROR >>>> Exception in Completed Message Poller thread \n", e);
        }

        return returnValue;
    }

    private void deleteProcessedMessaes(
            Map<String, List<String>> aToDelete)
    {
        RedisOperation.removeProcessedMessages(mClusterType, mRedisPoolIndex, aToDelete);
    }

    private void buildAndSendToKafka(
            List<SmppMessageRequest> aToSend,
            StringBuffer sb)
            throws Exception
    {
        final String lClientId = aToSend.get(0).getClientId();

        if (log.isDebugEnabled())
            log.debug("Client Id : '" + lClientId + "'");

        final SmppUserInfo lSmppUserInfo     = ConcatBuildMessageRequest.updateUserInfo(lClientId);

        boolean            canProcessMessage = true;
        final boolean      canValidateUDH    = CommonUtility.isEnabled(ItextosSmppUtil.getCutomFeatureValue(lClientId, CustomFeatures.UDH_VALIDATE_IN_SMPP));

        if (log.isDebugEnabled())
            log.debug("Can Validate UDH :" + canValidateUDH);

        if (canValidateUDH)
        {
            int totalParts = -1;

            for (final SmppMessageRequest smppRequest : aToSend)
            {
                final int msgTotalParts = extractUdh(smppRequest.getUdh());
                if (totalParts == -1)
                    totalParts = msgTotalParts;
                else
                    if (totalParts != msgTotalParts)
                    {
                        canProcessMessage = false;
                        break;
                    }
            }
        }

        if (canProcessMessage)
        {
            final StringBuilder     fullMessage = new StringBuilder();
            final List<MessagePart> msgParts    = new ArrayList<>();

            for (final SmppMessageRequest smppRequest : aToSend)
            {
                if (log.isDebugEnabled())
                    log.debug("MessageId : '" + smppRequest.getAckid() + "', Udh:'" + smppRequest.getUdh() + "', Message :" + smppRequest.getMessage());

                fullMessage.append(smppRequest.getMessage());

                final MessagePart msgObj = ConcatBuildMessageRequest.getMessagePartObj(smppRequest);
                msgParts.add(msgObj);
            }

            if (log.isDebugEnabled())
            {
                log.debug("Concatenate Long Message : " + fullMessage);
                log.debug("Message Parts Size : " + msgParts);
            }

            final MessageRequest lMessageRequest = ConcatBuildMessageRequest.getMessageRequest(aToSend.get(0), lSmppUserInfo, mClusterType, null, msgParts.size());
            ConcatBuildMessageRequest.setMessageParts(lMessageRequest, msgParts, fullMessage.toString());

            if (log.isDebugEnabled())
                log.debug("MessageRequest object before sending to Kafka ..:" + lMessageRequest);

            InterfaceUtil.sendToKafka(lMessageRequest,sb);
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("UDH total parts are incorrect.. Hence sending to kafka.." + canProcessMessage);

            for (final SmppMessageRequest smppRequest : aToSend)
            {
                // Setting Part Numer '1' for Rejection cases.
                smppRequest.setPartNumber(1);
                final MessageRequest lMessageRequest = ConcatBuildMessageRequest.getMessageRequest(smppRequest, lSmppUserInfo, PlatformStatusCode.SMPP_UDH_TOTAL_PART_MISMATCH);

                if (log.isDebugEnabled())
                    log.debug("MessageRequest object before sending to Kafka ..:" + lMessageRequest);

                InterfaceUtil.sendToKafka(lMessageRequest,sb);
            }
        }
    }

    private static int extractUdh(
            String aUdh)
    {

        if (aUdh != null)
        {
            if (log.isDebugEnabled())
                log.debug("UDH : " + aUdh);

            if (aUdh.startsWith(ItextosSmppConstants.UDH_0500))
                return new BigInteger(aUdh.substring(8, 10), 16).intValue();
            else
                if (aUdh.startsWith(ItextosSmppConstants.UDH_0608))
                    return new BigInteger(aUdh.substring(10, 12), 16).intValue();
        }
        return -999;
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
    }

}
