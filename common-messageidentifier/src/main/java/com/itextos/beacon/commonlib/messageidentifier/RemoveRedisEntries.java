package com.itextos.beacon.commonlib.messageidentifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

class RemoveRedisEntries
        extends
        MessageIdentifierConstants
{

    private static final Log   log                   = LogFactory.getLog(RemoveRedisEntries.class);
    private final String       mUserName;
    private final String       mIP;
    private final String       mUserInfo;
    private List<String>       mExistingAvailableInstanceIDs;
    private List<String>       mAlreadyInUseInstanceIDs;
    private final boolean      mCleanAll;
    private final List<String> mToBeAddedInAvailable = new ArrayList<>();
    private final List<String> mToBeCleared          = new ArrayList<>();

    RemoveRedisEntries(
            String aUserName,
            String aIp,
            boolean aCleanAll)
    {
        mUserName = aUserName;
        mIP       = aIp;
        mCleanAll = aCleanAll;
        mUserInfo = "IP : '" + mIP + "' User : '" + mUserName + "': ";

        if (log.isInfoEnabled())
            log.info(mUserInfo + "Remove Entries with CleanAll : '" + mCleanAll + "'");
    }

    void removeEntries()
            throws Exception
    {
        getDataFromRedis();

        if (mCleanAll)
            checkInUseInstanceID();

        checkFromStatus();
        removeAndAdd();

        if (log.isInfoEnabled())
            log.info((mCleanAll ? "All" : "Expired") + " entries are removed.");
    }

    private void checkFromStatus()
            throws Exception
    {
        if (log.isInfoEnabled())
            log.info(mUserInfo + "Checking instance IDs based on the status");

        try (
                Jedis jedis = RedisConnection.getConnection())
        {
            final Set<String> keys             = jedis.keys(KEY_OUTER_STATUS + "*");
            final int         instanceStartsAt = KEY_OUTER_STATUS.length() + 1;

            mToBeAddedInAvailable.clear();
            mToBeCleared.clear();

            for (final String statusKey : keys)
            {
                final String instanceID = statusKey.substring(instanceStartsAt);

                if (!mCleanAll)
                {
                    final String lastUpdatedTime = jedis.hget(statusKey, KEY_INNER_LAST_UPDATED);
                    final Date   d               = DateTimeUtility.getDateFromString(lastUpdatedTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
                    final long   diff            = System.currentTimeMillis() - d.getTime();

                    if (log.isDebugEnabled())
                        log.debug(mUserInfo + "Outer Key : '" + statusKey + "' Lastupdated : '" + lastUpdatedTime + "' Expiry Time : '"
                                + MessageIdentifierProperties.getInstance().getExpiryMinutesInMillis() + "' Diff : '" + diff + "'");

                    if (diff > MessageIdentifierProperties.getInstance().getExpiryMinutesInMillis())
                    {
                        if (!mExistingAvailableInstanceIDs.contains(instanceID))
                            mToBeAddedInAvailable.add(instanceID);
                        mToBeCleared.add(instanceID);
                    }
                }
                else
                {
                    if (!mExistingAvailableInstanceIDs.contains(instanceID))
                        mToBeAddedInAvailable.add(instanceID);
                    mToBeCleared.add(instanceID);
                }
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the details from Redis", e);
            throw new Exception("Exception while getting the details from Redis", e);
        }
    }

    private void removeAndAdd()
            throws Exception
    {

        if (log.isInfoEnabled())
        {
            log.info(mUserInfo + "Instance IDs to be added into the available list: '" + mToBeAddedInAvailable + "'");
            log.info(mUserInfo + "Instance IDs to be removed from the InUSE list  : '" + mToBeCleared + "'");
        }

        try (
                Jedis jedis = RedisConnection.getConnection();
                Pipeline pipe = jedis.pipelined();)
        {

            for (final String instID : mToBeCleared)
            {
                pipe.lrem(KEY_OUTER_IN_USE, 0, instID);

                pipe.hdel(KEY_OUTER_STATUS + ":" + instID, KEY_INNER_INTERFACE_TYPE, KEY_INNER_ALLOCATED_IP, KEY_INNER_ALLOCATED_TIME, KEY_INNER_LAST_UPDATED);
            }
            pipe.sync();

            if (log.isInfoEnabled())
                log.info(mUserInfo + "Removed instance IDs from the InUse List and status.");

            for (final String instID : mToBeAddedInAvailable)
                pipe.lpush(KEY_OUTER_AVAILABLE, instID);
            pipe.sync();

            if (log.isInfoEnabled())
                log.info(mUserInfo + "Added the instance IDs to the Available list.");
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the details from Redis", e);
            throw new Exception("Exception while getting the details from Redis", e);
        }
    }

    private void checkInUseInstanceID()
    {
        if (log.isInfoEnabled())
            log.info(mUserInfo + "Checking for the existing instance IDs");

        for (final String instanceID : mAlreadyInUseInstanceIDs)
        {
            if (!mExistingAvailableInstanceIDs.contains(instanceID))
                mToBeAddedInAvailable.add(instanceID);
            mToBeCleared.add(instanceID);
        }
    }

    private void getDataFromRedis()
            throws Exception
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
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the details from Redis", e);
            throw new Exception("Exception while getting the details from Redis", e);
        }
    }

}