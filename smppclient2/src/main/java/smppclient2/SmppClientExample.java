/*package smppclient2;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppClient;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.SmppSessionHandler;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.EnquireLinkResp;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppProcessingException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.util.DeliveryReceipt;

public class SmppClientExample {
    private static final Logger logger = LoggerFactory.getLogger(SmppClientExample.class);

    public static void main(String[] args) {
        SmppSessionConfiguration sessionConfig = new SmppSessionConfiguration();
        sessionConfig.setName("Test SMPP Client");
        sessionConfig.setHost("smpp.server.com");
        sessionConfig.setPort(2775);
        sessionConfig.setSystemId("systemId");
        sessionConfig.setPassword("password");
        sessionConfig.setType(SmppBindType.TRANSCEIVER);
        sessionConfig.setBindTimeout(3000);
        sessionConfig.setConnectTimeout(3000);

        SmppClient client = new DefaultSmppClient();
        SmppSession session = null;

        try {
            session = client.bind(sessionConfig, new ClientSmppSessionHandler());

            // Send a message
            SubmitSm submit = new SubmitSm();
            submit.setSourceAddress(new Address((byte) 0x03, (byte) 0x00, "12345"));
            submit.setDestAddress(new Address((byte) 0x01, (byte) 0x01, "9876543210"));
            submit.setShortMessage("Hello, this is a test message!".getBytes());

            SubmitSmResp submitResp = session.submit(submit, TimeUnit.SECONDS.toMillis(60));
            logger.info("Message submitted, message ID: {}", submitResp.getMessageId());

            // Enquire link to keep the connection alive
            EnquireLinkResp enquireLinkResp = session.enquireLink(new EnquireLink(), TimeUnit.SECONDS.toMillis(10));
            logger.info("Enquire link response: {}", enquireLinkResp);

            // Wait for delivery receipts
            // This would typically be handled in the session handler

        } catch (SmppTimeoutException | SmppChannelException | SmppProcessingException e) {
            logger.error("Failed to bind to SMPP server", e);
        } finally {
            if (session != null) {
                session.unbind(TimeUnit.SECONDS.toMillis(5000));
                session.destroy();
            }
            client.destroy();
        }
    }

    private static class ClientSmppSessionHandler implements SmppSessionHandler {
        @Override
        public void firePduRequestExpired(PduRequest pduRequest) {
            logger.warn("PDU request expired: {}", pduRequest);
        }

        @Override
        public PduResponse firePduRequestReceived(PduRequest pduRequest) {
            logger.info("Received PDU request: {}", pduRequest);
            if (pduRequest instanceof DeliverSm) {
                DeliverSm deliverSm = (DeliverSm) pduRequest;
                try {
                    String message = new String(deliverSm.getShortMessage());
                    if (deliverSm.getEsmClass() == 0x04) { // Check if it's a delivery receipt
                        DeliveryReceipt receipt = DeliveryReceiptParser.parse(message);
                        logger.info("Received delivery receipt: {}", receipt);
                    } else {
                        logger.info("Received message: {}", message);
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse delivery receipt", e);
                }
            }
            return pduRequest.createResponse();
        }

        @Override
        public void firePduRequestReceived(PduRequest pduRequest, PduResponse pduResponse) {
            logger.info("Received PDU request: {}, response: {}", pduRequest, pduResponse);
        }

        @Override
        public void fireChannelUnexpectedlyClosed() {
            logger.warn("Channel unexpectedly closed");
        }
    }
}*/