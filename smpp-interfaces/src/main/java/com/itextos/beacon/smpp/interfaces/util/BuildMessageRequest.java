package com.itextos.beacon.smpp.interfaces.util;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.MessagePart;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.http.interfaceutil.InterfaceUtil;
import com.itextos.beacon.smpp.objects.SessionDetail;
import com.itextos.beacon.smpp.objects.request.SmppMessageRequest;
import com.itextos.beacon.smpp.utils.ItextosSmppUtil;

public class BuildMessageRequest
{

    private static final Log log = LogFactory.getLog(BuildMessageRequest.class);

    private BuildMessageRequest()
    {}

    public static MessageRequest getMessageRequest(
            SmppMessageRequest aSmppMessageRequest,
            SessionDetail aSessionDetail,
            PlatformStatusCode aPlatformStatusCode) throws ItextosRuntimeException
    {
        final ClusterType     lClusterType    = aSessionDetail.getClusterType();
        final MessageType     lMsgType        = aSessionDetail.getMessageType();
        final MessagePriority lSmsPriority    = aSessionDetail.getMessagePriority();
        final RouteType       lRouteType      = aSmppMessageRequest.getRouteType();

        final MessageRequest  lMessageRequest = new MessageRequest(lClusterType, InterfaceType.SMPP, InterfaceGroup.SMPP, lMsgType, lSmsPriority, lRouteType, aSessionDetail.getAccountJson());

        lMessageRequest.setAppInstanceId(aSmppMessageRequest.getAppInstanceId());
        lMessageRequest.setFileId(aSmppMessageRequest.getAckid());
        lMessageRequest.setBaseMessageId(aSmppMessageRequest.getAckid());
        lMessageRequest.setClientSourceIp(aSmppMessageRequest.getClientIp());
        lMessageRequest.setMobileNumber(aSmppMessageRequest.getMobileNumber());
        lMessageRequest.setHeader(aSmppMessageRequest.getHeader());
        lMessageRequest.setDcs(aSmppMessageRequest.getDcs());
        lMessageRequest.setDestinationPort(aSmppMessageRequest.getDestPort());
        lMessageRequest.setDltEntityId(aSmppMessageRequest.getDltEntityId());
        lMessageRequest.setDltTemplateId(aSmppMessageRequest.getDltTemplateId());
        lMessageRequest.setDltTelemarketerId(aSmppMessageRequest.getDltTelemarketerId());
        lMessageRequest.setDlrRequestFromClient(CommonUtility.isTrue(aSmppMessageRequest.getDlrReqFromClient()));
        lMessageRequest.setTreatDomesticAsSpecialSeries(aSmppMessageRequest.isSpecialSeriesNumber());
        lMessageRequest.setIsHexMessage(aSmppMessageRequest.isHexMsg());
        lMessageRequest.setMessageClass(aSmppMessageRequest.getMsgClass().getKey());
        lMessageRequest.setSmppEsmClass(aSmppMessageRequest.getEsmClass());
        lMessageRequest.setSmppInstance(aSmppMessageRequest.getSmppInstance());
        lMessageRequest.setMessageTotalParts(1);
        lMessageRequest.setClientMessageId(aSmppMessageRequest.getCustMid());

        if ((lRouteType != null) && (lRouteType == RouteType.INTERNATIONAL))
            lMessageRequest.setCountry("");
        else
            lMessageRequest.setCountry(InterfaceUtil.getCountry());

        lMessageRequest.setMsgSource(InterfaceType.SMPP.getKey());

        final String lScheduleTime = aSmppMessageRequest.getScheduleTime();

        if ((lScheduleTime != null) && !lScheduleTime.isBlank())
            lMessageRequest.setScheduleDateTime(DateTimeUtility.getDateFromString(lScheduleTime, DateTimeFormat.DEFAULT));

        lMessageRequest.setMaxValidityInSec(CommonUtility.getInteger(aSmppMessageRequest.getMessageExpiry()));

        setMessageStatus(lMessageRequest, aPlatformStatusCode, aSmppMessageRequest);

        final Date        lReceivedTime = new Date();

        final MessagePart msgObj        = new MessagePart(lMessageRequest.getFileId());
        msgObj.setMessageReceivedDate(lReceivedTime);
        msgObj.setMessageReceivedTime(lReceivedTime);
        msgObj.setMessageActualReceivedDate(lReceivedTime);
        msgObj.setMessageActualReceivedTime(lReceivedTime);
        msgObj.setMessage(aSmppMessageRequest.getMessage());
        msgObj.setMessagePartNumber(aSmppMessageRequest.getPartNumber());
        msgObj.setUdh(aSmppMessageRequest.getUdh());
        msgObj.putValueExt(MiddlewareConstant.MW_SMPP_MSG_RECEIVED_TIME, DateTimeUtility.getFormattedDateTime(aSmppMessageRequest.getReceivedTime(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));

        lMessageRequest.addMessagePart(msgObj);
        lMessageRequest.setLongMessage(msgObj.getMessage());

        if (log.isDebugEnabled())
            log.debug("SMPP Received Time :" + msgObj.getValueExt(MiddlewareConstant.MW_SMPP_MSG_RECEIVED_TIME));

        if (aPlatformStatusCode == PlatformStatusCode.INVALID_DATA_CODING_SCHEME)
        {
            if (log.isDebugEnabled())
                log.debug("Invalid Data Coding Scheme :" + aPlatformStatusCode);
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("Set the Feature Code....");
            ItextosSmppUtil.setFeatureCode(lMessageRequest);
        }

        return lMessageRequest;
    }

    private static void setMessageStatus(
            MessageRequest aMessageRequest,
            PlatformStatusCode aPlatformStatusCode,
            SmppMessageRequest aSmppMessageRequest)
    {

        if ((aSmppMessageRequest.getInterfaceErrorCode() != null) && !aSmppMessageRequest.getInterfaceErrorCode().isEmpty())
        {
            aMessageRequest.setInterfaceRejected(true);
            aMessageRequest.setSubOriginalStatusCode(aSmppMessageRequest.getInterfaceErrorCode());
            aMessageRequest.setSyncRequest(true);
        }

        if (aPlatformStatusCode != null)
        {
            aMessageRequest.setInterfaceRejected(true);
            aMessageRequest.setSubOriginalStatusCode(aPlatformStatusCode.getStatusCode());
            aMessageRequest.setFailReason(aPlatformStatusCode.getStatusDesc());
        }
    }

}
