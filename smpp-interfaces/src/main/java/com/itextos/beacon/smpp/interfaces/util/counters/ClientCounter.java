package com.itextos.beacon.smpp.interfaces.util.counters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.smpp.account.SmppAccInfo;
import com.itextos.beacon.inmemory.smpp.account.util.SmppAccUtil;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;
import com.itextos.beacon.platform.smpputil.DeliverSmInfo;
import com.itextos.beacon.smpp.redisoperations.SmppRedisConnectionProvider;

import redis.clients.jedis.Jedis;

public class ClientCounter
{

    private static final Log    log                                  = LogFactory.getLog(ClientCounter.class);
    private static final String SUBMIT_DATE                          = "submit date";
    private static final long   DEFAULT_EXPIRE_MESSAGE_TIME_INMILLIS = CommonUtility.getLong(PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.SMPP_CONCAT_MESSAGE_EXPIRY_IN_SEC));

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final ClientCounter INSTANCE = new ClientCounter();

    }

    public static ClientCounter getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final ArrayList<String> clientIds = new ArrayList<>();

    private ClientCounter()
    {}

    public void init(
            String aClientIdList)
    {
        if (log.isDebugEnabled())
            log.debug("Client Id List : '" + aClientIdList + "'");

        if (aClientIdList != null)
        {
            final String[] allClientIds = aClientIdList.split(",");
            Collections.addAll(clientIds, allClientIds);
            clientIds.trimToSize();
        }
        if (log.isDebugEnabled())
            log.debug("Client Id List : " + clientIds);
    }

    public void incrementCustomerDLRSendCustomerSuccess(
            String aClientId,
            int aCount)
    {
        incrementRedisCounter(RedisCounterKeys.SMPP_INTERFACE_CUST_Q_WORKER_DLR_SEND_TO_CUSTOMER_SUCCESS, aClientId, aCount);
    }

    public void incrementCustomerDLRSendCustomerExpired(
            String aClientId,
            int aCount)
    {
        incrementRedisCounter(RedisCounterKeys.SMPP_INTERFACE_CUST_Q_WORKER_DLR_SEND_TO_CUSTOMER_EXPIRED, aClientId, aCount);
    }

    public void incrementCustomerDLRSentFeatureNotAvailable(
            String aClientId,
            int aCount)
    {
        incrementRedisCounter(RedisCounterKeys.SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_BUT_FEATURE_NOT_AVAILABLE, aClientId, aCount);
    }

    public void incrementCustomerDLRSentFeatureDoneSuccessReceived(
            String aClientId,
            int aCount)
    {
        incrementRedisCounter(RedisCounterKeys.SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_DONE_SUCCESS_RESPONSE_RECEIVED, aClientId, aCount);
    }

    public void incrementCustomerDLRSentFeatureDoneFailuerReceived(
            String aClientId,
            int aCount)
    {
        incrementRedisCounter(RedisCounterKeys.SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_DONE_FAILED_RESPONSE_RECEIVED, aClientId, aCount);
    }

    public void incrementCustomerDLRSentFeatureDoneExpiredReceived(
            String aClientId,
            int aCount)
    {
        incrementRedisCounter(RedisCounterKeys.SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_DONE_EXPIRED_RESPONSE_RECEIVED, aClientId, aCount);
    }

    public void incrementCustomerDLRSentFeatureDoneResponseNotReceivedAndExpired(
            String aClientId,
            int aCount)
    {
        incrementRedisCounter(RedisCounterKeys.SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_DONE_BUT_RESPONSE_NOT_RECEIVED_AND_EXPIRED, aClientId, aCount);
    }

    public void incrementCustomerDLRSentFeatureNotDoneTimedoutResponseReceived(
            String aClientId,
            int aCount)
    {
        incrementRedisCounter(RedisCounterKeys.SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_NOT_DONE_AND_TIMEDOUT_BUT_RESPONSE_RECEIVED, aClientId, aCount);
    }

    public void incrementCustomerDLRSentFeatureNotDoneTimedoutResponseNotReceived(
            String aClientId,
            int aCount)
    {
        incrementRedisCounter(RedisCounterKeys.SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_NOT_DONE_AND_TIMEDOUT_BUT_RESPONSE_NOT_RECEIVED, aClientId, aCount);
    }

    public void incrementSessionSendCustomerSuccess(
            String aClientId,
            int aCount)
    {
        incrementRedisCounter(RedisCounterKeys.SMPP_INTERFACE_SESSION_Q_WORKER_DLR_SEND_TO_CUSTOMER_SUCCESS, aClientId, aCount);
    }

    public void incrementSessionSendExpired(
            String aClientId,
            int aCount)
    {
        incrementRedisCounter(RedisCounterKeys.SMPP_INTERFACE_SESSION_Q_WORKER_DLR_SEND_TO_CUSTOMER_EXPIRED, aClientId, aCount);
    }

    public void incrementSessionTimeExpired(
            String aClientId,
            int aCount)
    {
        incrementRedisCounter(RedisCounterKeys.SMPP_INTERFACE_SESSION_Q_WORKER_EXPIRED_DUE_TO_TIME, aClientId, aCount);
    }

    public void incrementCustomerTimeExpired(
            String aClientId,
            int aCount)
    {
        incrementRedisCounter(RedisCounterKeys.SMPP_INTERFACE_CUST_Q_WORKER_EXPIRED_DUE_TO_TIME, aClientId, aCount);
    }

    private void incrementRedisCounter(
            RedisCounterKeys aRedisCoutnerKeyType,
            String aClientId,
            int aCount)
    {

        try (
                Jedis jedis = SmppRedisConnectionProvider.getRedis())
        {

            if (log.isDebugEnabled())
            {
                log.debug("aRedisCoutnerKeyType : '" + aRedisCoutnerKeyType + "' Client Id : '" + aClientId + "'");
                log.debug("Client Ids:'" + clientIds + "'");
            }

            if (clientIds.contains(aClientId))
                jedis.hincrBy("smppdlr:" + DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD), getRedisKey(aRedisCoutnerKeyType, aClientId), aCount);
        }
        catch (final Exception exp)
        {
            log.error("Redis Client increment error..." + exp.getMessage() + "Client Id: " + aClientId, exp);
        }
    }

    private static String getRedisKey(
            RedisCounterKeys aRedisCoutnerKeyType,
            String aClientId)
    {
        String redisKey = null;

        switch (aRedisCoutnerKeyType)
        {
            case SMPP_INTERFACE_CUST_Q_WORKER_DLR_SEND_TO_CUSTOMER_SUCCESS:
                redisKey = aClientId + ":CUST:DLRSEND:CUST:SUC";
                break;

            case SMPP_INTERFACE_CUST_Q_WORKER_DLR_SEND_TO_CUSTOMER_EXPIRED:
                redisKey = aClientId + ":CUST:DLRSEND:CUST:EXP";
                break;

            case SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_BUT_FEATURE_NOT_AVAILABLE:
                redisKey = aClientId + ":CUST:DLRSENT:FEA:NA";
                break;

            case SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_DONE_SUCCESS_RESPONSE_RECEIVED:
                redisKey = aClientId + ":CUST:DLRSENT:FEA:DONE:RES:SUC";
                break;

            case SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_DONE_FAILED_RESPONSE_RECEIVED:
                redisKey = aClientId + ":CUST:DLRSENT:FEA:DONE:RES:FAIL";
                break;

            case SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_DONE_EXPIRED_RESPONSE_RECEIVED:
                redisKey = aClientId + ":CUST:DLRSENT:FEA:DONE:RES:EXP";
                break;

            case SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_DONE_BUT_RESPONSE_NOT_RECEIVED_AND_EXPIRED:
                redisKey = aClientId + ":CUST:DLRSENT:FEA:DONE:RES:NR:EXP";
                break;

            case SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_NOT_DONE_AND_TIMEDOUT_BUT_RESPONSE_RECEIVED:
                redisKey = aClientId + ":CUST:DLRSENT:FEA:NOT:DONE:EXP:RES:REC";
                break;

            case SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_NOT_DONE_AND_TIMEDOUT_BUT_RESPONSE_NOT_RECEIVED:
                redisKey = aClientId + ":CUST:DLRSENT:FEA:NOT:DONE:EXP:RES:NOT:REC";
                break;

            case SMPP_INTERFACE_SESSION_Q_WORKER_DLR_SEND_TO_CUSTOMER_SUCCESS:
                redisKey = aClientId + ":SESS:DLRSEND:CUST:SUC";
                break;

            case SMPP_INTERFACE_SESSION_Q_WORKER_DLR_SEND_TO_CUSTOMER_EXPIRED:
                redisKey = aClientId + ":SESS:DLRSEND:CUST:EXP";
                break;

            case SMPP_INTERFACE_CUST_Q_WORKER_EXPIRED_DUE_TO_TIME:
                redisKey = aClientId + ":CUST:EXPIRED:TIME_ELAPSED";
                break;

            case SMPP_INTERFACE_SESSION_Q_WORKER_EXPIRED_DUE_TO_TIME:
                redisKey = aClientId + ":SESS:EXPIRED:TIME_ELAPSED";
                break;

            default:
                redisKey = aClientId + ":<BLANK>";
        }
        return redisKey;
    }

    public static boolean canProcessMessage(
            DeliverSmInfo aMap)
    {
        boolean returnValue = true;

        try
        {
            long              expireMessageTimeInMillis = DEFAULT_EXPIRE_MESSAGE_TIME_INMILLIS * 1000;

            String            lDnCustomDateFormat       = DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM.getKey();

            final SmppAccInfo lSmppAccInfo              = SmppAccUtil.getSmppAccountInfo(aMap.getClientId());

            if (lSmppAccInfo != null)
            {
                if (log.isDebugEnabled())
                    log.debug("DN Date Format : " + lSmppAccInfo.getDnDateFormat());

                lDnCustomDateFormat       = CommonUtility.nullCheck(lSmppAccInfo.getDnDateFormat()).isEmpty() ? lDnCustomDateFormat : lSmppAccInfo.getDnDateFormat();
                expireMessageTimeInMillis = lSmppAccInfo.getDnExpiryInSec() * 1000;
            }

            final String shortMessage = aMap.getShortMessage();
            final int    submitStart  = shortMessage.indexOf(SUBMIT_DATE) + SUBMIT_DATE.length() + 1;
            final int    dateEndsAt   = shortMessage.indexOf(" ", submitStart);
            final String dateString   = shortMessage.substring(submitStart, dateEndsAt);

            // submit date:161228155600 OR submit date:1612281556
            final Date   stime        = DateTimeUtility.getDateFromString(dateString, (dateString.length() == 12 ? DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM_SS.getKey() : lDnCustomDateFormat));

            final long   diff         = System.currentTimeMillis() - stime.getTime();
            returnValue = diff < expireMessageTimeInMillis;

            if (log.isDebugEnabled())
                log.debug("STime : '" + dateString + "', Diff : '" + diff + "' milliseconds, Result : '" + returnValue + "'");
        }
        catch (final Exception e)
        {
            log.error("Problem in validating Message Expiry. " + aMap, e);
        }
        return returnValue;
    }

}

enum RedisCounterKeys
{
    SMPP_INTERFACE_CUST_Q_WORKER_DLR_SEND_TO_CUSTOMER_SUCCESS,
    SMPP_INTERFACE_CUST_Q_WORKER_DLR_SEND_TO_CUSTOMER_EXPIRED,
    SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_BUT_FEATURE_NOT_AVAILABLE,
    SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_DONE_SUCCESS_RESPONSE_RECEIVED,
    SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_DONE_FAILED_RESPONSE_RECEIVED,
    SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_DONE_EXPIRED_RESPONSE_RECEIVED,
    SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_DONE_BUT_RESPONSE_NOT_RECEIVED_AND_EXPIRED,
    SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_NOT_DONE_AND_TIMEDOUT_BUT_RESPONSE_RECEIVED,
    SMPP_INTERFACE_CUST_Q_WORKER_DLR_SENT_FEATURE_NOT_DONE_AND_TIMEDOUT_BUT_RESPONSE_NOT_RECEIVED,
    SMPP_INTERFACE_SESSION_Q_WORKER_DLR_SEND_TO_CUSTOMER_SUCCESS,
    SMPP_INTERFACE_SESSION_Q_WORKER_DLR_SEND_TO_CUSTOMER_EXPIRED,
    SMPP_INTERFACE_CUST_Q_WORKER_EXPIRED_DUE_TO_TIME,
    SMPP_INTERFACE_SESSION_Q_WORKER_EXPIRED_DUE_TO_TIME;
}