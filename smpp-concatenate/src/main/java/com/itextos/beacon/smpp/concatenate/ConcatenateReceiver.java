package com.itextos.beacon.smpp.concatenate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.prometheusmetricsutil.smpp.SmppPrometheusInfo;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.http.interfaceutil.InterfaceUtil;
import com.itextos.beacon.smpp.objects.SmppUserInfo;
import com.itextos.beacon.smpp.objects.request.SmppMessageRequest;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;

public class ConcatenateReceiver
{

    private static final Log log                           = LogFactory.getLog(ConcatenateReceiver.class);
    private static final int REDIS_INSERT_FAIL             = -1;
    private static final int DUPLICATE_UDH_FOR_SAME_MOBILE = -2;

    public static void addSmppMessage(
            ClusterType aClusterType,
            SmppMessageRequest aSmppMessageRequest,
            boolean aInsertIntoDb,
            StringBuffer sb)
            throws Exception
    {
        final String refNumberString     = aSmppMessageRequest.getUdhReferenceNumber();
        final int    totalParts          = aSmppMessageRequest.getTotalParts();
        final int    partNumber          = aSmppMessageRequest.getPartNumber();
        final String clientId            = aSmppMessageRequest.getClientId();
        final String mobileNumber        = aSmppMessageRequest.getMobileNumber();
        final String messageJson         = aSmppMessageRequest.getJsonString();
        final long   receivedTime        = aSmppMessageRequest.getReceivedTime();
        final int    refNumber           = Integer.parseInt(refNumberString, 16);

        final String counterIncrementKey = CommonUtility.combine(RedisOperation.REDIS_CONCAT_CHAR, clientId, mobileNumber, refNumberString);
        final String messageKey          = CommonUtility.combine(RedisOperation.REDIS_CONCAT_CHAR, counterIncrementKey, Integer.toString(partNumber));

        sb.append("aClusterType : "+aClusterType+" : refNumber :  "+ refNumber+" : counterIncrementKey :"+counterIncrementKey+" : messageKey :"+ messageKey+" : totalParts :"+ totalParts+" : receivedTime :"+ receivedTime).append("\n");

        final int    messagesCount       = RedisOperation.pushMessageToRedis(aClusterType, refNumber, counterIncrementKey, messageKey, messageJson, totalParts, receivedTime);

        if (log.isDebugEnabled())
            log.debug("Concat message redis process state : " + messagesCount);

        sb.append("Concat message redis process state : " + messagesCount).append("\n");
        switch (messagesCount)
        {
            case DUPLICATE_UDH_FOR_SAME_MOBILE:
                send2Platform(aSmppMessageRequest, PlatformStatusCode.SMPP_SAME_UDH,sb);
                break;

            case REDIS_INSERT_FAIL:
                redisFallBack(aInsertIntoDb, aClusterType, aSmppMessageRequest);
                break;

            default: // Redis Success May be all Parts received.
                processConcatReady(aClusterType, refNumberString, counterIncrementKey, messagesCount, totalParts, refNumber);
                break;
        }
    }

    private static void redisFallBack(
            boolean aInsertIntoDb,
            ClusterType aClusterType,
            SmppMessageRequest aSmppMessageRequest)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("Redis insert failed. Need to insert into db ? '" + aInsertIntoDb + "'");

        if (aInsertIntoDb)
            insertIntoDbInMemory(aClusterType, aSmppMessageRequest);
        else
            if (log.isDebugEnabled())
                log.debug("May be called from DB Query.");
    }

    private static void insertIntoDbInMemory(
            ClusterType aClusterType,
            SmppMessageRequest aSmppMessageRequest)
            throws Exception
    {
        DbInmemoryCollectionFactory.getInstance().addMessage(aClusterType, aSmppMessageRequest);
    }

    private static void processConcatReady(
            ClusterType aClusterType,
            String refNumberString,
            String counterIncrementKey,
            int messagesCount,
            int totalParts,
            int refNumber)
    {
        if (log.isDebugEnabled())
            log.debug("Messages added succesfully to Redis. Ref Number '" + refNumberString + "'. Total messages pushed to Redis '" + messagesCount + "' Total Parts : '" + totalParts + "'");

        if (messagesCount == totalParts)
        {
            if (log.isDebugEnabled())
                log.debug("All parts are received. Hence adding the message ref number to the ready list '" + refNumberString + "'");

            RedisOperation.pushToConcatReady(aClusterType, refNumber, counterIncrementKey);
        }
    }

    private static void send2Platform(
            SmppMessageRequest aSmppMessageRequest,
            PlatformStatusCode aPlatformStatusCode,
            StringBuffer sb)
    {

        try
        {
            PrometheusMetrics.smppIncFailureCounts(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), SmppProperties.getInstance().getInstanceId(),
                    aSmppMessageRequest.getSystemId(), aSmppMessageRequest.getClientIp(), aSmppMessageRequest.getBindType()), aPlatformStatusCode.getKey(), aPlatformStatusCode.getStatusDesc());

            final SmppUserInfo lSmppUserInfo = ConcatBuildMessageRequest.updateUserInfo(aSmppMessageRequest.getClientId());
            // Setting PartNumer '1' for Rejection Cases
            aSmppMessageRequest.setPartNumber(1);
            final MessageRequest lMessageRequest = ConcatBuildMessageRequest.getMessageRequest(aSmppMessageRequest, lSmppUserInfo, aPlatformStatusCode);

            lMessageRequest.setAdditionalErrorInfo("Same UDH for the part - Part Number:'" + aSmppMessageRequest.getPartNumber() + "', Udh:'" + aSmppMessageRequest.getUdh() + "'");

            log.error("Same UDH for the part - Part Number:'" + aSmppMessageRequest.getPartNumber() + "', Udh:'" + aSmppMessageRequest.getUdh() + "', Mid:'" + aSmppMessageRequest.getAckid() + "'");

            if (log.isDebugEnabled())
                log.debug("Failed Message Request Object sending to Kafka : " + lMessageRequest);

            InterfaceUtil.sendToKafka(lMessageRequest,sb);

            if (log.isDebugEnabled())
                log.debug("Successfully send to kafka ........");
        }
        catch (final Exception e)
        {
            log.error("Exception while processing Message Request Object..", e);
        }
    }

}