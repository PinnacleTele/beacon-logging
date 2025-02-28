package com.itextos.beacon.platform.dnpcore.dlrclienthandover.process;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.smpp.account.SmppAccInfo;
import com.itextos.beacon.inmemory.smpp.account.util.SmppAccUtil;
import com.itextos.beacon.platform.dnpcore.util.DnStatus;

public class DlrSmppGenerator
{

    private static final Log    log                    = LogFactory.getLog(DlrSmppGenerator.class);

    private static final String UNSUPPORTED_MSG_FORMAT = "UNSUPPORTED FORMAT";

    public static DeliveryObject generateDlrQueue(
            DeliveryObject aDeliveryObject)
            throws Exception
    {
        DeliveryObject lNewDeliveryObj = null;

        try
        {
            lNewDeliveryObj = aDeliveryObject.getClonedDeliveryObject();

            final String lClientId = aDeliveryObject.getClientId();
            lNewDeliveryObj.setClientId(lClientId);

            final Timestamp lLastSent = new Timestamp(System.currentTimeMillis());
            if (log.isDebugEnabled())
                log.debug(" Processing SMPP_DLR_QUEUE for Message Id " + aDeliveryObject.getMessageId());

            lNewDeliveryObj.setSmppLastSent(lLastSent.toString());

            String lServiceType = aDeliveryObject.getSmppServiceType();
            if ((lServiceType != null) && (lServiceType.length() > 12))
                lServiceType = lServiceType.substring(0, 12);

            final String lStatusId = aDeliveryObject.getDnClientStatusCode();

            if (lStatusId != null)
                lNewDeliveryObj.setDnClientStatusCode(lStatusId);

            final String lRouteId = aDeliveryObject.getRouteId();
            if (lRouteId != null)
                lNewDeliveryObj.setRouteId(lRouteId);

            lNewDeliveryObj.setSmppServiceType(lServiceType);
            lNewDeliveryObj.setSmppSourceAddrTon(aDeliveryObject.getSmppSourceAddrTon());
            lNewDeliveryObj.setSmppSourceAddrNpi(aDeliveryObject.getSmppSourceAddrNpi());

            MessageUtil.setHeaderId(lNewDeliveryObj, MessageUtil.getHeaderId(aDeliveryObject));

            lNewDeliveryObj.setSmppDesteAddrTon(aDeliveryObject.getSmppDestAddrTon());
            lNewDeliveryObj.setSmppDestAddrNpi(aDeliveryObject.getSmppDestAddrNpi());

            final String lDestProperty = aDeliveryObject.getMobileNumber();
            String       lMNumber      = String.valueOf(lDestProperty);

            if ((lMNumber != null) && (lMNumber.length() >= 15))
            {
                lMNumber = lMNumber.substring(0, 14);
                log.info(" Changing the Mobile from " + lMNumber + " to " + lDestProperty);
            }

            lNewDeliveryObj.setMobileNumber(lMNumber);

            final String lEsmClass = "4";
            lNewDeliveryObj.setSmppEsmClass(lEsmClass);

            /*
             * String pid = (String) lNewNunMessage.getValue("protocol_id");
             * if ((pid != null) && (pid.length() > 2))
             * pid = pid.substring(0, 2);
             * lNewNunMessage.put("protocol_id", pid);
             */
            // lNewDeliveryObj.putValue(MiddlewareConstant.MW_SMPP_PRIORITY_FLAG,
            // aDeliveryObject.getValue(MiddlewareConstant.MW_SMPP_PRIORITY_FLAG));
            lNewDeliveryObj.setDlrRequestFromClient(aDeliveryObject.isDlrRequestFromClient());
            lNewDeliveryObj.setDcs(aDeliveryObject.getDcs());
            // lNewDeliveryObj.putValue(MiddlewareConstant.MW_SMPP_SM_LENGTH,
            // aDeliveryObject.getValue(MiddlewareConstant.MW_SMPP_SM_LENGTH));
            // lNewDeliveryObj.setSmpp(MiddlewareConstant.MW_SMS_PRIORITY,
            // aDeliveryObject.getValue(MiddlewareConstant.MW_SMS_PRIORITY));

            // create the short message and set
            final String lMessage = createSMPPShortMsg(aDeliveryObject, true);
            if (log.isDebugEnabled())
                log.debug("DLR_QUEUE short msg: " + lMessage);

            lNewDeliveryObj.putValue(MiddlewareConstant.MW_SHORT_MESSAGE, lMessage);
            /*
             * lNewNunMessage.putValue("sar_msg_ref_num",
             * aNunMessage.getValue("sar_msg_ref_num"));
             * lNewNunMessage.putValue("sar_total_segments",
             * aNunMessage.getValue("sar_total_segments"));
             * lNewNunMessage.putValue("sar_segment_seqnum",
             * aNunMessage.getValue("sar_segment_seqnum"));
             */
            // lNewDeliveryObj.putValue(MiddlewareConstant.MW_SMPP_MESSAGE_PAYLOAD,
            // aDeliveryObject.getValue(MiddlewareConstant.MW_SMPP_MESSAGE_PAYLOAD));

            // aNunMessage.putValue("source", "DLR");

            if (log.isDebugEnabled())
                log.debug(" Formed dlr_queue map: " + lNewDeliveryObj);
        }
        catch (final Exception e)
        {
            log.error("Exception in putIntoDlrQueue() ", e);
            throw e;
        }
        return lNewDeliveryObj;
    }

    private static String createSMPPShortMsg(
            DeliveryObject aDeliveryObject,
            boolean isMsgSupported)
    {
        // "id:<mid> sub:001 dlvrd:001 submit:<smppdate> done date:<ddate> stat:<status>
        // err:<errorCode> Text:<msg>";

        final StringBuilder sb = new StringBuilder();

        try
        {
            String            lDnCustomDateFormat = DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM.getKey();

            final SmppAccInfo lSmppAccInfo        = SmppAccUtil.getSmppAccountInfo(aDeliveryObject.getClientId());

            if (lSmppAccInfo != null)
            {
                if (log.isDebugEnabled())
                    log.debug("DN Date Format : " + lSmppAccInfo.getDnDateFormat());

                lDnCustomDateFormat = CommonUtility.nullCheck(lSmppAccInfo.getDnDateFormat()).isEmpty() ? lDnCustomDateFormat : lSmppAccInfo.getDnDateFormat();
            }

            String id = aDeliveryObject.getMessageId();

            if (id.isEmpty())
                id = aDeliveryObject.getFileId();

            Date sTime = aDeliveryObject.getMessageReceivedTime();
            sTime = sTime == null ? new Date() : sTime;

            final String lFormatedStime = DateTimeUtility.getFormattedDateTime(sTime, lDnCustomDateFormat);

            Date         dTime          = aDeliveryObject.getDeliveryTime();
            dTime = dTime == null ? new Date() : dTime;
            final String lFormatedDtime  = DateTimeUtility.getFormattedDateTime(dTime, lDnCustomDateFormat);

            String       lReplaceMessage = aDeliveryObject.getMessage();
            if ((lReplaceMessage != null) && (lReplaceMessage.length() > 0))
                lReplaceMessage = lReplaceMessage.replaceAll("%A%", "\\|");
            else
                lReplaceMessage = "";

            if (isMsgSupported)
            {
                if (lReplaceMessage.length() > 10)
                    lReplaceMessage = lReplaceMessage.substring(0, 10);
            }
            else
                lReplaceMessage = UNSUPPORTED_MSG_FORMAT;

            final String lDlrStatus = DnStatus.getDnStatus(aDeliveryObject.getDeliveryStatus());

            sb.append("id:").append(id);
            sb.append(" sub:001 dlvrd:001 submit date:").append(lFormatedStime);
            sb.append(" done date:").append(lFormatedDtime);
            sb.append(" stat:").append(lDlrStatus);
            sb.append(" err:").append(aDeliveryObject.getDnClientStatusCode());
           // sb.append(" Text:").append(lReplaceMessage);
            sb.append(" Text:").append(" ");
        }
        catch (final Exception e)
        {
            log.error("Error ::", e);
            throw e;
        }

        if (log.isDebugEnabled())
            log.debug("Generated smpp full dn message : '" + sb.toString() + "'");

        return sb.toString();
    }

}
