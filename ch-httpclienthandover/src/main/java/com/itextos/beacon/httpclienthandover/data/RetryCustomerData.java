package com.itextos.beacon.httpclienthandover.data;

import java.util.List;

public class RetryCustomerData
{

    private String       custId;
    private List<String> data;

    public RetryCustomerData(
            String aCustId,
            List<String> aData)
    {
        super();
        custId = aCustId;
        data   = aData;
    }

    public String getCustId()
    {
        return custId;
    }

    public void setCustId(
            String aCustId)
    {
        custId = aCustId;
    }

    public List<String> getData()
    {
        return data;
    }

    public void setData(
            List<String> aData)
    {
        data = aData;
    }

    @Override
    public String toString()
    {
        return "RetryCustomerData [custId=" + custId + ", data=" + data + "]";
    }

}
