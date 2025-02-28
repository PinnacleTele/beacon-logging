package com.itextos.beacon.platform.dlrpayloadgen.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.errorlog.SMSLog;
import com.itextos.beacon.inmemory.dnpayload.util.DNPUtil;
import com.itextos.beacon.inmemory.routeinfo.cache.RouteConfigInfo;
import com.itextos.beacon.inmemory.routeinfo.util.RouteUtil;
import com.itextos.beacon.platform.dnpcore.process.DlrInternalProcessor;

public class PushToDlrProcessor
{

    private static final Log log = LogFactory.getLog(PushToDlrProcessor.class);

    private PushToDlrProcessor()
    {}

    public static void handoverToEngine(
            String aPayLoadId,
            String lRedisKey,
            String aPayloadJson)
    {

        try
        {
            final DeliveryObject lDeliveryObject = getPayload(aPayloadJson);

            if (aPayloadJson != null)
            {
                final String  lMessageId             = lDeliveryObject.getMessageId();
                final boolean isDlrGenIgnoreByClient = DNPUtil.isDlrGenIgnoreable(lDeliveryObject.getClientId());

                if (isDlrGenIgnoreByClient)
                {
                    if (log.isDebugEnabled())
                        log.debug("Not generating dn since account is configured in payload.acc_ignore_from_dn_generation ClientId:" + lDeliveryObject.getClientId());
                    return;
                }

                final String          lRouteId        = lDeliveryObject.getRouteId();
                final boolean         isDomesticRoute = true;
                final RouteConfigInfo lRouteConfig    = RouteUtil.getRouteConfiguration(lRouteId);

                if (log.isDebugEnabled())
                    log.debug("Message Id: " + lMessageId + "routeid: " + lRouteId + " routeConfig:" + lRouteConfig);

                /*
                 * if ((lRouteConfig != null) && lRouteConfig.isIntlRoute())
                 * isDomesticRoute = false;
                 * if (isDomesticRoute)
                 * {
                 */
                lDeliveryObject.setCarrierStatusDesc(DlrPayloadGenUtil.getAppConfigValueAsString(ConfigParamConstants.DLR_GEN_SUCCESS_STATUS_FLAG));
                lDeliveryObject.setCarrierStatusCode(DlrPayloadGenUtil.getAppConfigValueAsString(ConfigParamConstants.DLR_GEN_SUCCESS_STATUS_ID));
                /*
                 * }
                 * else
                 * {
                 * lDeliveryObject.setCarrierStatusDesc(DlrPayloadGenUtil.
                 * getAppConfigValueAsString(ConfigParamConstants.DLR_GEN_FAIL_STATUS_FLAG));
                 * lDeliveryObject.setCarrierStatusCode(DlrPayloadGenUtil.
                 * getAppConfigValueAsString(ConfigParamConstants.DLR_GEN_FAIL_STATUS_ID));
                 * }
                 */

                if (log.isDebugEnabled())
                    log.debug(" isDomesticRoute:" + isDomesticRoute + " Messge Id:" + lMessageId);

                lDeliveryObject.setDlrFromInternal("dn_generated_from_payload");

                sendToDlrInternalTopic(lDeliveryObject);

                final String dnGenerateMapKey = CommonUtility.combine(aPayLoadId, lRedisKey);
                if (log.isDebugEnabled())
                    log.debug("dnGenerateMapKey:" + dnGenerateMapKey + " Messge Id:" + lMessageId);
                GeneratedDlrCountCache.getInstance().incrementAndPutToMap(dnGenerateMapKey, lRouteId, 1);
            }
        }
        catch (final Exception e)
        {
            log.error("Handover to Dlr Internal Topic... Exception", e);
        }
    }

    public static DeliveryObject getPayload(
            String aJson)
    {
        DeliveryObject lDeliveryObject = null;

        try
        {
            final Map<String, String> payload          = getAsHashMap(aJson);

            final ClusterType         lClusterType     = ClusterType.getCluster(payload.get(MiddlewareConstant.MW_PLATFORM_CLUSTER.getName()));
            final InterfaceType       lInterfaceType   = InterfaceType.getType(payload.get(MiddlewareConstant.MW_INTERFACE_TYPE.getName()));
            final InterfaceGroup      lInterfaceGrp    = InterfaceGroup.getType(payload.get(MiddlewareConstant.MW_INTERFACE_GROUP_TYPE.getName()));
            final MessageType         lMessageType     = MessageType.getMessageType(payload.get(MiddlewareConstant.MW_MSG_TYPE.getName()));
            final MessagePriority     lMessagePriority = MessagePriority.getMessagePriority(payload.get(MiddlewareConstant.MW_SMS_PRIORITY.getName()));
            final RouteType           lRouteType       = RouteType.getRouteType(payload.get(MiddlewareConstant.MW_INTL_MESSAGE.getName()));

            lDeliveryObject = new DeliveryObject(lClusterType, lInterfaceType, lInterfaceGrp, lMessageType, lMessagePriority, lRouteType);

            for (final Entry<String, String> entry : payload.entrySet())
                lDeliveryObject.putValue(MiddlewareConstant.getMiddlewareConstantByName(entry.getKey()), entry.getValue());

            MessageUtil.setHeaderId(lDeliveryObject, lDeliveryObject.getValue(MiddlewareConstant.MW_HEADER));
        }
        catch (

        final Exception e)
        {
            log.error("json to BaseMessage conversion problem json:" + aJson, e);
        }

        /*
         * try
         * {
         * final Map<String, String> payload = getAsHashMap(aJson);
         * for (final Entry<String, String> entry : payload.entrySet())
         * lDeliveryObject.putValue(MiddlewareConstant.getMiddlewareConstantByName(entry
         * .getKey()), entry.getValue());
         * }
         * catch (final Exception exp)
         * {
         * log.error("json to BaseMessage conversion problem json:" + aJson, exp);
         * }
         */
        return lDeliveryObject;
    }

    private static void sendToDlrInternalTopic(
            DeliveryObject aDeliveryObject)
    {

        try
        {
            aDeliveryObject.setNextComponent(Component.DLRINTLP.getKey());
            aDeliveryObject.setFromComponent(Component.DLR_GEN.getKey());
        	DlrInternalProcessor.forDLRInternal(aDeliveryObject,SMSLog.getInstance());

        //    MessageProcessor.writeMessage(Component.DLR_GEN, Component.DLRINTLP, aDeliveryObject);
        }
        catch (final Exception e)
        {
            log.error("Exception occer while handover to Dlr Internal Topic ..", e);
        }
    }

    private static Map<String, String> getAsHashMap(
            String json)
    {

        try
        {
            final JSONParser lParser = new JSONParser();
            return (JSONObject) lParser.parse(json);
        }
        catch (final Exception exp)
        {
            log.error("json to hashmap conversion problem..." + json, exp);
        }
        return new HashMap<>();
    }

}
