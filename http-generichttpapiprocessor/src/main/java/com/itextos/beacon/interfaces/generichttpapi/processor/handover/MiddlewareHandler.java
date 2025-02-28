package com.itextos.beacon.interfaces.generichttpapi.processor.handover;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessageClass;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.message.MessagePart;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.http.generichttpapi.common.data.BasicInfo;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceMessage;
import com.itextos.beacon.http.generichttpapi.common.interfaces.IResponseProcessor;
import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.http.generichttpapi.common.utils.InterfaceMessageClass;
import com.itextos.beacon.http.generichttpapi.common.utils.Utility;
import com.itextos.beacon.http.interfaceutil.InterfaceUtil;
import com.itextos.beacon.inmemory.interfaces.bean.InterfaceResponse;
import com.itextos.beacon.inmemory.interfaces.bean.InterfaceResponseCodeMapping;
import com.itextos.beacon.inmemory.interfaces.cache.GenericResponse;
import com.itextos.beacon.inmemory.interfaces.util.IInterfaceUtil;

public class MiddlewareHandler
{

    private static final Log          log                      = LogFactory.getLog(MiddlewareHandler.class);
    private static final String       STATUS_SUCCESS           = "success";
    private static final String       HTTP_STATUS_CODE_SUCCESS = "200";

    private final InterfaceMessage    mInterfaceMessage;
    private final BasicInfo           mBasicInfo;
    private final InterfaceStatusCode mMessageValidateStatus;
    private final InterfaceStatusCode mDestValidateStatus;

    public MiddlewareHandler(
            InterfaceMessage aMessageBean,
            BasicInfo aBasicInfo,
            InterfaceStatusCode aMessageValidateStatus,
            InterfaceStatusCode aDestValidateStatus)
    {
        mInterfaceMessage      = aMessageBean;
        mMessageValidateStatus = aMessageValidateStatus;
        mDestValidateStatus    = aDestValidateStatus;
        mBasicInfo             = aBasicInfo;
    }

    public void middleWareHandover(
            boolean isAsync,
            IResponseProcessor responseHandler,
            String aReqType,
            StringBuffer sb)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("Request Type for response : " + aReqType);

        final MessageRequest    lMessageRequest = generateMessageRequestObj(aReqType);
        
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append(" telemarketerid : lMessageRequest "+lMessageRequest.getDltTelemarketerId()).append("\t"); 


        final GenericResponse   lGenericResp    = IInterfaceUtil.getGenericResponse();
        final InterfaceResponse responseObject  = lGenericResp.getInterfaceResponse(lMessageRequest.getClientId(), responseHandler.getRequestType());

        if (log.isDebugEnabled())
            log.debug("Interface Response : " + responseObject);

        String lHttpStatus = HTTP_STATUS_CODE_SUCCESS;
        String lStatusCode = HTTP_STATUS_CODE_SUCCESS;

        if (responseObject != null)
        {
            final InterfaceResponseCodeMapping lResponseCodeMapping = responseObject.getResponseCodeMapping("200");

            if (lResponseCodeMapping != null)
            {
                lStatusCode = lResponseCodeMapping.getClientStatusCode();
                lHttpStatus = lResponseCodeMapping.getHttpStatus();
            }
        }

        lMessageRequest.setSyncRequest((isAsync == false));

        final String status_Id = (lMessageRequest.getSubOriginalStatusCode() != null) ? lMessageRequest.getSubOriginalStatusCode() : lStatusCode;

        // logAndUpdateCounter(aReqType, responseHandler, lMessageRequest, isAsync,
        // lHttpStatus, lStatusCode, status_Id);

        if (log.isDebugEnabled())
        {
            log.debug(" Send to middleware StatusId - " + mMessageValidateStatus.getStatusCode() + " and StatusDesc - " + mMessageValidateStatus.getStatusDesc());
            log.debug("Object Before sending to Kafka - " + lMessageRequest.toString());
        }

        InterfaceUtil.sendToKafka(lMessageRequest,sb);
    }

    private static void logAndUpdateCounter(
            InterfaceType aReqType,
            IResponseProcessor aResponseHandler,
            MessageRequest aMessageRequest,
            boolean aIsAsync,
            String aHttpStatus,
            String aStatusCode,
            String aStatusId)
    {
        if (log.isDebugEnabled())
            log.debug(aReqType + "" //
                    + aResponseHandler.getServletContext() + " "  //
                    + (aIsAsync ? APIConstants.PROCESS_TYPE_ASYNC : APIConstants.PROCESS_TYPE_SYNC) + " " //
                    + aMessageRequest.getClientSourceIp() + " " //
                    + aMessageRequest.getUser() + " " //
                    + true + " " //
                    + aHttpStatus + " "  //
                    + aStatusCode + " "  //
                    + aStatusId + " "   //
                    + (aMessageRequest.getFailReason() != null ? aMessageRequest.getFailReason() : "success"));

        try
        {
            final String userName = (aMessageRequest.getUser() == null) ? "" : aMessageRequest.getUser();

            if (log.isDebugEnabled())
                log.debug("User name used for promethues .. " + userName);

            PrometheusMetrics.apiIncrementStatusCount(aReqType, APIConstants.CLUSTER_INSTANCE, aMessageRequest.getClientSourceIp(), aStatusId, userName);
        }
        catch (final Exception e)
        {
            // ignore
        }
    }

    private MessageRequest generateMessageRequestObj(
            String aReqType)
            throws Exception
    {
        final JSONObject lUserDetails = mBasicInfo.getUserAccountInfo();

        ClusterType      lClusterType = Utility.getClusterType(CommonUtility.nullCheck(lUserDetails.get(MiddlewareConstant.MW_PLATFORM_CLUSTER.getName())).toLowerCase());

        // Setting default cluster type as bulk
        if (lClusterType == null)
            lClusterType = ClusterType.BULK;

        final MessageType     lMsgType       = MessageType.getMessageType(lUserDetails.get(MiddlewareConstant.MW_MSG_TYPE.getName()).toString());
        final MessagePriority lSmsPriority   = MessagePriority.getMessagePriority((String) lUserDetails.get(MiddlewareConstant.MW_SMS_PRIORITY.getName()));
        final MessageRequest  messageRequest = new MessageRequest(lClusterType, InterfaceType.HTTP_JAPI, InterfaceGroup.API, lMsgType, lSmsPriority, mInterfaceMessage.getRouteType(),
                lUserDetails.toJSONString());

        if (log.isDebugEnabled())
            log.debug("Message Object Begining..: " + messageRequest.toString());

        messageRequest.setAppInstanceId(APIConstants.getAppInstanceId());
        messageRequest.setFileId(mBasicInfo.getFileId());
        messageRequest.setBaseMessageId(mInterfaceMessage.getBaseMessageId());
        messageRequest.setClientId(mBasicInfo.getClientId());
        messageRequest.setClientSourceIp(mBasicInfo.getCustIp());
        messageRequest.setMobileNumber(getMobileNumber());
        messageRequest.setHeader(mInterfaceMessage.getHeader());
        messageRequest.setClientMessageId(mInterfaceMessage.getCustRef());
        messageRequest.setDcs(mInterfaceMessage.getDcs());
        messageRequest.setDestinationPort(mInterfaceMessage.getDestinationPort());
        messageRequest.setDltEntityId(mInterfaceMessage.getDltEntityId());
        messageRequest.setDltTemplateId(mInterfaceMessage.getDltTemplateId());
        messageRequest.setDltTelemarketerId(mInterfaceMessage.getTelemarketerId());

        messageRequest.setDlrRequestFromClient(CommonUtility.isTrue(mInterfaceMessage.getDlrReq()));
        messageRequest.setClientMaxSplit(CommonUtility.getInteger(mInterfaceMessage.getSplitMax()));
        messageRequest.setUrlTrackEnabled(CommonUtility.isEnabled(mInterfaceMessage.getUrlTrack()));
        messageRequest.setTreatDomesticAsSpecialSeries(CommonUtility.isTrue(mInterfaceMessage.getIsSpecialSeriesNumber()));
        messageRequest.setClientTemplateId(mInterfaceMessage.getTemplateId());
        messageRequest.setDltTelemarketerId(mInterfaceMessage.getTelemarketerId());

        messageRequest.setInterfaceCoutryCode(mInterfaceMessage.getCountryCode());
        messageRequest.setMsgSource(aReqType);
        messageRequest.setUrlShortnerReq(mInterfaceMessage.getUrlShortner());

        addSecheduleTime(messageRequest);
        setMaxValidatyTime(messageRequest);
        addParamAttributes(messageRequest, mInterfaceMessage);
        setMessageStatus(messageRequest);

        final MessagePart msgObj = new MessagePart(mInterfaceMessage.getMsgId());
        msgObj.setMessageReceivedDate(new Date(mBasicInfo.getRequestedTime()));
        msgObj.setMessageReceivedTime(new Date(mBasicInfo.getRequestedTime()));
        msgObj.setMessageActualReceivedDate(new Date(mBasicInfo.getRequestedTime()));
        msgObj.setMessageActualReceivedTime(new Date(mBasicInfo.getRequestedTime()));
        addMessage(messageRequest, msgObj);
        messageRequest.addMessagePart(msgObj);

        messageRequest.setLongMessage(msgObj.getMessage());

        return messageRequest;
    }

    private void setMessageStatus(
            MessageRequest aMessageRequest)
    {

        if ((mMessageValidateStatus != InterfaceStatusCode.SUCCESS) || (mDestValidateStatus != InterfaceStatusCode.SUCCESS))
        {
            aMessageRequest.setInterfaceRejected(true);

            if (mDestValidateStatus != InterfaceStatusCode.SUCCESS)
            {
                aMessageRequest.setSubOriginalStatusCode(mDestValidateStatus.getStatusCode());
                aMessageRequest.setFailReason(mDestValidateStatus.getStatusDesc());

                aMessageRequest.setAdditionalErrorInfo("Invalid mobile " + mInterfaceMessage.getMobileNumber());
            }
            else
            {
                aMessageRequest.setSubOriginalStatusCode(mMessageValidateStatus.getStatusCode());
                aMessageRequest.setFailReason(mMessageValidateStatus.getStatusDesc());
            }
        }
        else
            aMessageRequest.setCountry(getCountryName());
    }

    private String getCountryName()
    {
        if ((mInterfaceMessage.getRouteType() != null) && (mInterfaceMessage.getRouteType() == RouteType.INTERNATIONAL))
            return "";

        return InterfaceUtil.getCountry();
    }

    private void setMaxValidatyTime(
            MessageRequest aMessageRequest)
    {
        if ((mInterfaceMessage.getExpiry() != null) && !mInterfaceMessage.getExpiry().isBlank())
            try
            {
                final int lIntExpiry = CommonUtility.getInteger(mInterfaceMessage.getExpiry()) * 60;
                aMessageRequest.setMaxValidityInSec(lIntExpiry);
            }
            catch (final Exception e)
            {
                log.error("Exception occer while converting the Message Expiry..", e);
            }
    }

    private String getMobileNumber()
    {
        return CommonUtility.nullCheck(mInterfaceMessage.getMobileNumber(), true).isBlank() ? APIConstants.DEFAULT_DEST : mInterfaceMessage.getMobileNumber();
    }

    private void addSecheduleTime(
            MessageRequest aMessageRequest)
    {
        final String lScheduleTime = mBasicInfo.getScheduleTime();

        if (log.isDebugEnabled())
            log.debug("Schedule Time added in MessageRequest : " + lScheduleTime);

        try
        {
            if (!lScheduleTime.isBlank())
                aMessageRequest.setScheduleDateTime(DateTimeUtility.getDateFromString(lScheduleTime, DateTimeFormat.DEFAULT));
        }
        catch (final Exception e)
        {
            log.error("Exception while adding schedule time to MessageRequest Object ", e);
        }
    }

    private static void addParamAttributes(
            MessageRequest aMessageRequest,
            InterfaceMessage aInterfaceMessage)
    {
        aMessageRequest.setMessageTag(getStrippedParamValues(aInterfaceMessage.getMsgTag(), APIConstants.PARAMS_MIN_VALUE, APIConstants.MSGTAG_MAX_VALUE));
        aMessageRequest.setParam1(getStrippedParamValues(aInterfaceMessage.getParam1(), APIConstants.PARAMS_MIN_VALUE, APIConstants.PARAM1_MAX_VALUE));
        aMessageRequest.setParam2(getStrippedParamValues(aInterfaceMessage.getParam2(), APIConstants.PARAMS_MIN_VALUE, APIConstants.PARAM2_MAX_VALUE));
        aMessageRequest.setParam3(getStrippedParamValues(aInterfaceMessage.getParam3(), APIConstants.PARAMS_MIN_VALUE, APIConstants.PARAM3_MAX_VALUE));
        aMessageRequest.setParam4(getStrippedParamValues(aInterfaceMessage.getParam4(), APIConstants.PARAMS_MIN_VALUE, APIConstants.PARAM4_MAX_VALUE));
        aMessageRequest.setParam5(getStrippedParamValues(aInterfaceMessage.getParam5(), APIConstants.PARAMS_MIN_VALUE, APIConstants.PARAM5_MAX_VALUE));
        aMessageRequest.setParam6(getStrippedParamValues(aInterfaceMessage.getParam6(), APIConstants.PARAMS_MIN_VALUE, APIConstants.PARAM6_MAX_VALUE));
        aMessageRequest.setParam7(getStrippedParamValues(aInterfaceMessage.getParam7(), APIConstants.PARAMS_MIN_VALUE, APIConstants.PARAM7_MAX_VALUE));
        aMessageRequest.setParam8(getStrippedParamValues(aInterfaceMessage.getParam8(), APIConstants.PARAMS_MIN_VALUE, APIConstants.PARAM8_MAX_VALUE));
        aMessageRequest.setParam9(getStrippedParamValues(aInterfaceMessage.getParam9(), APIConstants.PARAMS_MIN_VALUE, APIConstants.PARAM9_MAX_VALUE));
        aMessageRequest.setParam10(getStrippedParamValues(aInterfaceMessage.getParam10(), APIConstants.PARAMS_MIN_VALUE, APIConstants.PARAM10_MAX_VALUE));
    }

    private static String getStrippedParamValues(
            String aParamValue,
            int aMinValue,
            int aMaxValue)
    {
        final String lTempVal = CommonUtility.nullCheck(aParamValue, true);

        if (!lTempVal.isEmpty())
        {
            if (lTempVal.length() >= aMaxValue)
                return lTempVal.substring(aMinValue, aMaxValue);
            return lTempVal;
        }
        return aParamValue;
    }

    private void addMessage(
            MessageRequest aMessageRequest,
            MessagePart aMessageObj)
    {
        String       lMsgType  = CommonUtility.nullCheck(mInterfaceMessage.getMsgType(), true);

        final String lMessage  = mInterfaceMessage.getMessage();
        String       lHexMsg   = null;
        MessageClass lMsgClass = null;

        if ("".equals(lMsgType))
            lMsgType = InterfaceMessageClass.PLAIN.getMessageType();

        final InterfaceMessageClass lTempMsgType = InterfaceMessageClass.getMessageType(lMsgType);

        if (lTempMsgType != null)
        {
            if (log.isDebugEnabled())
                log.debug("Message Type : " + lTempMsgType);

            switch (lTempMsgType)
            {
                case PLAIN:
                    lMsgClass = MessageClass.PLAIN_MESSAGE;
                    break;

                case FLASH:
                    lMsgClass = MessageClass.FLASH_PLAIN_MESSAGE;
                    break;

                case FLASH_UNICODE:
                    lMsgClass = MessageClass.FLASH_UNICODE_MESSAGE;
                    lHexMsg = lMessage;
                    break;

                case SPECIFIC_PORT:
                    lMsgClass = MessageClass.SP_PLAIN_MESSAGE;
                    break;

                case SPECIFIC_PORT_UNICODE:
                    lMsgClass = MessageClass.SP_UNICODE_MESSAGE;
                    lHexMsg = lMessage;
                    break;

                case UNICODE:
                    lMsgClass = MessageClass.UNICODE_MESSAGE;
                    lHexMsg = lMessage;
                    break;

                case BINARY:
                    lMsgClass = MessageClass.BINARY_MESSAGE;
                    lHexMsg = lMessage;
                    break;

                default:
                    lMsgClass = MessageClass.PLAIN_MESSAGE;
            }
        }

        if (InterfaceMessageClass.ADVANCE.getMessageType().equals(mInterfaceMessage.getMsgType()))
        {
            lMsgClass = MessageClass.BINARY_MESSAGE;

            lHexMsg   = lMessage;
        }

        if (lHexMsg != null)
        {
            aMessageObj.setMessage(lHexMsg);
            aMessageRequest.setIsHexMessage(true);
        }
        else
            aMessageObj.setMessage(CommonUtility.nullCheck(lMessage, true));

        aMessageRequest.setMessageClass(lMsgClass == null ? "" : lMsgClass.getKey());
    }

}