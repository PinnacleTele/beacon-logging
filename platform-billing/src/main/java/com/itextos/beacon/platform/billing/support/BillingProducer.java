package com.itextos.beacon.platform.billing.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.errorlog.SMSLog;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class BillingProducer
{

    private static final Log log = LogFactory.getLog(BillingProducer.class);

    private BillingProducer()
    {}

    public static void sendToBillingTopic(
            BaseMessage aBaseMessage,SMSLog sb)
    {

        try
        {
          	sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(" Sending to q:" + Component.T2DB_SUBMISSION.getKey() );

            MessageProcessor.writeMessage(Component.SUBBC, Component.T2DB_SUBMISSION, aBaseMessage);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Submittion Billing topic ..", e);
            sendToErrorLog(aBaseMessage, e);
        }
    }

    public static void sendToDlrQueryTopic(
            BaseMessage aBaseMessage,SMSLog sb)
    {

        try
        {
          	sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(" Sending to q:" + Component.DLRQMT.getKey() );

            MessageProcessor.writeMessage(Component.SUBBC, Component.DLRQMT, aBaseMessage);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Dlr Query topic ..", e);
            sendToErrorLog(aBaseMessage, e);
        }
    }

    public static void sendToWalletRefundTopic(
            BaseMessage aBaseMessage,StringBuffer sb)
    {

        try
        {
          	sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(" Sending to q:" + Component.WALLET_REFUND.getKey() );

            MessageProcessor.writeMessage(Component.SUBBC, Component.WALLET_REFUND, aBaseMessage);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Wallet Refund topic ..", e);
            sendToErrorLog(aBaseMessage, e);
        }
    }

    public static void sendToFullMessageTopic(
            BaseMessage aBaseMessage,SMSLog sb)
    {

        try
        {
          	sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(" Sending to q:" + Component.T2DB_FULL_MESSAGE.getKey() );

            MessageProcessor.writeMessage(Component.SUBBC, Component.T2DB_FULL_MESSAGE, aBaseMessage);
        }
        catch (final ItextosException e)
        {
            log.error("Exception occer while sending to Wallet Refund topic ..", e);
            sendToErrorLog(aBaseMessage, e);
        }
    }

    public static void sendToErrorLog(
            BaseMessage aBaseMessage,
            Exception aErrorMsg)
    {

        try
        {
        	
            PlatformUtil.sendToErrorLog(Component.SUBBC, aBaseMessage, aErrorMsg);
        }
        catch (final Exception e)
        {
            log.error("Exception while sending request to error log. " + aBaseMessage, e);
        }
    }

}
