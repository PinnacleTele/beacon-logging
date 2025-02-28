package com.itextos.beacon.commonlib.httpclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;

import com.itextos.beacon.commonlib.httpclient.helper.HTTPClientFactory;
import com.itextos.beacon.commonlib.httpclient.helper.HttpUtility;

public class HTTPRequestUtility
{

    private static final Log                        log                    = LogFactory.getLog(HTTPRequestUtility.class);
    private static final RequestConfig              DEFAULT_REQUEST_CONFIG = RequestConfig.custom().setSocketTimeout(500).setConnectTimeout(500).build();
    private static final Map<String, RequestConfig> map                    = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(HTTPRequestUtility.class);

    private HTTPRequestUtility()
    {}

    public static synchronized RequestConfig getRequestConfig(
            String aHostPort,
            Map<String, String> aConfig)
    {
        if (aHostPort == null)
            return DEFAULT_REQUEST_CONFIG;

        return map.computeIfAbsent(aHostPort, k -> buildConfig(aHostPort, aConfig));
    }

    private static synchronized RequestConfig buildConfig(
            String aHostPort,
            Map<String, String> aConfigurationMap)
    {
        RequestConfig requestConf = null;

        try
        {
            if (log.isDebugEnabled())
                log.debug("Building configuration for the Host and Port : '" + aHostPort + "'");

            final PropertiesConfiguration configAttributes  = HTTPClientFactory.getInstance().getConfigAttributes();
            final String                  strconnectTimeOut = aConfigurationMap.computeIfAbsent("connectionTimeOut", k -> configAttributes.getString("connection.timeout.millis", "10000"));
            final String                  strsocketTimeOut  = aConfigurationMap.computeIfAbsent("socketTimeOut", k -> configAttributes.getString("read.timeout.millis", "10000"));
            final String                  strauthEnabled    = aConfigurationMap.computeIfAbsent("authEnabled", k -> configAttributes.getString("auth.enabled", "false"));
            final String                  strproxyEnabled   = aConfigurationMap.computeIfAbsent("proxyEnabled", k -> configAttributes.getString("proxy.enabled", "false"));
            final boolean                 proxyEnabled      = Boolean.parseBoolean(strproxyEnabled);

            final Builder                 bl                = RequestConfig.custom();
            bl.setAuthenticationEnabled(Boolean.parseBoolean(strauthEnabled));
            bl.setSocketTimeout(Integer.parseInt(strsocketTimeOut));
            bl.setConnectTimeout(Integer.parseInt(strconnectTimeOut));
            bl.setConnectionRequestTimeout(Integer.parseInt(strconnectTimeOut));

            if (proxyEnabled)
            {
                final String strhost = aConfigurationMap.computeIfAbsent("proxy_host", k -> configAttributes.getString("proxy.host", "localhost"));
                final String strport = aConfigurationMap.computeIfAbsent("proxy_port", k -> configAttributes.getString("proxy.port", "80"));
                bl.setProxy(new HttpHost(strhost, Integer.parseInt(strport)));
            }

            requestConf = bl.build();
        }
        catch (final Exception e)
        {
            log.error("Exception while building request configuration for the Host Port : '" + aHostPort + "'", e);
            requestConf = DEFAULT_REQUEST_CONFIG;
        }

        return requestConf;
    }

    private static CloseableHttpClient getClient(
            String url)
    {
        if (url.toLowerCase().startsWith("https"))
            return HTTPClientFactory.getInstance().getHttpsClient();
        return HTTPClientFactory.getInstance().getHttpClient();
    }

    public static HttpResult doSendGZipPayloadRequest(
            String aBaseURL,
            HttpHeader<String, String> aHeaders,
            String aDataToPost)
    {
        final HttpPost httpPost = new HttpPost(aBaseURL);
        HttpUtility.populateHeaders(httpPost, aHeaders);

        ByteArrayInputStream  inputStream = null;
        CloseableHttpResponse response    = null;
        final HttpResult      result      = null;

        try (
                ByteArrayOutputStream compressedStream = new ByteArrayOutputStream();
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(compressedStream);)
        {
            gzipOutputStream.write(aDataToPost.getBytes(StandardCharsets.UTF_8));

            final byte data[] = compressedStream.toByteArray();
            inputStream = new ByteArrayInputStream(data);

            final InputStreamEntity inputStreamEntity = new InputStreamEntity(inputStream);
            httpPost.setHeader("Content-Encoding", "gzip");
            httpPost.setEntity(inputStreamEntity);
            httpPost.setConfig(getRequestConfig(aBaseURL, aHeaders));

            final HttpContext context = HttpClientContext.create();

            response = getClient(aBaseURL).execute(httpPost, context);

            HttpUtility.processResponse(response, result);
        }
        catch (final Throwable e)
        {
            HttpUtility.handleException(e, result);
        }
        finally
        {
            HttpUtility.closeResource(response);
            HttpUtility.closeResource(inputStream);
        }
        return result;
    }

    public static HttpResult processGetRequest(
            String aBaseURL,
            HttpParameter<String, String> aParametersMap,
            HttpHeader<String, String> aHeaderMap)
    {
        final String completeURL = HttpUtility.populateParameters(aBaseURL, aParametersMap);
        return processGetRequest(aBaseURL, completeURL, aHeaderMap);
    }

    public static HttpResult processGetRequest(
            String aHostPort,
            String aCompleteurl,
            HttpHeader<String, String> aHeaderMap)
    {
        if (log.isDebugEnabled())
            log.debug("Complete URL : '" + aCompleteurl + "'");

        final HttpResult      result   = new HttpResult();
        CloseableHttpResponse response = null;
        final HttpGet         httpGet  = new HttpGet(aCompleteurl);

        try
        {
            HttpUtility.populateHeaders(httpGet, aHeaderMap);

            final HttpContext context = HttpClientContext.create();
            httpGet.setConfig(getRequestConfig(aHostPort, aHeaderMap));

            final CloseableHttpClient client = getClient(aCompleteurl);

            response = client.execute(httpGet, context);
            HttpUtility.processResponse(response, result);
        }
        catch (final Exception e)
        {
            logger.error(e.getMessage());
            HttpUtility.handleException(e, result);
            httpGet.releaseConnection();
        }
        finally
        {
            HttpUtility.closeResource(response);
        }
        return result;
    }

    public static HttpResult doPostRequestQueryString(
            String aHostPort,
            String aCompleteurl,
            HttpHeader<String, String> aHeaderMap)
    {

        if (HTTPClientFactory.getInstance().isJdkClient())
        {
            if (log.isDebugEnabled())
                log.debug("doPostRequestQS  : JDK CLIENT CONNECTION");
            return doPostRequestQSJDK(aCompleteurl, aHeaderMap);
        }

        if (log.isDebugEnabled())
            log.debug("doPostRequestQS  : APACHE HTTP CLIENT CONNECTION");
        return doPostRequestQSApache(aHostPort, aCompleteurl, aHeaderMap);
    }

    public static HttpResult doPostRequestQSJDK(
            String aCompleteurl,
            HttpHeader<String, String> aHeaderMap)
    {
        return BasicHttpConnector.connect(aCompleteurl, aHeaderMap);
    }

    public static HttpResult doPostRequestQSApache(
            String aHostPort,
            String aCompleteurl,
            HttpHeader<String, String> aHeaderMap)
    {
        final HttpPost httpPost = new HttpPost(aCompleteurl);
        HttpUtility.populateHeaders(httpPost, aHeaderMap);

        final HttpResult result = new HttpResult();
        handlePostMethodProcess(aHostPort, aHeaderMap, aCompleteurl, httpPost, result);
        return result;
    }

    public static HttpResult doPostRequest(
            String aBaseurl,
            HttpParameter<String, String> aParameterData,
            HttpHeader<String, String> aHeaderMap)
    {
        final HttpResult result = new HttpResult();

        try
        {
            final HttpPost httpPost = new HttpPost(aBaseurl);
            HttpUtility.populateHeaders(httpPost, aHeaderMap);

            final String                   charset    = aHeaderMap.computeIfAbsent("charset", k -> "ISO-8859-1");
            final List<BasicNameValuePair> formparams = HttpUtility.populateNameValuePair(aParameterData);
            final UrlEncodedFormEntity     entity     = new UrlEncodedFormEntity(formparams, charset);

            entity.setChunked(true);
            httpPost.setEntity(entity);

            handlePostMethodProcess(aBaseurl, aHeaderMap, aBaseurl, httpPost, result);
        }
        catch (final Exception e)
        {
            log.error("Exception while doing the http post call.", e);
        }

        return result;
    }

    public static HttpResult doPostRequest(
            String aBaseurl,
            HttpHeader<String, String> aHeaderMap,
            String aDataToPost)
    {
        final HttpPost httpPost = new HttpPost(aBaseurl);
        HttpUtility.populateHeaders(httpPost, aHeaderMap);
        final String       contentType = aHeaderMap.computeIfAbsent("content_type", k -> "text/xml");
        final String       charset     = aHeaderMap.computeIfAbsent("charset", k -> "ISO-8859-1");
        final StringEntity entity      = new StringEntity(aDataToPost, ContentType.create(contentType, charset));
        httpPost.setEntity(entity);

        final HttpResult result = new HttpResult();
        handlePostMethodProcess(aBaseurl, aHeaderMap, aBaseurl, httpPost, result);
        return result;
    }

    private static void handlePostMethodProcess(
            String aHostPort,
            HttpHeader<String, String> aHeaderMap,
            String aCompleteurl,
            HttpPost httpPost,
            HttpResult result)
    {
        CloseableHttpResponse response = null;

        try
        {
            final HttpContext context = HttpClientContext.create();
            httpPost.setConfig(getRequestConfig(aHostPort, aHeaderMap));
            final CloseableHttpClient client = getClient(aCompleteurl);
            response = client.execute(httpPost, context);
            HttpUtility.processResponse(response, result);
        }
        catch (final Exception e)
        {
            log.error("Exception while doing the http post call.", e);
            HttpUtility.handleException(e, result);
            httpPost.releaseConnection();
        }
        finally
        {
            HttpUtility.closeResource(response);
        }
    }

}