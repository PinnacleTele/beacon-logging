package com.itextos.beacon.platform.smppdlrutil.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.platform.smpputil.DeliverSmInfo;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class SmppDlrRedisOperation
{

    private static final Log log = LogFactory.getLog(SmppDlrRedisOperation.class);

    private SmppDlrRedisOperation()
    {}

    static final String SMPP_DN_QUEUE = "smpp:dn:q:";

    public static boolean rpush(
            long aClientId,
            List<DeliveryObject> datalist)
    {
        boolean result = false;

        try (
                Jedis lJedis = SmppRedisConnectionProvider.getSmppDlrRedis(aClientId);
                Pipeline lPipe = lJedis.pipelined();)
        {
            // final List<JSONObject> lDnList = new ArrayList<>();

            final JsonArray jsonArray = new JsonArray();

            for (final DeliveryObject lDeliveryObject : datalist)
            {
                final DeliverSmInfo lDeliverSmInfo = getDeliveryInfo(lDeliveryObject);

             /*   String dn=lDeliverSmInfo.getShortMessage();
                dn=URLEncoder.encode(dn,"UTF-8");
                dn=dn.replaceAll("%0A", "");

                lDeliverSmInfo.setShortMessage(URLDecoder.decode(dn,"UTF-8"));
               */
                jsonArray.add(lDeliverSmInfo.getJson());
            }
            log.debug(jsonArray.toString());
            lPipe.rpush(SMPP_DN_QUEUE + aClientId, jsonArray.toString());

            lPipe.sync();
            if (log.isInfoEnabled())
                log.info(" aClientId : "+aClientId+" : Added successfully to redis " + datalist.size());
            result = true;
        }
        catch (final Exception e)
        {
            log.error("rpush()", e);
        }

        return result;
    }

    private static DeliverSmInfo getDeliveryInfo(
            DeliveryObject aDeliveryObject)
    {
        final DeliverSmInfo lDlvInfo = new DeliverSmInfo();

        lDlvInfo.setClientId(aDeliveryObject.getClientId());
        lDlvInfo.setSourceAddress(MessageUtil.getHeaderId(aDeliveryObject));
        lDlvInfo.setDestinationAddress(aDeliveryObject.getMobileNumber());
        lDlvInfo.setEsmClass(aDeliveryObject.getSmppEsmClass());
        lDlvInfo.setServiceType(aDeliveryObject.getSmppServiceType());
        lDlvInfo.setDataCoding(aDeliveryObject.getDcs());
        lDlvInfo.setMsgId(aDeliveryObject.getMessageId());
        lDlvInfo.setShortMessage(aDeliveryObject.getValue(MiddlewareConstant.MW_SHORT_MESSAGE));
        lDlvInfo.setReceivedTs(aDeliveryObject.getMessageReceivedTime().getTime());

        if (log.isDebugEnabled())
            log.debug("Carrier Submit Time : " + aDeliveryObject.getCarrierSubmitTime());

        final long lCarrSubmitTs = (aDeliveryObject.getCarrierSubmitTime() == null) ? 0 : aDeliveryObject.getCarrierSubmitTime().getTime();
        if (log.isDebugEnabled())
            log.debug("Carrier Submit Time : " + lCarrSubmitTs);
        lDlvInfo.setCarrierSubmitTs(lCarrSubmitTs);

        if (log.isDebugEnabled())
            log.debug("Carrier Received Time : " + aDeliveryObject.getDeliveryTime());

        final long lCarrReceivedTs = (aDeliveryObject.getDeliveryTime() == null) ? 0 : aDeliveryObject.getDeliveryTime().getTime();
        if (log.isDebugEnabled())
            log.debug("Carrier Received Time : " + lCarrReceivedTs);

        lDlvInfo.setDNReceivedTs(lCarrReceivedTs);

        return lDlvInfo;
    }

}
