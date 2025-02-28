package com.itextos.beacon.httpclienthandover.retry;

import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;

import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.httpclienthandover.utils.LogStatusEnum;
import com.itextos.beacon.httpclienthandover.utils.TopicSenderUtility;

public class ExpiredMessageLogger
        implements
        ITimedProcess
{

    public static final Log      log         = LogFactory.getLog(ExpiredMessageLogger.class);

    //
    private final String         clientId;
    private final boolean        isCustomerSpecific;
    private final TimedProcessor timeProcessor;
    private boolean              canContinue = true;

    public ExpiredMessageLogger(
            boolean aIsCustSpecific,
            String aCustID)
    {
        isCustomerSpecific = aIsCustSpecific;
        clientId           = aCustID;
        timeProcessor      = new TimedProcessor("Expired Message Logger - " + (aIsCustSpecific ? aCustID : "Default"), this, TimerIntervalConstant.DLR_HTTP_HANDOVER_EXPIRED_MESSAGE_LOG_INTERVAL);
        ExecutorSheduler.getInstance().addTask(timeProcessor, "Expired Message Logger - " + (aIsCustSpecific ? aCustID : "Default"));
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        final List<String> midList = RetryDataHolder.getInstance().getExpiredMessages(RetryUtils.DB_IN_QUERY_LIMIT, clientId);

        if (!midList.isEmpty())
        {
            final List<BaseMessage> baseMessages = RetryDBHelper.getBaseMessagesForMIDs(midList);

            if (!baseMessages.isEmpty())
            {
                logExpiredMessages(baseMessages);
                RetryDBHelper.deleteForMids(midList);
            }
            RedisHelper.deleteInProcessMessage(baseMessages, clientId);
        }

        return RetryDataHolder.getInstance().expiredMessagesCount() > RetryUtils.DB_IN_QUERY_LIMIT;
    }

    private void logExpiredMessages(
            List<BaseMessage> aBaseMessages)
    {
        final UUID        uniqueId   = UUID.randomUUID();
        final BaseMessage masterData = aBaseMessages.get(0).getClonedObject();
        masterData.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_UNIQUE_ID, "" + uniqueId);
        masterData.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_LOG_STATUS, LogStatusEnum.RETRY_EXPIRED.name());

        aBaseMessages.stream().forEach(baseMessage -> {

            if (isCustomerSpecific)
            {
                baseMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_UNIQUE_ID, "" + uniqueId);

                TopicSenderUtility.sendToMasterLogQueue(masterData);
                TopicSenderUtility.sendToChildLogQueue(baseMessage);
            }
            else
            {
                final BaseMessage tempMasterData = baseMessage.getClonedObject();

                final UUID        tempUniqueId   = UUID.randomUUID();
                tempMasterData.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_UNIQUE_ID, "" + tempUniqueId);
                tempMasterData.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_LOG_STATUS, LogStatusEnum.RETRY_EXPIRED.name());

                baseMessage.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_UNIQUE_ID, "" + tempUniqueId);
                TopicSenderUtility.sendToMasterLogQueue(tempMasterData);
                TopicSenderUtility.sendToChildLogQueue(baseMessage);
            }
        });
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}
