package com.itextos.beacon.httpclienthandover.data;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum HttpMethod
        implements
        ItextosEnum
{

    GET("0"),
    POST("1"),
    POSTQS("2");

    final String key;

    HttpMethod(
            String aKey)
    {
        key = aKey;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    public static HttpMethod getHttpMethod(
            String aKey)
    {
        if (aKey.equals(POST.getKey()))
            return POST;

        if (aKey.equals(POSTQS.getKey()))
            return POSTQS;

        return GET;
    }

}