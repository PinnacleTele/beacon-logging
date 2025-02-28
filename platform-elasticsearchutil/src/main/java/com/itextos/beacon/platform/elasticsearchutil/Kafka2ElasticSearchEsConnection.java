package com.itextos.beacon.platform.elasticsearchutil;

import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import com.itextos.beacon.platform.elasticsearchutil.utility.Kafka2ElasticsearchProperties;


class Kafka2ElasticSearchEsConnection
{

    private static final Log    log              = LogFactory.getLog(Kafka2ElasticSearchEsConnection.class);

    private final long          mThreadId;
    private boolean             connectionIsOpen = false;
    private long                lastUsed         = 0;
    private RestHighLevelClient lEsClient        = null;

    public Kafka2ElasticSearchEsConnection(
            long aThreadId)
    {
        mThreadId = aThreadId;
    }

    RestHighLevelClient getConnection()
    {
        if ((lEsClient == null) || !connectionIsOpen)
            lEsClient = getEsClient(mThreadId);

        connectionIsOpen = true;
        updateLastUsed();

        return lEsClient;
    }

    void updateLastUsed()
    {
        lastUsed = System.currentTimeMillis();

        if (log.isDebugEnabled())
            log.debug("Thread Id : '" + mThreadId + "' Last used updated " + lastUsed);
    }

    void checkAndCloseClient()
    {
        boolean canClose = false;

        if (connectionIsOpen)
        {
            if ((lastUsed != 0) && ((System.currentTimeMillis() - lastUsed) > Kafka2ElasticsearchProperties.getInstance().getExpireTime()))
                canClose = true;

            if (log.isDebugEnabled())
                log.debug("Check for close Thread Id : '" + mThreadId + "' connectionIsOpen " + connectionIsOpen + " Cur Time  : " + System.currentTimeMillis() + " Last used : " + lastUsed
                        + " Expire duration " + Kafka2ElasticsearchProperties.getInstance().getExpireTime() + " >> Canclose ? " + canClose);
        }
        else
            if (log.isDebugEnabled())
                log.debug("Thread Id : '" + mThreadId + "' Connection is already closed");

        if (canClose)
            try
            {
                lEsClient.close();
                lastUsed         = 0;
                connectionIsOpen = false;
                log.error("Thread Id : '" + mThreadId + "' Closed unused Elasticsearch connection");
            }
            catch (final IOException e)
            {
                // ignore the exception.
            }
    }

    private static RestHighLevelClient getEsClient(
            long aThreadId)
    {
        final String[]   hosts     = Kafka2ElasticsearchProperties.getInstance().getHostIps();
        final HttpHost[] hostArray = new HttpHost[hosts.length];
        int              index     = 0;

        for (final String tempHost : hosts)
        {
        	StringTokenizer st=new StringTokenizer(tempHost,":");
        	
        	String ip=st.nextToken();
        	int port=Integer.parseInt(st.nextToken());
            final HttpHost host1 = new HttpHost(ip,port, Kafka2ElasticsearchProperties.getInstance().getScheme());
            hostArray[index] = host1;
            index++;
        }

        log.error("Thread Id : '" + aThreadId + "' Creating a Elasticsearch connection with the hosts " + Arrays.asList(hosts));

        final RestClientBuilder builder = RestClient.builder(hostArray).setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                .setConnectTimeout(Kafka2ElasticsearchProperties.getInstance().getConTimeoutMillis()).setSocketTimeout(Kafka2ElasticsearchProperties.getInstance().getReadTimmeoutMillis()));

        return new RestHighLevelClient(builder);
    }

}