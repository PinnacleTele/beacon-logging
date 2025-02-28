package com.itextos.beacon.commonlib.dnddataloader.compare;

public class RedisDndObject
{

    private final int    redisIndex;
    private final String redisOuterKey;
    private final String redisInnerKey;
    private final String redisPreference;
    private final long   dest;

    public RedisDndObject(
            int aRedisIndex,
            String aRedisOuterKey,
            String aRedisInnerKey,
            String aRedisPreference)
    {
        redisIndex      = aRedisIndex;
        redisOuterKey   = aRedisOuterKey;
        redisInnerKey   = aRedisInnerKey;
        redisPreference = aRedisPreference;
        dest            = Long.parseLong(aRedisOuterKey + aRedisInnerKey);
    }

    public int getRedisIndex()
    {
        return redisIndex;
    }

    public String getRedisOuterKey()
    {
        return redisOuterKey;
    }

    public String getRedisInnerKey()
    {
        return redisInnerKey;
    }

    public String getRedisPreference()
    {
        return redisPreference;
    }

    public long getDest()
    {
        return dest;
    }

}