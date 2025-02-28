package com.itextos.beacon.interfaces.generichttpapi.processor.reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.Name;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JSONRequestReader
        extends
        AbstractReader
{

    private static final Log log = LogFactory.getLog(JSONRequestReader.class);

    private final String     mRequestType;
    private final String     mSource;

    StringBuffer sb=null;
    
    public JSONRequestReader(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse,
            String aSource,
            String aRequestType,
            StringBuffer sb)
    {
        super("json", aRequest, aResponse);
        this.sb=sb;
        this.sb.append("JSONRequestReader ").append("\n");
        mSource      = aSource;
        mRequestType = aRequestType;
    }

    @Override
    public void doProcess(
            String aJSonString)
    {
        if (log.isDebugEnabled())
            log.debug(" CustIp:  '" + mHttpRequest.getRemoteAddr() + "' request time: '" + System.currentTimeMillis() + " '");

        Timer jsonTimer      = null;
        Timer overAllProcess = null;

        try
        {
            overAllProcess = PrometheusMetrics.apiStartTimer(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_JSON, APIConstants.CLUSTER_INSTANCE, mHttpRequest.getRemoteAddr(), OVERALL);

            final JSONObject        lJsonObj         = getParsedJson(aJSonString, mRequestType);
            final String            lReqJson         = lJsonObj.toJSONString();

            final IRequestProcessor requestProcessor = new JSONRequestProcessor(lReqJson, mHttpRequest.getRemoteAddr(), System.currentTimeMillis(), MessageSource.GENERIC_JSON,
                    MessageSource.GENERIC_JSON,sb);

            requestProcessor.parseBasicInfo(mHttpRequest.getHeader(InterfaceInputParameters.AUTHORIZATION));

            jsonTimer = processRequest(requestProcessor, lJsonObj,sb);
        }
        catch (final Exception e)
        {
            log.error("Excception while processig Request JSON .", e);
            handleException(aJSonString);
        }
        finally
        {
            PrometheusMetrics.apiEndTimer(InterfaceType.HTTP_JAPI, APIConstants.CLUSTER_INSTANCE, jsonTimer);
            PrometheusMetrics.apiEndTimer(InterfaceType.HTTP_JAPI, APIConstants.CLUSTER_INSTANCE, overAllProcess);
        }
    }

    private void handleException(
            String aJSonString)
    {
        final IRequestProcessor      requestProcessor = new JSONRequestProcessor(aJSonString, mHttpRequest.getRemoteAddr(), System.currentTimeMillis(), MessageSource.GENERIC_JSON,
                MessageSource.GENERIC_JSON,sb);

        final InterfaceRequestStatus status           = new InterfaceRequestStatus(InterfaceStatusCode.INVALID_JSON, "");
        requestProcessor.setRequestStatus(status);

        sendResponse(requestProcessor);
    }

    private Timer processRequest(
            IRequestProcessor aRequestProcessor,
            JSONObject lJsonObj,StringBuffer sb)
    {
        InterfaceRequestStatus reqStatus = aRequestProcessor.validateBasicInfo();

        sb.append("processRequest : " );
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t"); 

        if (mRequestType != null)
        {
            if (log.isDebugEnabled())
                log.debug("Need to check the logic here.");
            reconstructJson(lJsonObj, aRequestProcessor);
        }

        final int lMessagesCount = aRequestProcessor.getMessagesCount();

        if (lMessagesCount == 0)
        {
            if (log.isDebugEnabled())
                log.debug("Requested Message Count '0':  '" + InterfaceStatusCode.INVALID_JSON + "' Message Object Missing in Given Request");

            reqStatus = new InterfaceRequestStatus(InterfaceStatusCode.INVALID_JSON, "Message Object Missing in Given Request");
            aRequestProcessor.setRequestStatus(reqStatus);

            sendResponse(aRequestProcessor);
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("Process the message.");

            if ((aRequestProcessor.getBasicInfo().getClusterType() == ClusterType.OTP) && (lMessagesCount > 1))
            {
                if (log.isDebugEnabled())
                    log.debug("Requested Message Count is greather than  '" + lMessagesCount + "' for cluster '" + ClusterType.OTP + "', Heance rejecting the request.");

                reqStatus = new InterfaceRequestStatus(InterfaceStatusCode.ACCESS_VIOLATION, "OTP account should not allow batch request");
                aRequestProcessor.setRequestStatus(reqStatus);

                sendResponse(aRequestProcessor);
            }
            else {
            /*	
                if (aRequestProcessor.getBasicInfo().isIsAsync())
                     doAsyncProcess(aRequestProcessor, reqStatus,sb);
                else
                    return doSyncProcess(aRequestProcessor, reqStatus,sb);
                  
            	*/
            	
                return doSyncProcess(aRequestProcessor, reqStatus,sb);

            }   
            
            return doSyncProcess(aRequestProcessor, reqStatus,sb); 
            
            

        }
        return null;
    }

    private Timer doSyncProcess(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus,StringBuffer sb)
    {
        Timer jsonTimer = null;

        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(" aReqStatus.getStatusCode() : "+aReqStatus.getStatusCode()).append("\t"); 

        if (aReqStatus.getStatusCode() == InterfaceStatusCode.SUCCESS)
        {
            final int lMessagesCount = aRequestProcessor.getMessagesCount();

            if (log.isDebugEnabled())
                log.debug("Requested Message Count :  '" + lMessagesCount + "'");

            if (lMessagesCount == 0)
            {
                if (log.isDebugEnabled())
                    log.debug("Message Count is '0':  '" + InterfaceStatusCode.INVALID_JSON + "' Message Object Missing in Given Request.");

                handleNoMessage(aRequestProcessor, aReqStatus);
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Processing valid messages");

                jsonTimer = processValidMessages(aRequestProcessor, aReqStatus,sb);
            }
        }
        sendResponse(aRequestProcessor);
        return jsonTimer;
    }

    private static void handleNoMessage(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus)
    {
        aReqStatus = new InterfaceRequestStatus(InterfaceStatusCode.INVALID_JSON, "Message Object Missing in Given Request.");
        aRequestProcessor.setRequestStatus(aReqStatus);
    }

    private Timer processValidMessages(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus,StringBuffer sb)
    {
        final int    lMessagesCount    = aRequestProcessor.getMessagesCount();
        final String lMessageId        = aReqStatus.getMessageId();

        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(" lMessagesCount : "+lMessagesCount).append("\t"); 

        final String lUserName         = getUserName(aRequestProcessor);
        final Timer  userSpecificTimer = PrometheusMetrics.apiStartTimer(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_JSON, APIConstants.CLUSTER_INSTANCE, mHttpRequest.getRemoteAddr(), lUserName);

        for (int i = 0; i < lMessagesCount; i++)
            PrometheusMetrics.apiIncrementAcceptCount(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_JSON, APIConstants.CLUSTER_INSTANCE, mHttpRequest.getRemoteAddr(), lUserName);

        if (lMessagesCount == 1)
            processSingleMessage(aRequestProcessor, aReqStatus, lMessageId,sb);
        else
            processMultipleMessage(aRequestProcessor, aReqStatus, lMessageId,sb);
        return userSpecificTimer;
    }

    private  void processMultipleMessage(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus,
            String aMessageId,StringBuffer sb)
    {
        // multiple messages
        if (log.isDebugEnabled())
            log.debug("MultipleMessage:  '" + InterfaceStatusCode.SUCCESS + "'");

        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t"); 

        aReqStatus = aRequestProcessor.getMultipleMessages(false);

        if (aReqStatus == null)
        {
            aReqStatus = new InterfaceRequestStatus(InterfaceStatusCode.SUCCESS, "");
            aReqStatus.setMessageId(aMessageId);
        }
        aRequestProcessor.setRequestStatus(aReqStatus);
    }

    private void processSingleMessage(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus,
            String aMessageId,
            StringBuffer sb)
    {
        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t"); 

        final InterfaceMessage lMessageObj = aRequestProcessor.getSingleMessage( sb);

        if (log.isDebugEnabled())
            log.debug("Processing Single message " + lMessageObj);

        if (lMessageObj == null)
        {
            aReqStatus = new InterfaceRequestStatus(InterfaceStatusCode.SUCCESS, "");
            aReqStatus.setMessageId(aMessageId);
            aRequestProcessor.setRequestStatus(aReqStatus);
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug(" Single Message:  '" + lMessageObj.getRequestStatus() + "'");

            aReqStatus = lMessageObj.getRequestStatus();

            aReqStatus.setMessageId(aMessageId);
            aRequestProcessor.setRequestStatus(aReqStatus);
            /*
             * if (aReqStatus.getStatusCode() == InterfaceStatusCode.SUCCESS)
             * {
             * aReqStatus.setMessageId(aMessageId);
             * aRequestProcessor.setRequestStatus(aReqStatus);
             * }
             * else
             * aRequestProcessor.setRequestStatus(aReqStatus);
             */
        }
    }
    
    /*

    private void doAsyncProcess(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus,StringBuffer sb)
    {
        if (log.isDebugEnabled())
            log.debug("Request process in asynchronous..");

        sb.append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t"); 

        String lUserName = "";

        if (aReqStatus.getStatusCode() == InterfaceStatusCode.SUCCESS)
        {
            lUserName = getUserName(aRequestProcessor);

            PrometheusMetrics.apiStartTimer(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_JSON, APIConstants.CLUSTER_INSTANCE, mHttpRequest.getRemoteAddr(), lUserName);

            final int    lMessagesCount = aRequestProcessor.getMessagesCount();
            final String lMessageId     = aReqStatus.getMessageId();

            for (int i = 0; i < lMessagesCount; i++)
                PrometheusMetrics.apiIncrementAcceptCount(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_JSON, APIConstants.CLUSTER_INSTANCE, mHttpRequest.getRemoteAddr(), lUserName);

            aReqStatus = new InterfaceRequestStatus(InterfaceStatusCode.SUCCESS, "");
            aRequestProcessor.setRequestStatus(aReqStatus);

            aReqStatus.setMessageId(lMessageId);

            sendResponse(aRequestProcessor);

            aRequestProcessor.pushKafkaTopic(MessageSource.GENERIC_JSON,sb);
        }
        else
            sendResponse(aRequestProcessor);
    }
*/
    private static JSONObject getParsedJson(
            String aJSonString,
            String aRequestType)
            throws ParseException
    {
        final JSONObject lJsonObj = Utility.parseJSON(aJSonString);

        if (aRequestType != null)
        {
            final String lAccessKeyVal = getAccessKeyFromCustomer(lJsonObj);
            if (log.isDebugEnabled())
                log.debug("Customized Access Key :" + lAccessKeyVal);

            if (lAccessKeyVal != null)
                lJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_KEY, lAccessKeyVal);
        }

        final String lReqJson = lJsonObj.toString();

        if (log.isDebugEnabled())
            log.debug("After parsing JSON Req - " + lReqJson);

        return lJsonObj;
    }

    private static String getAccessKeyFromCustomer(
            JSONObject lJsonObj)
    {
        String accessKey = (String) lJsonObj.get(InterfaceInputParameters.REQ_PARAMETER_KEY);

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
                    accessKey = (String) lJsonObj.get(CommonUtility.nullCheck(lKey, true));

                    if (log.isDebugEnabled())
                        log.debug("Customized AccessKey Param '" + lKey + "' Value : '" + accessKey + "'");

                    if (accessKey != null)
                        return accessKey;
                }
            }
        }

        return accessKey;
    }

    private static void reconstructJson(
            JSONObject aJsonObject,
            IRequestProcessor aRequestProcessor)
    {
        if (log.isDebugEnabled())
            log.debug("Custom Request Json : " + aJsonObject.toString());

        final String                   lClientId       = aRequestProcessor.getBasicInfo().getClientId();
        final JSONArray                lMessagesList   = new JSONArray();

        final InterfaceParameterLoader instance        = InterfaceParameterLoader.getInstance();

        // final JSONArray messageArray = (JSONArray)
        // aJsonObject.get(InterfaceInputParameters.REQ_PARAMETER_MESSAGES);

        final String                   lMessageListKey = instance.getParamterKey(lClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.MESSAGES_LIST);

        if (log.isDebugEnabled())
            log.debug("Message List Key : " + lMessageListKey);

        final JSONArray messageArray = Utility.getJSONArray(aJsonObject, lMessageListKey);

        if (log.isDebugEnabled())
            log.debug("Messages Array : " + messageArray);

        for (final Object lElement : messageArray)
        {
            final JSONObject lMessageJson = getMessageJson(lElement, lClientId);
            lMessagesList.add(lMessageJson);
        }

        aJsonObject.put(InterfaceInputParameters.REQ_PARAMETER_MESSAGES, lMessagesList);

        if (log.isDebugEnabled())
            log.debug("Customized Json String :" + aJsonObject.toJSONString());

        aRequestProcessor.setRequestString(aJsonObject.toJSONString());
        aRequestProcessor.resetRequestJson(aJsonObject);
    }

    private static JSONObject getMessageJson(
            Object aElement,
            String aClientId)
    {
        final JSONObject               lJsonMessage   = (JSONObject) aElement;
        final InterfaceParameterLoader instance       = InterfaceParameterLoader.getInstance();

        final String                   lCustomDestKey = instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.MOBILE_NUMBER);

        if (log.isDebugEnabled())
            log.debug("Custom Dest Key : " + lCustomDestKey);

        final String    lMessage        = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.MESSAGE));
        final JSONArray lMobileNumber   = Utility.getJSONArray(lJsonMessage, lCustomDestKey);
        final String    lHeader         = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.SIGNATURE));
        final String    lMessageType    = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.MESSAGE_TYPE));
        final String    lUdhi           = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.UDH_INCLUDE));
        final String    lUdh            = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.UDH));
        final String    lCountryCode    = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.COUNTRY_CODE));
        final String    lDataCoding     = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.DATA_CODING));
        final String    lDestPort       = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.DESTINATION_PORT));
        final String    lMsgExpiry      = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.MESSAGE_EXPIRY));
        final String    lCustomerRefNum = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.CUSTOMER_MSSAGE_ID));
        final String    lTemplateId     = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.TEMPLATE_ID));
        final JSONArray lTemplateValues = Utility.getJSONArray(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.TEMPLATE_VALUES));
        final String    lAppendCountry  = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.APPEND_COUNTRY_CODE));
        final String    lUrlTrack       = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.URL_TRACKING));
        final String    lMessageTag     = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.MSG_TAG));
        final String    lDltEntityId    = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.DLT_ENTITY_ID));
        final String    lDltTemplateId  = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.DLT_TEMPLATE_ID));
        final String    lParam1         = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM1));
        final String    lParam2         = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM2));
        final String    lParam3         = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM3));
        final String    lParam4         = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM4));
        final String    lParam5         = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM5));
        final String    lParam6         = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM6));
        final String    lParam7         = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM7));
        final String    lParam8         = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM8));
        final String    lParam9         = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM9));
        final String    lParam10        = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.PARAM10));
        final String    lDlrReq         = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.DLR_REQUIRED));
        final String    lMaxSplit       = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.MAX_SPLIT));
        final String    lUrlShortner    = Utility.getJSONValue(lJsonMessage, instance.getParamterKey(aClientId, InterfaceType.HTTP_JAPI, InterfaceParameter.URL_SHORTNER));

        if (log.isDebugEnabled())
            log.debug("Mobile Number from Client : " + lMobileNumber);

        final JSONObject lMessageJsonObj = new JSONObject();

        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_TEMPLATE_VALUES, lTemplateValues);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_DEST, lMobileNumber);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_MSG, lMessage);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_HEADER, lHeader);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_TYPE, lMessageType);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_UDHI, lUdhi);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_UDH, lUdh);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_APPEND_COUNTRY, lAppendCountry);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_URL_TRACK, lUrlTrack);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_DCS, lDataCoding);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_PORT, lDestPort);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_MSG_EXPIRY, lMsgExpiry);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_COUNTRY_CODE, lCountryCode);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_CUST_REF, lCustomerRefNum);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_TEMPLATE_ID, lTemplateId);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_MSG_TAG, lMessageTag);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_DLT_ENTITY_ID, lDltEntityId);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_DLT_TEMPLATE_ID, lDltTemplateId);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_PARAM1, lParam1);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_PARAM2, lParam2);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_PARAM3, lParam3);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_PARAM4, lParam4);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_PARAM5, lParam5);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_PARAM6, lParam6);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_PARAM7, lParam7);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_PARAM8, lParam8);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_PARAM9, lParam9);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_PARAM10, lParam10);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_DLR_REQ, lDlrReq);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_MAX_SPLIT, lMaxSplit);
        lMessageJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_URL_SHORTNER, lUrlShortner);

        return lMessageJsonObj;
    }

    @Override
    public void setContentType()
    {
        mHttpResponse.setContentType(InterfaceInputParameters.RES_CONTENT_TYPE_JSON);
    }

    @Override
    public void setContentLength(
            String aResponse)
    {
        mHttpResponse.setContentLength(aResponse.length());
    }

    @Override
    public void doProcess(
            JSONObject aJsonObj)
    {
        // TODO Auto-generated method stub
    }

}