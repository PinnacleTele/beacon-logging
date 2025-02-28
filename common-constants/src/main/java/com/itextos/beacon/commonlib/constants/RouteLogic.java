package com.itextos.beacon.commonlib.constants;

public enum RouteLogic
        implements
        ItextosEnum
{

    DEAULT("17"),
    LOGICID("1"),
    GOVT_LOGIC_ID("21"),
    ACC_FAILLIST_DOMESTIC_LOGICID("-12"),
    GLOBAL_FAILLIST_DOMESTIC_LOGICID("-13"),
    INTL_CLIENT_FAILLIST_LOGICID("-14"),
    INTL_GLOBAL_FAILLIST_LOGICID("-15");

    private String key;

    RouteLogic(
            String aValue)
    {
        key = aValue;
    }

    @Override
    public String getKey()
    {
        return key;
    }

}