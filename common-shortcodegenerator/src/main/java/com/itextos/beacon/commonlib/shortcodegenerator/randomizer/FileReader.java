package com.itextos.beacon.commonlib.shortcodegenerator.randomizer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileReader
        implements
        Runnable
{

    private static final Logger log = LogManager.getLogger(FileReader.class);
    private final String        mThreadName;

    public FileReader(
            String aThreadName)
    {
        mThreadName = aThreadName;
    }

    @Override
    public void run()
    {

        while (true)
        {
            final Path nextFile = InformationHolder.getInstance().getNextFileToProcess();

            try
            {
                log.debug("Next file to read : '" + nextFile + "'");

                if (nextFile == null)
                    break;

                final int recordsInFile = readFileContentAndPushToInmemory(nextFile);
                InformationHolder.getInstance().updateReadInfo(nextFile.toString(), recordsInFile);
            }
            catch (final IOException e1)
            {
                log.error("Exception while reading the file '" + nextFile + "' Exiting the application.", e1);
                System.exit(-1);
            }

            sleepForWhile(10);
        }
        log.fatal("Stopping File Reader Thread " + mThreadName);
    }

    private static int readFileContentAndPushToInmemory(
            Path aNextFile)
            throws IOException
    {

        try (
                final BufferedReader lReader = getReader(aNextFile))
        {
            int    count = 0;
            String s     = null;

            while ((s = lReader.readLine()) != null)
            {
                InformationHolder.getInstance().addReadData(s);
                count++;
            }
            return count;
        }
    }

    private static void sleepForWhile(
            long aTimeToSleepinMillies)
    {

        try
        {
            Thread.sleep(aTimeToSleepinMillies);
        }
        catch (final InterruptedException e)
        {
            log.error("InterruptedException", e);
        }
    }

    public static BufferedReader getReader(
            Path aNextFile)
            throws IOException
    {
        // if (log.isDebugEnabled())
        // log.debug("Creating Buffered Reader for file '" + aNextFile + "'");

        final File                f          = new File(aNextFile.toString());
        final InputStream         fileIs     = Files.newInputStream(aNextFile);
        final BufferedInputStream bufferedIs = new BufferedInputStream(fileIs, 65535);

        if (f.getName().endsWith(".txt") || f.getName().endsWith(".log"))
            return new BufferedReader(new InputStreamReader(bufferedIs));
        return new BufferedReader(new InputStreamReader(new GZIPInputStream(bufferedIs)));
    }

}