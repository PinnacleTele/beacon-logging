package com.itextos.beacon.http.cloudacceptor.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.ipvalidation.IPValidator;
import com.itextos.beacon.commonlib.messageidentifier.MessageIdentifier;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.http.cloudacceptor.common.logging.FailuerLogger;
import com.itextos.beacon.http.cloudacceptor.common.logging.LogLevel;
import com.itextos.beacon.http.clouddatautil.common.CloudDataConfig;
import com.itextos.beacon.http.clouddatautil.common.CloudUtility;
import com.itextos.beacon.http.clouddatautil.common.Constants;
import com.itextos.beacon.http.clouddatautil.common.RequestType;

public final class CloudAcceptorUtility
{

    private static final Log              log                         = LogFactory.getLog(CloudAcceptorUtility.class);
    private static final SimpleDateFormat sdf                         = new SimpleDateFormat("yyyy MMM dd HH:mm:ss.SSS z");
    private static final SimpleDateFormat sdfRequestTimeFormat        = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final int              STATUS_ACCEPTED             = 200;
    private static final int              STATUS_AUTHENTICATIN_FAILED = 401;
    private static final int              STATUS_INVALID_REQUEST      = 400;

    private static Date                   d                           = new Date();

    public static void processRequestResponse(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
    {
        final RedisRequest redisRequest = new RedisRequest();
        redisRequest.setReceivedTime(System.currentTimeMillis() + "");
        redisRequest.setReqType(RequestType.QS.name());
        final String temp = CommonUtility.nullCheck(aRequest.getQueryString(), true);

        if (log.isDebugEnabled())
            log.debug("Query String from request : '" + temp + "'");

        if ("".equals(temp))
            processRequestResponseUsingBody(aRequest, aResponse, redisRequest);
        else
            processRequestResponseUsingQueryString(aRequest, aResponse, temp, redisRequest);
    }

    private static void processRequestResponseUsingQueryString(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse,
            String aRequestString,
            RedisRequest aRedisRequest)
    {

        try
        {
            final String authenticationKey = CommonUtility.nullCheck(aRequest.getParameter("key"), true);

            processRequest(authenticationKey, aRequest, authenticationKey, aResponse, aRequestString, aRedisRequest);
        }
        catch (final Exception e)
        {
            log.error("Exception while Parsing the request.", e);
        }
    }

    private static void processRequestResponseUsingBody(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse,
            RedisRequest aRedisRequest)
    {

        try
        {
            final String request = getRequestString(aRequest);

            if (log.isDebugEnabled())
                log.debug("ParameterString from Body : '" + request + "'");

            if ("".equals(request))
            {
                log.error("Request String is coming as empty in Query String and in Body");
                sendResponse(aResponse, false, STATUS_INVALID_REQUEST, "Invalid Request");
                return;
            }

            final Map<String, String> reqParamsAsMap = splitRequest(request);

            if (log.isDebugEnabled())
                log.debug("Parameters from the Request : '" + reqParamsAsMap + "'");

            final String authenticationKey = CommonUtility.nullCheck(reqParamsAsMap.get("key"), true);

            processRequest(authenticationKey, aRequest, authenticationKey, aResponse, request, aRedisRequest);
        }
        catch (final Exception e)
        {
            log.error("Exception while Parsing the request.", e);
        }
    }

    public static void processJsonRequest(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
    {

        try
        {
            final RedisRequest redisRequest = new RedisRequest();
            redisRequest.setReceivedTime(System.currentTimeMillis() + "");
            redisRequest.setReqType(RequestType.JSON.name());

            final String     aRequestString    = CloudAcceptorUtility.getRequestString(aRequest);

            final JSONParser parser            = new JSONParser();
            final JSONObject jsonObject        = (JSONObject) parser.parse(aRequestString);
            final String     authenticationKey = CommonUtility.nullCheck(jsonObject.get("key"), true);

            processRequest(authenticationKey, aRequest, authenticationKey, aResponse, aRequestString, redisRequest);
        }
        catch (final Exception e)
        {
            log.error("Exception while Parsing the request.", e);
        }
    }

    private static void processRequest(
            String authenticationKey,
            HttpServletRequest aRequest,
            String aAuthenticationKey,
            HttpServletResponse aResponse,
            String aRequestString,
            RedisRequest aRedisRequest)
    {
        final CloudDataConfig cloudDataConfig = CloudUtility.getCloudDataConfig(aAuthenticationKey);

        if (log.isDebugEnabled())
            log.debug(" Authentication key from the request: '" + aAuthenticationKey + "'");

        final boolean isAuthSuccess = (cloudDataConfig != null);

        if (isAuthSuccess)
        {
            final String  clientIp     = aRequest.getRemoteAddr();
            final boolean ipValidation = IPValidator.getInstance().isValidIP("1", authenticationKey, clientIp, cloudDataConfig.getClientIP());
            if (log.isDebugEnabled())
                log.debug(" IP Validation : '" + ipValidation + "' clientIp : '" + clientIp + "' databaseIP : '" + cloudDataConfig.getClientIP() + "'");

            if (ipValidation)
            {
                aRedisRequest.setClientId(cloudDataConfig.getClientId());
                aRedisRequest.setClientIp(clientIp);
                aRedisRequest.setActualReq(aRequestString);

                boolean responseSent = false;
                if (cloudDataConfig.isWriteResponseFirst())
                    responseSent = sendResponse(aResponse, isAuthSuccess, STATUS_ACCEPTED, aRedisRequest);

                d.setTime(System.currentTimeMillis());

                TemporaryInMemoryCollection.getInstance().add(aRedisRequest.getRedisRequest(), aAuthenticationKey);

                if (!responseSent)
                    sendResponse(aResponse, isAuthSuccess, STATUS_ACCEPTED, aRedisRequest);
            }
            else
                sendResponse(aResponse, ipValidation, STATUS_AUTHENTICATIN_FAILED, "Unauthorized User");
        }
        else
        {
            FailuerLogger.log(CloudAcceptorUtility.class, LogLevel.ERROR, "Authentication failed -[ Key in request - '" + aAuthenticationKey + "']");

            sendResponse(aResponse, isAuthSuccess, STATUS_AUTHENTICATIN_FAILED, "Invalid credentials");
        }
    }

    private static Map<String, String> splitRequest(
            String aRequestString)
    {
        final Map<String, String> map = new HashMap<>();
        if (aRequestString == null)
            return map;
        final String[] tockens = aRequestString.split("&");

        if (tockens != null)
            if (tockens.length > 0)
                for (final String lTocken : tockens)
                    getKeyValues(lTocken, map);
            else
                getKeyValues(aRequestString, map);
        return map;
    }

    private static void getKeyValues(
            String aKeyValueString,
            Map<String, String> map)
    {
        final String[] subtockens = aKeyValueString.split("=");

        try
        {
            final String key   = URLDecoder.decode(CommonUtility.nullCheck(subtockens[0]), StandardCharsets.UTF_8.name());
            final String value = URLDecoder.decode(CommonUtility.nullCheck(subtockens[1]), StandardCharsets.UTF_8.name());
            map.put(key, value);
        }
        catch (final Exception ex)
        {}
    }

    private static String getRequestString(
            HttpServletRequest aRequest)
    {
        BufferedReader     br        = null;
        final StringBuffer sb        = new StringBuffer();
        String             reqString = null;

        try
        {
            br = new BufferedReader(new InputStreamReader(aRequest.getInputStream()));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
                sb.append(sCurrentLine);
            reqString = sb.toString();

            if (log.isDebugEnabled())
                log.debug("ReqString from Body '" + reqString + "'");
        }
        catch (final Exception e)
        {
            log.error("Error reading in the Request.", e);
        }
        finally
        {

            try
            {
                if (br != null)
                    br.close();
            }
            catch (final Exception ex)
            {
                // ignore it
            }
        }
        return reqString;
    }

    private static boolean sendResponse(
            HttpServletResponse aResponse,
            boolean isSuccess,
            int aStatus,
            String aErrorInfo)
    {
        return sendResponse(aResponse, isSuccess, aStatus, aErrorInfo, null);
    }

    private static boolean sendResponse(
            HttpServletResponse aResponse,
            boolean isSuccess,
            int aStatus,
            RedisRequest aRedisRequest)
    {
        return sendResponse(aResponse, isSuccess, aStatus, null, aRedisRequest);
    }

    /**
     * @param aResponse
     * @param isSuccess
     * @param aStatus
     * @param aErrorInfo
     * @param aRedisRequest
     *
     * @return
     */
    private static boolean sendResponse(
            HttpServletResponse aHttpResponse,
            boolean isSuccess,
            int aStatus,
            String aErrorInfo,
            RedisRequest aRedisRequest)
    {

        try
        {
            String reason = aErrorInfo;
            aHttpResponse.setStatus(aStatus);

            if (isSuccess)
            {
                final String fileID = MessageIdentifier.getInstance().getNextId();
                if (aRedisRequest != null)
                    aRedisRequest.setFileId(fileID);

                reason = "Application Accepted";
            }

            final String response = getGeneralJsonResponse(aStatus + "", reason, aRedisRequest);

            aHttpResponse.setContentType(Constants.RES_CONTENT_TYPE_JSON);

            try (
                    PrintWriter writer = aHttpResponse.getWriter())
            {
                writer.println(response);
                writer.flush();
            }

            return true;
        }
        catch (final Exception e)
        {
            log.error("Exception while sending the response.", e);
        }
        return false;
    }

    public static String getGeneralJsonResponse(
            String statusCode,
            String reason,
            RedisRequest aRedisRequest)
    {
        final JSONObject          obj = new JSONObject();
        final Map<String, String> map = new HashMap<>();
        map.put(Constants.RESP_PARAMETER_CODE, statusCode);
        map.put(Constants.RESP_PARAMETER_REASON, reason);

        obj.put(Constants.RESP_PARAMETER_STATUS, map);
        obj.put(Constants.RESP_PARAMETER_REQID, aRedisRequest == null ? "" : aRedisRequest.getFileId());
        obj.put(Constants.RESP_PARAMETER_REQTIME, getResponseDateTimeString());

        return obj.toString();
    }

    private static String getResponseDateTimeString()
    {
        return DateTimeUtility.getFormattedDateTime(System.currentTimeMillis(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    }

}