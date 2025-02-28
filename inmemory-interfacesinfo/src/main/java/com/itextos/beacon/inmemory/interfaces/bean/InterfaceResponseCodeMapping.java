package com.itextos.beacon.inmemory.interfaces.bean;

public class InterfaceResponseCodeMapping
{

    private final String mItextosStatusCode;
    private final String mClientStatusCode;
    private final String mClientReason;
    private final String mClientStatusInfo;
    private final String mHttpStatus;

    public InterfaceResponseCodeMapping(
            String aItextosStatusCode,
            String aClientStatusCode,
            String aClientReason,
            String aClientStatusInfo,
            String aHttpStatus)
    {
        super();
        mItextosStatusCode = aItextosStatusCode;
        mClientStatusCode  = aClientStatusCode;
        mClientReason      = aClientReason;
        mClientStatusInfo  = aClientStatusInfo;
        mHttpStatus        = aHttpStatus;
    }

    public String getItextosStatusCode()
    {
        return mItextosStatusCode;
    }

    public String getClientStatusCode()
    {
        return mClientStatusCode;
    }

    public String getClientReason()
    {
        return mClientReason;
    }

    public String getClientStatusInfo()
    {
        return mClientStatusInfo;
    }

    public String getHttpStatus()
    {
        return mHttpStatus;
    }

    @Override
    public String toString()
    {
        return "InterfaceResponseCodeMapping [mItextosStatusCode=" + mItextosStatusCode + ", mClientStatusCode=" + mClientStatusCode + ", mClientReason=" + mClientReason + ", mClientStatusInfo="
                + mClientStatusInfo + ", mHttpStatus=" + mHttpStatus + "]";
    }

}