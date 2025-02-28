package com.itextos.beacon.commonlib.constants;

public enum RedisKeys
        implements
        ItextosEnum
{

    CLIENTINFO_BY_CLID("clientinfo:clid:"),
    OPT_IN("optin:");

    private String key;

    RedisKeys(
            String aKey)
    {
        key = aKey;
    }

    @Override
    public String getKey()
    {
        return key;
    }

}