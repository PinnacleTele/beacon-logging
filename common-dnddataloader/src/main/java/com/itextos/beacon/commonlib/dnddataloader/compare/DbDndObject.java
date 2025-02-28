package com.itextos.beacon.commonlib.dnddataloader.compare;

class DbDndObject
{

    private final long   dest;
    private final String preferences;

    DbDndObject(
            String aRedisParentKey,
            String aRedisKey,
            String aPreferences)
    {
        dest        = Long.parseLong(aRedisParentKey + aRedisKey);
        preferences = aPreferences;
    }

    long getDest()
    {
        return dest;
    }

    String getPreferences()
    {
        return preferences;
    }

}
