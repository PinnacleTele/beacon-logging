package com.itextos.beacon.inmemory.dlrquery.objects;

public class DlrHeaderParam
{

    private final String mParamName;
    private final String mParamValue;

    public DlrHeaderParam(
            String aParamName,
            String aParamValue)
    {
        super();
        mParamName  = aParamName;
        mParamValue = aParamValue;
    }

    public String getParamName()
    {
        return mParamName;
    }

    public String getParamValue()
    {
        return mParamValue;
    }

    @Override
    public String toString()
    {
        return "DlrHeaderParam [mParamName=" + mParamName + ", mParamValue=" + mParamValue + "]";
    }

}