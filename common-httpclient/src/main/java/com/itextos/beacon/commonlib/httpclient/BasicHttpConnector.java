package com.itextos.beacon.commonlib.httpclient;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.httpclient.helper.HttpUtility;

public class BasicHttpConnector
{

    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 2000;
    private static final int DEFAULT_READ_TIMEOUT_MILLIS    = 2000;
    private static final Log log                            = LogFactory.getLog(BasicHttpConnector.class);

    private BasicHttpConnector()
    {}

    public static HttpResult connect(
            String aCompleteUrl)
    {
        return connect(aCompleteUrl, false);
    }

    public static HttpResult connect(
            String aCompleteUrl,
            boolean aReturnResponseErrorString)
    {
        return connect(aCompleteUrl, DEFAULT_CONNECT_TIMEOUT_MILLIS, DEFAULT_READ_TIMEOUT_MILLIS, aReturnResponseErrorString);
    }

    public static HttpResult connect(
            String aCompleteUrl,
            int aConnectTimeout,
            int aReadTimeout)
    {
        return connect(aCompleteUrl, aConnectTimeout, aReadTimeout, false);
    }

    public static HttpResult connect(
            String aCompleteUrl,
            int aConnectTimeout,
            int aReadTimeout,
            boolean aReturnResponseErrorString)
    {
        return connect(aCompleteUrl, null, null, aConnectTimeout, aReadTimeout, aReturnResponseErrorString);
    }

    public static HttpResult connect(
            String aUrl,
            HttpParameter<String, String> aParameterMap)
    {
        return connect(aUrl, aParameterMap, false);
    }

    public static HttpResult connect(
            String aUrl,
            HttpParameter<String, String> aParameterMap,
            boolean aReturnResponseErrorString)
    {
        return connect(aUrl, aParameterMap, DEFAULT_CONNECT_TIMEOUT_MILLIS, DEFAULT_READ_TIMEOUT_MILLIS, aReturnResponseErrorString);
    }

    public static HttpResult connect(
            String aUrl,
            HttpParameter<String, String> aParameterMap,
            int aConnectTimeout,
            int aReadTimeout)
    {
        return connect(aUrl, aParameterMap, aConnectTimeout, aReadTimeout, false);
    }

    public static HttpResult connect(
            String aUrl,
            HttpParameter<String, String> aParameterMap,
            int aConnectTimeout,
            int aReadTimeout,
            boolean aReturnResponseErrorString)
    {
        return connect(aUrl, aParameterMap, null, aConnectTimeout, aReadTimeout, aReturnResponseErrorString);
    }

    public static HttpResult connect(
            String aUrl,
            HttpParameter<String, String> aParameterMap,
            HttpHeader<String, String> aHeaderMap)
    {
        final String completeUrl = HttpUtility.populateParameters(aUrl, aParameterMap);
        return connect(completeUrl, aHeaderMap, DEFAULT_CONNECT_TIMEOUT_MILLIS, DEFAULT_READ_TIMEOUT_MILLIS, false);
    }

    public static HttpResult connect(
            String aUrl,
            HttpParameter<String, String> aParameterMap,
            HttpHeader<String, String> aHeaderMap,
            int aConnectTimeout,
            int aReadTimeout)
    {
        return connect(aUrl, aParameterMap, aHeaderMap, aConnectTimeout, aReadTimeout, false);
    }

    public static HttpResult connect(
            String aUrl,
            HttpParameter<String, String> aParameterMap,
            HttpHeader<String, String> aHeaderMap,
            int aConnectTimeout,
            int aReadTimeout,
            boolean aReturnResponseErrorString)
    {
        final String completeUrl = HttpUtility.populateParameters(aUrl, aParameterMap);
        return connect(completeUrl, aHeaderMap, aConnectTimeout, aReadTimeout, aReturnResponseErrorString);
    }

    public static HttpResult connect(
            String aCompleteUrl,
            HttpHeader<String, String> aHeaderMap)
    {
        return connect(aCompleteUrl, aHeaderMap, DEFAULT_CONNECT_TIMEOUT_MILLIS, DEFAULT_READ_TIMEOUT_MILLIS, false);
    }

    private static HttpResult connect(
            String aCompleteUrl,
            HttpHeader<String, String> aHeaderMap,
            int aConnectTimeout,
            int aReadTimeout,
            boolean aReturnResponseErrorString)
    {
        if (log.isDebugEnabled())
            log.debug("URL to connect : '" + aCompleteUrl + "'");

        final HttpResult  result     = new HttpResult();
        HttpURLConnection connection = null;

        try
        {
            final URL url = new URL(aCompleteUrl);

            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setConnectTimeout(aConnectTimeout);
            connection.setReadTimeout(aReadTimeout);

            HttpUtility.populateHeaders(connection, aHeaderMap);

            final int statusCode = connection.getResponseCode();

            if (log.isDebugEnabled())
                log.debug("Status : '" + statusCode + "', URL [" + aCompleteUrl + "]");

            result.setSuccess(true);
            result.setStatusCode(statusCode);

            if (aReturnResponseErrorString)
            {
                final String responseString = HttpUtility.readContent(connection, true);
                final String errorString    = HttpUtility.readContent(connection, false);
                result.setResponseString(responseString);
                result.setErrorString(errorString);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while connecting to URL '" + aCompleteUrl + "'", e);
            HttpUtility.handleException(e, result, connection);
        }

        if (log.isDebugEnabled())
            log.debug("Result : '" + result + "', URL [" + aCompleteUrl + "]");

        return result;
    }

}