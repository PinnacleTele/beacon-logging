package com.itextos.beacon.commonlib.constants;

/**
 * DOMESTIC / INTERNATIONAL
 */
public enum RouteType
        implements
        ItextosEnum
{

    DOMESTIC("0"),
    INTERNATIONAL("1");

    private final String key;

    RouteType(
            String aKey)
    {
        key = aKey;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    public static RouteType getRouteType(
            int aRouteType)
    {
        if (aRouteType == 0)
            return DOMESTIC;
        else
            if (aRouteType == 1)
                return INTERNATIONAL;
        return null;
    }

    public static RouteType getRouteType(
            String aRouteType)
    {
        return getRouteType(Integer.parseInt(aRouteType));
    }

}