package com.itextos.beacon.commonlib.dnddataloader.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.dnddataloader.common.CountHolder;
import com.itextos.beacon.commonlib.dnddataloader.common.DndInfo;
import com.itextos.beacon.commonlib.dnddataloader.common.InMemoryDataHolder;
import com.itextos.beacon.commonlib.dnddataloader.enums.RedisRecordStatus;
import com.itextos.beacon.commonlib.dnddataloader.util.DndPropertyProvider;

public abstract class DndDataOperation
        implements
        Runnable
{

    private static final Log log         = LogFactory.getLog(DndDataOperation.class);
    private final String     operation;
    private final String     threadName;
    private boolean          canContinue = true;
    private boolean          stopped     = false;

    protected DndDataOperation(
            String aOperation,
            String aThreadName)
    {
        operation  = aOperation;
        threadName = aThreadName;
    }

    @Override
    public void run()
    {
        int noDataCount = 0;

        while (canContinue)
        {
            final List<DndInfo> dataFromInMemory = operation.equals("AddUpdate") ? InMemoryDataHolder.getInstance().getAddUpdateData(DndPropertyProvider.getInstance().getRedisWriterBatchCount())
                    : InMemoryDataHolder.getInstance().getDeleteData(DndPropertyProvider.getInstance().getRedisWriterBatchCount());

            if (dataFromInMemory.isEmpty())
            {
                if (log.isDebugEnabled())
                    log.debug(operation + " : No data available in memory");
                noDataCount++;

                if (noDataCount > 10)
                {
                    canContinue = false;
                    stopped     = true;
                    log.error(operation + " : No Data for last 10 iterations. Stopping reader.");
                }
                else
                    sleepForAWhile(1000);
            }
            else
            {
                noDataCount = 0;
                processData(dataFromInMemory);
            }
        }
    }

    private void processData(
            List<DndInfo> aDataFromInMemory)
    {
        final Map<Integer, List<DndInfo>> operationMapList = new HashMap<>();

        for (final DndInfo dndInfo : aDataFromInMemory)
        {
            final int           redisIndex = dndInfo.getRedisIndex();
            final List<DndInfo> list       = operationMapList.computeIfAbsent(redisIndex, k -> new ArrayList<>());
            list.add(dndInfo);
        }

        final Map<RedisRecordStatus, Integer> lProcessData = callRedisOperation(operationMapList);
        CountHolder.getInstance().updateRedisResponse(lProcessData);
    }

    protected abstract Map<RedisRecordStatus, Integer> callRedisOperation(
            Map<Integer, List<DndInfo>> aOperationMapList);

    private static void sleepForAWhile(
            int aI)
    {

        try
        {
            Thread.sleep(aI);
        }
        catch (final InterruptedException e)
        {}
    }

    public boolean isStopped()
    {
        return stopped;
    }

    public String getThreadName()
    {
        return threadName;
    }

}
