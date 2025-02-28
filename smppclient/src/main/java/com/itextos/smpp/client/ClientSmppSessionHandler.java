package com.itextos.smpp.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.smpp.PduAsyncResponse;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.DeliverSmResp;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.type.Address;

public class ClientSmppSessionHandler
        extends
        DefaultSmppSessionHandler
{

    private static final Log log = LogFactory.getLog(ClientSmppSessionHandler.class);
    private int              cnt = 1;

    public ClientSmppSessionHandler()
    {
        super();
    }

    @Override
    public void fireExpectedPduResponseReceived(
            PduAsyncResponse pduAsyncResponse)
    {
        log.info("Handling response PDU: {}" + pduAsyncResponse);
        pduAsyncResponse.getResponse().setReferenceObject(pduAsyncResponse.getRequest().getReferenceObject());
        processPduResponse(pduAsyncResponse.getResponse());
    }

    private void processPduResponse(
            PduResponse pduResponse)
    {
        final SubmitSmResp submitSmResp = (SubmitSmResp) pduResponse;
        log.info("submt_sm_resp: commandStatus for new [" + submitSmResp.getCommandStatus() + "=" + submitSmResp.getMessageId() + "][Sequence Number -]" + submitSmResp.getSequenceNumber());
    }

    @Override
    public void fireUnexpectedPduResponseReceived(
            PduResponse pduResponse)
    {
        log.info("fireUnexpectedPduResponseReceived possible unhandled response*********************" + pduResponse);

        final DeliverSmResp deliverSmResp = (DeliverSmResp) pduResponse;
        log.info("deliver_sm_resp: commandStatus for new [" + deliverSmResp.getCommandStatus() + "=" + deliverSmResp.getResultMessage() + "][Sequence Number -]" + deliverSmResp.getSequenceNumber());
    }

    @Override
    public void firePduRequestExpired(
            PduRequest pduRequest)
    {
        log.fatal("PDU request expired: {}" + pduRequest);
    }

    @Override
    public PduResponse firePduRequestReceived(
            PduRequest pduRequest)
    {
        PduResponse response = pduRequest.createResponse();
        System.out.println("Received Time : " + System.currentTimeMillis());
        System.out.println("SMS Received : " + pduRequest.getCommandId());
        System.out.println("Sequence No :" + pduRequest.getSequenceNumber());

        if (pduRequest.getCommandId() == SmppConstants.CMD_ID_DELIVER_SM)
        {
            final DeliverSm mo             = (DeliverSm) pduRequest;
            final int       length         = mo.getShortMessageLength();
            final Address   source_address = mo.getSourceAddress();
            final Address   dest_address   = mo.getDestAddress();
            final byte[]    shortMessage   = mo.getShortMessage();
            final String    SMS            = new String(shortMessage);

            log.debug(source_address + ", " + dest_address + ", " + SMS);
        }
        log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> DSM Response -" + response.getCommandStatus());
        response.setSequenceNumber(pduRequest.getSequenceNumber());

        if (cnt == 10)
        {
            response = null;
            cnt      = 1;

            try
            {
                Thread.sleep(5000);
            }
            catch (final InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        cnt++;

        log.debug("Response >>>>>>>>>>>>>>>>>>>" + response);

        return response;
    }

}