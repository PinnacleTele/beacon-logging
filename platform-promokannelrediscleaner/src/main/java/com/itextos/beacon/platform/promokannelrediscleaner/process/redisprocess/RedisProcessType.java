package com.itextos.beacon.platform.promokannelrediscleaner.process.redisprocess;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

enum RedisProcessType
        implements
        ItextosEnum
{

    NO_ACTION("0"),
    FLUSH_DB("1"),
    INSERT_ALL_RECORDS("2"),
    INSERT_FOR_SEPECIFIC_CLIENTS("3"),

    ;

    private final String key;

    RedisProcessType(
            String aKey)
    {
        key = aKey;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    private static Map<String, RedisProcessType> allProcess = new ConcurrentHashMap<>();

    static RedisProcessType getRedisProcess(
            String aKey)
    {

        if (allProcess.isEmpty())
        {
            final RedisProcessType[] lValues = RedisProcessType.values();
            for (final RedisProcessType rp : lValues)
                allProcess.put(rp.getKey(), rp);
        }
        return allProcess.get(aKey);
    }

}