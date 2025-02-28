package com.itextos.beacon.commonlib.messageidentifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.errorlog.RedisDataPopulatorLog;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

class PopulateDataStatic
        extends
        MessageIdentifierConstants
{

    enum ExpiryStatus
    {
        EXPIRED,
        NOT_EXPIRED,
        IN_USE,
        AVAILABLE;
    }

    private static final Log                 log = LogFactory.getLog(PopulateDataStatic.class);
    private final String                     mUserName;
    private final String                     mIP;
    private final int                        mStartId;
    private final int                        mEndId;
    private final String                     mUserInfo;
    private List<String>                     mExistingAvailableInstanceIDs;
    private List<String>                     mAlreadyInUseInstanceIDs;
    private Map<String, Map<String, String>> mInstanceStatus;

    PopulateDataStatic(
            String aUserName,
            String aIp,
            int aStartId,
            int aEndId)
    {
        mUserName = aUserName;
        mIP       = aIp;
        mStartId  = aStartId;
        mEndId    = aEndId;
        mUserInfo = "IP : '" + mIP + "' User : '" + mUserName + "': ";
    }

    void populate()
            throws ItextosException
    {
        getDataFromRedis();

        populateData();
    }

    private void populateData()
            throws ItextosException
    {
        if (log.isInfoEnabled())
            log.info(mUserInfo + "Populating data into redis");
        
        
        RedisDataPopulatorLog.log(" populateData() : mExistingAvailableInstanceIDs : "+mExistingAvailableInstanceIDs);

        RedisDataPopulatorLog.log(" populateData() : mAlreadyInUseInstanceIDs : "+mAlreadyInUseInstanceIDs);

        final List<String> tobeAdded   = new ArrayList<>();
        final List<String> tobeCleared = new ArrayList<>();

        for (int index = mStartId; index <= mEndId; index++)
        {
            final String currInstanceID = index + "";

            if (log.isDebugEnabled())
                log.debug(mUserInfo + "Working in the instance id : '" + index + "'");

            final boolean availableInAvailList = mExistingAvailableInstanceIDs.contains(currInstanceID);

            if (availableInAvailList)
            {
                log.error(mUserInfo + "'" + currInstanceID + "' is already in the available list.");
                continue;
            }

            final boolean availableInInuseList = mAlreadyInUseInstanceIDs.contains(currInstanceID);

            if (availableInInuseList)
            {
                log.error(mUserInfo + "'" + currInstanceID + "' is in the IN-USE list.");
                final ExpiryStatus checkForExpiry = checkForExpiry(currInstanceID);

                if ((checkForExpiry == ExpiryStatus.AVAILABLE) || (checkForExpiry == ExpiryStatus.EXPIRED))
                    tobeCleared.add(currInstanceID);
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug(mUserInfo + "'" + currInstanceID + "' will be added in the available list.");
                tobeAdded.add(currInstanceID);
            }
        }

        RedisDataPopulatorLog.log(" populateData() : tobeCleared : "+tobeCleared);

        if (!tobeCleared.isEmpty())
        {
            clearExistingInstnaceIDs(tobeCleared);
            tobeAdded.addAll(tobeCleared);
        }

        RedisDataPopulatorLog.log(" populateData() : tobeAdded : "+tobeAdded);

        if (!tobeAdded.isEmpty())
            addIntoAvailableList(tobeAdded);

        final String s = "Population of the instance ID from '" + mStartId + "' to '" + mEndId + "' is completed.";

        if (log.isInfoEnabled())
            log.info(mUserInfo + s);
        System.out.println(MessageIdentifierConstants.NEW_LINE + s + MessageIdentifierConstants.NEW_LINE);
    }

    private void addIntoAvailableList(
            List<String> aTobeAdded)
            throws ItextosException
    {

        try (
                final Jedis jedis = RedisConnection.getConnection();
                Pipeline pipe = jedis.pipelined())
        {
            for (final String instID : aTobeAdded)
                pipe.lpush(KEY_OUTER_AVAILABLE, instID);

            pipe.sync();

            if (log.isInfoEnabled())
                log.info(mUserInfo + "Adding the instance IDs in the available list is completed.");
        }
        catch (final Exception e)
        {
            log.error("Exception while clearing the existing instnace IDs.", e);
            throw new ItextosException("Exception while clearing the existing instnace IDs.", e);
        }
    }

    private void clearExistingInstnaceIDs(
            List<String> aTobeCleared)
            throws ItextosException
    {

        try (
                final Jedis jedis = RedisConnection.getConnection();
                Pipeline pipe = jedis.pipelined())
        {

            for (final String instID : aTobeCleared)
            {
                pipe.lrem(KEY_OUTER_IN_USE, 1, instID);
                pipe.hdel(KEY_OUTER_STATUS + ":" + instID, KEY_INNER_INTERFACE_TYPE, KEY_INNER_ALLOCATED_IP, KEY_INNER_ALLOCATED_TIME, KEY_INNER_LAST_UPDATED);
                pipe.sync();

                if (log.isInfoEnabled())
                    log.info(mUserInfo + "Removed entries related to instanceID '" + instID + "' is completed.");
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while clearing the existing instnace IDs.", e);
            throw new ItextosException("Exception while clearing the existing instnace IDs.", e);
        }
    }

    private ExpiryStatus checkForExpiry(
            String aCurrInstanceID)
    {
        ExpiryStatus              status;
        final Map<String, String> statusInfo = mInstanceStatus.get(aCurrInstanceID);

        if (statusInfo != null)
        {
            final String interfaceTypeTemp = statusInfo.get(KEY_INNER_INTERFACE_TYPE);
            final String allocatedIP       = statusInfo.get(KEY_INNER_ALLOCATED_IP);
            final String allocatedTime     = statusInfo.get(KEY_INNER_ALLOCATED_TIME);
            final String lastUpdatedTime   = statusInfo.get(KEY_INNER_LAST_UPDATED);

            final Date   lastUpdateTime    = DateTimeUtility.getDateFromString(lastUpdatedTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
            final long   diff              = System.currentTimeMillis() - lastUpdateTime.getTime();

            final String temp              = "Instance ID : '" + aCurrInstanceID + "' Allocated IP : '" + allocatedIP + "' Allocated Time : '" + allocatedTime + "' Lastupdated : '" + lastUpdatedTime
                    + "' Expiry Time : '" + MessageIdentifierProperties.getInstance().getExpiryMinutesInMillis() + "' Diff : '" + diff + "' Interface Type : '" + interfaceTypeTemp + "'";

            if (log.isDebugEnabled())
                log.debug(mUserInfo + temp);

            if (diff > MessageIdentifierProperties.getInstance().getExpiryMinutesInMillis())
            {
                status = ExpiryStatus.EXPIRED;
                final String s = "Instance id '" + aCurrInstanceID + "' is available in the IN-USE list and the it WAS EXPIRED. Hence it will be reused. Information : " + temp;
                log.error(mUserInfo + s);
                System.err.println(s);
            }
            else
            {
                final String s = "Instance id '" + aCurrInstanceID + "' is available in the IN-USE list and the it is STILL ACTIVE. Hence it will not be reused. Information : " + temp;
                log.error(mUserInfo + s);
                System.err.println(s);
                status = ExpiryStatus.NOT_EXPIRED;
            } // End of if (diff >
              // MessageIdentifierProperties.getInstance().getExpiryMinutesInMillis())
        }
        else
        {
            log.error(mUserInfo + "Although the instance id is available in the IN-USE list, there is no status information available for it. Hencce it will be added to the available list.");
            status = ExpiryStatus.AVAILABLE;
        }
        return status;
    }

    private void getDataFromRedis()
            throws ItextosException
    {
        if (log.isInfoEnabled())
            log.info(mUserInfo + "Getting the existing instances info from Redis.");

        try (
                Jedis jedis = RedisConnection.getConnection();
                Pipeline pipe = jedis.pipelined();)
        {
            final Response<List<String>> resAvailable = pipe.lrange(KEY_OUTER_AVAILABLE, 0, -1);
            final Response<List<String>> resInuse     = pipe.lrange(KEY_OUTER_IN_USE, 0, -1);

            pipe.sync();

            mExistingAvailableInstanceIDs = resAvailable.get();
            mAlreadyInUseInstanceIDs      = resInuse.get();

            mExistingAvailableInstanceIDs = mExistingAvailableInstanceIDs == null ? new ArrayList<>() : mExistingAvailableInstanceIDs;
            mAlreadyInUseInstanceIDs      = mAlreadyInUseInstanceIDs == null ? new ArrayList<>() : mAlreadyInUseInstanceIDs;
            mInstanceStatus               = new HashMap<>();

            if (!mAlreadyInUseInstanceIDs.isEmpty())
            {
                final Map<String, Response<Map<String, String>>> responses = new HashMap<>();

                for (final String tempID : mAlreadyInUseInstanceIDs)
                {
                    final Response<Map<String, String>> hgetAll = pipe.hgetAll(KEY_OUTER_STATUS + ":" + tempID);
                    responses.put(tempID, hgetAll);
                }

                pipe.sync();

                for (final Entry<String, Response<Map<String, String>>> entry : responses.entrySet())
                    mInstanceStatus.put(entry.getKey(), entry.getValue().get());
            }
            
            RedisDataPopulatorLog.log("getDataFromRedis() : mInstanceStatus "+mInstanceStatus);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the details from Redis", e);
            throw new ItextosException("Exception while getting the details from Redis", e);
        }
    }

}