package com.itextos.beacon.httpclienthandover.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.ErrorObject;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class TopicSenderUtility
{

    private static final Log log = LogFactory.getLog(TopicSenderUtility.class);

    public static void sendToRetryQueue(
            BaseMessage aBaseMessage)
    {
        if (log.isDebugEnabled())
            log.debug("Retry Queue Message: '" + aBaseMessage + "'");

        try
        {
            MessageProcessor.writeMessage(Component.HTTP_DLR, Component.T2DB_CLIENT_HANDOVER_RETRY_DATA, aBaseMessage);
        }
        catch (final ItextosException e)
        {
            log.error("Exception while sending the message to  Retry  topic for the component '" + Component.HTTP_DLR + "' Message '" + aBaseMessage + "'", e);
        }
    }

    public static void sendToErrorLog(
            Component aComponent,
            BaseMessage aBaseMessage,
            Exception aException)
    {

        try
        {
            final ErrorObject errorObject = aBaseMessage.getErrorObject(aComponent, aException);
            MessageProcessor.writeMessage(aComponent, Component.T2DB_ERROR_LOG, errorObject);
        } catch (final ItextosRuntimeException e)
        {
            log.error("Exception while sending the message to error log topic for the component '" + aComponent + "' Message '" + aBaseMessage + "' Error [[[" + CommonUtility.getStackTrace(aException)
                    + "]]]", e);
        }
        catch (final ItextosException e)
        {
            log.error("Exception while sending the message to error log topic for the component '" + aComponent + "' Message '" + aBaseMessage + "' Error [[[" + CommonUtility.getStackTrace(aException)
                    + "]]]", e);
        }
    }

    public static void sendToChildLogQueue(
            BaseMessage aMessage)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Base Message Object going to write in T2DB_CLIENT_HANDOVER_LOG: " + aMessage.toString());
            MessageProcessor.writeMessage(Component.HTTP_DLR, Component.T2DB_CLIENT_HANDOVER_LOG, aMessage);
        }
        catch (final ItextosException e)
        {
            log.error("Exception while sending the message to  Child Log  topic for the component '" + Component.HTTP_DLR + "' Message '" + aMessage + "'", e);
        }
    }

    public static void sendToMasterLogQueue(
            BaseMessage aMessage)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Base Message Object going to write in T2DB_CLIENT_HANDOVER_LOG: " + aMessage.toString());
            MessageProcessor.writeMessage(Component.HTTP_DLR, Component.T2DB_CLIENT_HANDOVER_MASTER_LOG, aMessage);
        }
        catch (final ItextosException e)
        {
            log.error("Exception while sending the message to  Master Log topic for the component '" + Component.HTTP_DLR + "' Message '" + aMessage + "'", e);
        }
    }

}
