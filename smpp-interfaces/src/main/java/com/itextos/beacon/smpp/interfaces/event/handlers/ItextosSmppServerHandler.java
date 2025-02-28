package com.itextos.beacon.smpp.interfaces.event.handlers;

import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.type.SmppProcessingException;
import com.itextos.beacon.smpp.interfaces.event.ItextosBindUnbindInterface;

public class ItextosSmppServerHandler
        extends
        ItextosSmppServerHelper
{

    public ItextosSmppServerHandler(
            ItextosBindUnbindInterface aItextosSmppSessionBindUnbindHandler)
    {
        super(aItextosSmppSessionBindUnbindHandler);
    }

    @Override
    public void sessionBindRequested(
            Long aSessionId,
            SmppSessionConfiguration aSessionConfiguration,
            final BaseBind aBindRequest)
            throws SmppProcessingException
    {
        handleSessionBindRequest(aSessionId, aSessionConfiguration, aBindRequest);
    }

    @Override
    public void sessionCreated(
            Long aSessionId,
            SmppServerSession aServerSession,
            BaseBindResp aBindResponse)
            throws SmppProcessingException
    {
        handleSessionCreated(aSessionId, aServerSession, aBindResponse);
    }

    @Override
    public void sessionDestroyed(
            Long aSessionId,
            SmppServerSession aServerSession)
    {
        handleSessionDestroyed(aSessionId, aServerSession);
    }

    public void shutdownInitiated()
    {
        shutdownInitiated = true;
    }

}
