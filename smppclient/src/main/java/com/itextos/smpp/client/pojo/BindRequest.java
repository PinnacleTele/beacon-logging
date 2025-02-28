package com.itextos.smpp.client.pojo;

public class BindRequest
{

    private int    mBindType;
    private String mHost;
    private int    mPort;
    private String mSystemId;
    private String mPassword;
    private int    mWindowSize;

    public int getBindType()
    {
        return mBindType;
    }

    public void setBindType(
            int aMBindType)
    {
        mBindType = aMBindType;
    }

    public String getHost()
    {
        return mHost;
    }

    public void setHost(
            String aMHost)
    {
        mHost = aMHost;
    }

    public int getPort()
    {
        return mPort;
    }

    public void setPort(
            int aMPort)
    {
        mPort = aMPort;
    }

    public String getSystemId()
    {
        return mSystemId;
    }

    public void setSystemId(
            String aMSystemId)
    {
        mSystemId = aMSystemId;
    }

    public String getPassword()
    {
        return mPassword;
    }

    public void setPassword(
            String aMPassword)
    {
        mPassword = aMPassword;
    }

    public int getWindowSize()
    {
        return mWindowSize;
    }

    public void setWindowSize(
            int aMWindowSize)
    {
        mWindowSize = aMWindowSize;
    }

    @Override
    public String toString()
    {
        return "BindRequest [mBindType=" + mBindType + ", mHost=" + mHost + ", mPort=" + mPort + ", mSystemId=" + mSystemId + ", mPassword=" + mPassword + ", mWindowSize=" + mWindowSize + "]";
    }

}
