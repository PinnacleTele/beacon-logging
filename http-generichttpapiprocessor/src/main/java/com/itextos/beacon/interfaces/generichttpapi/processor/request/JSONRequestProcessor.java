package com.itextos.beacon.interfaces.generichttpapi.processor.request;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.http.generichttpapi.common.data.BasicInfo;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceMessage;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceRequestStatus;
import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.http.generichttpapi.common.utils.InterfaceInputParameters;
import com.itextos.beacon.http.generichttpapi.common.utils.Utility;
import com.itextos.beacon.http.interfaceutil.MessageSource;
import com.itextos.beacon.interfaces.generichttpapi.processor.handover.MiddlewareHandler;
import com.itextos.beacon.interfaces.generichttpapi.processor.response.GenerateJSONResponse;
import com.itextos.beacon.interfaces.generichttpapi.processor.response.GenerateQueryStringResponse;
import com.itextos.beacon.interfaces.generichttpapi.processor.validate.MessageValidater;

public class JSONRequestProcessor
        extends
        AbstractRequestProcessor
{

    private static final Log log           = LogFactory.getLog(JSONRequestProcessor.class);
    private JSONArray        mMessageArray = null;

    StringBuffer sb=null;
 
    public JSONRequestProcessor(
            String aRequestString,
            String aCustomerIP,
            long aRequestedTime,
            String aReqType,
            String aResponseType,
            StringBuffer sb)
    {
        super(aRequestString, aCustomerIP, aRequestedTime, aReqType, aResponseType);
        
        sb.append(" JSONRequestProcessor : aRequestString : "+aRequestString +"  aResponseType : "+aResponseType).append("\n");

        this.sb=sb;
        if (MessageSource.GENERIC_QS.equals(aResponseType))
            mResponseProcessor = new GenerateQueryStringResponse(aCustomerIP);
        else
            if (MessageSource.GENERIC_JSON.equals(aResponseType))
                mResponseProcessor = new GenerateJSONResponse(aCustomerIP);
            else
                log.error("Invalid Request Type specified. Response Type '" + aResponseType + "'");
    }

    @Override
    public void parseBasicInfo(
            String authorization)
            throws ItextosException
    {

        try
        {
            final JSONObject lJsonObject = Utility.parseJSON(mRequestString);
            if (log.isDebugEnabled())
                log.debug("JSON Object in Parse Basic Info : - " + lJsonObject);
            String lServletName = Utility.getJSONValue(lJsonObject, "servletName");

            if (null == lServletName)
                lServletName = "JsonReceiver";

            mResponseProcessor.setServletContext(lServletName);

            if (!(lJsonObject.containsKey(InterfaceInputParameters.REQ_PARAMETER_KEY)))
            {
                if (log.isDebugEnabled())
                    log.debug("Basic Authorization value  : " + authorization);

                processAuthRequest(authorization, lJsonObject);
            }

            mParsedJson    = lJsonObject;
            mRequestString = mParsedJson.toString();

            parseJSONString();
        }
        catch (final Exception e)
        {
            final String err = "Exception while parsing the JSON. JSONString : '" + mRequestString + "'";
            log.error(err, e);
            throw new ItextosException(err, e);
        }
    }

    private static void processAuthRequest(
            String aAuthorization,
            JSONObject aJsonObject)
    {

        if (aAuthorization != null)
        {
            String[] key = null;

            try
            {
                key = Utility.getAccessKey(aAuthorization);

                if (log.isDebugEnabled())
                    log.debug("Authorization value after decode : " + key);
            }
            catch (final Exception e)
            {
                log.error("Invalid authorization value", e);
            }

            if ((key != null) && (key.length == 2))
            {
                aJsonObject.put(InterfaceInputParameters.REQ_PARAMETER_USERNAME, CommonUtility.nullCheck(key[0]));
                aJsonObject.put(InterfaceInputParameters.REQ_PARAMETER_KEY, CommonUtility.nullCheck(key[1]));
            }
        }
    }

    private void parseJSONString()
    {
        final String lVersion      = CommonUtility.nullCheck(Utility.getJSONValue(mParsedJson, InterfaceInputParameters.REQ_PARAMETER_VERSION), true);
        final String lAccessKey    = CommonUtility.nullCheck(Utility.getJSONValue(mParsedJson, InterfaceInputParameters.REQ_PARAMETER_KEY), true);
        final String lReportingKey = CommonUtility.nullCheck(Utility.getJSONValue(mParsedJson, InterfaceInputParameters.REQ_PARAMETER_REPORTING_KEY), true);
        final String lEncrypt      = CommonUtility.nullCheck(Utility.getJSONValue(mParsedJson, InterfaceInputParameters.REQ_PARAMETER_ENCRYPTED), true);
        final String lScheduleTime = CommonUtility.nullCheck(Utility.getJSONValue(mParsedJson, InterfaceInputParameters.REQ_PARAMETER_SCHEDULE_AT), true);

        mBasicInfo = new BasicInfo(lVersion, lAccessKey, lEncrypt, lScheduleTime, mCustIp, mRequestedTime);
        mBasicInfo.setReportingKey(lReportingKey);

        String servletName = Utility.getJSONValue(mParsedJson, "servletName");

        if (null == servletName)
            servletName = "JsonReceiver";

        mResponseProcessor.setServletContext(servletName);

        if (log.isDebugEnabled())
            log.debug("Basic Info :  '" + mBasicInfo + "'");
    }

    public void processFromQueue(
            JSONObject aParsedjson,
            String aFileId,
            String lClientId)
    {

        try
        {
            mParsedJson = aParsedjson;
            parseJSONString();

            Utility.setAccInfo(mBasicInfo, lClientId);

            final String lUserName = CommonUtility.nullCheck(mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_USER.getName()));

            mResponseProcessor.setUname(lUserName);

            final String lTimeZone     = CommonUtility.nullCheck(mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_TIME_ZONE.getName()));
            final String lScheduleTime = CommonUtility.nullCheck(mBasicInfo.getScheduleTime());

            if (!lScheduleTime.isBlank() && !lTimeZone.isBlank())
                Utility.changeScheduleTimeToGivenOffset(lTimeZone, mBasicInfo);

            mBasicInfo.setFileId(aFileId);
            getMessagesCount();
            getMultipleMessages(true);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            log.error("Error While continuing from queue", e);
            pushKafkaTopic(MessageSource.GENERIC_JSON);
        }
    }

    @Override
    public InterfaceMessage getSingleMessage(StringBuffer sb)
    {
    	
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t"); 

        InterfaceMessage lMessage = null;

        try
        {
            final JSONObject lJsonMessage = (JSONObject) mMessageArray.get(0);

            if (lJsonMessage.containsKey(InterfaceInputParameters.REQ_PARAMETER_DEST))
            {
                final JSONArray lMultipleDests = (JSONArray) lJsonMessage.get(InterfaceInputParameters.REQ_PARAMETER_DEST);

                if (log.isDebugEnabled())
                    log.debug("Destination Array:  '" + lMultipleDests + "'");
                
                sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append("Destination array is :  '" + lMultipleDests.size()); 


                if (lMultipleDests.isEmpty())
                {
         
                    lMessage = handleNoDest(lJsonMessage);
                    lMessage.setRouteType(RouteType.DOMESTIC);
                    sb.append("Destination array is empty:  '" + lMultipleDests.size() + "' status '" + InterfaceStatusCode.DESTINATION_EMPTY + "'").append("\n");
                    send2Mw(lMessage, InterfaceStatusCode.DESTINATION_EMPTY, false,sb);
                }
                else
                    if (lMultipleDests.size() == 1)
                    {
                      
                        lMessage = processSingleMessage(lJsonMessage, lMultipleDests,sb);
                    }
                    else
                    {
                        if (log.isDebugEnabled())
                            log.debug("[One - Many  Single message multiple destination  ");

                        final InterfaceRequestStatus lRequestStatus = getMultipleMessages(false);

                        lMessage = new InterfaceMessage();
                        if (lRequestStatus == null)
                            lMessage.setRequestStatus(new InterfaceRequestStatus(InterfaceStatusCode.SUCCESS, null));
                        else
                            lMessage.setRequestStatus(lRequestStatus);
                    }
            }
            else
            {
                final InterfaceRequestStatus lRequestStatus = new InterfaceRequestStatus(InterfaceStatusCode.DESTINATION_EMPTY, "");

                if (log.isDebugEnabled())
                    log.debug("Dest array is missing  ");

                lMessage = getMessage(lJsonMessage);
                lMessage.setRequestStatus(lRequestStatus);
                lMessage.setRouteType(RouteType.DOMESTIC);

                send2Mw(lMessage, InterfaceStatusCode.DESTINATION_EMPTY, false,sb);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while parsing messages ", e);

            final InterfaceRequestStatus lRequestStatus = new InterfaceRequestStatus(InterfaceStatusCode.INVALID_REQUEST, null);
            lMessage = new InterfaceMessage();
            lMessage.setRequestStatus(lRequestStatus);
        }
        return lMessage;
    }

    private InterfaceMessage processSingleMessage(
            JSONObject lJsonMessage,
            JSONArray lMultipleDests,StringBuffer sb)
            throws Exception
    {
        final InterfaceMessage lMessage = getMessage(lJsonMessage);
        
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("InterfaceMessage : getTelemarketerId() : "+lMessage.getTelemarketerId()).append("\n"); 


        if (log.isDebugEnabled())
            log.debug("parse message:  '" + lMessage + "'");

        final String lDest = CommonUtility.nullCheck(lMultipleDests.get(0));

        if (log.isDebugEnabled())
            log.debug("mobile Number:  '" + lDest + "'");

        final MessageValidater lMessageValidater = new MessageValidater(lMessage, mBasicInfo,sb);

        // lDest = appendCountryCode(lMessage, lDest);

        InterfaceStatusCode    lClientStatus     = lMessageValidater.validate();

        if (log.isDebugEnabled())
            log.debug("message object validation:  '" + lClientStatus + "'");

        if (lClientStatus == InterfaceStatusCode.SUCCESS)
            lClientStatus = lMessageValidater.validateDest(lDest,sb);

        
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append(" lClientStatus : " +lClientStatus); 

        if (lClientStatus != InterfaceStatusCode.SUCCESS)
            lMessage.setRouteType(RouteType.DOMESTIC);

        if (log.isDebugEnabled())
            log.debug("mobile Number validation:  '" + lClientStatus + "'");

        if (lClientStatus == InterfaceStatusCode.SUCCESS)
        {
            final String lScheduleTime = mBasicInfo.getScheduleTime();
            final Date   lToDate       = "".equals(lScheduleTime) ? new Date() : DateTimeUtility.getDateFromString(lScheduleTime, DateTimeFormat.DEFAULT);

            if (log.isDebugEnabled())
                log.debug("Date to check trai blockout:  '" + lToDate + "'");

            lClientStatus = lMessageValidater.validateTraiBlockOut(lToDate);

            if (lClientStatus == InterfaceStatusCode.SUCCESS)
                if (log.isDebugEnabled())
                    log.debug("Single message  " + lMessage + " send to kafka ");
        }

        send2Mw(lMessage, lClientStatus, false,sb);

        final InterfaceRequestStatus lRequestStatus = new InterfaceRequestStatus(lClientStatus, null);

        if (log.isDebugEnabled())
            log.debug("single message status:  '" + lRequestStatus + "'");
        lMessage.setRequestStatus(lRequestStatus);
        return lMessage;
    }

    private  InterfaceMessage handleNoDest(
            JSONObject aJsonMessage)
    {
        final InterfaceRequestStatus lRequestStatus = new InterfaceRequestStatus(InterfaceStatusCode.DESTINATION_EMPTY, null);
        final InterfaceMessage       lMessage       = getMessage(aJsonMessage);

        lMessage.setRequestStatus(lRequestStatus);
        return lMessage;
    }

    @Override
    public InterfaceRequestStatus getMultipleMessages(
            boolean isAsync)

    {
        InterfaceRequestStatus lRequestStatus = null;
        
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t"); 


        try
        {
            InterfaceStatusCode lClientAccessStatus = null;
            final String        lCluster            = (String) mBasicInfo.getUserAccountInfo().get(MiddlewareConstant.MW_PLATFORM_CLUSTER.getName());

            if ((APIConstants.OTP_CLUSTER.equalsIgnoreCase(lCluster)))
            {
                log.error("For OTP cluster, Multiplt message is not applicable.");

                if (isAsync)
                    lClientAccessStatus = InterfaceStatusCode.ACCESS_VIOLATION;
                else
                    return new InterfaceRequestStatus(InterfaceStatusCode.ACCESS_VIOLATION, null);
            }

            for (int msgIndex = 0, arrayLength = mMessageArray.size(); msgIndex < arrayLength; msgIndex++)
            {
                if (log.isDebugEnabled())
                    log.debug("message Array Length:  '" + arrayLength + "'");

                final JSONObject lJsonMessage = (JSONObject) mMessageArray.get(msgIndex);

                if (log.isDebugEnabled())
                    log.debug("jsonMessage:  '" + lJsonMessage + "'");

                final InterfaceMessage lMessage = getMessage(lJsonMessage);

                if (log.isDebugEnabled())
                    log.debug("message Object:  '" + lMessage + "'");

                final MessageValidater    lMessageValidater        = new MessageValidater(lMessage, mBasicInfo,sb);
                final InterfaceStatusCode lMessageValidationStatus = lMessageValidater.validate();
                InterfaceStatusCode       lMiddlewareStatus        = getMiddlewareStatus(lClientAccessStatus, lMessageValidationStatus);

                if (log.isDebugEnabled())
                    log.debug("ClientAccessStatus: '" + lClientAccessStatus + " Message Validation : '" + lMessageValidationStatus + "' MiddlewareStatus : '" + lMiddlewareStatus + "'");

                if (lMiddlewareStatus == InterfaceStatusCode.SUCCESS)
                {
                    if (log.isDebugEnabled())
                        log.debug("Mssage Validation Status : " + lMessageValidationStatus);

                    if (lJsonMessage.containsKey(InterfaceInputParameters.REQ_PARAMETER_DEST))
                    {
                        final JSONArray jsonDestination      = (JSONArray) lJsonMessage.get(InterfaceInputParameters.REQ_PARAMETER_DEST);

                        int             lMobileNumbersLength = 0;
                        if (jsonDestination != null)
                            lMobileNumbersLength = jsonDestination.size();

                        if (lMobileNumbersLength > 0)
                            handleMultipleMobileNumber(lMessage, jsonDestination, lMessageValidater, lMiddlewareStatus, isAsync);
                        else
                        {
                            lMiddlewareStatus = InterfaceStatusCode.DESTINATION_EMPTY;
                            handleNoDestArray(lMessage, lMiddlewareStatus, isAsync);
                        }
                    }
                    else
                    {
                        lMiddlewareStatus = InterfaceStatusCode.DESTINATION_EMPTY;
                        handleNoDestArray(lMessage, lMiddlewareStatus, isAsync);
                    }
                }
                else
                    handleNoDestArray(lMessage, lMiddlewareStatus, isAsync);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception in processing multple message", e);
            lRequestStatus = new InterfaceRequestStatus(InterfaceStatusCode.INTERNAL_SERVER_ERROR, "");
        }
        return lRequestStatus;
    }

    private void handleNoDestArray(
            InterfaceMessage aMessage,
            InterfaceStatusCode aMiddlewareStaus,
            boolean aIsAsync)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("dest array is missing:  ");

        aMessage.setRouteType(RouteType.DOMESTIC);
        aMessage.setMobileNumber(APIConstants.DEFAULT_DEST);

        send2Mw(aMessage, aMiddlewareStaus, aIsAsync,sb);
    }

    private void handleMultipleMobileNumber(
            InterfaceMessage aMessage,
            JSONArray aJsonDestination,
            MessageValidater aMessageValidater,
            InterfaceStatusCode aMiddlewareStaus,
            boolean aIsAsync)
            throws Exception
    {
        final String lScheduleTime = mBasicInfo.getScheduleTime();
        final Date   lToDate       = "".equals(lScheduleTime) ? new Date() : DateTimeUtility.getDateFromString(lScheduleTime, DateTimeFormat.DEFAULT);

        if (log.isDebugEnabled())
            log.debug("Date to check trai blockout:  '" + lToDate + "'");

        for (final Object lElement : aJsonDestination)
        {
            InterfaceStatusCode destStatus = InterfaceStatusCode.SUCCESS;
            String              lDest      = null;

            try
            {
                lDest = CommonUtility.nullCheck(lElement);
            }
            catch (final Exception e)
            {
                lDest = APIConstants.DEFAULT_DEST;
                log.error("Dest validation failed for " + lElement, e);
                sb.append("Dest validation failed for " + lElement+" error : "+ErrorMessage.getStackTraceAsString(e));
                destStatus = InterfaceStatusCode.DESTINATION_INVALID;
            }

            if (log.isDebugEnabled())
                log.debug("Mobile number:  '" + lDest + "'");

            if ((destStatus == InterfaceStatusCode.SUCCESS))
            {
                // lDest = appendCountryCode(aMessage, lDest);

                destStatus = aMessageValidater.validateDest(lDest,sb);

                if (log.isDebugEnabled())
                    log.debug("Mobile number validation status:  '" + destStatus + "'");

                if (destStatus == InterfaceStatusCode.SUCCESS)
                {
                    destStatus = aMessageValidater.validateTraiBlockOut(lToDate);

                    if (log.isDebugEnabled())
                        log.debug("Validate trai blockout status:  '" + destStatus + "'");
                }
                else
                {
                    // Mobile Validation fail case setting RouteType is Domestic.
                    aMessage.setRouteType(RouteType.DOMESTIC);
                    aMessage.setMobileNumber(lDest);
                }
            }
            else
            {
                // Mobile Validation fail case setting RouteType is Domestic.
                aMessage.setRouteType(RouteType.DOMESTIC);
                aMessage.setMobileNumber(lDest);
            }

            if (log.isDebugEnabled())
                log.debug("Multiple message " + aMessage);

            Utility.setMessageId(aMessage);

            final MiddlewareHandler middlewareHandler = new MiddlewareHandler(aMessage, mBasicInfo, aMiddlewareStaus, destStatus);
            middlewareHandler.middleWareHandover(aIsAsync, mResponseProcessor, mReqType,sb);
        }
    }

    private  InterfaceMessage getMessage(
            JSONObject aMessageJson)
    {
        final InterfaceMessage messageBean = new InterfaceMessage();

        sb.append(" JSONRequestProcessor :  aMessageJson.toJSONString() : "+aMessageJson.toJSONString()).append("\n");

        messageBean.setMessage(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_MSG));
        messageBean.setHeader(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_HEADER));
        messageBean.setMsgType(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_TYPE));

        messageBean.setAppendCountry(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_APPEND_COUNTRY));
        messageBean.setUrlTrack(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_URL_TRACK));
        messageBean.setDcs(CommonUtility.getInteger(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_DCS), Constants.DEFAULT_ENTRY));
        messageBean.setDestinationPort(CommonUtility.getInteger(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_PORT)));
        messageBean.setExpiry(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_MSG_EXPIRY));
        messageBean.setCountryCode(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_COUNTRY_CODE));
        messageBean.setCustRef(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_CUST_REF));
        messageBean.setTemplateId(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_TEMPLATE_ID));
        messageBean.setSplitMax(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_MAX_SPLIT));
        messageBean.setUrlShortner(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_URL_SHORTNER));

        if (log.isDebugEnabled())
            log.debug("template id " + messageBean.getTemplateId());

        if (messageBean.getTemplateId() != null)
            setTemplateMessageValues(aMessageJson, messageBean);

        messageBean.setMsgTag(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_MSG_TAG));
        messageBean.setParam1(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_PARAM1));
        messageBean.setParam2(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_PARAM2));
        messageBean.setParam3(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_PARAM3));
        messageBean.setParam4(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_PARAM4));
        messageBean.setParam5(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_PARAM5));
        messageBean.setParam6(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_PARAM6));
        messageBean.setParam7(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_PARAM7));
        messageBean.setParam8(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_PARAM8));
        messageBean.setParam9(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_PARAM9));
        messageBean.setParam10(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_PARAM10));
        messageBean.setDltEntityId(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_DLT_ENTITY_ID));
        messageBean.setDltTemplateId(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_DLT_TEMPLATE_ID));
        messageBean.setDltTelemarketerId(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_DLT_TMA_ID));
        messageBean.setTelemarketerId(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_DLT_TMA_ID));

        messageBean.setDlrReq(CommonUtility.nullCheck(Utility.getJSONValue(aMessageJson, InterfaceInputParameters.REQ_PARAMETER_DLR_REQ)));

        return messageBean;
    }

    private static void setTemplateMessageValues(
            JSONObject aJsonMessage,
            InterfaceMessage aMessageBean)
    {
        JSONArray lTemplateValues = null;

        try
        {
            lTemplateValues = (JSONArray) aJsonMessage.get(InterfaceInputParameters.REQ_PARAMETER_TEMPLATE_VALUES);

            final int lIntReqTemplateParamSize = lTemplateValues.size();

            if (log.isDebugEnabled())
                log.debug("Template Values size : " + lIntReqTemplateParamSize);

            final int      lIntTemplateMaxParamSize = Utility.getConfigParamsValueAsInt(ConfigParamConstants.SMS_TEMPLATE_MAX_PARAMS);

            final String[] values                   = new String[lIntTemplateMaxParamSize];

            if ((lTemplateValues != null) && (lIntReqTemplateParamSize > 0))
            {
                for (int i = 0; i < lIntTemplateMaxParamSize; i++)
                    if (i >= lIntReqTemplateParamSize)
                        values[i] = "";
                    else
                        values[i] = (String) lTemplateValues.get(i);
                aMessageBean.setTemplateValues(values);
            }
        }
        catch (final Exception e)
        {
            log.info("template value is missing");
        }
    }

    @Override
    public String generateResponse()
    {
        return mResponseProcessor.generateResponse();
    }

    @Override
    public int getHttpStatus()
    {
        return mResponseProcessor.getHttpStatus();
    }

    @Override
    public int getMessagesCount()
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("getMessagesCount() - JSON : " + mParsedJson);

            mMessageArray = (JSONArray) mParsedJson.get(InterfaceInputParameters.REQ_PARAMETER_MESSAGES);

            if (mMessageArray != null)
                return mMessageArray.size();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            log.error("Message object is missing");
        }

        return 0;
    }

    @Override
    public int getNumbersCount(
            int aIndex)
    {

        try
        {
            final JSONObject jsonMessage = (JSONObject) mMessageArray.get(aIndex);

            if (jsonMessage.containsKey(InterfaceInputParameters.REQ_PARAMETER_DEST))
            {
                final JSONArray lJsonDesrArr = (JSONArray) jsonMessage.get(InterfaceInputParameters.REQ_PARAMETER_DEST);

                return lJsonDesrArr.size();
            }
        }
        catch (final Exception e)
        {
            log.debug("Ignore the exception..", e);
        }

        return 0;
    }

    public String getRequestString()
    {
        return mRequestString;
    }

    @Override
    public void resetRequestJson(
            JSONObject aJsonString)
    {
        mParsedJson = aJsonString;
    }

    private void send2Mw(
            InterfaceMessage aMessage,
            InterfaceStatusCode aMiddlewareStaus,
            boolean aIsAsync,
            StringBuffer sb)
            throws Exception
    {
        Utility.setMessageId(aMessage);
        
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append(" telemarketerid : InterfaceMessage "+aMessage.getTelemarketerId()).append("\t"); 

        
        final MiddlewareHandler middlewareHandler = new MiddlewareHandler(aMessage, mBasicInfo, aMiddlewareStaus, InterfaceStatusCode.SUCCESS);
        middlewareHandler.middleWareHandover(aIsAsync, mResponseProcessor, mReqType,sb);
    }

	

}