package com.itextos.beacon.commonlib.dnddataloader.redis;

import java.util.List;
import java.util.Map;

import com.itextos.beacon.commonlib.dnddataloader.common.DndInfo;
import com.itextos.beacon.commonlib.dnddataloader.enums.RedisRecordStatus;

public class RedisDataPusher
        extends
        DndDataOperation
{

    public RedisDataPusher(
            String aThreadName)
    {
        super("AddUpdate", aThreadName);
    }

    @Override
    protected Map<RedisRecordStatus, Integer> callRedisOperation(
            Map<Integer, List<DndInfo>> aOperationMapList)
    {
        return RedisOperations.updateDestPref(aOperationMapList);
    }

}