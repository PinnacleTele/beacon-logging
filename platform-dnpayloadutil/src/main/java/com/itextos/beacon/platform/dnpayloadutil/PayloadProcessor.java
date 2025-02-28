package com.itextos.beacon.platform.dnpayloadutil;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.commonlib.utility.RoundRobin;
import com.itextos.beacon.inmemory.dnpayload.util.DNPUtil;
import com.itextos.beacon.platform.dnpayloadutil.common.PayloadUtil;
import com.itextos.beacon.platform.dnpayloadutil.dao.PayloadInsertInDB;
import com.itextos.beacon.smslog.payLoadReceiverLog;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class PayloadProcessor
{

    private static final Log    log                        = LogFactory.getLog(PayloadProcessor.class);

    public static final String  REDIS_INDEX_NOT_APPLICABLE = "NA";
    public static final String  REDIS_INDEX_REFER_DB       = "-1";
    public static final String  PAYLOAD_OPERATION_SUCCESS  = "1";
    public static final String  PAYLOAD_OPERATION_FAILED   = "0";
    private static final String REDISKEY_PREFIX_NEW        = "dnpayload-expire:";

    private PayloadProcessor()
    {}

    private static void getDLRPayload(
            SubmissionObject aSubmissionObject)
    {
        final long lSysdate      = System.currentTimeMillis();
        long       lSts          = lSysdate;
        long       lActualTs     = lSysdate;

        final int  lRetryAttempt = aSubmissionObject.getRetryAttempt();

        if (lRetryAttempt != 0)
        {
            lSts      = aSubmissionObject.getCarrierSubmitTime().getTime();
            lActualTs = aSubmissionObject.getActualCarrierSubmitTime().getTime();
        }

        final Date lScheduleTime = aSubmissionObject.getScheduleDateTime();
        Date       lStime        = aSubmissionObject.getMessageReceivedTime();

        if ((lScheduleTime != null) && (aSubmissionObject.getAttemptCount() == null) && (lRetryAttempt == 0))
            lStime = lScheduleTime;

        final int    lDNAdjust  = CommonUtility.getInteger(aSubmissionObject.getDnAdjustEnabled(), 0);
        final String lClientId  = aSubmissionObject.getClientId();
        final long   lSTimelong = lStime.getTime();

        if (lSts < lSTimelong)
        {
   
            aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" :: Looks Platfrom time is less than the time received from client (stime) so adjusting sts with random number");

            final int lMaxRandomInSec = CommonUtility.getInteger(PayloadUtil.getAppConfigValueAsString(ConfigParamConstants.GLOBAL_DN_ADJUSTMENT_IN_SEC), 10);

            if (lMaxRandomInSec > 0)
                lSts = lSTimelong + (CommonUtility.getRandomNumber(0, lMaxRandomInSec) * 1000);
            else
                lSts = lSTimelong;

         
            
            aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" ::: Adjusted sts:" + lSts + " for stime:" + lSTimelong);

        }

        if (lDNAdjust != 0)
        {
            final long    lDNAdjustMills     = lDNAdjust * 1000L;
            final boolean isWhiteListed      = PayloadUtil.checkNumberWhiteListed(aSubmissionObject.getMobileNumber());
            final boolean lExcludeCircleList = PayloadUtil.isCircleInExcludeList(lClientId, aSubmissionObject.getCircle());

             aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" ::: isWhiteListed:" + isWhiteListed + " circleInExcludeList:" + lExcludeCircleList);

            if (((lSts - lSTimelong) > lDNAdjustMills) && !isWhiteListed && !lExcludeCircleList)
                lSts = lSTimelong + (CommonUtility.getRandomNumber(0, lDNAdjust + 1) * 1000);
        }

        aSubmissionObject.setCarrierSubmitTime(new Date(lSts));
        aSubmissionObject.setActualCarrierSubmitTime(new Date(lActualTs));

        if (aSubmissionObject.getAttemptCount() == null)
            aSubmissionObject.setAttemptCount("1");
    }

    private static String getJsonFromMessage(
            SubmissionObject aSubmissionObject)
    {
        final String                  lClientId           = aSubmissionObject.getClientId();
        final HashMap<String, String> lPayloadMap         = new HashMap<>();
        final PropertiesConfiguration mPayloadprops       = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.DN_PAYLOAD_PARAMS_PROPERTIES, false);
        mPayloadprops.addProperty("param6", "dummy");
        final Iterator<String>        lIterator           = mPayloadprops.getKeys();
        final boolean                 isPayloadCanFullMsg = CommonUtility.isEnabled(PayloadUtil.getCutomFeatureValue(lClientId, CustomFeatures.PAYLOAD_LOGMSG_YN));

        while (lIterator.hasNext())
        {
            final String lPayloadParamkey = lIterator.next();
            final String lKeyValue        = CommonUtility.nullCheck(aSubmissionObject.getValue(MiddlewareConstant.getMiddlewareConstantByName(lPayloadParamkey)), true);

      
            if (lKeyValue.isEmpty())
                continue;

            final MiddlewareConstant lMwConstant = MiddlewareConstant.getMiddlewareConstantByName(lPayloadParamkey);

            switch (lMwConstant)
            {
                case MW_INTERFACE_GROUP_TYPE:
                    lPayloadMap.put(lPayloadParamkey, lKeyValue);
                    break;

                case MW_LONG_MSG:
                    if (isPayloadCanFullMsg)
                        lPayloadMap.put(lPayloadParamkey, lKeyValue);
                    break;

                // $CASES-OMITTED$
                default:
                    lPayloadMap.put(lPayloadParamkey, lKeyValue);
            }
        }

        final String json = new JSONObject(lPayloadMap).toString();


        aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+"  ::: payload json:-->" + json);

        return json;
    }

    public static String storePayload(
            SubmissionObject aSubmissionObject)
    {
        final boolean isNoPayloadForPromoMsg = CommonUtility.isEnabled(PayloadUtil.getAppConfigValueAsString(ConfigParamConstants.NOPAYLOAD_FOR_PROMO_MSG));

    
        aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+" : app_config_params promo.nopayload=" + isNoPayloadForPromoMsg);

        final MessageType lMsgType = aSubmissionObject.getMessageType();

        getDLRPayload(aSubmissionObject);

        /*
        if (isNoPayloadForPromoMsg && (lMsgType == MessageType.PROMOTIONAL) && !aSubmissionObject.isIsIntl())
        {
            aSubmissionObject.setRetryAttempt(0);

            if (log.isInfoEnabled())
                log.info(aSubmissionObject.getMessageId() + " Not adding payload for Promotional Message");

            return REDIS_INDEX_NOT_APPLICABLE;
        }

*/
        return insertIntoRedisOrDB(aSubmissionObject);
    }

    private static String insertIntoRedisOrDB(
            SubmissionObject aSubmissionObject)
    {
        String  returnValue = REDIS_INDEX_NOT_APPLICABLE;
        boolean isDone      = false;

        while (!isDone)
        {
            final String      lMessageId   = aSubmissionObject.getMessageId();
            final ClusterType lClusterType = aSubmissionObject.getClusterType();

         

            aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+"  ::: storing payload--->" + lMessageId + " user cluster--->" + lClusterType.getKey());

            final Date   expireDateTime       = getExpiryDateTime(aSubmissionObject);
            final String payloadExpiryTime    = DateTimeUtility.getFormattedDateTime(expireDateTime, DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH);
            final int    lRetryAttempt        = aSubmissionObject.getRetryAttempt();
            // final String lPayload = getDLRPayload(aSubmissionObject);
            final String lPayload             = getJsonFromMessage(aSubmissionObject);
            payLoadReceiverLog.log(lPayload);
            final Date   lSTime               = aSubmissionObject.getMessageReceivedTime();
            final Date   dateStime            = lSTime;

            final int    lPayloadRedisPoolCnt = RedisConnectionProvider.getInstance().getRedisPoolCount(lClusterType, Component.DN_PAYLOAD);
            final int    lPayloadRedisIndex   = RoundRobin.getInstance().getCurrentIndex("payload", lPayloadRedisPoolCnt);
            final String lRedisIndex          = Integer.toString(lPayloadRedisIndex);

            aSubmissionObject.setPayloadExpiry(payloadExpiryTime);

            boolean isDBIsert = false;

            try (
                    Jedis lJedisConn = RedisConnectionProvider.getInstance().getConnection(lClusterType, Component.DN_PAYLOAD, lPayloadRedisIndex);)
            {
             
                final String key    = REDISKEY_PREFIX_NEW + payloadExpiryTime;
                final String lField = CommonUtility.combine(lMessageId, String.valueOf(lRetryAttempt));

                final long   lCnt   = lJedisConn.hset(key, lField, lPayload);

                    aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+"  :: payload added successfully-->" + key);

                if (lCnt > 0)
                {
                     
                    aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+"  ::" + lClusterType + ", Added in Redis Index Value : '" + lRedisIndex + "', Field '" + lField + "', Mid :'" + lMessageId + "'");

                    returnValue = lRedisIndex;
                }
                else
                {
                    isDBIsert   = true;
                    returnValue = storePayloadInDB(lMessageId, lRetryAttempt, dateStime.getTime(), lClusterType.getKey(), lPayload, expireDateTime.getTime());
                    aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+"  :: Cluster:'" + lClusterType.getKey() + "', MessageId:'" + lMessageId + "', Key:'" + key + "', Field :'" + lField + "', Redis Insert Status:'" + lCnt + "'");

                
                }

                isDone = true;
            }
            catch (final Exception exp)
            {
                log.error("  :: Some problem in inserting into " + (isDBIsert ? "Database.. Will try after 100 ms" : "Redis. Will try with DB Insert."), exp);

                aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+"  :: Some problem in inserting into " + (isDBIsert ? "Database.. Will try after 100 ms" : "Redis. Will try with DB Insert.")+" ::: "+ErrorMessage.getStackTraceAsString(exp));

                try
                {

                    if (!isDBIsert)
                    {
                        returnValue = storePayloadInDB(lMessageId, lRetryAttempt, dateStime.getTime(), lClusterType.getKey(), lPayload, expireDateTime.getTime());
                        isDone      = true;
                    }
                }
                catch (final Exception e)
                {
                    aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+"  ::: problem storing to redis and DB retrying after 100 millis due to..."+ ErrorMessage.getStackTraceAsString(e));

                    log.error("  ::: problem storing to redis and DB retrying after 100 millis due to...", e);
                }
            }
            if (!isDone)
                CommonUtility.sleepForAWhile(100L);
        }
        return returnValue;
    }

    private static String storePayloadInDB(
            String aMessageId,
            int aRetryAttempt,
            long aTime,
            String aKey,
            String aPayload,
            long aExpiryTime)
            throws Exception
    {
        return PayloadInsertInDB.storePayload(aMessageId, aRetryAttempt, aTime, aKey, aPayload, aExpiryTime);
    }

    private static Date getExpiryDateTime(
            SubmissionObject aSubmissionObject)
    {
        // calculate expiry from current time and set payload_expiry

        final Calendar lCalExpiry = Calendar.getInstance();
        lCalExpiry.setLenient(false);
        lCalExpiry.add(Calendar.HOUR_OF_DAY, DNPUtil.getPayloadExpiry(aSubmissionObject.getClientId()));
        lCalExpiry.set(Calendar.MINUTE, 0);
        lCalExpiry.set(Calendar.SECOND, 0);
        lCalExpiry.set(Calendar.MILLISECOND, 0);

       
        aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+"  ::: calculated expiry=" + lCalExpiry );


        return lCalExpiry.getTime();
    }

    public static DeliveryObject retrivePayload(
            DeliveryObject aDeliveryObject)
    {

        try
        {
            final String lMessageId    = aDeliveryObject.getMessageId();
            final int    lRetryAttempt = aDeliveryObject.getRetryAttempt();
            final String lRedisIndex   = aDeliveryObject.getPayloadRedisId();
        
            aDeliveryObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aDeliveryObject.getBaseMessageId()+"  ::: Payload Redis Index : " + lRedisIndex );

            String lKeyPartDateTime = aDeliveryObject.getPayloadExpiry();

        
            aDeliveryObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aDeliveryObject.getBaseMessageId()+"  ::: Payload Expiry ..Key : " + lKeyPartDateTime );

            if (lKeyPartDateTime == null)
            {
                final Date lStime = aDeliveryObject.getMessageReceivedTime();
                lKeyPartDateTime = DateTimeUtility.getFormattedDateTime(lStime, DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH);
            }

            final String lRedisKey    = REDISKEY_PREFIX_NEW + lKeyPartDateTime;
            String       lJsonPayload = null;

            if (lRedisIndex.equals(REDIS_INDEX_REFER_DB))
            {
                 
                aDeliveryObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aDeliveryObject.getBaseMessageId()+"  ::: retriving payload from mysql for key--->" + lRedisKey);

                lJsonPayload = PayloadInsertInDB.retrivePayload(lMessageId, lRetryAttempt);
            }
            else
            {
    
                aDeliveryObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aDeliveryObject.getBaseMessageId()+" ::: Going to verify from redis.....");

                if (!REDIS_INDEX_NOT_APPLICABLE.equals(lRedisIndex))
                {
                    lJsonPayload = getPayloadFromRedis(aDeliveryObject, lRedisIndex, lMessageId, lRetryAttempt, lRedisKey);

                    
                    aDeliveryObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aDeliveryObject.getBaseMessageId()+"  ::: Payload from redis ---> " + lJsonPayload);

                }
            }

            if (lJsonPayload == null)
            {
                aDeliveryObject.setDnPayloadStatus(REDIS_INDEX_REFER_DB);
                return aDeliveryObject;
            }

            payLoadReceiverLog.log(lJsonPayload);
            
            final Map<String, String> payload = getAsHashMap(aDeliveryObject,lJsonPayload);

            if (log.isDebugEnabled())
                log.debug("Payload from Redis/DB :: " + payload);

            for (final Entry<String, String> entry : payload.entrySet())
                aDeliveryObject.putValue(MiddlewareConstant.getMiddlewareConstantByName(entry.getKey()), entry.getValue());

            MessageUtil.setHeaderId(aDeliveryObject, aDeliveryObject.getValue(MiddlewareConstant.MW_HEADER));

            aDeliveryObject.setDnPayloadStatus(PAYLOAD_OPERATION_SUCCESS);
        }
        catch (final Exception exp)
        {
            aDeliveryObject.setDnPayloadStatus(PAYLOAD_OPERATION_FAILED);
            log.error("Problem retreiving payload...", exp);
            exp.printStackTrace();
        }
        return aDeliveryObject;
    }

    private static String getPayloadFromRedis(
            DeliveryObject aDeliveryObject,
            String lRedisIndex,
            String lMessageId,
            int lRetryAttempt,
            String lRedisKey)
    {
        final ClusterType lCluster = aDeliveryObject.getClusterType();

        try (
                final Jedis lJedisConn = RedisConnectionProvider.getInstance().getConnection(lCluster, Component.DN_PAYLOAD, CommonUtility.getInteger(lRedisIndex));
                final Pipeline pipe = lJedisConn.pipelined();)
        {
            final String lRedisField = CommonUtility.combine(lMessageId, String.valueOf(lRetryAttempt));

             aDeliveryObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aDeliveryObject.getBaseMessageId()+"  ::: Retriving payload from redis for key--->" + lRedisKey + " field==>" + lRedisField);

            final Response<String> resp  = pipe.hget(lRedisKey, lRedisField);
            final Response<Long>   lHdel = pipe.hdel(lRedisKey, lRedisField);

            pipe.sync();

            return resp.get();
        }
    }

    public static void deletePayload(
            SubmissionObject aSubmissionObject)
    {

        try
        {
            final String lMessageId    = aSubmissionObject.getMessageId();
            final int    lRetryAttempt = aSubmissionObject.getRetryAttempt();
            final String lRedisIndex   = CommonUtility.nullCheck(aSubmissionObject.getPayloadRedisId());
            String       lKeyDateTime  = aSubmissionObject.getPayloadExpiry();

            if (lKeyDateTime == null)
                lKeyDateTime = DateTimeUtility.getFormattedDateTime(aSubmissionObject.getMessageReceivedTime(), DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH);

            final String lRedisKey = REDISKEY_PREFIX_NEW + lKeyDateTime;

            if (lRedisIndex.equals(REDIS_INDEX_REFER_DB))
            {
         
                aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+"  ::: retriving payload from mysql for key--->" + lRedisKey);

                PayloadInsertInDB.deletePayload(lMessageId, lRetryAttempt);
            }
            else
            {
      
                
                aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+"  ::: Calling Payload Redis option to remove the payload :'" + lRedisIndex + "', Redis Key:'" + lRedisKey + "', Mid:'" + lMessageId + "'");

                if (!REDIS_INDEX_NOT_APPLICABLE.equals(lRedisIndex))
                    deletePayloadFromRedis(aSubmissionObject, lRedisIndex, lRedisKey, lMessageId, lRetryAttempt);
                else
                {
                    aSubmissionObject.setDnPayloadStatus(REDIS_INDEX_NOT_APPLICABLE);
                    
                    aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+"  ::: payload not deleted for redisindex NA");

                }
            }
        }
        catch (final Exception exp)
        {
            exp.printStackTrace();
            aSubmissionObject.setDnPayloadStatus(PAYLOAD_OPERATION_FAILED);
            log.error("problem retreiving payload...", exp);
        }
    }

    private static void deletePayloadFromRedis(
            SubmissionObject aSubmissionObject,
            String lRedisIndex,
            String lRedisKey,
            String lMid,
            int lRetryAttempt)
    {
        final ClusterType lCluster = aSubmissionObject.getClusterType();

        try (
                Jedis lJedisConn = RedisConnectionProvider.getInstance().getConnection(lCluster, Component.DN_PAYLOAD, CommonUtility.getInteger(lRedisIndex));)
        {
            final String lFileId = CommonUtility.combine(lMid, String.valueOf(lRetryAttempt));

            aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+"  ::: deleting payload from redis for key--->" + lRedisKey + " field==>" + lFileId);

            final long lDeleted = lJedisConn.hdel(lRedisKey, lFileId);

      
            aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+"  ::: delete for key=" + lRedisKey + " field=" + lFileId + " status=" + lDeleted);
            if (lDeleted > 0)
                aSubmissionObject.setDnPayloadStatus(PAYLOAD_OPERATION_SUCCESS);
            else
                aSubmissionObject.setDnPayloadStatus(REDIS_INDEX_REFER_DB);
        }
    }

    public static void removePayload(
            SubmissionObject aSubmissionObject)
    {
        final String lMessageId     = aSubmissionObject.getMessageId();
        final int    lRetryAttempt  = aSubmissionObject.getRetryAttempt();
        final String lPayloadRid    = aSubmissionObject.getPayloadRedisId();
        final Date   lStime         = aSubmissionObject.getMessageReceivedTime();
        final String lPayloadExpiry = aSubmissionObject.getPayloadExpiry();

        try
        {

            if (!REDIS_INDEX_NOT_APPLICABLE.equals(lPayloadRid) && (aSubmissionObject.getMtMessageRetryIdentifier() != null) && (lPayloadRid != null))
            {
                deletePayload(aSubmissionObject);
                final String lPayloadStatus = CommonUtility.nullCheck(aSubmissionObject.getDnPayloadStatus(), true);

                if (!PAYLOAD_OPERATION_SUCCESS.equals(lPayloadStatus)) {
                     
                    aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+"  ::: problem deleting payload for mid --->" + lMessageId + " retry attempt=" + lRetryAttempt + " payloadrid=" + lPayloadRid + " payload_expiry=" + lPayloadExpiry + " stime="
                            + lStime);

                }

                if (PAYLOAD_OPERATION_FAILED.equals(lPayloadStatus))
                    PayloadRedisDeleteTask.getInstance().addToInmemQueue(aSubmissionObject);
            }
        }
        catch (final Exception exp)
        {
            aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+"  ::: Remove payload error on message retry blockout/expire... Message Id --->" + lMessageId + " retry attempt=" + lRetryAttempt + " payloadrid=" + lPayloadRid + " stime=" + lStime+" ::: "+ErrorMessage.getStackTraceAsString(exp));
            log.error("  ::: Remove payload error on message retry blockout/expire... Message Id --->" + lMessageId + " retry attempt=" + lRetryAttempt + " payloadrid=" + lPayloadRid + " stime=" + lStime,
                    exp);
        }
    }

    private static Map<String, String> getAsHashMap(
            DeliveryObject aDeliveryObject, String json)
    {

        try
        {
            final JSONParser lParser = new JSONParser();
            return (JSONObject) lParser.parse(json);
        }
        catch (final Exception exp)
        {
        	
        	aDeliveryObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aDeliveryObject.getBaseMessageId()+"  ::: json to hashmap conversion problem..." + json+"  ::: "+ErrorMessage.getStackTraceAsString(exp));

            log.error("  ::: json to hashmap conversion problem..." + json, exp);
        }
        return new HashMap<>();
    }

}