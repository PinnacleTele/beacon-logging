package com.itextos.beacon.commonlib.httpclient;

import java.util.Map;

public class HttpResult
{

    private boolean             mSuccess;
    private int                 mStatusCode;
    private String              mResponseString;
    private String              mErrorString;
    private Throwable           mException;
    private Map<String, String> responseHeader;

    public boolean isSuccess()
    {
        return mSuccess;
    }

    public void setSuccess(
            boolean aSuccess)
    {
        mSuccess = aSuccess;
    }

    public int getStatusCode()
    {
        return mStatusCode;
    }

    public void setStatusCode(
            int aStatusCode)
    {
        mStatusCode = aStatusCode;
    }

    public String getResponseString()
    {
        return mResponseString;
    }

    public void setResponseString(
            String aResponseString)
    {
        mResponseString = aResponseString;
    }

    public String getErrorString()
    {
        return mErrorString;
    }

    public void setErrorString(
            String aErrorString)
    {
        mErrorString = aErrorString;
    }

    public Throwable getException()
    {
        return mException;
    }

    public void setException(
            Throwable aException)
    {
        mException = aException;
    }

    public Map<String, String> getResponseHeader()
    {
        return responseHeader;
    }

    public void setResponseHeader(
            Map<String, String> aResponseHeader)
    {
        responseHeader = aResponseHeader;
    }

    @Override
    public String toString()
    {
        return "HttpResult [mSuccess=" + mSuccess + ", mStatusCode=" + mStatusCode + ", mResponseString=" + mResponseString + ", mErrorString=" + mErrorString + ", mException=" + mException
                + ", responseHeader=" + responseHeader + "]";
    }

}