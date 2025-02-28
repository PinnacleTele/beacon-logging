package com.itextos.beacon.interfaces.generichttpapi.processor.reader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceMessage;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceRequestStatus;
import com.itextos.beacon.http.generichttpapi.common.interfaces.IRequestProcessor;
import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.http.generichttpapi.common.utils.InterfaceInputParameters;
import com.itextos.beacon.http.generichttpapi.common.utils.Utility;
import com.itextos.beacon.http.interfaceparameters.InterfaceParameter;
import com.itextos.beacon.http.interfaceparameters.InterfaceParameterLoader;
import com.itextos.beacon.http.interfaceutil.MessageSource;
import com.itextos.beacon.interfaces.generichttpapi.processor.request.JSONRequestProcessor;

import io.prometheus.client.Histogram.Timer;

public class QSRequestReader
        extends
        AbstractReader
{

    private static final Log log = LogFactory.getLog(QSRequestReader.class);
    private final String     servletName;
    private final String     mRequestType;

    StringBuffer sb;
    
    public QSRequestReader(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse,
            String aServletName,
            String aRequestType,
            StringBuffer sb)
    {
        super("queryString", aRequest, aResponse);
        servletName  = aServletName;
        mRequestType = aRequestType;
        this.sb=sb;
        sb.append(" servletName : "+servletName ).append("\n");

        sb.append(" aRequestType : "+aRequestType ).append("\n");
    }

    @Override
    public void processGetRequest()
    {

        try
        {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("servletName", servletName);

            if (mRequestType == null)
            {
                buildJson(jsonObject, null, mRequestType);
                sb.append("buildJson Json").append("\n");
                doProcess(jsonObject);
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Custimize QueryString Request processing..");

                buildBadsicInfo(jsonObject);
                sb.append("buildBadsicInfo Json").append("\n");
                doProcess(jsonObject);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while parsing the JSON", e);
        }
    }

    @Override
    public void doProcess(
            JSONObject aJsonObj)
    {
        if (log.isDebugEnabled())
            log.debug(" source_ip:  '" + mHttpRequest.getRemoteAddr() + "' request received time: '" + System.currentTimeMillis() + " '");
        sb.append(" source_ip:  '" + mHttpRequest.getRemoteAddr() + "' request received time: '" + System.currentTimeMillis() + " '").append("\n");
        String lUserName      = NO_USER;
        Timer  overAllProcess = null;
        Timer  jsonProcess    = null;

        try
        {
            overAllProcess = PrometheusMetrics.apiStartTimer(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_QS, APIConstants.CLUSTER_INSTANCE, mHttpRequest.getRemoteAddr(), OVERALL);

            String jsonString = aJsonObj.toString();

            if (mRequestType != null)
                jsonString = buildBadsicInfo(aJsonObj);

            final IRequestProcessor requestProcessor = new JSONRequestProcessor(jsonString, mHttpRequest.getRemoteAddr(), System.currentTimeMillis(), MessageSource.GENERIC_QS,
                    MessageSource.GENERIC_QS,sb);

            requestProcessor.parseBasicInfo(mHttpRequest.getHeader(InterfaceInputParameters.AUTHORIZATION));

            final InterfaceRequestStatus reqStatus = requestProcessor.validateBasicInfo();

            sb.append("reqStatus : "+reqStatus).append("\n");
            
            if (reqStatus.getStatusCode() == InterfaceStatusCode.SUCCESS)
            {
                lUserName   = getUserName(requestProcessor);

                jsonProcess = PrometheusMetrics.apiStartTimer(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_QS, APIConstants.CLUSTER_INSTANCE, mHttpRequest.getRemoteAddr(), lUserName);
                PrometheusMetrics.apiIncrementAcceptCount(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_QS, APIConstants.CLUSTER_INSTANCE, mHttpRequest.getRemoteAddr(), lUserName);

                updateRequestObjectBasedOnRequestType(requestProcessor, aJsonObj);

                final int messagesCount = requestProcessor.getMessagesCount();

                if (log.isDebugEnabled())
                    log.debug(" messageCount:  '" + messagesCount + "'");
                
                sb.append(" messageCount:  '" + messagesCount + "'").append("\n");
                
                if (messagesCount == 0)
                {
                    if (log.isDebugEnabled())
                        log.debug(" messageCount==0:  '" + InterfaceStatusCode.INVALID_JSON + "' Message Object Missing");

                    handleNoMessage(requestProcessor, reqStatus);
                }
                else
                {
                    if (log.isDebugEnabled())
                        log.debug("Processing valid messages");

                    processValidMessages(requestProcessor, reqStatus);
                }
            }
            sendResponse(requestProcessor);
        }
        catch (final Exception e)
        {
            log.error("Excception while processig QueryString request", e);
            handleException(aJsonObj.toJSONString());
        }
        finally
        {
            PrometheusMetrics.apiEndTimer(InterfaceType.HTTP_JAPI, APIConstants.CLUSTER_INSTANCE, jsonProcess);
            PrometheusMetrics.apiEndTimer(InterfaceType.HTTP_JAPI, APIConstants.CLUSTER_INSTANCE, overAllProcess);
        }
    }

    private void handleException(
            String aJsonString)
    {
        final IRequestProcessor      requestProcessor = new JSONRequestProcessor(aJsonString, mHttpRequest.getRemoteAddr(), System.currentTimeMillis(), MessageSource.GENERIC_QS,
                MessageSource.GENERIC_QS,sb);
        final InterfaceRequestStatus status           = new InterfaceRequestStatus(InterfaceStatusCode.INVALID_REQUEST, "");
        requestProcessor.setRequestStatus(status);
        sendResponse(requestProcessor);
    }

    private void updateRequestObjectBasedOnRequestType(
            IRequestProcessor aRequestProcessor,
            JSONObject aJsonObj)
            throws ParseException
    {

        if (mRequestType != null)
        {
            buildJson(aJsonObj, aRequestProcessor.getBasicInfo().getClientId(), mRequestType);

            final String aJSonString = aJsonObj.toString();

            if (log.isDebugEnabled())
                log.debug("Custom Json String :" + aJSonString);

            aRequestProcessor.setRequestString(aJSonString);
            aRequestProcessor.resetRequestJson(aJsonObj);
        }
    }

    private void processValidMessages(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus)
    {
        final int messagesCount = aRequestProcessor.getMessagesCount();

        if (messagesCount == 1)
            processSingleMessage(aRequestProcessor, aReqStatus,sb);
        else
            processMultipleMessage(aRequestProcessor, aReqStatus);
    }

    private static void handleNoMessage(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus)
    {
        aReqStatus = new InterfaceRequestStatus(InterfaceStatusCode.INVALID_JSON, "Message Object Missing");
        aRequestProcessor.setRequestStatus(aReqStatus);
    }

    private static void processMultipleMessage(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus)
    {
        final String messageId = aReqStatus.getMessageId();
        if (log.isDebugEnabled())
            log.debug(" MultipleMessage:  '" + InterfaceStatusCode.SUCCESS + "'");

        aReqStatus = aRequestProcessor.getMultipleMessages(false);

        if (aReqStatus == null)
        {
            aReqStatus = new InterfaceRequestStatus(InterfaceStatusCode.SUCCESS, "");
            aReqStatus.setMessageId(messageId);
        }
        aRequestProcessor.setRequestStatus(aReqStatus);
    }

    private static void processSingleMessage(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus,StringBuffer sb)
    {
        final InterfaceMessage message   = aRequestProcessor.getSingleMessage(sb);
        final String           messageId = aReqStatus.getMessageId();

        if (message == null)
        {
            aReqStatus = new InterfaceRequestStatus(InterfaceStatusCode.SUCCESS, "");
            aReqStatus.setMessageId(messageId);
            aRequestProcessor.setRequestStatus(aReqStatus);
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug(" singleMessage:  '" + message.getRequestStatus() + "'");

            aReqStatus = message.getRequestStatus();

            aReqStatus.setMessageId(messageId);
            aRequestProcessor.setRequestStatus(aReqStatus);
            /*
             * if (aReqStatus.getStatusCode() == InterfaceStatusCode.SUCCESS)
             * {
             * aReqStatus.setMessageId(messageId);
             * aRequestProcessor.setRequestStatus(aReqStatus);
             * }
             * else
             * aRequestProcessor.setRequestStatus(aReqStatus);
             */
        }
    }

    public String buildBadsicInfo(
            JSONObject jsonObject)
    {
        if (log.isDebugEnabled())
            log.debug("buildBadsicInfo() - QS Request :" + mHttpRequest.getQueryString());

        final String version       = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_VERSION);

        final String accessKey     = getAccessKeyFromCustomer();

        final String encrypt       = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_ENCRYPTED);
        final String lScheduleTime = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_SCHEDULE_AT);

        jsonObject.put(InterfaceInputParameters.REQ_PARAMETER_VERSION, version);
        if (accessKey != null)
            jsonObject.put(InterfaceInputParameters.REQ_PARAMETER_KEY, accessKey);

        jsonObject.put(InterfaceInputParameters.REQ_PARAMETER_ENCRYPTED, encrypt);
        jsonObject.put(InterfaceInputParameters.REQ_PARAMETER_SCHEDULE_AT, lScheduleTime);

        return jsonObject.toString();
    }

    private String getAccessKeyFromCustomer()
    {
        String accessKey = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_KEY);

        if (accessKey == null)
        {
            final String possbileAccessKeys = CommonUtility.nullCheck(Utility.getConfigParamsValueAsString(ConfigParamConstants.ACCESS_KEY_PARAMS), true);

            if (log.isDebugEnabled())
                log.debug("possbileAccessKeys  " + possbileAccessKeys);

            if (!possbileAccessKeys.isBlank())
            {
                final String[] lInputAccessKeyList = possbileAccessKeys.split(",");

                for (final String lKey : lInputAccessKeyList)
                {
                    accessKey = mHttpRequest.getParameter(CommonUtility.nullCheck(lKey, true));

                    if (log.isDebugEnabled())
                        log.debug("Customized AccessKey Param '" + lKey + "' Value : '" + accessKey + "'");

                    if (accessKey != null)
                        return accessKey;
                }
            }
        }

        return accessKey;
    }

    public void buildJson(
            JSONObject jsonObject,
            String aClientId,
            String aRequestType)
            throws ParseException
    {
        if (log.isDebugEnabled())
            log.debug("QS Query String :" + mHttpRequest.getQueryString());
        String lScheduleTime   = null;
        String lDest           = null;
        String lMessage        = null;
        String lHeader         = null;
        String lMsgType        = null;
        String lUdhi           = null;
        String lUdh            = null;
        String lAppendCountry  = null;
        String lUrlTrack       = null;
        String lDcs            = null;
        String lSpecialPort    = null;
        String lMsgExpiry      = null;
        String lCountryCode    = null;
        String lCustRef        = null;
        String lTemplateId     = null;
        String lTemplateValues = null;
        String lDltEntityId    = null;
        String lDltTemplateId  = null;
        String lDltTMAId  = null;

        String lMsgtag         = null;
        String lParam1         = null;
        String lParam2         = null;
        String lParam3         = null;
        String lParam4         = null;
        String lParam5         = null;
        String lParam6         = null;
        String lParam7         = null;
        String lParam8         = null;
        String lParam9         = null;
        String lParam10        = null;
        String lDlrReq         = null;
        String lMaxsplit       = null;
        String lUrlShortner    = null;
        String lEmailTo    = null;
        String lEmailFrom    = null;
        String lEmailFromName    = null;
        String lEmailSubject    = null;

        if (aRequestType == null)
        {
            final String version   = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_VERSION);
            final String accessKey = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_KEY);
            final String encrypt   = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_ENCRYPTED);
            lScheduleTime   = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_SCHEDULE_AT);
            lDest           = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_DEST);
            lMessage        = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_MSG);
            lHeader         = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_HEADER);
            lMsgType        = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_TYPE);
            lUdhi           = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_UDHI);
            lUdh            = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_UDH);
            lAppendCountry  = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_APPEND_COUNTRY);
            lCountryCode    = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_COUNTRY_CODE);
            lUrlTrack       = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_URL_TRACK);
            lDcs            = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_DCS);
            lSpecialPort    = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_PORT);
            lMsgExpiry      = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_MSG_EXPIRY);
            lCustRef        = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_CUST_REF);
            lTemplateId     = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_TEMPLATE_ID);
            lTemplateValues = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_TEMPLATE_VALUES);
            lDltEntityId    = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_DLT_ENTITY_ID);
            lDltTemplateId  = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_DLT_TEMPLATE_ID);
            lDltTMAId  = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_DLT_TMA_ID);

            lMsgtag         = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_MSG_TAG);
            lParam1         = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_PARAM1);
            lParam2         = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_PARAM2);
            lParam3         = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_PARAM3);
            lParam4         = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_PARAM4);
            lParam5         = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_PARAM5);
            lParam6         = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_PARAM6);
            lParam7         = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_PARAM7);
            lParam8         = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_PARAM8);
            lParam9         = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_PARAM9);
            lParam10        = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_PARAM10);
            lDlrReq         = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_DLR_REQ);
            lMaxsplit       = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_MAX_SPLIT);
            lUrlShortner    = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_URL_SHORTNER);

            lEmailTo    = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_EMAIL_TO);
            lEmailFrom    = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_EMAIL_FROM);
            lEmailFromName    = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_EMAIL_FROM_NAME);
            lEmailSubject    = mHttpRequest.getParameter(InterfaceInputParameters.REQ_PARAMETER_EMAIL_SUBJECT);

            jsonObject.put(InterfaceInputParameters.REQ_PARAMETER_VERSION, version);
            if (accessKey != null)
                jsonObject.put(InterfaceInputParameters.REQ_PARAMETER_KEY, accessKey);

            jsonObject.put(InterfaceInputParameters.REQ_PARAMETER_ENCRYPTED, encrypt);
            jsonObject.put(InterfaceInputParameters.REQ_PARAMETER_SCHEDULE_AT, lScheduleTime);
        }
        else
        {
            final InterfaceParameterLoader interfaceParams = InterfaceParameterLoader.getInstance();
            lMessage        = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.MESSAGE));
            lDest           = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.MOBILE_NUMBER));
            lHeader         = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.SIGNATURE));
            lMsgType        = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.MESSAGE_TYPE));
            lUdhi           = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.UDH_INCLUDE));
            lUdh            = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.UDH));
            lCountryCode    = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.COUNTRY_CODE));
            lDcs            = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.DATA_CODING));
            lSpecialPort    = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.DESTINATION_PORT));
            lMsgExpiry      = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.MESSAGE_EXPIRY));
            lCustRef        = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.CUSTOMER_MSSAGE_ID));
            lTemplateId     = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.TEMPLATE_ID));
            lTemplateValues = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.TEMPLATE_VALUES));
            // lScheduleTime =
            // mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId,
            // InterfaceType.HTTP_JAPI, InterfaceParameter.SCHEDULE_TIME));
            lAppendCountry  = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.APPEND_COUNTRY_CODE));
            lUrlTrack       = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.URL_TRACKING));
            lDltEntityId    = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.DLT_ENTITY_ID));
            lDltTemplateId  = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.DLT_TEMPLATE_ID));
            lDltTMAId  = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.DLT_TMA_ID));

            lMsgtag         = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.MSG_TAG));
            lParam1         = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM1));
            lParam2         = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM2));
            lParam3         = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM3));
            lParam4         = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM4));
            lParam5         = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM5));
            lParam6         = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM6));
            lParam7         = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM7));
            lParam8         = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM8));
            lParam9         = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM9));
            lParam10        = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM10));
            lDlrReq         = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.DLR_REQUIRED));
            lMaxsplit       = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.MAX_SPLIT));
            lUrlShortner    = mHttpRequest.getParameter(interfaceParams.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.URL_SHORTNER));
        }

        if ((lMessage != null) && !lMessage.isBlank())
        {
            lMessage = lMessage.replaceAll("\\r", "\n");

            if (log.isDebugEnabled())
                log.debug("process() - Replce NewLine message ' " + lMessage + "  '");
        }

        final JSONArray  lMessagesList = new JSONArray();
        final JSONObject messageObject = new JSONObject();

        // jsonObject.put(InterfaceInputParameters.REQ_PARAMETER_SCHEDULE_AT,
        // CommonUtility.nullCheck(lScheduleTime, true));

        if (log.isDebugEnabled())
            log.debug("Message Tag Value :" + lMsgtag);

        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_TEMPLATE_VALUES, Utility.splitIntoJsonArray(lTemplateValues, "~"));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_MSG, CommonUtility.nullCheck(lMessage, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_HEADER, CommonUtility.nullCheck(lHeader, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_TYPE, CommonUtility.nullCheck(lMsgType, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_UDHI, CommonUtility.nullCheck(lUdhi, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_UDH, CommonUtility.nullCheck(lUdh, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_APPEND_COUNTRY, CommonUtility.nullCheck(lAppendCountry, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_URL_TRACK, CommonUtility.nullCheck(lUrlTrack, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_DCS, CommonUtility.nullCheck(lDcs, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_PORT, CommonUtility.nullCheck(lSpecialPort, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_MSG_EXPIRY, CommonUtility.nullCheck(lMsgExpiry, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_COUNTRY_CODE, CommonUtility.nullCheck(lCountryCode, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_CUST_REF, CommonUtility.nullCheck(lCustRef, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_TEMPLATE_ID, CommonUtility.nullCheck(lTemplateId, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_DLT_ENTITY_ID, CommonUtility.nullCheck(lDltEntityId, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_DLT_TEMPLATE_ID, CommonUtility.nullCheck(lDltTemplateId, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_DLT_TMA_ID, CommonUtility.nullCheck(lDltTMAId, true));

        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_MSG_TAG, CommonUtility.nullCheck(lMsgtag, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_PARAM1, CommonUtility.nullCheck(lParam1, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_PARAM2, CommonUtility.nullCheck(lParam2, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_PARAM3, CommonUtility.nullCheck(lParam3, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_PARAM4, CommonUtility.nullCheck(lParam4, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_PARAM5, CommonUtility.nullCheck(lParam5, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_PARAM6, CommonUtility.nullCheck(lParam6, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_PARAM7, CommonUtility.nullCheck(lParam7, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_PARAM8, CommonUtility.nullCheck(lParam8, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_PARAM9, CommonUtility.nullCheck(lParam9, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_PARAM10, CommonUtility.nullCheck(lParam10, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_DLR_REQ, CommonUtility.nullCheck(lDlrReq, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_MAX_SPLIT, CommonUtility.nullCheck(lMaxsplit, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_URL_SHORTNER, CommonUtility.nullCheck(lUrlShortner, true));

        
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_EMAIL_TO, CommonUtility.nullCheck(lEmailTo, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_EMAIL_FROM, CommonUtility.nullCheck(lEmailFrom, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_EMAIL_FROM_NAME, CommonUtility.nullCheck(lEmailFromName, true));
        messageObject.put(InterfaceInputParameters.REQ_PARAMETER_EMAIL_SUBJECT, CommonUtility.nullCheck(lEmailSubject, true));

        if ((lDest != null) && !lDest.isBlank())
            messageObject.put(InterfaceInputParameters.REQ_PARAMETER_DEST, Utility.splitIntoJsonArray(lDest, ","));

        lMessagesList.add(messageObject);
        jsonObject.put(InterfaceInputParameters.REQ_PARAMETER_MESSAGES, lMessagesList);
    }

    @Override
    public void setContentLength(
            String aResponse)
    {
        mHttpResponse.setContentLength(aResponse.length());
    }

    @Override
    public void doProcess(
            String aParsedString)
    {
        log.debug("Abstract method..");
    }

    @Override
    public void setContentType()
    {
        mHttpResponse.setContentType(InterfaceInputParameters.RES_CONTENT_TYPE_JSON);
    }

}