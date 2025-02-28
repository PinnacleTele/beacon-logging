package com.itextos.beacon.httpclienthandover.retry;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.httpclienthandover.common.DLRProcessor;
import com.itextos.beacon.httpclienthandover.common.IHandoverProcessor;

public class HandoverRetryReaper
        implements
        ITimedProcess
{

    private static final Log     log         = LogFactory.getLog(HandoverRetryReaper.class);

    private final TimedProcessor timedProcessor;
    private boolean              canContinue = true;
    private final boolean        isClientSpecific;
    private final String         mClientId;

    public HandoverRetryReaper(
            boolean aIsCustSpecific,
            String aCustID)
    {
        isClientSpecific = aIsCustSpecific;
        mClientId        = aCustID;
        timedProcessor   = new TimedProcessor("Client Handover Retry Reaper - " + (aIsCustSpecific ? aCustID : "Default"), this, TimerIntervalConstant.DLR_HTTP_HANDOVER_HANDOVER_RETRY_REAPER);
        ExecutorSheduler.getInstance().addTask(timedProcessor, "Client Handover Retry Reaper - " + (aIsCustSpecific ? aCustID : "Default"));
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        final List<String> remainingMessages = RetryDataHolder.getInstance().getInProcessMessages(RetryUtils.DB_IN_QUERY_LIMIT, mClientId);

        if (!remainingMessages.isEmpty())
            processDLR(remainingMessages, isClientSpecific, mClientId);

        return RetryDataHolder.getInstance().inProcessCount();
    }

    private static void processDLR(
            List<String> aMessagesToProcess,
            boolean aIsCustomerSpecific,
            String aCustId)
    {
        final List<BaseMessage> messageList = RetryDBHelper.getBaseMessagesForMIDs(aMessagesToProcess);

        if (log.isDebugEnabled())
            log.debug("Base Message from database: '" + messageList.size() + "' | From Redis: '" + aMessagesToProcess.size() + "'");

        final List<BaseMessage> clonedBaseMessage = messageList.stream().map(baseMessage -> baseMessage.getClonedObject()).collect(Collectors.toList());
        RetryDBHelper.deleteForMids(aMessagesToProcess);

        // TODO If there is no mesage put it to retry queue
        if (!messageList.isEmpty())
        {
            final IHandoverProcessor process = new DLRProcessor(messageList, aIsCustomerSpecific, aCustId);
            process.process();
        }

        RedisHelper.deleteInProcessMessage(clonedBaseMessage, aCustId);
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}
