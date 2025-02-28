package com.itextos.beacon.inmemory.interfaces.bean;

import java.util.HashMap;
import java.util.Map;

public class InterfaceResponse
{

    private final String                                    mClientId;
    private final String                                    mMsgSource;
    private final String                                    mResponseTemplate;
    private final String                                    mResponseContentType;
    private final String                                    mDateTimeFormat;
    private final Map<String, InterfaceResponseCodeMapping> responseCodeMapping = new HashMap<>();

    public InterfaceResponse(
            String aClientId,
            String aMsgSource,
            String aResponseTemplate,
            String aResponseContentType,
            String aDateTimeFormat)
    {
        super();
        mClientId            = aClientId;
        mMsgSource           = aMsgSource;
        mResponseTemplate    = aResponseTemplate;
        mResponseContentType = aResponseContentType;
        mDateTimeFormat      = aDateTimeFormat;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public String getMsgSource()
    {
        return mMsgSource;
    }

    public String getResponseTemplate()
    {
        return mResponseTemplate;
    }

    public String getResponseContentType()
    {
        return mResponseContentType;
    }

    public String getDateTimeFormat()
    {
        return mDateTimeFormat;
    }

    public void addResponseCodeMapping(
            InterfaceResponseCodeMapping aInterfaceResponseCodeMapping)
    {
        if (aInterfaceResponseCodeMapping == null)
            return;

        if (!responseCodeMapping.containsKey(aInterfaceResponseCodeMapping.getItextosStatusCode()))
            responseCodeMapping.put(aInterfaceResponseCodeMapping.getItextosStatusCode(), aInterfaceResponseCodeMapping);
    }

    public InterfaceResponseCodeMapping getResponseCodeMapping(
            String aItextosStatusCode)
    {
        return responseCodeMapping.get(aItextosStatusCode);
    }

}