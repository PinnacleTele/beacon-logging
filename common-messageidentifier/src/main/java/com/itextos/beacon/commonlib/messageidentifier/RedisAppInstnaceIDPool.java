package com.itextos.beacon.commonlib.messageidentifier;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

class RedisAppInstnaceIDPool
        extends
        MessageIdentifierConstants
{

    private static final Log    log = LogFactory.getLog(RedisAppInstnaceIDPool.class);

    private final String        mMessageIdentifierSourceType;
    private final InterfaceType mInterfaceType;
    private final String        mLocalIP;
    private String              mAppInstanceID;

    RedisAppInstnaceIDPool(
            final InterfaceType aInterfaceType)
    {
        mMessageIdentifierSourceType = "REDIS";
        mInterfaceType               = aInterfaceType;

        try
        {
            mLocalIP = InetAddress.getLocalHost().getHostAddress();
        }
        catch (final UnknownHostException e)
        {
            throw new RuntimeException("Unable to identify the IP of the machine in which this application is running.", e);
        }
    }

    String getMessageIdentifierSourceType()
    {
        return mMessageIdentifierSourceType;
    }

    InterfaceType getInterfaceType()
    {
        return mInterfaceType;
    }

    String getNextAppInstanceID()
    {

        try (
                Jedis jedis = RedisConnection.getConnection();)
        {
            final String available = KEY_OUTER_AVAILABLE;
            final String inuse     = KEY_OUTER_IN_USE;

            if (log.isDebugEnabled())
                log.debug("Popup from : '" + available + "' to : '" + inuse + "'");

            mAppInstanceID = jedis.rpoplpush(available, inuse);

            if (log.isDebugEnabled())
                log.debug("Popup instance id from : '" + available + "' to : '" + inuse + "' is : '" + mAppInstanceID + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while get the Next App Instance ID from Redis.", e);
        }

        if (mAppInstanceID == null)
            checkForUnusedInstanceID();

        updateInitialAllocattion();

        return mAppInstanceID;
    }

    private void checkForUnusedInstanceID()
    {

        try (
                Jedis jedis = RedisConnection.getConnection();)
        {
            final String status = KEY_OUTER_STATUS + "*";

            if (log.isDebugEnabled())
                log.debug("Checking for unused app instance id in : '" + status + "'");

            final Set<String> keys = jedis.keys(status);

            if ((keys != null) && (!keys.isEmpty()))
            {
                final Set<MessageIdUsedInfo> set                = new HashSet<>();
                final List<String>           expiredInstanceIDs = new ArrayList<>();

                for (final String outer : keys)
                {
                    final List<String> hmget             = jedis.hmget(outer, KEY_INNER_INTERFACE_TYPE, KEY_INNER_ALLOCATED_IP, KEY_INNER_ALLOCATED_TIME, KEY_INNER_LAST_UPDATED);

                    final String       interfaceTypeTemp = hmget.get(INDEX_INTERFACE_TYPE);
                    final String       allocatedIP       = hmget.get(INDEX_ALLOCATED_IP);
                    final String       allocatedTime     = hmget.get(INDEX_ALLOCATED_TIME);
                    final String       lastUpdatedTime   = hmget.get(INDEX_LAST_UPDATED_TIME);

                    final Date         d                 = DateTimeUtility.getDateFromString(lastUpdatedTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
                    final long         diff              = System.currentTimeMillis() - d.getTime();

                    if (log.isDebugEnabled())
                        log.debug("Outer Key : '" + outer + "' Allocated IP : '" + allocatedIP + "' Allocated Time : '" + allocatedTime + "' Lastupdated : '" + lastUpdatedTime + "' Expiry Time : '"
                                + MessageIdentifierProperties.getInstance().getExpiryMinutesInMillis() + "' Diff : '" + diff + "' Interface Type : '" + interfaceTypeTemp + "'");

                    if (diff > MessageIdentifierProperties.getInstance().getExpiryMinutesInMillis())
                    {
                        final String            tempAppInstanceID = outer.substring((KEY_OUTER_STATUS).length() + 1);
                        final MessageIdUsedInfo info              = new MessageIdUsedInfo(tempAppInstanceID, interfaceTypeTemp, allocatedIP, allocatedTime, lastUpdatedTime, d.getTime());
                        set.add(info);
                        expiredInstanceIDs.add(tempAppInstanceID);
                    }
                }

                if (!set.isEmpty())
                {
                    boolean result = false;

                    for (final MessageIdUsedInfo usedInfo : set)
                    {
                        log.warn("About to remove the unused mAppInstanceID '" + usedInfo.getAppInstanceID() + "' for the interface type : '" + mInterfaceType + "'");

                        result = resetAppInstanceID(mInterfaceType, usedInfo.getAppInstanceID(), false, mLocalIP);

                        log.warn("Result of removing the unused mAppInstanceID '" + usedInfo.getAppInstanceID() + "' for the interface type : '" + mInterfaceType + "' is '" + result + "'");

                        if (result)
                        {
                            mAppInstanceID = usedInfo.getAppInstanceID();
                            break;
                        }
                    }

                    if (!result)
                        log.warn("Unable to find an App Instance ID from the available expired List. Expired List : '" + expiredInstanceIDs + "'");
                }
                else
                    log.warn("NO Message Identifier App Instance id has expired till now.");
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while get the Next App Instance ID from Redis.", e);
        }
    }

    String getCurrentAppInstanceID()
    {
        return mAppInstanceID;
    }

    private void updateInitialAllocattion()
    {
        if (mAppInstanceID != null)
            try (
                    Jedis jedis = RedisConnection.getConnection())
            {
                final String              outerKey   = KEY_OUTER_STATUS + ":" + mAppInstanceID;
                final String              insertDate = DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);

                final Map<String, String> toPush     = new HashMap<>();
                toPush.put(KEY_INNER_INTERFACE_TYPE, mInterfaceType.getKey());
                toPush.put(KEY_INNER_ALLOCATED_IP, mLocalIP);
                toPush.put(KEY_INNER_ALLOCATED_TIME, insertDate);
                toPush.put(KEY_INNER_LAST_UPDATED, insertDate);

                final String hmset = jedis.hmset(outerKey, toPush);

                if (log.isDebugEnabled())
                    log.debug("Result of redis update : '" + hmset + "'");
            }
            catch (final Exception e)
            {
                log.error("Exception while get the Next App Instance ID from Redis.", e);
            }
        else
            log.error("No need to update the status in Redis as unable to identify the appInstance.");
    }

    void updateLastUsedTime()
    {

        try (
                Jedis jedis = RedisConnection.getConnection())
        {
            final String outerKey               = KEY_OUTER_STATUS + ":" + mAppInstanceID;
            final String insertDate             = DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
            final String interfaceTypeFromRedis = jedis.hget(outerKey, KEY_INNER_INTERFACE_TYPE);

            if ((interfaceTypeFromRedis == null) || interfaceTypeFromRedis.trim().equals(""))
            {
                final Map<String, String> toPush = new HashMap<>();
                toPush.put(KEY_INNER_INTERFACE_TYPE, mInterfaceType.getKey());
                toPush.put(KEY_INNER_ALLOCATED_IP, mLocalIP);
                toPush.put(KEY_INNER_ALLOCATED_TIME, insertDate);
                toPush.put(KEY_INNER_LAST_UPDATED, insertDate);
                toPush.put(KEY_INNER_ADDITIONAL_INFO, "Added the primary info as these are missing in Redis by the time : '" + insertDate + "'");

                final String hmset = jedis.hmset(outerKey, toPush);

                if (log.isDebugEnabled())
                    log.debug("Result of redis update : '" + hmset + "'");

                // We may also check the App Instance Ids in use.
                final List<String> lrange = jedis.lrange(KEY_OUTER_IN_USE, 0, -1);

                if ((lrange == null) || !lrange.contains(mAppInstanceID))
                {
                    final Long lpush = jedis.lpush(KEY_OUTER_IN_USE, mAppInstanceID);

                    if (log.isDebugEnabled())
                        log.debug("Result of redis when pushed instance ID to IN_USE list : '" + lpush + "'");
                }
                final Long lrem = jedis.lrem(KEY_OUTER_AVAILABLE, 0, mAppInstanceID);

                if (log.isDebugEnabled())
                    log.debug("Result of redis removing of the instance ID from AVAILABLE List : '" + lrem + "'");
            }
            else
            {
                final Long hset = jedis.hset(outerKey, KEY_INNER_LAST_UPDATED, insertDate);

                if (log.isDebugEnabled())
                    log.debug("Result of redis update of Last Used Time : '" + hset + "'");
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while get the Next App Instance ID from Redis.", e);
        }
    }

    void resetAppInstanceID()
    {
        resetAppInstanceID(mInterfaceType, mAppInstanceID, true, mLocalIP);
    }

    private static boolean resetAppInstanceID(
            final InterfaceType aInterfaceType,
            final String aAppInstanceID,
            final boolean aMoveItToAvailable,
            final String aLocalIP)
    {
        boolean returnValue = true;

        try (
                Jedis jedis = RedisConnection.getConnection();//
                Pipeline pipe = jedis.pipelined();//
        )
        {
            final String inuse     = KEY_OUTER_IN_USE;
            final String available = KEY_OUTER_AVAILABLE;
            final String outerKey  = KEY_OUTER_STATUS + ":" + aAppInstanceID;

            final Long   lrem      = jedis.lrem(inuse, 1, aAppInstanceID);

            if (lrem < 1)
                log.error("Unable to remove the instance ID : '" + aAppInstanceID + "' from the used list");

            Long lpush = null;

            if (aMoveItToAvailable)
                lpush = jedis.lpush(available, aAppInstanceID);
            else
                lpush = jedis.lpush(inuse, aAppInstanceID);

            if (log.isDebugEnabled())
                log.debug("LPush result '" + lpush + "' for instance id : '" + aAppInstanceID + "' Move to Available : '" + aMoveItToAvailable + "'");

            final String[]               fields           =
            { //
                    KEY_INNER_INTERFACE_TYPE, //
                    KEY_INNER_ALLOCATED_IP, //
                    KEY_INNER_ALLOCATED_TIME, //
                    KEY_INNER_LAST_UPDATED //
            };

            final Response<List<String>> hmget            = pipe.hmget(outerKey, fields);

            final Response<Long>         interfaceTypeSet = pipe.hdel(outerKey, KEY_INNER_INTERFACE_TYPE);
            final Response<Long>         ipSet            = pipe.hdel(outerKey, KEY_INNER_ALLOCATED_IP);
            final Response<Long>         allocatedTimeSet = pipe.hdel(outerKey, KEY_INNER_ALLOCATED_TIME);
            final Response<Long>         updatedTimeSet   = pipe.hdel(outerKey, KEY_INNER_LAST_UPDATED);
            pipe.sync();

            String             oldInterfaceType   = null;
            String             oldIp              = null;
            String             oldAssignedTime    = null;
            String             oldLastUpdatedTime = null;
            final List<String> list               = hmget.get();

            if (list.size() == 4)
            {
                oldInterfaceType   = list.get(INDEX_INTERFACE_TYPE);
                oldIp              = list.get(INDEX_ALLOCATED_IP);
                oldAssignedTime    = list.get(INDEX_ALLOCATED_TIME);
                oldLastUpdatedTime = list.get(INDEX_LAST_UPDATED_TIME);
            }

            if (log.isDebugEnabled())
            {
                log.debug("Result of redis value of interface type : '" + oldInterfaceType + "'");
                log.debug("Result of redis value of IP             : '" + oldIp + "'");
                log.debug("Result of redis value of Allocated Time : '" + oldAssignedTime + "'");
                log.debug("Result of redis value of Last Used Time : '" + oldLastUpdatedTime + "'");

                log.debug("Result of redis update of interface type : '" + interfaceTypeSet.get() + "'");
                log.debug("Result of redis update of IP             : '" + ipSet.get() + "'");
                log.debug("Result of redis update of Allocated Time : '" + allocatedTimeSet.get() + "'");
                log.debug("Result of redis update of Last Used Time : '" + updatedTimeSet.get() + "'");
            }

            if (log.isFatalEnabled())
                if (aMoveItToAvailable)
                    log.fatal("'" + aAppInstanceID + "' has been moved from the used list to available list ( Currently assigned for the interface type : '" + oldInterfaceType + "' assigned in IP '"
                            + oldIp + "' assigned at '" + oldAssignedTime + "' Last updated at '" + oldLastUpdatedTime + "' )");
                else
                    log.fatal("'" + aAppInstanceID + "' has been reassigned to a new instance for the interface type : '" + aInterfaceType + "' in IP '" + aLocalIP
                            + "' ( Previously assigned for the interface type : '" + oldInterfaceType + "' in assigned in IP '" + oldIp + "' assigned at '" + oldAssignedTime + "' Last updated at '"
                            + oldLastUpdatedTime + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while get the Next App Instance ID from Redis.", e);
            returnValue = false;
        }

        return returnValue;
    }

}