package com.itextos.beacon.smpp.interfaces.workers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.DeliverSmResp;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.smpputil.DeliverSmInfo;
import com.itextos.beacon.smpp.objects.SmppUserInfo;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;

public class DNAndPullWorker
{

    private static final Log logger = LogFactory.getLog(DNAndPullWorker.class);

    private final String     systemId;

    public DNAndPullWorker(
            String aSystemId)
    {
        systemId = aSystemId;
    }

    /**
     * sends message to client
     *
     * @param aDeliveySmInfo
     *
     * @return true if msg sent successfully, false otherwise
     *
     * @throws Exception
     */
    public boolean sendMessage(
            SmppServerSession session,
            DeliverSmInfo aDeliveySmInfo,
            SmppUserInfo userDetail)
    {
        if (logger.isDebugEnabled())
            logger.debug("DeliverySMWorker sendeMessage() DelvierySmInfo : " + aDeliveySmInfo);

        boolean status = false;

        try
        {
            final String srcAddress  = aDeliveySmInfo.getSourceAddress();
            final String destAddress = aDeliveySmInfo.getDestinationAddress();
            final int    datacoding  = aDeliveySmInfo.getDataCoding();
            final String serviceType = CommonUtility.nullCheck(aDeliveySmInfo.getServiceType(), true);
            final Byte   esm         = Byte.decode(aDeliveySmInfo.getEsmClass());
            final String dnMsg       = aDeliveySmInfo.getShortMessage();
            final String msgId       = aDeliveySmInfo.getMsgId();

            aDeliveySmInfo.setMsgId(msgId);

            final DeliverSm request = WorkerUtil.getDeliverSmRequest(serviceType, esm, datacoding, dnMsg, srcAddress, destAddress, aDeliveySmInfo, userDetail);

            try
            {
                final int dnRequestTimeout = SmppProperties.getInstance().getApiDnReqTimeout();
                aDeliveySmInfo.setSubmitTs();

                final WindowFuture<Integer, PduRequest, PduResponse> afuture = session.sendRequestPdu(request, dnRequestTimeout, true);

                if (logger.isInfoEnabled())
                    logger.info("deliver sm sent waiting for deliver sm resp...");

                afuture.await();

                final DeliverSmResp resp = (DeliverSmResp) afuture.getResponse();
                aDeliveySmInfo.setResponseTime();

                if ((resp != null) && (resp.getCommandStatus() == 0))
                {
                    status = true;
                    if (logger.isDebugEnabled())
                        logger.debug("afuture.isSuccess()=" + afuture.isSuccess() + "\nresponse=" + afuture.getResponse());
                }
                else
                    logger.warn("failed status=" + (resp != null ? resp.getCommandStatus() : "null status response"));
            }
            catch (final Exception e)
            {
                logger.error("Problem while sending the deliver sm message...", e);
            }
        }
        catch (final Exception e1)
        {
            logger.error(systemId + " Deliver sm construction failed ", e1);
        }

        return status;
    }

}
