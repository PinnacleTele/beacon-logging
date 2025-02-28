package com.itextos.beacon.smpp.concatenate;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.smpp.objects.request.SmppMessageRequest;
import com.itextos.beacon.smpp.utils.AccountDetails;

public class ConcatExpiryUtil
{

    private static final Log  log                         = LogFactory.getLog(ConcatExpiryUtil.class);

    private static final long ADDITIONAL_WAIT_TIME        = 5 * 60 * 1000L;
    private static final long MESSAGE_EXPIRE_TIME         = CommonUtility.getLong(AccountDetails.getConfigParamsValueAsString(ConfigParamConstants.SMPP_CONCAT_MESSAGE_EXPIRY_IN_SEC)) * 1000L;
    private static final long INVALID_FIRST_RECEIVED_TIME = 0L;

    private ConcatExpiryUtil()
    {}

    public static boolean checkExpiry(
            ClusterType aClusterType,
            int aRedisPoolIndex,
            String aRefKey,
            boolean aCanLog)
    {
        long lReceivedTime = 0;

        try
        {
            if (log.isDebugEnabled())
                log.debug(String.format("Cluster Type : '%s' Redis Pool Index : '%d' Ref Key : '%s' CanLog : '%s'", aClusterType, aRedisPoolIndex, aRefKey, aCanLog));

            lReceivedTime = RedisOperation.getFirstReceivedTime(aClusterType, aRedisPoolIndex, aRefKey);

            if (INVALID_FIRST_RECEIVED_TIME >= lReceivedTime)
            {
                log.error(String.format("First received time is not proper for the details Cluster Type : '%s' Redis Pool Index : '%d' Ref Key : '%s' CanLog : '%s'", aClusterType, aRedisPoolIndex,
                        aRefKey, aCanLog));
                return false;
            }

            if (isExpired(lReceivedTime))
            {
                log.error(String.format("Expired Refrence Key - Cluster Type : '%s' Redis Pool Index : '%d' Ref Key : '%s' CanLog : '%s' First Received Time '%s'", aClusterType, aRedisPoolIndex,
                        aRefKey, aCanLog, new Date(lReceivedTime)));

                if (aCanLog)
                    RedisOperation.concatExpiryLog(aClusterType, aRedisPoolIndex, aRefKey);

                return true;
            }
        }
        catch (final Exception e)
        {
            log.error(MessageFormat.format(
                    "Exception occer while Verify the Expiry time for Cluster Type : ''{0}'' Redis Pool Index : ''{1}'' Ref Key : ''{2}'' CanLog : ''{3}'' First Received Time ''{4}'' FirstReceivedTime Long ''{5}''",
                    aClusterType, aRedisPoolIndex, aRefKey, aCanLog, new Date(lReceivedTime), lReceivedTime), e);
        }

        return false;
    }
    
    
    public static boolean checkOrphanExpiry(
            ClusterType aClusterType,
            int aRedisPoolIndex,
            String aRefKey,
            boolean aCanLog, String aPayload)
    {
        long lReceivedTime = 0;

        try
        {
            if (log.isDebugEnabled())
                log.debug(String.format("Cluster Type : '%s' Redis Pool Index : '%d' Ref Key : '%s' CanLog : '%s'", aClusterType, aRedisPoolIndex, aRefKey, aCanLog));

            lReceivedTime = RedisOperation.getFirstReceivedTime(aClusterType, aRedisPoolIndex, aRefKey);

            if (INVALID_FIRST_RECEIVED_TIME >= lReceivedTime)
            {
                log.error(String.format("First received time is not proper for the details Cluster Type : '%s' Redis Pool Index : '%d' Ref Key : '%s' CanLog : '%s'", aClusterType, aRedisPoolIndex,
                        aRefKey, aCanLog));
                
                final SmppMessageRequest messageRequest = new SmppMessageRequest(aPayload);
                
                lReceivedTime =   messageRequest.getReceivedTime();
                
                if(log.isDebugEnabled())
                	log.debug("Received time from Payload :'" +lReceivedTime +"'" );
                
                if (INVALID_FIRST_RECEIVED_TIME >= lReceivedTime)                
                return false;
            }

            if (isExpired(lReceivedTime))
            {
                log.error(String.format("Expired Refrence Key - Cluster Type : '%s' Redis Pool Index : '%d' Ref Key : '%s' CanLog : '%s' First Received Time '%s'", aClusterType, aRedisPoolIndex,
                        aRefKey, aCanLog, new Date(lReceivedTime)));

                if (aCanLog)
                    RedisOperation.concatExpiryLog(aClusterType, aRedisPoolIndex, aRefKey);

                return true;
            }
        }
        catch (final Exception e)
        {
            log.error(MessageFormat.format(
                    "Exception occer while Verify the Expiry time for Cluster Type : ''{0}'' Redis Pool Index : ''{1}'' Ref Key : ''{2}'' CanLog : ''{3}'' First Received Time ''{4}'' FirstReceivedTime Long ''{5}''",
                    aClusterType, aRedisPoolIndex, aRefKey, aCanLog, new Date(lReceivedTime), lReceivedTime), e);
        }

        return false;
    }

    public static boolean isExpired(
            long aReceivedTime)
    {
        return (aReceivedTime + MESSAGE_EXPIRE_TIME + ADDITIONAL_WAIT_TIME) < System.currentTimeMillis();
    }

}
