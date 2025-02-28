package com.itextos.beacon.smpp.interfaces.event.handlers;

import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionHandler;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.type.SmppProcessingException;

public class ItextosBindUnbindHandler
        extends
        ItextosBindUnbindHelper

{

    @Override
    public SmppSessionHandler sessionCreated(
            Long sessionId,
            SmppServerSession session,
            BaseBindResp preparedBindResponse)
            throws SmppProcessingException
    {
        return handleSessionCreated(sessionId, session, preparedBindResponse);
    }

    @Override
    public void sessionDestroyed(
            Long sessionId,
            SmppServerSession session)
    {
        handleSessionDestroyed(sessionId, session);
    }

}