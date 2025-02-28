package com.itextos.beacon.platform.singledn.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.customfeatures.pojo.DlrTypeInfo;
import com.itextos.beacon.platform.singledn.data.DeliveryInfo;
import com.itextos.beacon.platform.singledn.data.SingleDnRequest;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

public class RedisOperation
{

    private static final Log    log                            = LogFactory.getLog(RedisOperation.class);

    private static final char   REDIS_KEY_SEPARATOR            = ':';
    private static final String REDIS_KEY_SNGLE_DN             = "singledn";
    private static final String REDIS_KEY_SNGLE_DN_DATA        = "data";
    private static final String REDIS_KEY_SNGLE_DN_CREATE_TIME = "createtime";
    private static final String REDIS_KEY_SNGLE_DN_DUPLICATE   = "duplicate";
    private static final String REDIS_KEY_SNGLE_DN_EXPIRY      = "expiry";
    private static final String REDIS_KEY_SNGLE_DN_DELETE      = "delete";
    private static final String REDIS_KEY_ALL                  = "*";

    private RedisOperation()
    {}

    public static void incrementCountersAndinsertDnData(
            SingleDnRequest aSingleDnRequest,
            String aExpInSec)
    {

        try (
                final Jedis jedis = getRedisConnection(aSingleDnRequest.getClientId());)
        {
            final String       clientId      = aSingleDnRequest.getClientId();
            final String       baseMessageId = aSingleDnRequest.getBaseMessageId();
            final DeliveryInfo lDeliveryInfo = aSingleDnRequest.getDeliveryInfo();
            final int          partNo        = lDeliveryInfo.getPartNo();
            final int          lDlrExpiry    = CommonUtility.getInteger(aExpInSec, 0);

            final Date         expiryTime    = getExpiryTime(lDlrExpiry);

            if (log.isDebugEnabled())
            {
                log.debug("Redis operation for Client Id : '" + clientId + "' Base Message Id : '" + baseMessageId + "' Part No : '" + partNo + "'");
                log.debug("SetExpiry time for the Client Id : '" + clientId + "', Expiry Time :'" + expiryTime + "'");
            }

            String     key        = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_SNGLE_DN, REDIS_KEY_SNGLE_DN_DATA, clientId, baseMessageId);
            final Long insertCout = jedis.hset(key, "" + partNo, lDeliveryInfo.getDnJson());

            if (log.isDebugEnabled())
                log.debug("Redis operation Results " + insertCout);

            if (insertCout == 1)
            {
                if (log.isDebugEnabled())
                    log.debug("Adding the time information for the first record.");

                key = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_SNGLE_DN, REDIS_KEY_SNGLE_DN_CREATE_TIME, clientId);
                final String field = CommonUtility.combine(REDIS_KEY_SEPARATOR, baseMessageId);
                jedis.hset(key, field, DateTimeUtility.getFormattedDateTime(expiryTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while inserting the DN info into Redis " + aSingleDnRequest, e);
        }
    }

    public static List<DeliveryInfo> getPreviousDns(
            SingleDnRequest aSingleDnRequest)
    {
        final List<DeliveryInfo> returnValue = new ArrayList<>();

        try (
                final Jedis jedis = getRedisConnection(aSingleDnRequest.getClientId());)
        {
            final String              key     = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_SNGLE_DN, REDIS_KEY_SNGLE_DN_DATA, aSingleDnRequest.getClientId(),
                    aSingleDnRequest.getBaseMessageId());

            // This will have the partNo = JsonMap.
            final Map<String, String> lDnInfo = jedis.hgetAll(key);

            for (final Entry<String, String> entry : lDnInfo.entrySet())
                returnValue.add(gtDeliveryInfo(CommonUtility.getInteger(entry.getKey()), entry.getValue()));
        }
        return returnValue;
    }

    private static DeliveryInfo gtDeliveryInfo(
            int aPartNumber,
            String aDnJson)
    {
        final DeliveryInfo lDeliveryInfo = new DeliveryInfo(0, aPartNumber, aDnJson);
        return lDeliveryInfo;
    }

    public static boolean addToProcessedDn(
            SingleDnRequest aSingleDnRequest,
            DlrTypeInfo aDlrTypeInfo)
    {

        try (
                final Jedis jedis = getRedisConnection(aSingleDnRequest.getClientId());
                final Pipeline pipeline = jedis.pipelined();)
        {
            final String clientId      = aSingleDnRequest.getClientId();
            final String baseMessageId = aSingleDnRequest.getBaseMessageId();

            if (log.isDebugEnabled())
                log.debug("Redis operation for Client Id : '" + clientId + "' Base Message Id : '" + baseMessageId + "'");

            String     key    = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_SNGLE_DN, REDIS_KEY_SNGLE_DN_DUPLICATE, clientId);
            final Long dupVal = jedis.sadd(key, baseMessageId);

            if (log.isDebugEnabled())
                log.debug("Duplicate Check Redis Response : " + dupVal);

            if (dupVal > 0)
            {
                final int lExpVal = CommonUtility.getInteger(aDlrTypeInfo.getExpiryInSec(), 172800);
                if (log.isDebugEnabled())
                    log.debug("DN Wait for Sec : " + lExpVal);

                final Date expiryTime = getExpiryTime(lExpVal);

                if (log.isDebugEnabled())
                    log.debug("Expiry Time : " + expiryTime);

                final String expiryTimeStr = DateTimeUtility.getFormattedDateTime(expiryTime, DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM);
                key = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_SNGLE_DN, REDIS_KEY_SNGLE_DN_DUPLICATE, REDIS_KEY_SNGLE_DN_EXPIRY, clientId, expiryTimeStr);
                pipeline.lpush(key, baseMessageId);
            }

            key = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_SNGLE_DN, REDIS_KEY_SNGLE_DN_DATA, REDIS_KEY_SNGLE_DN_DELETE, clientId);
            pipeline.lpush(key, baseMessageId);

            pipeline.sync();

            return (dupVal == 0) ? true : false;
        }
        catch (final Exception e)
        {
            log.error("Exception while adding the processed Single DN information for the client id '" + aSingleDnRequest.getClientId() + "' Base Message Id : '" + aSingleDnRequest.getBaseMessageId()
                    + "'", e);

            return false;
        }
    }

    private static Date getExpiryTime(
            int aExpVal)
    {
        final long currTime = System.currentTimeMillis();

        final Date dt       = new Date();
        dt.setTime(currTime + (aExpVal * 1000));

        return dt;
    }

    public static boolean isSingleDnAlreadSent(
            String aClientId,
            String aBaseMessageId)
    {

        try (
                final Jedis jedis = getRedisConnection(aClientId);)
        {
            if (log.isDebugEnabled())
                log.debug("Redis operation Checking for SingleDn Duplicate for Client Id : '" + aClientId + "' Base Message Id : '" + aBaseMessageId + "'");

            final String key = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_SNGLE_DN, REDIS_KEY_SNGLE_DN_DUPLICATE, aClientId);
            return jedis.sismember(key, aBaseMessageId);
        }
        catch (final Exception e)
        {
            log.error("Exception while adding the processed Single DN information for the client id '" + aClientId + "' Base Message Id : '" + aBaseMessageId + "'", e);
            return false;
        }
    }

    public static boolean doDeleteSingleDnData(
            String aClientId)
    {

        try (
                final Jedis jedis = getRedisConnection(aClientId);
                final Pipeline pipeline = jedis.pipelined();)
        {
            final String       key       = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_SNGLE_DN, REDIS_KEY_SNGLE_DN_DATA, REDIS_KEY_SNGLE_DN_DELETE, aClientId);

            final List<String> lBaseMids = jedis.lrange(key, 0, -1);

            if (!lBaseMids.isEmpty())
            {

                for (final String baseMsgId : lBaseMids)
                {
                    final String aRecordKey  = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_SNGLE_DN, REDIS_KEY_SNGLE_DN_DATA, aClientId, baseMsgId);
                    final String aCreateTime = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_SNGLE_DN, REDIS_KEY_SNGLE_DN_CREATE_TIME, aClientId);

                    pipeline.del(aRecordKey);
                    pipeline.lrem(key, 1, baseMsgId);
                    pipeline.del(aCreateTime, baseMsgId);
                }
                pipeline.sync();
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while deleting the processed Single DN information for the client id '" + aClientId + "'", e);
            return false;
        }

        return true;
    }

    public static boolean removeDuplicateCheckData(
            String aClientId)
    {

        try (
                final Jedis jedis = getRedisConnection(aClientId);
                final Pipeline pipeline = jedis.pipelined();)
        {
            final String       lExpTime  = DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM);
            final String       key       = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_SNGLE_DN, REDIS_KEY_SNGLE_DN_DUPLICATE, REDIS_KEY_SNGLE_DN_EXPIRY, aClientId, lExpTime);

            final List<String> lBaseMids = jedis.lrange(key, 0, -1);

            if (!lBaseMids.isEmpty())
            {

                for (final String baseMsgId : lBaseMids)
                {
                    final String dupCheckRemovekey = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_SNGLE_DN, REDIS_KEY_SNGLE_DN_DUPLICATE, aClientId);
                    pipeline.srem(dupCheckRemovekey, baseMsgId);
                }
                pipeline.del(key);

                pipeline.sync();
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while deleting the Duplicate Check Single DN information for the client id '" + aClientId + "'", e);
            return false;
        }

        return true;
    }

    /**
     * Removing Old Duplicate SingleDN keys
     *
     * @param aClientId
     *
     * @return
     */
    public static boolean removeOldDuplicateCheckData(
            String aClientId,
            int aMaxKeysFetchLen)
    {

        try (
                final Jedis jedis = getRedisConnection(aClientId);)
        {
            final long   lExpTime = CommonUtility.getLong(DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM));
            final String parenkey = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_SNGLE_DN, REDIS_KEY_SNGLE_DN_DUPLICATE, REDIS_KEY_SNGLE_DN_EXPIRY, aClientId, REDIS_KEY_ALL);

            if (log.isDebugEnabled())
                log.debug("Duplicate Parent Key :" + parenkey);

            final List<String> aKeys = new ArrayList<>();

            final Set<String>  lKeys = jedis.keys(parenkey);

            if (!lKeys.isEmpty())
                for (final String key : lKeys)
                {
                    if (log.isDebugEnabled())
                        log.debug("Expiry Duplicate key : " + key);

                    final long oldExpTime = Long.parseLong(key.substring(key.lastIndexOf(REDIS_KEY_SEPARATOR) + 1));

                    if (log.isDebugEnabled())
                        log.debug("Old Exp Time :'" + oldExpTime + "', Current Time : '" + lExpTime + "', is Less then curremt Time :'" + (oldExpTime < lExpTime) + "'");

                    if (oldExpTime < lExpTime)
                        aKeys.add(key);
                }

            if (log.isDebugEnabled())
                log.debug("Duplicate Check Keys :" + aKeys);

            if (!aKeys.isEmpty())
                return removeOldDuplicateCheckData(aClientId, aKeys, jedis, aMaxKeysFetchLen);
        }
        catch (final Exception e)
        {
            log.error("Exception while deleting the Duplicate Check Single DN information for the client id '" + aClientId + "'", e);
            return false;
        }

        return true;
    }

    public static boolean removeOldDuplicateCheckData(
            String aClientId,
            List<String> aOldDupKeys,
            Jedis aJedis,
            int aMaxKeysFetchLen)
    {

        try (
                final Pipeline pipeline = aJedis.pipelined();)
        {

            for (final String key : aOldDupKeys)
            {
                if (log.isDebugEnabled())
                    log.debug("Old Duplicate Key :" + key);

                long lKeysLength = aJedis.llen(key);

                if (log.isDebugEnabled())
                    log.debug("Duplicate key records list :'" + lKeysLength + "', for the client :'" + aClientId + "'");

                while (lKeysLength > 0)
                {
                    final int lMaxLength = (int) (lKeysLength > aMaxKeysFetchLen ? aMaxKeysFetchLen : lKeysLength);

                    if (log.isDebugEnabled())
                        log.debug("Max Old Duplicate Records to processs  :'" + lMaxLength + "', for the client :'" + aClientId + "'");

                    final List<String> lBaseMids = aJedis.lrange(key, 0, lMaxLength);

                    if (log.isDebugEnabled())
                        log.debug("Base Mid's List :" + lBaseMids.size());

                    if (!lBaseMids.isEmpty())
                    {

                        for (final String baseMsgId : lBaseMids)
                        {
                            final String dupCheckRemovekey = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_SNGLE_DN, REDIS_KEY_SNGLE_DN_DUPLICATE, aClientId);
                            pipeline.srem(dupCheckRemovekey, baseMsgId);
                        }

                        pipeline.sync();
                    }
                    lKeysLength = aJedis.llen(key);

                    if (log.isDebugEnabled())
                        log.debug("Old Duplicate key records list :'" + lKeysLength + "', for the client :'" + aClientId + "'");
                }
                aJedis.del(key);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while deleting the Old Duplicate Check Single DN information for the client id '" + aClientId + "'", e);
            return false;
        }

        return true;
    }

    public static boolean checkExpiryData(
            String aClientId,
            int aMaxKeysFetchLen)
    {

        try (
                final Jedis jedis = getRedisConnection(aClientId);
                final Pipeline pipeline = jedis.pipelined();)
        {
            final String aCreateTime = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_SNGLE_DN, REDIS_KEY_SNGLE_DN_CREATE_TIME, aClientId);
            final String aDeletekey  = CommonUtility.combine(REDIS_KEY_SEPARATOR, REDIS_KEY_SNGLE_DN, REDIS_KEY_SNGLE_DN_DATA, REDIS_KEY_SNGLE_DN_DELETE, aClientId);

            long         lKeysLength = jedis.hlen(aCreateTime);

            if (log.isDebugEnabled())
                log.debug("Create Time key records list :'" + lKeysLength + "', for the client :'" + aClientId + "'");

            while (lKeysLength > 0)
            {
                final int lMaxLength = (int) (lKeysLength > aMaxKeysFetchLen ? aMaxKeysFetchLen : lKeysLength);

                if (log.isDebugEnabled())
                    log.debug("Max Time Key Records to processs  :'" + lMaxLength + "', for the client :'" + aClientId + "'");

                expiryProcess(jedis, lMaxLength, aCreateTime, pipeline, aDeletekey);

                lKeysLength = jedis.hlen(aCreateTime);

                if (log.isDebugEnabled())
                    log.debug("Curremt Create Time key records list :'" + lKeysLength + "', for the client :'" + aClientId + "'");
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while deleting the Duplicate Check Single DN information for the client id '" + aClientId + "'", e);
            return false;
        }
        return true;
    }

    private static void expiryProcess(
            Jedis jedis,
            int lMaxLength,
            String aCreateTime,
            Pipeline aPipeline,
            String aDeletekey)
    {
        final ScanResult<Entry<String, String>> result      = jedis.hscan(aCreateTime, "0", new ScanParams().count(lMaxLength));

        final List<Entry<String, String>>       entryResult = result.getResult();

        for (final Entry<String, String> anEntry : entryResult)
        {
            final String lField    = anEntry.getKey();
            final String lValue    = anEntry.getValue();

            final Date   redisDate = DateTimeUtility.getDateFromString(lValue, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);

            if (redisDate.getTime() < System.currentTimeMillis())
            {
                if (log.isDebugEnabled())
                    log.debug("Adding BaseMid into delete process key..'" + lField + "'");

                aPipeline.lpush(aDeletekey, lField);
                aPipeline.hdel(aCreateTime, lField);
            }
        }
        aPipeline.sync();
    }

    private static Jedis getRedisConnection(
            String aClientId)
    {
        final int redisPoolIndex  = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.INTRIM_DN);
        final int lRedisPollIndex = (int) (Long.parseLong(aClientId) % redisPoolIndex);
        return getRedisConnection(Component.INTRIM_DN, lRedisPollIndex);
    }

    private static Jedis getRedisConnection(
            Component aComponent,
            int lRedisPoolIndex)
    {
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, aComponent, (lRedisPoolIndex + 1));
    }

}