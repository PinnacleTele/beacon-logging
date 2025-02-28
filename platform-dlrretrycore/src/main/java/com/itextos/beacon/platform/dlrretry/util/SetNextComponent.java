package com.itextos.beacon.platform.dlrretry.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.errorlog.SMSLog;
import com.itextos.beacon.platform.ch.processor.CarrierHandoverProcess;
import com.itextos.beacon.platform.dnpcore.process.DlrInternalProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class SetNextComponent
{

    private static final Log log = LogFactory.getLog(SetNextComponent.class);

    private SetNextComponent()
    {}

    public static void sendMessageToDeliverySM(
            DeliveryObject aDeliveryObject,
            String aErrorCode,
            String aReason)
    {
        if (!CommonUtility.nullCheck(aErrorCode, true).isEmpty())
            aDeliveryObject.setDnOrigianlstatusCode(PlatformStatusCode.getStatusDesc(aErrorCode).getStatusCode());

        if (!CommonUtility.nullCheck(aReason, true).isEmpty())
            aDeliveryObject.setFailReason(aReason);

        aDeliveryObject.setIndicateDnFinal(1);
        aDeliveryObject.setDlrFromInternal(Component.DLRR.getKey());

        // aNextComponentMap.put(Component.DLRINTLP, aDeliveryObject);

        try
        {
            aDeliveryObject.setNextComponent(Component.DLRINTLP.getKey());
            aDeliveryObject.setFromComponent(Component.DLRR.getKey());
        	DlrInternalProcessor.forDLRInternal(aDeliveryObject,SMSLog.getInstance());

         //   MessageProcessor.writeMessage(Component.DLRR, Component.DLRINTLP, aDeliveryObject);
        }
        catch (final Exception e)
        {
            sendToErrorLog(aDeliveryObject, e);
        }

        if (log.isDebugEnabled())
            log.debug("Called adding aNextComponentMap : " + aDeliveryObject);
    }

    public static void sendMessageToRetryWithDelayQ(
            DeliveryObject aDeliveryObject,
            String aErrorCode,
            String aReason,
            String aRouteId)
            throws Exception
    {

        try
        {
            final int lRetryAttempt = aDeliveryObject.getRetryAttempt();

            final int lCurrentRetry = (lRetryAttempt != 0) ? lRetryAttempt + 1 : 1;

            aDeliveryObject.setRetryAttempt(lCurrentRetry);

            if (!CommonUtility.nullCheck(aErrorCode, true).isEmpty())
                aDeliveryObject.setDnOrigianlstatusCode(PlatformStatusCode.getStatusDesc(aErrorCode).getStatusCode());

            if (aDeliveryObject.isCurrent())
            {
                if (log.isInfoEnabled())
                    log.info("current message-->sending to route q direct");
                DlrWaitRetryUtil.process(aDeliveryObject);
            }
            else
            {
                if (log.isInfoEnabled())
                    log.info(" message send to delayed retry queue" + " retry interval==>" + aDeliveryObject.getRetryInterval() + aDeliveryObject);

                DnBasedRetryRedisProcessor.pushToRedis(aDeliveryObject);
            }
        }
        catch (final Exception e1)
        {
            log.error(" Unable to send to sendMessageToRetryWithDelayQ", e1);
            throw e1;
        }
    }

    public static void sendMessageToVoiceQ(
            DeliveryObject aDeliveryObject)
    {
        // aNextComponentMap.put(Component.AVDN, aDeliveryObject);

        try
        {
            MessageProcessor.writeMessage(Component.AVDN, Component.VOICE_PROCESS, aDeliveryObject);
        }
        catch (final ItextosException e)
        {
            sendToErrorLog(aDeliveryObject, e);
        }
        if (log.isDebugEnabled())
            log.debug("sendMessageToVoiceQ called adding resultMap==" + aDeliveryObject);
    }

    public static void sendMessageToRC(
            MessageRequest aMessageRequest)
    {

        try
        {
//            MessageProcessor.writeMessage(Component.DLRR, Component.CH, aMessageRequest);
            aMessageRequest.setFromComponent(Component.DLRR.getKey());
            aMessageRequest.setNextComponent(Component.CH.getKey());
            CarrierHandoverProcess.forCH(aMessageRequest, aMessageRequest.getClusterType());
          
        }
        catch (final Exception e)
        {
            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToErrorLog(
            BaseMessage aBaseMessage,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.AVDN, aBaseMessage, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log. " + aBaseMessage, e);
        }
    }

}
