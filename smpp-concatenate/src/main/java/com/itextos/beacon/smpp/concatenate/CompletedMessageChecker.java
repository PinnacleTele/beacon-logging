package com.itextos.beacon.smpp.concatenate;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.smpp.utils.AccountDetails;

public class CompletedMessageChecker
        implements
        ITimedProcess
{

    private static final Log     log                     = LogFactory.getLog(CompletedMessageChecker.class);
    private static final int     MAX_MESSAGES_PER_QUERY  = 1000;
    // private static final long MESSAGE_EXPIRE_TIME = 3 * 60 * 60 * 1000; // TODO:
    // To configure in app_config
    private static final long    MESSAGE_EXPIRE_TIME     = (CommonUtility.getLong(AccountDetails.getConfigParamsValueAsString(ConfigParamConstants.SMPP_CONCAT_MESSAGE_EXPIRY_IN_SEC)) * 1000);
    private static final String  ITERATION_INITIAL_INDEX = "0";
    private static final String  ITERATION_FINAL_INDEX   = "0";

    private final ClusterType    mClusterType;
    private final int            mRedisPoolIndex;
    private final TimedProcessor mTimedProcessor;
    private boolean              mCanContinue            = true;

    public CompletedMessageChecker(
            ClusterType aClusterType,
            int aRedisPoolIndex)
    {
        super();
        mClusterType    = aClusterType;
        mRedisPoolIndex = aRedisPoolIndex;
        
        mTimedProcessor = new TimedProcessor("CompletedMessageChecker:" + mClusterType + "~" + mRedisPoolIndex, this, TimerIntervalConstant.SMPP_CONCAT_MESSAGE_CHECKER_INTERVAL);
    
        
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "CompletedMessageChecker:" + mClusterType + "~" + mRedisPoolIndex);
		
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {
        checkForCompletedMessages();
        return false;
    }

    private void checkForCompletedMessages()
    {
        boolean isIterationCompleted = false;

        while (!isIterationCompleted)
        {
            final Map<String, Object> lConcatMessageRefNumbers = RedisOperation.getConcatMessageRefNumbers(mClusterType, mRedisPoolIndex, ITERATION_INITIAL_INDEX, MAX_MESSAGES_PER_QUERY);

            if (log.isDebugEnabled())
                log.debug("ConcatMessage Ref Number : " + lConcatMessageRefNumbers);

            final String nextIndex = (String) lConcatMessageRefNumbers.get(RedisOperation.REDIS_RESULT_KEY_CURSOR);
            isIterationCompleted = ITERATION_FINAL_INDEX.equals(nextIndex);

            final List<PendingMessageInfo> pendingMessageList = (List<PendingMessageInfo>) lConcatMessageRefNumbers.get(RedisOperation.REDIS_RESULT_KEY_PAYLOAD);

            if ((pendingMessageList != null) && !pendingMessageList.isEmpty())
                for (final PendingMessageInfo pmi : pendingMessageList)
                {
                    if (log.isDebugEnabled())
                        log.debug("Pending Message  : " + pmi.isAllPartsReceived());

                    if (pmi.isAllPartsReceived())
                    {
                        final boolean isExpired = ConcatExpiryUtil.checkExpiry(mClusterType, mRedisPoolIndex, pmi.getRefNumber(), true);

                        if (log.isDebugEnabled())
                            log.debug("Concat Expiry for Incorrect UDH data..:" + isExpired);
                    }
                    else
                    {
                        if (log.isDebugEnabled())
                            log.debug("All parts not received & verifyin the message has expired...Received time "
                                    + DateTimeUtility.getFormattedDateTime(pmi.getReceivedTime(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));

                        if (log.isDebugEnabled())
                            log.debug("Configured Expiry time : " + MESSAGE_EXPIRE_TIME + "Expiry Time :" + (pmi.getReceivedTime() + MESSAGE_EXPIRE_TIME) + ", Current Time:"
                                    + System.currentTimeMillis());

                        if (pmi.isExpired(MESSAGE_EXPIRE_TIME))
                            ExpiryMessageCollectionFactory.getInstance().addMessage(mClusterType, mRedisPoolIndex, pmi.getRefNumber());
                    }
                }
        }
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
    }

}