package com.itextos.beacon.commonlib.dnddataloader.redis;

import java.util.List;
import java.util.Map;

import com.itextos.beacon.commonlib.dnddataloader.common.DndInfo;
import com.itextos.beacon.commonlib.dnddataloader.enums.RedisRecordStatus;

public class RedisDataDeleter
        extends
        DndDataOperation
{

    public RedisDataDeleter(
            String aThreadName)
    {
        super("Delete", aThreadName);
    }

    @Override
    protected Map<RedisRecordStatus, Integer> callRedisOperation(
            Map<Integer, List<DndInfo>> aOperationMapList)
    {
        return RedisOperations.deletePref(aOperationMapList);
    }

}