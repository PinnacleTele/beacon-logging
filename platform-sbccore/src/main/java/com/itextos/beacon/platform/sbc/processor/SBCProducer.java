package com.itextos.beacon.platform.sbc.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;
import com.itextos.beacon.platform.prc.process.RejectionProcess;

public class SBCProducer
{

    private SBCProducer()
    {}

    private static final Log log = LogFactory.getLog(SBCProducer.class);

    public static void sendToNextTopic(
            MessageRequest aMessageRequest)
    {
        final int lRetryAttempt = aMessageRequest.getRetryAttempt();

        if (lRetryAttempt == 0)
        {
            if (log.isDebugEnabled())
                log.debug("Sending to biller & DN topics..");

            if (CommonUtility.nullCheck(aMessageRequest.getSubOriginalStatusCode(), true).isBlank())
                aMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.SYSTEM_ERROR.getStatusCode());

            sendToPlatformRejection(aMessageRequest);
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("sendToNextTopic sending biller dn queues");
            aMessageRequest.setSubOriginalStatusCode(getAppConfigValueAsString(ConfigParamConstants.PROMO_FAILED_TEMPORARY_ERROR_CODE));
            aMessageRequest.setRetryMessageRejected(true);

            sendToPlatformRejection(aMessageRequest);
        }
    }

    private static void sendToPlatformRejection(
            MessageRequest aMessageRequest)
    {

        try
        {
            aMessageRequest.setPlatfromRejected(true);
       //     MessageProcessor.writeMessage(Component.SBC, Component.PRC, aMessageRequest);

            aMessageRequest.setFromComponent(Component.SBC.getKey());
            aMessageRequest.setNextComponent(Component.PRC.getKey());
            RejectionProcess.forPRC(aMessageRequest);

        }
        catch (final Exception e)
        {
            sendToErrorLog(aMessageRequest, e);
        }
    }

    private static void sendToErrorLog(
            MessageRequest aMessageRequest,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.SBC, aMessageRequest, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log", e);
        }
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

}
