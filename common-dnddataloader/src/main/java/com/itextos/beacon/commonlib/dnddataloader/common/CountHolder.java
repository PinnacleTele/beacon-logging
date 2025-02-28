package com.itextos.beacon.commonlib.dnddataloader.common;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.dnddataloader.enums.RedisRecordStatus;

public class CountHolder
{

    private static final Log log = LogFactory.getLog(CountHolder.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final CountHolder INSTANCE = new CountHolder();

    }

    public static CountHolder getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final AtomicInteger         totalReq                      = new AtomicInteger(0);
    private final AtomicInteger         addUpdateReq                  = new AtomicInteger(0);
    private final AtomicInteger         deleteReq                     = new AtomicInteger(0);
    private final AtomicInteger         invalidOperationReq           = new AtomicInteger(0);
    private final AtomicInteger         invalidMobileNumberReq        = new AtomicInteger(0);
    private final AtomicInteger         redisOperationAdd             = new AtomicInteger(0);
    private final AtomicInteger         redisOperationUpdate          = new AtomicInteger(0);
    private final AtomicInteger         redisOperationDelete          = new AtomicInteger(0);
    private final AtomicInteger         redisOperationNotAvailable    = new AtomicInteger(0);

    private final AtomicInteger         compareNotAvailableInRedis    = new AtomicInteger(0);
    private final AtomicInteger         compareNoMismatch             = new AtomicInteger(0);
    private final AtomicInteger         comparePreferencesMismatch    = new AtomicInteger(0);
    private final AtomicInteger         compareNotAvailableInDatabase = new AtomicInteger(0);

    private final Map<String, FileInfo> fileDetails                   = new HashMap<>();

    private CountHolder()
    {}

    public void incrementAddUpdateRequest()
    {
        totalReq.incrementAndGet();
        addUpdateReq.incrementAndGet();
    }

    public void incrementDeleteRequest()
    {
        totalReq.incrementAndGet();
        deleteReq.incrementAndGet();
    }

    public void incrementInvalidMobileNumberRequest()
    {
        totalReq.incrementAndGet();
        invalidMobileNumberReq.incrementAndGet();
    }

    public void incrementInvalidOperationRequest()
    {
        totalReq.incrementAndGet();
        invalidOperationReq.incrementAndGet();
    }

    public void incrementNotAvailableInRedis()
    {
        compareNotAvailableInRedis.incrementAndGet();
    }

    public void incrementNoMismatch()
    {
        compareNoMismatch.incrementAndGet();
    }

    public void incrementNotAvailableinDatabase()
    {
        compareNotAvailableInDatabase.incrementAndGet();
    }

    public void incrementPreferencesMismatch()
    {
        comparePreferencesMismatch.incrementAndGet();
    }

    public void updateRedisResponse(
            Map<RedisRecordStatus, Integer> aUpdateDestPref)
    {

        for (final Entry<RedisRecordStatus, Integer> entry : aUpdateDestPref.entrySet())
        {
            final int count = entry.getValue();

            switch (entry.getKey())
            {
                case ADDED:
                    redisOperationAdd.addAndGet(count);
                    break;

                case DELETED:
                    redisOperationDelete.addAndGet(count);
                    break;

                case NOT_AVAILABLE:
                    redisOperationNotAvailable.addAndGet(count);
                    break;

                case UPDATED:
                    redisOperationUpdate.addAndGet(count);
                    break;

                default:
                    break;
            }
        }
    }

    public void setStartTime(
            File aCsvFile)
    {
        final FileInfo fi = new FileInfo(aCsvFile.getAbsolutePath());
        fi.setStartTime();
        fileDetails.put(aCsvFile.getAbsolutePath(), fi);
    }

    public void setEndTime(
            File aCsvFile,
            boolean aIsSuccess,
            int aRecordsCount)
    {
        final FileInfo lFileInfo = fileDetails.get(aCsvFile.getAbsolutePath());
        lFileInfo.setEndTime();
        lFileInfo.setSuccess(aIsSuccess);
        lFileInfo.setRecordsCount(aRecordsCount);
    }

    public void printFileInfo()
    {

        if (fileDetails.isEmpty())
        {
            log.debug("No files processed.");
            return;
        }

        for (final Entry<String, FileInfo> entry : fileDetails.entrySet())
        {
            final String   fileName = entry.getKey();
            final FileInfo fi       = entry.getValue();
            log.debug("File : '" + fileName + "' Started : " + fi.getStartTime() + " Ended : " + fi.getEndTime() + " Records Count " + fi.getRecordsCount());
        }
    }

    public long getAddUpdateReq()
    {
        return addUpdateReq.longValue();
    }

    public long getDeleteReq()
    {
        return deleteReq.longValue();
    }

    public long getInvalidOperationReq()
    {
        return invalidOperationReq.longValue();
    }

    public long getInvalidMobileNumberReq()
    {
        return invalidMobileNumberReq.longValue();
    }

    public long getRedisOperationAdd()
    {
        return redisOperationAdd.longValue();
    }

    public long getRedisOperationUpdate()
    {
        return redisOperationUpdate.longValue();
    }

    public long getRedisOperationDelete()
    {
        return redisOperationDelete.longValue();
    }

    public long getRedisOperationNotAvailable()
    {
        return redisOperationNotAvailable.longValue();
    }

    public long getCompareNotAvailableInRedis()
    {
        return compareNotAvailableInRedis.longValue();
    }

    public long getCompareNoMismatch()
    {
        return compareNoMismatch.longValue();
    }

    public long getComparePreferencesMismatch()
    {
        return comparePreferencesMismatch.longValue();
    }

    public long getCompareNotAvailableInDatabase()
    {
        return compareNotAvailableInDatabase.longValue();
    }

    public long getTotalReq()
    {
        return totalReq.longValue();
    }

}

class FileInfo
{

    private final String fileName;
    private long         startTime;
    private long         endTime;
    private int          recordsCount;
    private boolean      isSuccess;

    FileInfo(
            String aFileName)
    {
        fileName = aFileName;
    }

    public void setSuccess(
            boolean aIsSuccess)
    {
        isSuccess = aIsSuccess;
    }

    public boolean isSuccess()
    {
        return isSuccess;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime()
    {
        startTime = System.currentTimeMillis();
    }

    public long getEndTime()
    {
        return endTime;
    }

    public void setEndTime()
    {
        endTime = System.currentTimeMillis();
    }

    public int getRecordsCount()
    {
        return recordsCount;
    }

    public void setRecordsCount(
            int aRecordsCount)
    {
        recordsCount = aRecordsCount;
    }

    public String getFileName()
    {
        return fileName;
    }

}