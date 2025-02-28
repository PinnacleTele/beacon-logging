package com.itextos.beacon.commonlib.httpclient.helper;

public class HttpConstants
{

    protected HttpConstants()
    {}

    public static final int    ERR_CONNECT_TIMEOUT = -101;
    public static final int    ERR_READ_TIMEOUT    = -102;
    public static final int    ERR_GENERIC         = -500;

    public static final String SOCKET_TIMEOUT      = "socket-timeout";
    public static final String CONNECTION_TIMEOUT  = "connection-timeout";

}
