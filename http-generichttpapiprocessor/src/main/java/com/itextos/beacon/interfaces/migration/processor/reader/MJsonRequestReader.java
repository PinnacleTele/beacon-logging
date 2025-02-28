package com.itextos.beacon.interfaces.migration.processor.reader;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceMessage;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceRequestStatus;
import com.itextos.beacon.http.generichttpapi.common.interfaces.IRequestProcessor;
import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.http.generichttpapi.common.utils.InterfaceInputParameters;
import com.itextos.beacon.http.generichttpapi.common.utils.InterfaceMessageClass;
import com.itextos.beacon.http.generichttpapi.common.utils.Utility;
import com.itextos.beacon.http.interfaceutil.MessageSource;
import com.itextos.beacon.http.migration.common.utils.MJsonRequestParameters;
import com.itextos.beacon.interfaces.generichttpapi.processor.request.JSONRequestProcessor;

import io.prometheus.client.Histogram.Timer;

public class MJsonRequestReader
        extends
        MAbsractReader
{

    private static final Log log = LogFactory.getLog(MJsonRequestReader.class);

    private final String     mRequestType;
    private final String     mSource;

    StringBuffer sb=null;
    public MJsonRequestReader(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse,
            String aSource,
            String aRequestType,
            StringBuffer sb)
    {
        super(aSource, aRequest, aResponse);
        mSource      = aSource;
        mRequestType = aRequestType;
        this.sb=sb;
    }

    @Override
    public void doProcess(
            String aJSonString)
    {
        if (log.isDebugEnabled())
            log.debug(" CustIp:  '" + mHttpRequest.getRemoteAddr() + "' request time: '" + System.currentTimeMillis() + " '");
        Timer overAllProcess = null;

        try
        {
            overAllProcess = PrometheusMetrics.apiStartTimer(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_JSON, APIConstants.CLUSTER_INSTANCE, mHttpRequest.getRemoteAddr(), OVERALL);

            final JSONObject lJsonObj  = getParsedJson(aJSonString);
            JSONArray        jsonArray = (JSONArray) lJsonObj.get("smslist");

            if (log.isDebugEnabled())
                log.debug("JSON Array Size is   - " + jsonArray.size());

            jsonArray = replaceRequestDataInputKey(jsonArray);
            if (log.isDebugEnabled())
                log.debug("JSON Array to Process   - " + jsonArray);
            lJsonObj.remove("smslist");
            lJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_MESSAGES, jsonArray);
            if (log.isDebugEnabled())
                log.debug("JSON Array to Process - " + lJsonObj);
            final String lReqJson = lJsonObj.toJSONString();
            if (log.isDebugEnabled())
                log.debug("After parsing JSON String is  - " + lReqJson);
            final IRequestProcessor requestProcessor = new JSONRequestProcessor(lReqJson, mHttpRequest.getRemoteAddr(), System.currentTimeMillis(), mRequestType, MessageSource.GENERIC_JSON,sb);

            requestProcessor.parseBasicInfo(mHttpRequest.getHeader(InterfaceInputParameters.AUTHORIZATION));

            processRequest(requestProcessor, lJsonObj);
        }
        catch (final Exception e)
        {
            log.error("Excception while processig Request JSON .", e);
        }
        finally
        {
            PrometheusMetrics.apiEndTimer(InterfaceType.HTTP_JAPI, APIConstants.CLUSTER_INSTANCE, overAllProcess);
        }
    }

    private static JSONObject getParsedJson(
            String aJSonString)
            throws ParseException
    {
        final JSONParser lJsonParser = new JSONParser();
        final JSONObject lJsonObj    = Utility.parseJSON(aJSonString);
        lJsonObj.put(InterfaceInputParameters.REQ_PARAMETER_KEY, lJsonObj.get(MJsonRequestParameters.REQ_PARAMETER_PASSWORD));
        lJsonObj.remove(MJsonRequestParameters.REQ_PARAMETER_PASSWORD);
        lJsonObj.put("version", "1.0");
        lJsonObj.put("encrypt", "0");
        return lJsonObj;
    }

    private void processRequest(
            IRequestProcessor aRequestProcessor,
            JSONObject lJsonObj)
    {
        InterfaceRequestStatus reqStatus      = aRequestProcessor.validateBasicInfo();

        // if (mRequestType != null)
        // {
        // if (log.isDebugEnabled())
        // log.debug("Need to check the logic here.");
        // reconstructJson(lJsonObj, aRequestProcessor);
        // }

        final int              lMessagesCount = aRequestProcessor.getMessagesCount();

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
            else
                doSyncProcess(aRequestProcessor, reqStatus);
        }
    }

    private void doSyncProcess(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus)
    {

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

                processValidMessages(aRequestProcessor, aReqStatus);
            }
        }
        sendResponse(aRequestProcessor);
    }

    private  void handleNoMessage(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus)
    {
        aReqStatus = new InterfaceRequestStatus(InterfaceStatusCode.INVALID_JSON, "Message Object Missing in Given Request.");
        aRequestProcessor.setRequestStatus(aReqStatus);
    }

    private  void processValidMessages(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus
           )
    {
        final int    lMessagesCount = aRequestProcessor.getMessagesCount();
        final String lMessageId     = aReqStatus.getMessageId();

        if (lMessagesCount == 1)
            processSingleMessage(aRequestProcessor, aReqStatus, lMessageId,sb);
        else
            processMultipleMessage(aRequestProcessor, aReqStatus, lMessageId);
    }

    private  void processMultipleMessage(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus,
            String aMessageId)
    {
        // multiple messages
        if (log.isDebugEnabled())
            log.debug("MultipleMessage:  '" + InterfaceStatusCode.SUCCESS + "'");

        aReqStatus = aRequestProcessor.getMultipleMessages(false);

        if (aReqStatus == null)
        {
            aReqStatus = new InterfaceRequestStatus(InterfaceStatusCode.SUCCESS, "");
            aReqStatus.setMessageId(aMessageId);
        }
        aRequestProcessor.setRequestStatus(aReqStatus);
    }

    private  void processSingleMessage(
            IRequestProcessor aRequestProcessor,
            InterfaceRequestStatus aReqStatus,
            String aMessageId,
            StringBuffer sb)
    {
        final InterfaceMessage lMessageObj = aRequestProcessor.getSingleMessage(sb);

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

    @Override
    public void doProcess(
            JSONObject aJsonObj)
    {

        try
        {
            final String lReqJson = aJsonObj.toJSONString();

            if (log.isDebugEnabled())
                log.debug("After parsing JSON String is  - " + lReqJson);
            final IRequestProcessor requestProcessor = new JSONRequestProcessor(lReqJson, mHttpRequest.getRemoteAddr(), System.currentTimeMillis(), mRequestType, MessageSource.GENERIC_QS,sb);

            requestProcessor.parseBasicInfo(mHttpRequest.getHeader(InterfaceInputParameters.AUTHORIZATION));

            processRequest(requestProcessor, aJsonObj);
        }
        catch (final Exception e)
        {
            log.error("Exception while processing the Request ", e);
        }
    }

    @Override
    public void sendResponse(
            IRequestProcessor aRequestProcessor)
    {

        try
        {
            final String response = aRequestProcessor.generateResponse();
            if (log.isDebugEnabled())
                log.debug(" response to user after sending to redis:  '" + response + "'");

            setContentType();

            mHttpResponse.setStatus(aRequestProcessor.getHttpStatus());
            setContentLength(response);
            final PrintWriter writer = mHttpResponse.getWriter();
            writer.println(response);
            writer.flush();
            writer.close();
        }
        catch (final IOException e)
        {
            log.error("Error while closing the printwriter", e);
        }
    }

    private static JSONArray replaceRequestDataInputKey(
            JSONArray aJsonArray)
    {
        final JSONArray lreplacedJsonArray = new JSONArray();

        try
        {

            for (final Object lElement : aJsonArray)
            {
                final JSONParser parser              = new JSONParser();

                String           lMsgTypeToProcess   = null;
                final JSONObject lMessageArrayObject = (JSONObject) lElement;
                lMessageArrayObject.put(InterfaceInputParameters.REQ_PARAMETER_DEST, lMessageArrayObject.get(MJsonRequestParameters.REQ_PARAMETER_TOLIST));
                lMessageArrayObject.remove(MJsonRequestParameters.REQ_PARAMETER_TOLIST);
                lMessageArrayObject.put(InterfaceInputParameters.REQ_PARAMETER_HEADER, lMessageArrayObject.get(MJsonRequestParameters.REQ_PARAMETER_FROM));
                lMessageArrayObject.remove(MJsonRequestParameters.REQ_PARAMETER_FROM);
                lMessageArrayObject.put(InterfaceInputParameters.REQ_PARAMETER_MSG, lMessageArrayObject.get(MJsonRequestParameters.REQ_PARAMETER_CONTENT));
                lMessageArrayObject.remove(MJsonRequestParameters.REQ_PARAMETER_CONTENT);
                lMessageArrayObject.put(InterfaceInputParameters.REQ_PARAMETER_DLT_ENTITY_ID, lMessageArrayObject.get(MJsonRequestParameters.REQ_PARAMETER_ENTITY_ID));
                lMessageArrayObject.remove(MJsonRequestParameters.REQ_PARAMETER_ENTITY_ID);
                lMessageArrayObject.put(InterfaceInputParameters.REQ_PARAMETER_DLT_TEMPLATE_ID, lMessageArrayObject.get(MJsonRequestParameters.REQ_PARAMETER_TEMPLATE_ID));
                lMessageArrayObject.remove(MJsonRequestParameters.REQ_PARAMETER_TEMPLATE_ID);

                if (lMessageArrayObject.get(MJsonRequestParameters.REQ_PARAMETER_MSGTYPE.toLowerCase()) != null)
                {
                    lMessageArrayObject.put(InterfaceInputParameters.REQ_PARAMETER_TYPE, lMessageArrayObject.get(MJsonRequestParameters.REQ_PARAMETER_MSGTYPE.toLowerCase()));
                    lMessageArrayObject.remove(MJsonRequestParameters.REQ_PARAMETER_MSGTYPE);
                    lMsgTypeToProcess = getMessageType(lMessageArrayObject);
                    lMessageArrayObject.put(InterfaceInputParameters.REQ_PARAMETER_TYPE, lMsgTypeToProcess);
                }
                else
                    if (Utility.isMessageContainsUnicode((String) lMessageArrayObject.get(InterfaceInputParameters.REQ_PARAMETER_MSG)))
                        lMessageArrayObject.put(InterfaceInputParameters.REQ_PARAMETER_TYPE, InterfaceMessageClass.UNICODE.getMessageType());
                    else
                        lMessageArrayObject.put(InterfaceInputParameters.REQ_PARAMETER_TYPE, InterfaceMessageClass.PLAIN.getMessageType());
                lreplacedJsonArray.add(lMessageArrayObject);
                if (log.isDebugEnabled())
                    log.debug("Data Replaced JSON Array is - " + lreplacedJsonArray);
            }
        }
        catch (final Exception e)
        {
            log.error(e);
        }
        return lreplacedJsonArray;
    }

    private static String getMessageType(
            JSONObject aElement)
    {
        String lMsgType = null;

        try
        {

            if (aElement.containsKey(InterfaceInputParameters.REQ_PARAMETER_TYPE))
            {
                final String msgType = (String) aElement.get(InterfaceInputParameters.REQ_PARAMETER_TYPE);
                if (msgType != null)
                    lMsgType = doMsgTypeReplace(msgType);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while get the ", e);
        }
        return lMsgType;
    }

    @Override
    public void setContentType()
    {
        mHttpResponse.setContentType(InterfaceInputParameters.RES_CONTENT_TYPE_JSON);
    }

    private String setHttpStatus(
            String aResponse)
    {

        if (aResponse.contains(Character.toString(Constants.DEFAULT_CONCATENATE_CHAR)))
        {
            final String[] httpRes = aResponse.split(Character.toString(Constants.DEFAULT_CONCATENATE_CHAR));

            if (log.isDebugEnabled())
                log.debug(" Http status to the user for MT response  :  '" + httpRes[0] + "' and http status code '" + httpRes[1] + "'");

            mHttpResponse.setStatus(Integer.parseInt(httpRes[1]));
            return httpRes[0];
        }
        return aResponse;
    }

    @Override
    public void setContentLength(
            String aResponse)
    {
        mHttpResponse.setContentLength(aResponse.length());
    }

}
