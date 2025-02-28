package com.itextos.beacon.smpp.interfaces.event;

import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionHandler;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.type.SmppProcessingException;

public interface ItextosBindUnbindInterface
{

    SmppSessionHandler sessionCreated(
            Long sessionId,
            SmppServerSession session,
            BaseBindResp preparedBindResponse)
            throws SmppProcessingException;

    void sessionDestroyed(
            Long sessionId,
            SmppServerSession session);

}
