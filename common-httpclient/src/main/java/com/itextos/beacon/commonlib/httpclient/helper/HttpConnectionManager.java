package com.itextos.beacon.commonlib.httpclient.helper;

import java.util.concurrent.TimeUnit;

import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpConnectionManager
        extends
        PoolingHttpClientConnectionManager
{

    public HttpConnectionManager(
            Registry<ConnectionSocketFactory> socketFactoryRegistry)
    {
        super(socketFactoryRegistry);
    }

    @Override
    protected void finalize()
            throws Throwable
    {

        try
        {
            closeIdleConnections(0, TimeUnit.SECONDS);
        }
        catch (final Exception e)
        {}

        try
        {
            closeExpiredConnections();
        }
        catch (final Exception e)
        {}

        try
        {
            shutdown();
        }
        catch (final Exception e)
        {}
        super.finalize();
    }

}