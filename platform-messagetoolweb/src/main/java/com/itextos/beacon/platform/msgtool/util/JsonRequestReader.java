package com.itextos.beacon.platform.msgtool.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.itextos.beacon.commonlib.utility.CommonUtility;

public class JsonRequestReader
{

    private static final Log log = LogFactory.getLog(JsonRequestReader.class);

    private JsonRequestReader()
    {}

    public static String getRequestFromBody(
            HttpServletRequest aRequest,
            String aParamterName)
            throws Exception
    {
        String reqString = aRequest.getParameter(aParamterName);
        reqString = CommonUtility.nullCheck(reqString, true);

        if ("".equals(reqString))
            reqString = getRequestFromBody(aRequest);
        return reqString;
    }

    public static String getRequestFromBody(
            HttpServletRequest aRequest)
            throws Exception
    {

        try (
                BufferedReader br = new BufferedReader(new InputStreamReader(aRequest.getInputStream()));)
        {
            int                 bytesRead  = -1;
            final char[]        charBuffer = new char[1024];

            final StringBuilder sb         = new StringBuilder();
            while ((bytesRead = br.read(charBuffer)) > 0)
                sb.append(charBuffer, 0, bytesRead);

            return sb.toString();
        }
        catch (final Exception e)
        {
            log.error("Exception while reading data from HTTP Request. " + aRequest.getServerName() + ":" + aRequest.getServerPort() + "/" + aRequest.getServletContext() + " Path :'"
                    + aRequest.getServletPath() + "'", e);
            throw e;
        }
    }

    public static JSONObject parseJSON(
            String aJsonString)
            throws ParseException
    {
        return (JSONObject) new JSONParser().parse(aJsonString);
    }

}
