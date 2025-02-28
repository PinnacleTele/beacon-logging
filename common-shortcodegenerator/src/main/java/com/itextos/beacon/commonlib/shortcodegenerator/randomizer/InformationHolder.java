package com.itextos.beacon.commonlib.shortcodegenerator.randomizer;

import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itextos.beacon.commonlib.shortcodegenerator.randomizer.util.PropertyReader;

public class InformationHolder
{

    private static final Logger log = LogManager.getLogger(InformationHolder.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final InformationHolder INSTANCE = new InformationHolder();

    }

    public static InformationHolder getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final BlockingQueue<Path>   mFileNames          = new LinkedBlockingQueue<>(PropertyReader.getInstance().getNoOfFileReader() * 2);
    private final BlockingQueue<String> mReadingData        = new LinkedBlockingQueue<>(PropertyReader.getInstance().getMaxInMemorySize());
    private final BlockingQueue<String> mWritingData        = new LinkedBlockingQueue<>(PropertyReader.getInstance().getMaxInMemorySize() * 2);
    private int                         mWriteFileIndex     = 0;
    private int                         mReadFileCount      = 0;
    private final Map<String, Integer>  mReadFileRecordMap  = new HashMap<>();
    private final Map<String, Integer>  mWriteFileRecordMap = new HashMap<>();
    private int                         mTotalFiles         = 0;

    private InformationHolder()
    {}

    public void setTotalFiles(
            int aFilesCount)
    {
        mTotalFiles = aFilesCount;
    }

    public void addFilesToProcess(
            Path aFilePath)
    {

        try
        {
            mFileNames.put(aFilePath);
        }
        catch (final Exception e)
        {
            log.error("Problem while adding the filename to the list.", e);
        }
    }

    public int getFilesWaitingFoRead()
    {
        return mFileNames.size();
    }

    public Path getNextFileToProcess()
    {
        final Path lPoll = mFileNames.poll();
        if (lPoll != null)
            mReadFileCount++;
        return lPoll;
    }

    public void updateReadInfo(
            String aFileName,
            int aRecordsCount)
    {
        // log.debug("Completed reading file " + aFileName + " Records count " +
        // aRecordsCount);

        final Integer lPut = mReadFileRecordMap.put(aFileName, aRecordsCount);

        if (lPut != null)
            log.error("File Reading :: Something is fishy. File '" + aFileName + "' already has some records here '" + lPut + "' New Records count '" + aRecordsCount + "'");

        if ((mReadFileCount % 50) == 0)
        {
            log.debug("Total files read count : '" + mReadFileCount + "'. Will take some rest for 10 seconds.");
            sleepForWhile(10 * 1000L);
        }
    }

    private static void sleepForWhile(
            long aL)
    {

        try
        {
            Thread.sleep(aL);
        }
        catch (final InterruptedException e)
        {}
    }

    public void updateWriteInfo(
            String aFileName,
            int aRecordsCount)
    {
        final Integer lPut = mWriteFileRecordMap.put(aFileName, aRecordsCount);

        if (lPut != null)
            log.error("File Writing :: Something is fishy. File '" + aFileName + "' already has some records here '" + lPut + "' New Records count '" + aRecordsCount + "'");

        if ((mWriteFileIndex % 50) == 0)
        {
            log.debug("Total files write count : '" + mWriteFileIndex + "'. Will take some rest for 5 seconds.");
            sleepForWhile(5 * 1000L);
        }
    }

    public int getReadFileCount()
    {
        return mReadFileCount;
    }

    public int getTotalFileCount()
    {
        return mTotalFiles;
    }

    public void addReadData(
            String aString)
    {

        try
        {
            mReadingData.put(aString);
        }
        catch (final InterruptedException e)
        {
            log.error("Exception while adding data into the inmemory queue.", e);
        }
    }

    public void addWriteData(
            List<String> aDataFromRedis)
    {

        try
        {
            final int addDataSize = aDataFromRedis.size();
            int       count       = 0;

            while ((mWritingData.size() + addDataSize) > PropertyReader.getInstance().getMaxInMemorySize())
            {
                count++;
                log.debug("Waiting for the write data in memory to reduce. current size " + mWritingData.size() + " Iteration " + count);

                try
                {
                    Thread.sleep(1000);
                }
                catch (final Exception e)
                {}
            }

            mWritingData.addAll(aDataFromRedis);
        }
        catch (final Exception e)
        {
            log.error("Exception while pushing data to the inmemory", e);
        }
    }

    public int getWritingDataSize()
    {
        return mWritingData.size();
    }

    public void checkAndWriteToFile(
            boolean aWriteRemaingData)
    {
        final List<String> returnValue = new ArrayList<>(PropertyReader.getInstance().getMaxRecordsPerFile());

        synchronized (mWritingData)
        {
            if (aWriteRemaingData || (mWritingData.size() > PropertyReader.getInstance().getMaxRecordsPerFile()))
                mWritingData.drainTo(returnValue, PropertyReader.getInstance().getMaxRecordsPerFile());
        }
        writeToFile(returnValue);
    }

    private static void writeToFile(
            List<String> aReturnValue)
    {
        final String     fileNameAlone = InformationHolder.getInstance().getNextFileName();
        final FileWriter writer        = new FileWriter(aReturnValue, fileNameAlone);
        /*
        final Thread     t             = new Thread(writer, "File Writer - " + fileNameAlone);
        t.start();
        */
        Thread virtualThread = Thread.ofVirtual().start(writer);

        virtualThread.setName( "File Writer - " + fileNameAlone);
    }

    public synchronized String getNextFileName()
    {
        return PropertyReader.getInstance().getFileNamePrefix() + "_" + (++mWriteFileIndex) + ".gz";
    }

    public List<String> getReadData(
            int aCount)
    {
        final int          listSize    = mReadingData.size();
        final int          size        = (aCount < listSize) ? aCount : listSize;

        final List<String> returnValue = new ArrayList<>(size);
        mReadingData.drainTo(returnValue, size);

        // if (log.isDebugEnabled())
        // log.debug("Inmemory for Reading Returning data " + returnValue.size() + "
        // remaining " + mReadingData.size());

        return returnValue;
    }

    public boolean isAllFilesRead()
    {
        return mTotalFiles == mReadFileCount;
    }

    public int getReadingDataSize()
    {
        return mReadingData.size();
    }

    public void reconcile()
    {
        long readCount  = 0;
        long writeCount = 0;

        for (final Entry<String, Integer> entry : mReadFileRecordMap.entrySet())
        {
            log.debug("Reading file : " + entry.getKey() + " " + entry.getValue());
            readCount += entry.getValue();
        }

        for (final Entry<String, Integer> entry : mWriteFileRecordMap.entrySet())
        {
            log.debug("Writing file : " + entry.getKey() + " " + entry.getValue());
            writeCount += entry.getValue();
        }
        final DecimalFormat df = new DecimalFormat("##,##,##,##,##,##,###");
        df.setMaximumFractionDigits(0);
        df.setMinimumFractionDigits(0);

        if (mTotalFiles == mReadFileCount)
        {
            if (readCount == writeCount)
                log.fatal("Read and write counts are matching. Read Count : '" + df.format(readCount) + "' Write Count '" + df.format(writeCount) + "'");
            else
                log.fatal("ERRRRRRRRRRRRRRRRRRRRRRRRRRRRRROOOOOOOOOOOOOOOR Read and write counts are not matching. Read Count : '" + df.format(readCount) + "' Write Count '" + df.format(writeCount)
                        + "'");
        }
        else
            log.fatal("ERRRRRRRRRRRRRRRRRRRRRRRRRRRRRROOOOOOOOOOOOOOOR Count mismatch with the total files and files read");
    }

}