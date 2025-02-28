package com.itextos.beacon.smpp.interfaces.workers;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.msgflowutil.billing.BillingDatabaseTableIndentifier;
import com.itextos.beacon.platform.smpputil.DeliverSmInfo;
import com.itextos.beacon.smpp.objects.SessionDetail;
import com.itextos.beacon.smpp.objects.SmppUserInfo;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;

public class DnPostLogGen
{

    private static final Log log = LogFactory.getLog(DnPostLogGen.class);

    private void DnPostLogGen()
    {}

    public static DeliveryObject getDeliverObject(
            DeliverSmInfo aDeliverSm,
            SessionDetail aSessionDetail) throws ItextosRuntimeException
    {
        final SmppUserInfo   lUserINfo       = aSessionDetail.getSmppUserInfo();

        final DeliveryObject lDeliveryObject = new DeliveryObject(lUserINfo.getClusterType(), InterfaceType.SMPP, InterfaceGroup.SMPP, lUserINfo.getMessageType(), lUserINfo.getMessagePriority(),
                RouteType.DOMESTIC);

        lDeliveryObject.setClientId(aDeliverSm.getClientId());
        lDeliveryObject.setHeader(aDeliverSm.getSourceAddress());
        lDeliveryObject.setMobileNumber(aDeliverSm.getDestinationAddress());
        lDeliveryObject.setMessageId(aDeliverSm.getMsgId());
        lDeliveryObject.setMsgReceivedTime(DateTimeUtility.getDateFromString(DateTimeUtility.getFormattedDateTime(new Date(aDeliverSm.getReceivedTs()), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS),
                DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
        lDeliveryObject.setMessageActualReceivedTime(DateTimeUtility
                .getDateFromString(DateTimeUtility.getFormattedDateTime(new Date(aDeliverSm.getReceivedTs()), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
        lDeliveryObject.setSmppInstance(SmppProperties.getInstance().getInstanceId());
        lDeliveryObject.putValue(MiddlewareConstant.MW_SMPP_DN_SUBMIT_TIME, DateTimeUtility.getFormattedDateTime(new Date(aDeliverSm.getSubmiTs()), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));

        if (aDeliverSm.getResponseTime() > 0)
            lDeliveryObject.putValue(MiddlewareConstant.MW_SMPP_DN_RESPONSE_TIME,
                    DateTimeUtility.getFormattedDateTime(new Date(aDeliverSm.getResponseTime()), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));

        lDeliveryObject.putValue(MiddlewareConstant.MW_SMPP_PROTOCAL, "SMPP");
        lDeliveryObject.putValue(MiddlewareConstant.MW_SMPP_STATUS, aDeliverSm.getDnStatus().name());
        lDeliveryObject.putValue(MiddlewareConstant.MW_RETRY_ATTEMPT, MessageUtil.getStringFromInt(aDeliverSm.getRetryAttempt()));
        lDeliveryObject.putValue(MiddlewareConstant.MW_SMPP_REASON, aDeliverSm.getReason());
        lDeliveryObject.putValue(MiddlewareConstant.MW_SMPP_SYSTEM_ID, lUserINfo.getSystemId());
        lDeliveryObject.putValue(MiddlewareConstant.MW_CLIENT_SOURCE_IP, aSessionDetail.getHost());

        if (log.isDebugEnabled())
            log.debug("Carrier Submit TIme :" + aDeliverSm.getCarrierSubmitTs());

        if (aDeliverSm.getCarrierSubmitTs() > 0)
            lDeliveryObject.setCarrierSubmitTime(DateTimeUtility.getDateFromString(
                    DateTimeUtility.getFormattedDateTime(new Date(aDeliverSm.getCarrierSubmitTs()), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));

        if (log.isDebugEnabled())
            log.debug("Carrier Submit TIme :" + aDeliverSm.getDNReceivedTs());

        if (aDeliverSm.getDNReceivedTs() > 0)
            lDeliveryObject.setDeliveryTime(DateTimeUtility.getDateFromString(DateTimeUtility.getFormattedDateTime(new Date(aDeliverSm.getDNReceivedTs()), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS),
                    DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));

        return lDeliveryObject;
    }

    public static void identifySuffix(
            DeliveryObject aDeliveryObject)
    {

        try
        {
            final BillingDatabaseTableIndentifier lBillingDatabaseTableIndentifier = new BillingDatabaseTableIndentifier(aDeliveryObject);
            lBillingDatabaseTableIndentifier.identifySuffix();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while identifying table suffix...", e);
        }
    }

}
