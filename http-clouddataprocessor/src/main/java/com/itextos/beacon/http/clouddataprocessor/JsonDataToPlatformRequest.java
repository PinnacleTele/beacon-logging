package com.itextos.beacon.http.clouddataprocessor;

class JsonDataToPlatformRequest
        implements
        IDataToPlatformRequest
{

    private final String mJsonRequest;

    public JsonDataToPlatformRequest(
            String aJsonRequest)
    {
        super();
        mJsonRequest = aJsonRequest;
    }

    @Override
    public String toString()
    {
        return "JsonDataToPlatformRequest [mJsonRequest=" + mJsonRequest + "]";
    }

    @Override
    public String getJsonString()
    {
        return mJsonRequest;
    }

}