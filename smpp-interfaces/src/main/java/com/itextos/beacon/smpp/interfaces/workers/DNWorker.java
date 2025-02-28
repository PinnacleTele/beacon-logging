package com.itextos.beacon.smpp.interfaces.workers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.smpputil.DeliverSmInfo;
import com.itextos.beacon.smpp.interfaces.event.handlers.ItextosSmppSessionHandler;
import com.itextos.beacon.smpp.interfaces.util.Communicator;
import com.itextos.beacon.smpp.objects.SmppUserInfo;
import com.itextos.beacon.smpp.utils.ItextosSmppUtil;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;
import com.itextos.beacon.smslog.DnTimeStampLog;

public class DNWorker
{

    private static final Log log       = LogFactory.getLog(DNWorker.class);

    private String           mSystemId = "";

    public DNWorker(
            String aSystemId)
    {
        mSystemId = aSystemId;
    }

    public WindowFuture<Integer, PduRequest, PduResponse> sendMessage(
            ItextosSmppSessionHandler sessionHandler,
            DeliverSmInfo aDeliverySmInfo)
    {
        if (log.isDebugEnabled())
            log.debug("DeliverySMWorker sendeMessage() _deliverSMObj : " + aDeliverySmInfo);

        WindowFuture<Integer, PduRequest, PduResponse> afuture   = null;
        Integer                                        seqNumber = null;
        final boolean                                  isSync    = false;

        try
        {
            // CustomFeatures.IS_SYNC_SMPP_DN
            // final String isSyncSmppDn =
            // AccountDetails.getAccountCustomeFeature(sessionHandler.getClientId(), null);
            // log.info("is Sync Smpp Dn - " + isSyncSmppDn);

            final SmppServerSession session = sessionHandler.getSession();

            if (session.isBound())
            {
                final SmppUserInfo lSmppUserInfo = sessionHandler.getSmppUserInfo();
                String             serviceType   = "";
                final String       srcAddress    = aDeliverySmInfo.getSourceAddress();
                final String       destAddress   = aDeliverySmInfo.getDestinationAddress();
                String             dnMsg         = "";
                final int          datacoding    = aDeliverySmInfo.getDataCoding();
                final String       bindType      = ItextosSmppUtil.getBindName(session.getBindType());

                serviceType = aDeliverySmInfo.getServiceType();
                if (serviceType != null)
                    serviceType = "";

                final Byte esm = Byte.decode(aDeliverySmInfo.getEsmClass());
                dnMsg = aDeliverySmInfo.getShortMessage();

                final String msgId = aDeliverySmInfo.getMsgId();

                aDeliverySmInfo.setMsgId(msgId);

                try
                {
                    final String timeZone = CommonUtility.nullCheck(lSmppUserInfo.getAccountTimeZone());

                    StringBuffer sb=new StringBuffer();
                    if (!"".equals(timeZone))
                        dnMsg = WorkerUtil.convertTimeFromIstToIntlTimeZone(dnMsg, timeZone,aDeliverySmInfo.getMsgId(),sb);
                
                    sb.append(aDeliverySmInfo.getMsgId()+" timeZone : "+timeZone).append("\n");
                    sb.append(aDeliverySmInfo.getMsgId()+" dnMsg : "+dnMsg).append("\n");

                    DnTimeStampLog.log(sb.toString());
                }
                catch (final Exception ignore)
                {}

                final DeliverSm request = WorkerUtil.getDeliverSmRequest(serviceType, esm, datacoding, dnMsg, srcAddress, destAddress, aDeliverySmInfo, lSmppUserInfo);

                try
                {
                    if (log.isDebugEnabled())
                        log.debug("DeliverSM Packet :" + request.toString());

                    seqNumber = request.getSequenceNumber();

                    final int dnRequestTimeout = SmppProperties.getInstance().getApiDnReqTimeout();
                    aDeliverySmInfo.setSubmitTs();

                    if (log.isInfoEnabled())
                        log.info("trying to send pdu>>>>>");

                    Communicator.sendDeliverSMRequestLog(sessionHandler.getSessionDetail());

                    sessionHandler.addDeliverySmInfo(request.getSequenceNumber(), aDeliverySmInfo);
                    afuture = session.sendRequestPdu(request, dnRequestTimeout, isSync);

                    if (log.isInfoEnabled())
                        log.info("deliver sm sent waiting for deliver sm resp...");
                }
                catch (final Exception e)
                {

                    if (e instanceof SmppTimeoutException)
                    {
                        if (log.isWarnEnabled())
                            log.warn("window full failed --->" + aDeliverySmInfo.getMsgId());
                    }
                    else
                        log.error("Problem while sending the deliver sm message...", e);
                }
            }
            else
                if (log.isWarnEnabled())
                    log.warn(mSystemId.toLowerCase() + " session is not bound...Deliver_SM not sent.");
        }
        catch (final Exception e1)
        {
            log.error(mSystemId + " Deliver sm construction failed ", e1);
        }

        if (afuture == null)
            try
            {

                if (seqNumber != null)
                {
                    sessionHandler.removeDeliverySmInfo(seqNumber);
                    log.info("Failed to submit DN, Remove the seq number from map -" + seqNumber);
                }
            }
            catch (final Exception e)
            {}

        return afuture;
    }

}
