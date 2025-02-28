package com.itextos.beacon.commonlib.dnddataloader.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.itextos.beacon.commonlib.dnddataloader.util.DndPropertyProvider;

public class InMemoryDataHolder
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final InMemoryDataHolder INSTANCE = new InMemoryDataHolder();

    }

    public static InMemoryDataHolder getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final BlockingQueue<File>    filesToRead            = new LinkedBlockingQueue<>(DndPropertyProvider.getInstance().getFileReaderThreadCount());
    private final BlockingQueue<DndInfo> dataToAddUpdateInRedis = new LinkedBlockingQueue<>(DndPropertyProvider.getInstance().getInMemoryMaxSize());
    private final BlockingQueue<DndInfo> dataToDeleteInRedis    = new LinkedBlockingQueue<>(DndPropertyProvider.getInstance().getInMemoryMaxSize() / 2);
    private int                          fileReadcompleted      = 0;

    private InMemoryDataHolder()
    {}

    public int getDataToAddUpdate()
    {
        return dataToAddUpdateInRedis.size();
    }

    public int getDataToDelete()
    {
        return dataToDeleteInRedis.size();
    }

    public int getFilesToRead()
    {
        return filesToRead.size();
    }

    public int getFileReadCompleted()
    {
        return fileReadcompleted;
    }

    public void incrementReadCompleted()
    {
        ++fileReadcompleted;
    }

    public void addData(
            DndInfo aDndInfo)
            throws InterruptedException
    {

        switch (aDndInfo.getDndAction())
        {
            case APPEND_OR_UPDATE:
                dataToAddUpdateInRedis.put(aDndInfo);
                CountHolder.getInstance().incrementAddUpdateRequest();
                break;

            case DELETE:
                dataToDeleteInRedis.put(aDndInfo);
                CountHolder.getInstance().incrementDeleteRequest();
                break;

            case INVALID:
                CountHolder.getInstance().incrementInvalidOperationRequest();
                break;

            case INVALID_NUMBER:
                CountHolder.getInstance().incrementInvalidMobileNumberRequest();
                break;

            default:
                break;
        }
    }

    public List<DndInfo> getAddUpdateData(
            int aRedisWriterBatchCount)
    {
        final int           size     = aRedisWriterBatchCount > dataToAddUpdateInRedis.size() ? dataToAddUpdateInRedis.size() : aRedisWriterBatchCount;
        final List<DndInfo> toReturn = new ArrayList<>(size);
        dataToAddUpdateInRedis.drainTo(toReturn, size);
        return toReturn;
    }

    public List<DndInfo> getDeleteData(
            int aRedisWriterBatchCount)
    {
        final int           size     = aRedisWriterBatchCount > dataToDeleteInRedis.size() ? dataToDeleteInRedis.size() : aRedisWriterBatchCount;
        final List<DndInfo> toReturn = new ArrayList<>(size);
        dataToDeleteInRedis.drainTo(toReturn, size);
        return toReturn;
    }

    public void addFilesToRead(
            File aFile)
            throws InterruptedException
    {
        filesToRead.put(aFile);
    }

    public File getNextFileToRead()
    {
        return filesToRead.poll();
    }

}