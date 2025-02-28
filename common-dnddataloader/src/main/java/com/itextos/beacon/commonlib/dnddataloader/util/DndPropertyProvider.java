package com.itextos.beacon.commonlib.dnddataloader.util;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class DndPropertyProvider
{

    private static final PropertiesConfiguration PROPERTIES          = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.DND_PROPERTIES, true);
    private static final String                  DND_DATA_TABLE_NAME = "dnd_data";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final DndPropertyProvider INSTANCE = new DndPropertyProvider();

    }

    public static DndPropertyProvider getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final String csvSourceFilePath;
    private final String csvDestFilePath;
    private final int    fileReaderThreadCount;
    private final int    redisWriterThreadCount;
    private final int    dbReaderThreadCount;
    private final int    redisWriterBatchCount;
    private final int    inMemoryMaxSize;
    private final int    redisRetryCount;

    private DndPropertyProvider()
    {
        csvSourceFilePath      = CommonUtility.nullCheck(PROPERTIES.getProperty("file.source.path"), true);
        csvDestFilePath        = CommonUtility.nullCheck(PROPERTIES.getProperty("file.destination.path"), true);
        fileReaderThreadCount  = PROPERTIES.getInt("file.reader.thread.count", 5);
        redisWriterThreadCount = PROPERTIES.getInt("redis.writer.thread.count", 50);
        dbReaderThreadCount    = PROPERTIES.getInt("db.reader.thread.count", 50);
        redisWriterBatchCount  = PROPERTIES.getInt("redis.writer.batch.count", 10_000);
        inMemoryMaxSize        = PROPERTIES.getInt("inmemory.queue.max.size", 5_00_000);
        redisRetryCount        = PROPERTIES.getInt("redis.retry.count", 3);
    }

    public String getCsvSourceFilePath()
    {
        return csvSourceFilePath;
    }

    public String getCsvDestinationFilePath()
    {
        return csvDestFilePath;
    }

    public int getFileReaderThreadCount()
    {
        return fileReaderThreadCount;
    }

    public int getRedisWriterThreadCount()
    {
        return redisWriterThreadCount;
    }

    public int getDbReaderThreadCount()
    {
        return dbReaderThreadCount;
    }

    public int getRedisWriterBatchCount()
    {
        return redisWriterBatchCount;
    }

    public int getInMemoryMaxSize()
    {
        return inMemoryMaxSize;
    }

    public int getRedisRetryCount()
    {
        return redisRetryCount;
    }

    public static String getDnDDataTableName()
    {
        return DND_DATA_TABLE_NAME;
    }

}