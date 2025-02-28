package com.itextos.beacon.smpp.interfaces.event.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.smpp.PduAsyncResponse;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import com.itextos.beacon.smpp.objects.SessionDetail;

public class ItextosSmppSessionHandler
        extends
        ItextosSmppSessionHelper
{

    private static final Log log = LogFactory.getLog(ItextosSmppSessionHandler.class);

    public ItextosSmppSessionHandler(
            SessionDetail aSessionDetail)
    {
        super(aSessionDetail);
    }

    @Override
    public String lookupResultMessage(
            int aCommandStatus)
    {
        log.error(STR_NOT_IMPLEMENTED + "Command passed '" + aCommandStatus + "'");
        return null;
    }

    @Override
    public String lookupTlvTagName(
            short aTag)
    {
        log.error(STR_NOT_IMPLEMENTED + "Tag passed '" + aTag + "'");
        return null;
    }

    @Override
    public void fireChannelUnexpectedlyClosed()
    {
        handleChannelUnExpectedClose();
    }

    @Override
    public PduResponse firePduRequestReceived(
            PduRequest aPduRequest)
    {
        return handlePduRequestReceived(aPduRequest);
    }

    @Override
    public void firePduRequestExpired(
            PduRequest aPduRequest)
    {
        handlePduRequestExpired(aPduRequest);
    }

    @Override
    public void fireExpectedPduResponseReceived(
            PduAsyncResponse aPduAsyncResponse)
    {
        handleExpectedPduResponseReceived(aPduAsyncResponse);
    }

    @Override
    public void fireUnexpectedPduResponseReceived(
            PduResponse aPduResponse)
    {
        handleUnexpectedPduResponseReceived(aPduResponse);
    }

    @Override
    public void fireUnrecoverablePduException(
            UnrecoverablePduException aException)
    {
        log.error(STR_NOT_IMPLEMENTED, aException);
    }

    @Override
    public void fireRecoverablePduException(
            RecoverablePduException aException)
    {
        log.error(STR_NOT_IMPLEMENTED, aException);
    }

    @Override
    public void fireUnknownThrowable(
            Throwable aThrowable)
    {
        handleUnknownThrowable(aThrowable);
    }

}