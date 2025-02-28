package com.itextos.beacon.smpp.concatenate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.errorlog.ConcateRedisLog;
import com.itextos.beacon.errorlog.ErrorLog;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Transaction;

class RedisOperation
{

    private static final Log    log                           = LogFactory.getLog(RedisOperation.class);

    public static final char    REDIS_CONCAT_CHAR             = ':';
    public static final String  REDIS_RESULT_KEY_CURSOR       = "cursor";
    public static final String  REDIS_RESULT_KEY_PAYLOAD      = "payload";

    private static final int    FIRST_ENTRY                   = 1;
    private static final String REDIS_KEY_RECEIVED            = "smpp:concat:received";
    private static final String REDIS_KEY_RECEIVED_INITIAL    = "smpp:concat:received:initial";
    private static final String REDIS_KEY_CONCATE_ALL         = "smpp:concat:all";
    private static final String REDIS_KEY_CONCATE_READY       = "smpp:concat:ready";
    private static final String REDIS_KEY_TOTAL_PARTS         = "totalparts";
    private static final String REDIS_KEY_FIRST_RECEIVED_TIME = "firstreceivedtime";
    private static final String REDIS_KEY_RECEIVED_EXPIRED    = "smpp:concat:received:expired";

    private RedisOperation()
    {}

    public static int pushMessageToRedis(
            ClusterType aClusterType,
            int aRefNumber,
            String aCliDestRefNumber,
            String aMessageKey,
            String aMessage,
            int aTotalParts,
            long aRecivedTime)
    {
        int       increment      = -1;

        final int redisPoolIndex = RedisOperation.getRedisPoolIndex(aClusterType, aRefNumber);

        if (log.isDebugEnabled())
            log.debug("Redis Pool index : " + redisPoolIndex);

        /*
         * if (!RedisMemoryChecker.getInstance().canWrite(aClusterType, redisPoolIndex))
         * return increment;
         */

        try (
                Jedis jedis = getConnection(aClusterType, redisPoolIndex);
                Transaction trans = jedis.multi();)

        {
        	
        	ConcateRedisLog.getInstance().log("aMessage :  "+ aMessage);
            final Response<Long> lHincrBy = trans.hincrBy(REDIS_KEY_RECEIVED, aCliDestRefNumber, 1);
            final Response<Long> lHset    = trans.hsetnx(REDIS_KEY_CONCATE_ALL, aMessageKey, aMessage);

            trans.exec();
            increment = lHincrBy.get().intValue();

            if (lHset != null)
            {
                if (log.isDebugEnabled())
                    log.debug("Message Key:'" + aMessageKey + "', HSet Value :'" + lHset.get() + "'");

                if (lHset.get() == 1)
                    populateInitialInfo(jedis, increment, aCliDestRefNumber, aTotalParts, aRecivedTime);
                else
                    if (lHset.get() == 0)
                    {
                        jedis.hincrBy(REDIS_KEY_RECEIVED, aCliDestRefNumber, -1);
                        increment = -2;
                    }
            }
        }
        catch (final Exception e)
        {
            log.error("problem pushing to redis..", e);
            ErrorLog.log("concate problem pushing to redis.."+ErrorMessage.getStackTraceAsString(e));
            increment = -1;
        }

        return increment;
    }

    private static void populateInitialInfo(
            Jedis aJedis,
            int aIncrement,
            String aCounterIncrement,
            int aTotalParts,
            long aRecivedTime)
    {

        if (aIncrement == FIRST_ENTRY)
        {
            final Map<String, String> initialValues = new HashMap<>();
            initialValues.put(CommonUtility.combine(REDIS_CONCAT_CHAR, aCounterIncrement, REDIS_KEY_TOTAL_PARTS), Integer.toString(aTotalParts));
            initialValues.put(CommonUtility.combine(REDIS_CONCAT_CHAR, aCounterIncrement, REDIS_KEY_FIRST_RECEIVED_TIME), Long.toString(aRecivedTime));
            aJedis.hmset(REDIS_KEY_RECEIVED_INITIAL, initialValues);
        }
    }

    public static boolean pushToConcatReady(
            ClusterType aClusterType,
            int aRefNumber,
            String aAllPartsReceivedKey)
    {
        boolean   pushed         = false;
        final int redisPoolIndex = RedisOperation.getRedisPoolIndex(aClusterType, aRefNumber);

        try (
                Jedis jedis = getConnection(aClusterType, redisPoolIndex);)
        {
            pushed = jedis.lpush(REDIS_KEY_CONCATE_READY, aAllPartsReceivedKey) == 1;
        }
        catch (final Exception e)
        {
            log.error("problem pushing concat ready..", e);
        }

        return pushed;
    }

    public static boolean pushConcatReady(
            ClusterType aClusterType,
            int aRedisPoolIndex,
            String key)
    {
        boolean pushed = false;

        try (
                Jedis jedis = getConnection(aClusterType, aRedisPoolIndex);)
        {
            pushed = jedis.lpush(REDIS_KEY_CONCATE_READY, key) == 1;
        }
        catch (final Exception e)
        {
            log.error("problem pushing concat ready..", e);
        }

        return pushed;
    }

    public static List<String> getCompletedMessageRefNumbers(
            ClusterType aClusterType,
            int aRedisPoolIndex,
            int aMaxCount)
    {
        final List<String> returnValue = new ArrayList<>();

        try (
                Jedis jedis = getConnection(aClusterType, aRedisPoolIndex);)

        {
            if (log.isDebugEnabled())
                log.debug(aClusterType + ":" + aRedisPoolIndex);

            final Long lLlen = jedis.llen(REDIS_KEY_CONCATE_READY);

            if (log.isDebugEnabled())
                log.debug("getCompletedMessageRefNumbers() - Concat ready keys length : '" + lLlen + "'");

            if (lLlen > 0)
            {
                final int count = (int) (aMaxCount > lLlen ? lLlen : aMaxCount);
                returnValue.addAll(getListFromRedis(jedis, count));
            }
        }
        if (log.isDebugEnabled())
            log.debug("Return Value : " + returnValue);

        return returnValue;
    }

    private static List<String> getListFromRedis(
            Jedis aJedis,
            int aCount)
    {
        final List<String> returnValue = new ArrayList<>(aCount);

        try
        {

            for (int index = 0; index < aCount; index++)
            {
                final String refNumber = aJedis.rpop(REDIS_KEY_CONCATE_READY);

                if (refNumber == null)
                    break;

                final boolean added = returnValue.contains(refNumber);
                if (added)
                    log.error("Ref number already added in the list May be a duplicate." + refNumber + "'");
                else
                    returnValue.add(refNumber);
            }

            if (log.isDebugEnabled())
                log.debug("Concat Ready Key list :" + returnValue);
        }
        catch (final Exception e)
        {
            throw e;
        }

        return returnValue;
    }

    public static Map<String, Object> getConcatMessageRefNumbers(
            ClusterType aClusterType,
            int aRedisPoolIndex,
            String aStartPosition,
            int aMaxCount)
    {
        final HashMap<String, Object> returnValue = new HashMap<>();

        try (
                Jedis jedis = getConnection(aClusterType, aRedisPoolIndex);)
        {
            final ScanResult<Entry<String, String>> result = jedis.hscan(REDIS_KEY_RECEIVED, aStartPosition, new ScanParams().count(aMaxCount));
            returnValue.put(REDIS_RESULT_KEY_CURSOR, result.getCursor());

            final List<Entry<String, String>> entryResult        = result.getResult();

            final List<PendingMessageInfo>    pendingMessageList = new ArrayList<>();
            for (final Entry<String, String> anEntry : entryResult)
                pendingMessageList.add(new PendingMessageInfo(anEntry.getKey(), CommonUtility.getInteger(anEntry.getValue(), -1)));

            getTotalPartsAndReceivedTime(jedis, pendingMessageList);

            returnValue.put(REDIS_RESULT_KEY_PAYLOAD, pendingMessageList);
        }
        return returnValue;
    }

    /*
     * private static void getTotalPartsAndReceivedTime(
     * Jedis aJedis,
     * List<PendingMessageInfo> aPendingMessageList)
     * {
     * Pipeline pipe = null;
     * try
     * {
     * pipe = aJedis.pipelined();
     * final Map<String, Response<List<String>>> jedisResponse = new HashMap<>();
     * for (final PendingMessageInfo pmi : aPendingMessageList)
     * {
     * final String[] toGetKeys = new String[2];
     * toGetKeys[0] = CommonUtility.combine(REDIS_CONCAT_CHAR, pmi.getRefNumber(),
     * REDIS_KEY_TOTAL_PARTS);
     * toGetKeys[1] = CommonUtility.combine(REDIS_CONCAT_CHAR, pmi.getRefNumber(),
     * REDIS_KEY_FIRST_RECEIVED_TIME);
     * jedisResponse.put(pmi.getRefNumber(), pipe.hmget(REDIS_KEY_RECEIVED_INITIAL,
     * toGetKeys));
     * }
     * pipe.sync();
     * for (final PendingMessageInfo pmi : aPendingMessageList)
     * {
     * final String refNumber = pmi.getRefNumber();
     * final Response<List<String>> lResponse = jedisResponse.get(refNumber);
     * if (lResponse != null)
     * {
     * final List<String> resultList = lResponse.get();
     * if ((resultList != null) && (resultList.size() == 2))
     * {
     * final int totalParts = CommonUtility.getInteger(resultList.get(0), -999);
     * final long receivedTime = CommonUtility.getLong(resultList.get(1), -5555);
     * pmi.setTotalPartsCount(totalParts);
     * pmi.setReceivedTime(receivedTime);
     * }
     * }
     * }
     * }
     * finally
     * {
     * if (pipe != null)
     * pipe.close();
     * }
     * }
     */

    private static void getTotalPartsAndReceivedTime(
            Jedis aJedis,
            List<PendingMessageInfo> aPendingMessageList)
    {

        try (
                Pipeline pipe = aJedis.pipelined())
        {
            final Map<String, Response<List<String>>> jedisResponse = new HashMap<>();

            for (final PendingMessageInfo pmi : aPendingMessageList)
            {
                final String[] toGetKeys = new String[2];
                toGetKeys[0] = CommonUtility.combine(REDIS_CONCAT_CHAR, pmi.getRefNumber(), REDIS_KEY_TOTAL_PARTS);
                toGetKeys[1] = CommonUtility.combine(REDIS_CONCAT_CHAR, pmi.getRefNumber(), REDIS_KEY_FIRST_RECEIVED_TIME);
                jedisResponse.put(pmi.getRefNumber(), pipe.hmget(REDIS_KEY_RECEIVED_INITIAL, toGetKeys));
            }
            pipe.sync();

            for (final PendingMessageInfo pmi : aPendingMessageList)
            {
                final String                 refNumber = pmi.getRefNumber();
                final Response<List<String>> lResponse = jedisResponse.get(refNumber);

                if (lResponse != null)
                {
                    final List<String> resultList = lResponse.get();

                    if ((resultList != null) && (resultList.size() == 2))
                    {
                        final int  totalParts   = CommonUtility.getInteger(resultList.get(0), -999);
                        final long receivedTime = CommonUtility.getLong(resultList.get(1), -5555);
                        pmi.setTotalPartsCount(totalParts);
                        pmi.setReceivedTime(receivedTime);
                    }
                }
            }
        }
    }

    public static Jedis getConnection(
            ClusterType aClusterType,
            int aRedisPoolIndex)
    {
        return RedisConnectionProvider.getInstance().getConnection(aClusterType, Component.SMPP_CONCAT, (aRedisPoolIndex + 1));
    }

    public static int getRedisPoolIndex(
            ClusterType aClusterType,
            int aRefNumber)
    {
        final int lRedisPoolCount = RedisConnectionProvider.getInstance().getRedisPoolCount(aClusterType, Component.SMPP_CONCAT);
        return aRefNumber % lRedisPoolCount;
    }

    public static Map<String, String> getMessagesForRefNumber(
            ClusterType aClusterType,
            int aRedisPoolIndex,
            String aRefNumber,
            boolean aCheckForAllPartsReceived)
    {
        final Map<String, String> resultMap = new LinkedHashMap<>();

        try (
                Jedis jedis = getConnection(aClusterType, aRedisPoolIndex);)
        {
            final String totalParts = jedis.hget(REDIS_KEY_RECEIVED_INITIAL, CommonUtility.combine(REDIS_CONCAT_CHAR, aRefNumber, REDIS_KEY_TOTAL_PARTS));

            if (log.isDebugEnabled())
                log.debug("getMatchingValues() - get Message Parts count for key " + aRefNumber + " :: count :" + totalParts);

            if (totalParts != null)
            {
                final int      totalPartsCount = Integer.parseInt(totalParts);
                final String[] partFields      = new String[totalPartsCount];

                for (int partNo = 0; partNo < totalPartsCount; partNo++)
                {
                    if (log.isDebugEnabled())
                        log.debug("Part Number : " + partNo);

                    partFields[partNo] = aRefNumber + ":" + (partNo + 1);
                }

                if (log.isDebugEnabled())
                    log.debug("Part field map before hmGet :" + Arrays.asList(partFields));

                final List<String> result = jedis.hmget(REDIS_KEY_CONCATE_ALL, partFields);

                for (int i = 0; i < partFields.length; i++)
                {
                    if (log.isDebugEnabled())
                        log.debug("Part fields : " + partFields[i]);

                    if (result.get(i) != null)
                        resultMap.put(partFields[i], result.get(i));
                }

                if (log.isDebugEnabled())
                    log.debug("Final ResultMap : " + resultMap);

                // in anycase the number of messages are not matching with the total parts.
                if ((aCheckForAllPartsReceived) && (resultMap.size() != totalPartsCount))
                    return null;
            }
        }
        catch (final Exception e)
        {
            log.error("problem getting matching values ..", e);
        }
        return resultMap;
    }

    public static void removeProcessedMessages(
            ClusterType aClusterType,
            int aRedisPoolIndex,
            Map<String, List<String>> aToDelete)
    {
        if (log.isDebugEnabled())
            log.debug("Remove Concat Redis Info :" + aToDelete);

        try (
                Jedis jedis = getConnection(aClusterType, aRedisPoolIndex);
                Pipeline pipe = jedis.pipelined();)
        {

            for (final Entry<String, List<String>> entry : aToDelete.entrySet())
            {
                final String[] toDeleteKeys = new String[2];

                toDeleteKeys[0] = CommonUtility.combine(REDIS_CONCAT_CHAR, entry.getKey(), REDIS_KEY_TOTAL_PARTS);
                toDeleteKeys[1] = CommonUtility.combine(REDIS_CONCAT_CHAR, entry.getKey(), REDIS_KEY_FIRST_RECEIVED_TIME);

                pipe.hdel(REDIS_KEY_RECEIVED, entry.getKey());
                pipe.hdel(REDIS_KEY_RECEIVED_INITIAL, toDeleteKeys);

                // final List<String> temp = entry.getValue();
                // final String[] msgKeys = new String[temp.size()];
                //
                // int index = 0;
                // for (final String s : temp)
                // msgKeys[index++] = s;
                //
                // pipe.hdel(REDIS_KEY_CONCATE_ALL, msgKeys);

                final List<String> temp = entry.getValue();

                for (final String s : temp)
                {
                    if (log.isDebugEnabled())
                        log.debug("Concat ALL Key : " + s);

                    pipe.hdel(REDIS_KEY_CONCATE_ALL, s);
                }
            }
            pipe.sync();
        }
    }

    public static long getFirstReceivedTime(
            ClusterType aClusterType,
            int aRedisPoolIndex,
            String aRefKey)
    {
        String lFirstReceivedTime = "";

        try (
                Jedis jedis = getConnection(aClusterType, aRedisPoolIndex);)
        {
            lFirstReceivedTime = jedis.hget(REDIS_KEY_RECEIVED_INITIAL, CommonUtility.combine(REDIS_CONCAT_CHAR, aRefKey, REDIS_KEY_FIRST_RECEIVED_TIME));
        }
        return CommonUtility.getLong(lFirstReceivedTime);
    }

    public static void concatExpiryLog(
            ClusterType aClusterType,
            int aRedisPoolIndex,
            String aRefKey)
    {

        try (
                Jedis jedis = getConnection(aClusterType, aRedisPoolIndex);
                Pipeline pipe = jedis.pipelined();)
        {
            final String lValue = jedis.hget(REDIS_KEY_RECEIVED, aRefKey);

            if (log.isDebugEnabled())
                log.debug("Expirey Value for the key :'" + aRefKey + "', Received Count :'" + lValue + "'");

            pipe.lpush(REDIS_KEY_RECEIVED_EXPIRED, aRefKey);

            if ((lValue != null) && (lValue != "0"))
                pipe.hincrBy(REDIS_KEY_RECEIVED, aRefKey, -1);

            pipe.sync();
        }
    }

    public static Map<String, Object> getAll(
            ClusterType aClusterType,
            int aRedisPoolIndex,
            String aCursor,
            int aMaxCount)
    {
        final HashMap<String, Object> lResult = new HashMap<>();

        try (
                Jedis jedis = getConnection(aClusterType, aRedisPoolIndex);)
        {
            final ScanResult<Entry<String, String>> scanResult = jedis.hscan(REDIS_KEY_CONCATE_ALL, aCursor, new ScanParams().count(aMaxCount));
            lResult.put("cursor", scanResult.getCursor());
            final List<Entry<String, String>> entryResult = scanResult.getResult();
            final HashMap<String, String>     payload     = new HashMap<>();

            for (final Entry<String, String> anEntry : entryResult)
                payload.put(anEntry.getKey(), anEntry.getValue());

            if (!payload.isEmpty())
                lResult.put("payload", payload);
        }

        return lResult;
    }

    public static void removeProcessedMessages(
            ClusterType aClusterType,
            int aRedisPoolIndex,
            List<String> aToDelete)
    {
        if (log.isDebugEnabled())
            log.debug("Often Remove Concat Redis Info :" + aToDelete);

        try (
                Jedis jedis = getConnection(aClusterType, aRedisPoolIndex);
                Pipeline pipe = jedis.pipelined();)
        {

            for (final String key : aToDelete)
            {
                final String   finalKey     = key.substring(0, key.lastIndexOf(":"));

                final String[] toDeleteKeys = new String[2];

                toDeleteKeys[0] = CommonUtility.combine(REDIS_CONCAT_CHAR, finalKey, REDIS_KEY_TOTAL_PARTS);
                toDeleteKeys[1] = CommonUtility.combine(REDIS_CONCAT_CHAR, finalKey, REDIS_KEY_FIRST_RECEIVED_TIME);

                pipe.hdel(REDIS_KEY_RECEIVED, finalKey);
                pipe.hdel(REDIS_KEY_RECEIVED_INITIAL, toDeleteKeys);

                pipe.hdel(REDIS_KEY_CONCATE_ALL, key);
            }
            pipe.sync();
        }
    }

}