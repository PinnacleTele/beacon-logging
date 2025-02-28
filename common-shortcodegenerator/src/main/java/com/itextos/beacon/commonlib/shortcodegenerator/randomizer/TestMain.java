package com.itextos.beacon.commonlib.shortcodegenerator.randomizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itextos.beacon.commonlib.shortcodegenerator.randomizer.util.PropertyReader;
import com.itextos.beacon.commonlib.shortcodegenerator.randomizer.util.RedisConnectionPool;

public class TestMain
{

    private static final Logger log = LogManager.getLogger(TestMain.class);

    public static void main(
            String[] args)
    {
        final long startTime = System.currentTimeMillis();

        startAndClearRedis();
        deleteOldGenratedFiles();

        final InMemorySizePrinter   lInMemorySizePrinter = startMemorySizePrinter();
        final List<RedisDataReader> lRedisReaderList     = startFileReaders(lInMemorySizePrinter);

        final WriteDataMonitor      wdm                  = new WriteDataMonitor();
        final Thread                writeDataMonitor     = new Thread(wdm, "WriteDataMonitor");
        writeDataMonitor.start();

        checkForCompletion(lRedisReaderList, lInMemorySizePrinter, wdm);

        finalizeData();

        final long endTime = System.currentTimeMillis();

        log.fatal("Finished loading and randomizing the data.");
        log.fatal("Process Started : " + new Date(startTime));
        log.fatal("Process Ended   : " + new Date(endTime));
        log.fatal("Time Taken      : " + ((endTime - startTime) / (60 * 1000)) + " minutes");
    }

    private static void finalizeData()
    {
        log.fatal("Assuming all the process has been completed.");
        InformationHolder.getInstance().reconcile();
    }

    private static void checkForCompletion(
            List<RedisDataReader> aStartFileReaders,
            InMemorySizePrinter aInMemorySizePrinter,
            WriteDataMonitor aWdm)
    {

        while (true)
        {
            boolean allCompleted = true;

            for (final RedisDataReader reader : aStartFileReaders)
            {
                log.debug("Is Completed : '" + reader.getThreadName() + "' - " + reader.isCompleted());

                allCompleted = allCompleted && reader.isCompleted();
                if (!reader.isCompleted())
                    break;
            }
            if (allCompleted)
                break;

            try
            {
                Thread.sleep(5000);
            }
            catch (final InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        aWdm.stopMe();

        while (!aWdm.isStopped())
        {
            log.debug("Waiting for write to file to complete.");

            try
            {
                Thread.sleep(100);
            }
            catch (final InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        log.fatal("Calling the final file to be processed.");
        InformationHolder.getInstance().checkAndWriteToFile(true);
        aInMemorySizePrinter.stopMe();
    }

    private static void deleteOldGenratedFiles()
    {
        if (PropertyReader.getInstance().isDeletePartialCreatedFiles())
            try (
                    final Stream<Path> paths = Files.walk(Paths.get(PropertyReader.getInstance().getOutputFolder())))
            {
                paths.forEach(path -> {

                    if (path.toFile().isDirectory())
                    {
                        log.error("Path is a directory. Leaving this path.");
                        return;
                    }

                    if (log.isDebugEnabled())
                        log.debug("Deleting File : Path '" + path + "'");

                    try
                    {
                        Files.delete(path);
                    }
                    catch (final Exception e)
                    {
                        log.error("Exception while deleting the file '" + path + "'. Exiting the application.", e);
                        System.exit(-1);
                    }
                });
            }
            catch (final IOException e1)
            {
                log.error("Exception while getting the filenames.", e1);
            }
    }

    private static void startAndClearRedis()
    {

        try
        {
            RedisConnectionPool.getInstance();

            if (PropertyReader.getInstance().isFlushRedisStartup())
                RedisConnectionPool.getInstance().flushAll();
        }
        catch (final Exception e)
        {
            log.error("Exception while creating / flushing the Redis. Exiting the application.", e);
            throw e;
        }
    }

    private static List<RedisDataReader> startFileReaders(
            InMemorySizePrinter aInMemorySizePrinter)
    {
        if (log.isDebugEnabled())
            log.debug("Input Folder '" + PropertyReader.getInstance().getInputFolder() + "'");

        startFilenameAddThread();

        waitForCount(1, 2, "File To Add to read list");

        startFileReadThreads();

        startRedisPusher(aInMemorySizePrinter);

        waitForCount(2, PropertyReader.getInstance().getNoOfFileReader(), "File To Read and add to inmemory");

        return startRedisReader();
    }

    private static void startRedisPusher(
            InMemorySizePrinter aInMemorySizePrinter)
    {
        log.debug("Starting Redis Pusher");

        for (int index = 1; index <= PropertyReader.getInstance().getNoOfRedisPusher(); index++)
        {
            final String threadName = "RedisPusher-" + index;

            log.debug("Starting Redis Pusher " + threadName);

            final Thread th = new Thread(new InmemToRedisPusher(threadName, aInMemorySizePrinter), threadName);
            th.start();
        }
    }

    private static void waitForCount(
            int aType,
            int aWaitCount,
            String aString)
    {
        int count = (aType == 1) ? InformationHolder.getInstance().getFilesWaitingFoRead() : InformationHolder.getInstance().getReadFileCount();

        while (count < aWaitCount)
        {
            if (log.isDebugEnabled())
                log.debug("Sleeping for the number of files " + aString + " to reach " + aWaitCount + ". Current count " + count);

            try
            {
                Thread.sleep(10 * 1000L);
            }
            catch (final InterruptedException e)
            {}
            count = (aType == 1) ? InformationHolder.getInstance().getFilesWaitingFoRead() : InformationHolder.getInstance().getReadFileCount();
        }
    }

    private static InMemorySizePrinter startMemorySizePrinter()
    {
        final InMemorySizePrinter lInMemorySizePrinter = new InMemorySizePrinter();
        final Thread              th                   = new Thread(lInMemorySizePrinter, "InmemorySizePrinter");
        th.start();
        th.setPriority(Thread.MAX_PRIORITY);
        return lInMemorySizePrinter;
    }

    private static List<RedisDataReader> startRedisReader()
    {
        log.fatal("Starting the Redis Reader");

        final List<RedisDataReader> readerList = new ArrayList<>();

        for (int index = 0; index < PropertyReader.getInstance().getNoOfRedisReader(); index++)
        {
            final String threadName = "Redis Reader " + (index + 1);

            if (log.isDebugEnabled())
                log.debug("Starting Redis reader '" + threadName + "'");

            final RedisDataReader redisDataReader = new RedisDataReader(threadName);
            final Thread          th              = new Thread(redisDataReader, threadName);
            th.start();
            readerList.add(redisDataReader);
        }
        return readerList;
    }

    private static void startFileReadThreads()
    {
        log.fatal("Starting the File Reader");

        for (int index = 0; index < PropertyReader.getInstance().getNoOfFileReader(); index++)
        {
            final String threadName = "File Reader " + (index + 1);

            if (log.isDebugEnabled())
                log.debug("Starting File reader '" + threadName + "'");

            final FileReader fileReader = new FileReader(threadName);
            final Thread     th         = new Thread(fileReader, threadName);
            th.start();
        }
    }

    private static void startFilenameAddThread()
    {
        log.fatal("Starting the file name adder");

        new Thread(() -> {
            if (log.isDebugEnabled())
                log.debug("Starting file reading threads for the folder '" + PropertyReader.getInstance().getInputFolder() + "'");

            try (
                    final Stream<Path> paths = Files.walk(Paths.get(PropertyReader.getInstance().getInputFolder())))
            {
                final List<Path> allFiles = new ArrayList<>();
                paths.forEach(path -> {

                    try
                    {

                        if (path.toFile().isDirectory())
                        {
                            log.error("Path is a directory. Leaving this path.");
                            return;
                        }

                        allFiles.add(path);
                    }
                    catch (final Exception e)
                    {
                        log.error("Exception while getting the filenames. Exiting the application.", e);
                        System.exit(-1);
                    }
                });

                log.debug("Total files in the list " + allFiles.size());

                InformationHolder.getInstance().setTotalFiles(allFiles.size());

                final List<Integer> sent = new ArrayList<>();
                final Random        rand = new Random();

                while (sent.size() < allFiles.size())
                {
                    final int index = rand.nextInt(allFiles.size());

                    if (!sent.contains(index))
                    {
                        sent.add(index);
                        final Path lPath = allFiles.get(index);
                        // if (log.isDebugEnabled())
                        // log.debug("Adding File Path to the list '" + lPath + "'");
                        InformationHolder.getInstance().addFilesToProcess(lPath);
                    }
                }

                log.fatal("Completed adding all the filenames to the reading list");
            }
            catch (final IOException e1)
            {
                log.error("Exception while getting the filenames.", e1);
            }
        }, "FileNamingAddingThread").start();
    }

}
