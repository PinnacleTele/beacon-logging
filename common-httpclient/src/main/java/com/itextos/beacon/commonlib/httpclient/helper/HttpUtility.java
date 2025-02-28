package com.itextos.beacon.commonlib.httpclient.helper;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.httpclient.HttpHeader;
import com.itextos.beacon.commonlib.httpclient.HttpParameter;
import com.itextos.beacon.commonlib.httpclient.HttpResult;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class HttpUtility
        extends
        HttpConstants
{

    private static final Log log = LogFactory.getLog(HttpUtility.class);

    private HttpUtility()
    {
        super();
    }

    public static String populateParameters(
            String aUrl,
            HttpParameter<String, String> aParameterMap)
    {
        if ((aParameterMap == null) || aParameterMap.isEmpty())
            return aUrl;

        final StringBuilder sb = new StringBuilder(aUrl);

        if (!aUrl.endsWith("?"))
            sb.append("?");

        for (final Entry<String, String> entry : aParameterMap.entrySet())
            if (entry.getValue() != null)
                sb.append(CommonUtility.encode(entry.getKey().trim())).append("=").append(CommonUtility.encode(entry.getValue())).append("&");

        String returnValue = sb.toString();
        if (returnValue.endsWith("&"))
            returnValue = returnValue.substring(0, returnValue.length() - 1);
        return returnValue;
    }

    public static void populateHeaders(
            HttpRequestBase aRequestBase,
            HttpHeader<String, String> aHeaderMap)
    {
        if (aHeaderMap != null)
            for (final Entry<String, String> entry : aHeaderMap.entrySet())
                if (entry.getValue() != null)
                    aRequestBase.addHeader(entry.getKey(), entry.getValue());
    }

    public static void populateHeaders(
            HttpURLConnection aCon,
            HttpHeader<String, String> aHeaderMap)
    {
        if (aHeaderMap != null)
            for (final Entry<String, String> entry : aHeaderMap.entrySet())
                if (entry.getValue() != null)
                    aCon.setRequestProperty(entry.getKey(), entry.getValue());
    }

    public static String readContent(
            HttpURLConnection aConnection,
            boolean aResponseRead)
    {
        if (aConnection != null)
            try (
                    final InputStream lInputStream = aResponseRead ? aConnection.getInputStream() : aConnection.getErrorStream();
                    final BufferedReader br = new BufferedReader(new InputStreamReader(lInputStream));)
            {
                return br.lines().parallel().collect(Collectors.joining(Constants.PLATFORM_NEW_LINE_CHAR));
            }
            catch (final Exception e)
            {
                // ignore
            }
        return null;
    }

    public static List<BasicNameValuePair> populateNameValuePair(
            HttpParameter<String, String> aParameterData)
    {
        final List<BasicNameValuePair> formparams = new ArrayList<>();

        if (aParameterData != null)
            for (final Entry<String, String> entry : aParameterData.entrySet())
                if (entry.getValue() != null)
                    formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        return formparams;
    }

    public static void handleException(
            Throwable aException,
            HttpResult aResult)
    {
        handleException(aException, aResult, null);
    }

    public static void handleException(
            Throwable aException,
            HttpResult aResult,
            HttpURLConnection aConnection)
    {
        if ((aResult == null) || (aException == null))
            return;

        log.error("Some Exception while doing the http process.", aException);

        aResult.setSuccess(false);
        aResult.setException(aException);
        aResult.setErrorString(aException.getMessage());

        if (aConnection != null)
        {
            final String errorString = CommonUtility.nullCheck(readContent(aConnection, false), true);
            if (errorString.isEmpty())
                aResult.setErrorString(errorString);
        }

        if (aException instanceof SocketTimeoutException)
            aResult.setStatusCode(ERR_READ_TIMEOUT);
        else
            if (aException instanceof ConnectTimeoutException)
                aResult.setStatusCode(ERR_CONNECT_TIMEOUT);
            else
                aResult.setStatusCode(ERR_GENERIC);
    }

    public static void processResponse(
            CloseableHttpResponse aResponse,
            HttpResult aResult)
            throws ItextosRuntimeException
    {
        final Header[] ht = aResponse.getAllHeaders();

        if (ht != null)
        {
            final HashMap<String, String> resheader = new HashMap<>();
            for (final Header lElement : ht)
                resheader.put(lElement.getName(), lElement.getValue());
            aResult.setResponseHeader(resheader);
        }
        aResult.setSuccess(true);
        aResult.setStatusCode(aResponse.getStatusLine().getStatusCode());

        final HttpEntity responseEntity = aResponse.getEntity();

        try
        {
            aResult.setResponseString(EntityUtils.toString(responseEntity));
        }
        catch (
                ParseException
                | IOException e)
        {
            throw new ItextosRuntimeException("Exception while getting the response.", e);
        }
        finally
        {
            EntityUtils.consumeQuietly(responseEntity);
        }
    }

    public static void closeResource(
            Closeable acloseable)
    {

        try
        {
            if (acloseable != null)
                acloseable.close();
        }
        catch (final Exception e)
        {
            // ignore
        }
    }

}