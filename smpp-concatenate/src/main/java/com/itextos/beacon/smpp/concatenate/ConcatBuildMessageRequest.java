package com.itextos.beacon.smpp.concatenate;

import java.util.Date;
import java.util.List;

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
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.MessagePart;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.messageidentifier.MessageIdentifier;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.http.interfaceutil.InterfaceUtil;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.itextos.beacon.smpp.objects.SmppUserInfo;
import com.itextos.beacon.smpp.objects.request.SmppMessageRequest;
import com.itextos.beacon.smpp.utils.AccountDetails;
import com.itextos.beacon.smpp.utils.ItextosSmppUtil;

public class ConcatBuildMessageRequest
{

    private static final Log log = LogFactory.getLog(ConcatBuildMessageRequest.class);

    private ConcatBuildMessageRequest()
    {}

    public static MessageRequest getMessageRequest(
            SmppMessageRequest aSmppMessageRequest,
            SmppUserInfo aSmppUserInfo,
            ClusterType aCluster,
            PlatformStatusCode aPlatformStatusCode,
            int aTotalMsgParts) throws ItextosRuntimeException
    {
        final ClusterType     lClusterType    = aCluster;
        final MessageType     lMsgType        = aSmppUserInfo.getMessageType();
        final MessagePriority lSmsPriority    = aSmppUserInfo.getMessagePriority();
        final RouteType       lRouteType      = aSmppMessageRequest.getRouteType();

        final MessageRequest  lMessageRequest = new MessageRequest(lClusterType, InterfaceType.SMPP, InterfaceGroup.SMPP, lMsgType, lSmsPriority, lRouteType, aSmppUserInfo.getAccountJson());

        final String          lFileId         = MessageIdentifier.getInstance().getNextId();
        lMessageRequest.setAppInstanceId(aSmppMessageRequest.getAppInstanceId());
        lMessageRequest.setFileId(lFileId);
        lMessageRequest.setBaseMessageId(lFileId);
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
        lMessageRequest.setFeatureCode(aSmppMessageRequest.getFeatureCode().getKey());
        lMessageRequest.setMessageTotalParts(aTotalMsgParts);

        setMessageStatus(lMessageRequest, aPlatformStatusCode);

        return lMessageRequest;
    }

    private static void setMessageStatus(
            MessageRequest aMessageRequest,
            PlatformStatusCode aPlatformStatusCode)
    {

        if (aPlatformStatusCode != null)
        {
            aMessageRequest.setInterfaceRejected(true);
            aMessageRequest.setSubOriginalStatusCode(aPlatformStatusCode.getStatusCode());
            aMessageRequest.setFailReason(aPlatformStatusCode.getStatusDesc());
        }
    }

    public static void setMessageParts(
            MessageRequest aMessageRequest,
            List<MessagePart> aMsgParts,
            String aLongMessage)
    {
        for (final MessagePart mp : aMsgParts)
            aMessageRequest.addMessagePart(mp);

        aMessageRequest.setLongMessage(aLongMessage);
    }

    public static MessageRequest getMessageRequest(
            SmppMessageRequest aSmppMessageRequest,
            SmppUserInfo aUserInfo,
            PlatformStatusCode aPlatformStatusCode) throws ItextosRuntimeException
    {
        final ClusterType     lClusterType    = aUserInfo.getClusterType();
        final MessageType     lMsgType        = aUserInfo.getMessageType();
        final MessagePriority lSmsPriority    = aUserInfo.getMessagePriority();
        final RouteType       lRouteType      = aSmppMessageRequest.getRouteType();

        final MessageRequest  lMessageRequest = new MessageRequest(lClusterType, InterfaceType.SMPP, InterfaceGroup.SMPP, lMsgType, lSmsPriority, lRouteType, aUserInfo.getAccountJson());

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

        setMessageStatus(lMessageRequest, aPlatformStatusCode);

        final Date        lReceivedTime = new Date();

        final MessagePart msgObj        = new MessagePart(lMessageRequest.getFileId());
        msgObj.setMessageReceivedDate(lReceivedTime);
        msgObj.setMessageReceivedTime(lReceivedTime);
        msgObj.setMessageActualReceivedDate(lReceivedTime);
        msgObj.setMessageActualReceivedTime(lReceivedTime);
        msgObj.setMessagePartNumber(aSmppMessageRequest.getPartNumber());
        msgObj.setMessage(aSmppMessageRequest.getMessage());
        msgObj.setUdh(aSmppMessageRequest.getUdh());
        msgObj.setUdhi(CommonUtility.getInteger(aSmppMessageRequest.getUdhi()));
        msgObj.putValueExt(MiddlewareConstant.MW_SMPP_MSG_RECEIVED_TIME, String.valueOf(aSmppMessageRequest.getReceivedTime()));

        lMessageRequest.addMessagePart(msgObj);
        lMessageRequest.setLongMessage(msgObj.getMessage());

        ItextosSmppUtil.setFeatureCode(lMessageRequest);

        return lMessageRequest;
    }

    public static SmppUserInfo updateUserInfo(
            String aClientId)
            throws ItextosException
    {
        final UserInfo userInfo = AccountDetails.getUserInfoByClientId(aClientId);

        log.debug("Client id : '" + aClientId + "' -userInfo : " + userInfo);

        if (userInfo != null)
            return new SmppUserInfo(userInfo);

        throw new ItextosException("Unable to find the user details for the user '" + aClientId + "'");
    }

    public static MessagePart getMessagePartObj(
            SmppMessageRequest aSmppRequest)
    {
        final Date        lReceivedTime = new Date();

        final MessagePart msgObj        = new MessagePart(aSmppRequest.getAckid());
        msgObj.setMessageReceivedDate(lReceivedTime);
        msgObj.setMessageReceivedTime(lReceivedTime);
        msgObj.setMessageActualReceivedDate(lReceivedTime);
        msgObj.setMessageActualReceivedTime(lReceivedTime);
        msgObj.setMessage(aSmppRequest.getMessage());
        msgObj.setMessagePartNumber(aSmppRequest.getPartNumber());
        msgObj.setUdh(aSmppRequest.getUdh());
        msgObj.setUdhi(CommonUtility.getInteger(aSmppRequest.getUdhi()));
        msgObj.putValueExt(MiddlewareConstant.MW_SMPP_MSG_RECEIVED_TIME, String.valueOf(aSmppRequest.getReceivedTime()));

        return msgObj;
    }

}
