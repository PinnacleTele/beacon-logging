package com.itextos.beacon.http.clouddatautil.common;

import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum RequestType
        implements
        ItextosEnum
{

    QS("qs"),
    JSON("json");

    private final String key;

    RequestType(
            String aKey)
    {
        key = aKey;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    private static final Map<String, RequestType> mAllTypes = new HashMap<>();
    static
    {
        final RequestType[] lValues = RequestType.values();

        for (final RequestType ip : lValues)
            mAllTypes.put(ip.key, ip);
    }

    public static RequestType getRequestType(
            String aKey)
    {
        return mAllTypes.get(aKey.toLowerCase());
    }

}
