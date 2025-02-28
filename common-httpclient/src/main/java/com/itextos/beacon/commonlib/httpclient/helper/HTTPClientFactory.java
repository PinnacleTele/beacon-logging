package com.itextos.beacon.commonlib.httpclient.helper;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.httpclient.HTTPRequestUtility;
import com.itextos.beacon.commonlib.httpclient.HttpHeader;
import com.itextos.beacon.commonlib.httpclient.HttpParameter;
import com.itextos.beacon.commonlib.httpclient.HttpResult;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class HTTPClientFactory
{

    private static final Log      log                          = LogFactory.getLog(HTTPClientFactory.class);
    private static final String[] ENABLED_PROTOCOLS            = new String[]
    { "SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2" };
    private static final int      DEFAULT_CONNECTION_IN_POOL   = 20000;
    private static final int      DEFAULT_CONNECTION_PER_URL   = 2000;
    private static final String   DEFAULT_SSL_CERTIFICATE_PATH = System.getProperty("java.home") + "/lib/security/cacerts";
    private static final String   DEFAULT_SSL_FILE_PASS_PHARSE = "changeit";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final HTTPClientFactory INSTANCE = new HTTPClientFactory();

    }

    public static HTTPClientFactory getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private CloseableHttpClient     httpClient        = null;
    private CloseableHttpClient     httpsClient       = null;
    private HttpConnectionManager   connectionManager = null;
    private PropertiesConfiguration configAttributes;
    private String                  configFileName;

    private HTTPClientFactory()
    {
        initialize();
    }

    private synchronized void initialize()
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Initializing the HTTP Client Factory");

            initConfigurations();
            initializeConnectionManager();
        }
        catch (final Exception e)
        {
            log.error("Exception while initializing the HTTP Client factory.", e);
        }
    }

    private void initConfigurations()
    {

        try
        {
            configAttributes = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.HTTP_CONNECTOR_PROPERTIES, false);

            if (log.isDebugEnabled())
            {
                log.debug("Printing the configuration property key and value");
                final Iterator<String> itr = configAttributes.getKeys();

                while (itr.hasNext())
                {
                    final String key = itr.next();
                    log.debug(" key : '" + key + "' value '" + configAttributes.getString(key) + "'");
                }
            }
        }
        catch (final Exception e)
        {
            log.error("All conf to be taken as default because Config properties Supplied is Invalid path:" + configFileName, e);
        }
    }

    private void initializeConnectionManager()
            throws KeyManagementException,
            NoSuchAlgorithmException,
            KeyStoreException
    {
        final SSLContext sslcontext        = createSSLContext();

        final String     propProtocolsList = CommonUtility.nullCheck(configAttributes.getString("ssl_instance"), true);
        final String[]   protocolList      = "".equals(propProtocolsList) ? ENABLED_PROTOCOLS : propProtocolsList.split(",");

        if (log.isDebugEnabled())
            log.debug("Added protocols to client: " + Arrays.asList(protocolList));

        final SSLConnectionSocketFactory        socketFactory         = new SSLConnectionSocketFactory(sslcontext, protocolList, null, createHostNameVerifier());
        final Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", socketFactory).build();

        connectionManager = new HttpConnectionManager(socketFactoryRegistry);

        connectionManager.setMaxTotal(CommonUtility.getInteger(configAttributes.getString("max.connections.per.host"), DEFAULT_CONNECTION_PER_URL));
        connectionManager.setDefaultMaxPerRoute(CommonUtility.getInteger(configAttributes.getString("max.http.connections"), DEFAULT_CONNECTION_IN_POOL));

        // TODO do we need it ?
        RequestConfig.custom().build();

        createHttpClient();
        createHttpsClient();
    }

    private static HostnameVerifier createHostNameVerifier()
    {
        return NoopHostnameVerifier.INSTANCE;
    }

    public boolean isJdkClient()
    {
        return CommonUtility.isEnabled(configAttributes.getString("jdk.http.client.enable", "0"));
    }

    private void createHttpsClient()
    {
        if (log.isDebugEnabled())
            log.debug("Creating HTTPS client");

        try
        {
            final String certificateFilePath = configAttributes.getString("sslpath", DEFAULT_SSL_CERTIFICATE_PATH);
            final String pass                = configAttributes.getString("sslp", DEFAULT_SSL_FILE_PASS_PHARSE);

            if (log.isDebugEnabled())
                log.debug("Final SSL Certificate File Path is : '" + certificateFilePath + "'");

            final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

            try (
                    FileInputStream instream = new FileInputStream(new File(certificateFilePath)))
            {
                trustStore.load(instream, pass.toCharArray());
            }

            // setup(aSslcontext, protocolList);
            httpsClient = HttpClients.custom().setConnectionManager(connectionManager).build();
        }
        catch (final Exception e)
        {
            log.error("Exception in creating https client", e);
        }
    }

    private void setup(
            SSLContext aSslcontext,
            String[] aProtocolList)
    {
        final SSLConnectionSocketFactory               sslsf          = new SSLConnectionSocketFactory(aSslcontext, aProtocolList, null, createHostNameVerifier());
        // final LayeredConnectionSocketFactory lcsf = new
        // SSLConnectionSocketFactory(aSslcontext, aProtocolList, null,
        // createHostNameVerifier());
        final RegistryBuilder<ConnectionSocketFactory> schemeRegistry = RegistryBuilder.create();
        schemeRegistry.register("http", PlainConnectionSocketFactory.getSocketFactory());
        schemeRegistry.register("https", sslsf);
        final PoolingHttpClientConnectionManager poolingmgr = new PoolingHttpClientConnectionManager(schemeRegistry.build());
        poolingmgr.setMaxTotal(CommonUtility.getInteger(configAttributes.getString("max.connections.per.host"), DEFAULT_CONNECTION_PER_URL));
        poolingmgr.setDefaultMaxPerRoute(CommonUtility.getInteger(configAttributes.getString("max.http.connections"), DEFAULT_CONNECTION_IN_POOL));
        httpsClient = HttpClients.custom().setSSLSocketFactory(sslsf).setConnectionManager(connectionManager).build();
    }

    private void createHttpClient()
    {
        if (log.isDebugEnabled())
            log.debug("Creating HTTP client");
        httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
    }

    public CloseableHttpClient getHttpClient()
    {
        return httpClient;
    }

    public CloseableHttpClient getHttpsClient()
    {
        return httpsClient;
    }

    public void closeHttpClients()
    {

        try
        {
            if (httpClient != null)
                httpClient.close();

            if (httpsClient != null)
                httpsClient.close();
        }
        catch (final Exception e)
        {}
    }

    public void destroy()
    {

        try
        {
            closeHttpClients();
        }
        catch (final Exception e)
        {}

        try
        {
            connectionManager.closeIdleConnections(0, TimeUnit.SECONDS);
            connectionManager.shutdown();
        }
        catch (final Exception e)
        {}

        try
        {
            connectionManager.shutdown();
        }
        catch (final Exception e)
        {}
    }

    public PropertiesConfiguration getConfigAttributes()
    {
        return configAttributes;
    }

    private static SSLContext createSSLContext()
            throws KeyManagementException,
            NoSuchAlgorithmException,
            KeyStoreException
    {
        return SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy()
        {

            @Override
            public boolean isTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType)
                    throws java.security.cert.CertificateException
            {
                return true;
            }

        }).build();
    }

    public static void main(
            String[] args)
    {
        final long                          start   = System.currentTimeMillis();
        final HttpHeader<String, String>    map     = new HttpHeader<>();
        final HttpParameter<String, String> content = new HttpParameter<>();

        content.put("Operator", "AIRTEL");
        content.put("Circle", "DELHI");
        content.put("dtime", "2019-03-11 11:05:32");
        content.put("user_mid", "952075985089417");
        content.put("MID", "1043925100035696100");
        content.put("stime", "2019-03-11 11:00:04");

        content.put("DEST", "919818958062");
        content.put("TYPE", "0");
        content.put("SEND", "KITPAY");
        content.put("Reason", "DELIVRD");
        content.put("status", "001");

        map.put("connectionTimeOut", "1000");
        map.put("socketTimeOut", "12000");

        final HttpResult result = HTTPRequestUtility.processGetRequest("https://smshook.kitpay.in/vendor/unicel/V34hfnyjVro5WUVn/smshook", content, map);
        // HTTPResult result =
        // HTTPRequestUtility.doGetRequest("https://smshook.kitpay.in/vendor/unicel/V34hfnyjVro5WUVn/smshook",
        // map, "https://dev.karix.local:9243/URLSample/SampleServlet");

        // if
        // (result.result.contains("org.apache.http.conn.ConnectionPoolTimeoutException"))
        System.out.println(new java.util.Date() + " - ApacheHTTPClient run  : '" + result.isSuccess() + "' TIME TAKEN " + (System.currentTimeMillis() - start));
    }

}