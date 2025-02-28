package com.itextos.beacon.platform.ch.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.errorlog.SMSLog;
import com.itextos.beacon.platform.dch.processor.DummyCarrierHandoverProcess;
import com.itextos.beacon.platform.dnpayloadutil.PayloadProcessor;
import com.itextos.beacon.platform.msgflowutil.billing.BillingDatabaseTableIndentifier;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;
import com.itextos.beacon.platform.prc.process.RejectionProcess;
import com.itextos.beacon.platform.sbc.processor.SBConsumer;
import com.itextos.beacon.platform.subbiller.processor.BillerProcessor;

public class CHProducer
{

    private static final Log log = LogFactory.getLog(CHProducer.class);

    private CHProducer()
    {}

    public static void sendToBlockout(
            MessageRequest aMessageRequest)
    {

        try
        {
            MessageProcessor.writeMessage(Component.CH, Component.SBC, aMessageRequest);
        /*    
            aMessageRequest.setFromComponent(Component.CH.getKey());
            aMessageRequest.setNextComponent(Component.SBC.getKey());
            SBConsumer.forSPC(aMessageRequest);
        */
        }
        catch (final Exception e)
        {
            
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+"  Exception occer while sending to Schedule/Blockout topic .."+ErrorMessage.getStackTraceAsString(e));

            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToRetryRoute(
            MessageRequest aMessageRequest)
    {

        try
        {
            MessageProcessor.writeMessage(Component.CH, Component.RCH, aMessageRequest);
        }
        catch (final ItextosException e)
        {
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+"  Exception occer while sending to Retry Carrier Handover topic .."+ErrorMessage.getStackTraceAsString(e));

            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToPlatfromRejection(
            SubmissionObject aSubmissionObject)
    {

        try
        {
            aSubmissionObject.setPlatfromRejected(true);
      //      MessageProcessor.writeMessage(Component.CH, Component.PRC, aSubmissionObject);
            aSubmissionObject.setFromComponent(Component.CH.getKey());
            aSubmissionObject.setNextComponent(Component.PRC.getKey());
            RejectionProcess.forPRC(aSubmissionObject);

        }
        catch (final Exception e)
        {
            log.error("Exception occer while sending to Platform Rejection topic ..", e);
            aSubmissionObject.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aSubmissionObject.getBaseMessageId()+"  Exception occer while sending to Platform Rejection topic .."+ErrorMessage.getStackTraceAsString(e));

            sendToErrorLog(aSubmissionObject, e);
        }
    }

    public static void sendToPlatfromRejection(
            MessageRequest aMessageRequest)
    {

        try
        {
            aMessageRequest.setPlatfromRejected(true);
         //   MessageProcessor.writeMessage(Component.CH, Component.PRC, aMessageRequest);
        
            aMessageRequest.setFromComponent(Component.CH.getKey());
            aMessageRequest.setNextComponent(Component.PRC.getKey());
            RejectionProcess.forPRC(aMessageRequest);

        }
        catch (final Exception e)
        {
            log.error("Exception occer while sending to Platform Rejection topic ..", e);
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+"  Exception occer while sending to  Platform Rejection topic .."+ErrorMessage.getStackTraceAsString(e));

            sendToErrorLog(aMessageRequest, e);
        }
    }

    public static void sendToDummyRoute(
            SubmissionObject aSubmissionObject,SMSLog sb)
    {

        try
        {
            //MessageProcessor.writeMessage(Component.CH, Component.DCH, aSubmissionObject);
            aSubmissionObject.setFromComponent(Component.CH.getKey());
            aSubmissionObject.setNextComponent(Component.DCH.getKey());
            DummyCarrierHandoverProcess.forDCH(aSubmissionObject,sb);
        }
        catch (final Exception e)
        {
            log.error("Exception occer while sending to Dummy Carrier Handover topic ..", e);
            sendToErrorLog(aSubmissionObject, e);
        }
    }

    public static void sendToAgingInsert(
            SubmissionObject aSubmissionObject)
    {

        try
        {
            MessageProcessor.writeMessage(Component.CH, Component.AGIN, aSubmissionObject);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Aging Dlr Insert topic ..", e);
            sendToErrorLog(aSubmissionObject, e);
        }
    }

    public static void sendToAgingProcess(
            SubmissionObject aSubmissionObject)
    {

        try
        {
            MessageProcessor.writeMessage(Component.CH, Component.ADNP, aSubmissionObject);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Aging Dlr Process topic ..", e);
            sendToErrorLog(aSubmissionObject, e);
        }
    }

    public static void sendToInterim(
            SubmissionObject aSubmissionObject)
    {

        try
        {
            identifySuffix(aSubmissionObject);

            MessageProcessor.writeMessage(Component.CH, Component.T2DB_INTERIM_FAILUERS, aSubmissionObject);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Intrim Failure topic ..", e);
            sendToErrorLog(aSubmissionObject, e);
        }
    }

    public static void sendToSubBilling(
            BaseMessage aBaseMessage,SMSLog sb)
    {

        try
        {
         //   MessageProcessor.writeMessage(Component.CH, Component.SUBBC, aBaseMessage);
            
            
            aBaseMessage.setFromComponent(Component.CH.getKey());
            aBaseMessage.setNextComponent(Component.SUBBC.getKey());
            BillerProcessor.forSUBPC(aBaseMessage,sb);

        }
        catch (final Exception e)
        {
            log.error("Exception occer while sending to Final Process for MT topic ..", e);
            sendToErrorLog(aBaseMessage, e);
        }
    }

    public static void sendToNextLevel(
            MessageRequest aMessageRequest)
    {
        sendToPlatfromRejection(aMessageRequest);
    }

    public static void sendToNextLevel(
            SubmissionObject aSubmissionObject,
            MessageRequest aMessageRequest,
            boolean isPartial)
    {

        try
        {

            try
            {
                aSubmissionObject.setMtMessageRetryIdentifier(Constants.ENABLED);
                PayloadProcessor.removePayload(aSubmissionObject);
            }
            catch (final Exception exp)
            {}

            final int lRetryAttempt = aSubmissionObject.getRetryAttempt();

            if (lRetryAttempt == 0)
            {
                if (isPartial)
                    sendToPlatfromRejection(aSubmissionObject);
                else
                    sendToPlatfromRejection(aMessageRequest);
            }
            else
            {
                final String  lClientId = aSubmissionObject.getClientId();
                final boolean isAging   = CommonUtility.isEnabled(CHProcessUtil.getCutomFeatureValue(lClientId, CustomFeatures.IS_AGING_ENABLE));
                final boolean isFastDn  = CommonUtility.isEnabled(CHProcessUtil.getCutomFeatureValue(lClientId, CustomFeatures.IS_FASTDN_ENABLE));

                if (isAging || isFastDn)
                {
                    // Final dn consumer will take care of handover to Client & Billing
                    aSubmissionObject.setIndicateFinalDn(Constants.ENABLED);

                    final ClusterType lCluster        = aSubmissionObject.getClusterType();

                    final boolean     isAgingDNReject = CommonUtility.isEnabled(CHProcessUtil.getAppConfigValueAsString(lCluster.getKey() + ConfigParamConstants.PLATFORM_REJ_DLR_HANDOVER));

                    if (isAgingDNReject)
                    {
                        if (log.isDebugEnabled())
                            log.debug("Message sending to Platform Reject  ...");
                        if (isPartial)
                            sendToPlatfromRejection(aSubmissionObject);
                        else
                            sendToPlatfromRejection(aMessageRequest);
                    }
                    else
                        sendToAgingProcess(aSubmissionObject);
                }
                else
                {
                    if (log.isDebugEnabled())
                        log.debug("Message sending to Platform Reject  ...");

                    if (isPartial)
                        sendToPlatfromRejection(aSubmissionObject);
                    else
                        sendToPlatfromRejection(aMessageRequest);
                }
            }
        }
        catch (final Exception e)
        {
            sendToErrorLog(aSubmissionObject, e);
        }
    }

    public static void sendToErrorLog(
            BaseMessage aBaseMessage,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.CH, aBaseMessage, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log. " + aBaseMessage, e);
        }
    }

    private static void identifySuffix(
            BaseMessage aBaseMessage)
    {

        try
        {
            final BillingDatabaseTableIndentifier lBillingDatabaseTableIndentifier = new BillingDatabaseTableIndentifier(aBaseMessage);
            lBillingDatabaseTableIndentifier.identifySuffix();
        }
        catch (final Exception e)
        {
        	
            log.error("Exception occer while identifying table suffix...", e);
        }
    }

}
