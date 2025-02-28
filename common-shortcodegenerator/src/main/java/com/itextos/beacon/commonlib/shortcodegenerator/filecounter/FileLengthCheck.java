package com.itextos.beacon.commonlib.shortcodegenerator.filecounter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileLengthCheck
        implements
        Runnable
{

    private static final Logger log = LogManager.getLogger(FileLengthCheck.class);

    @Override
    public void run()
    {

        while (true)
        {
            final File file = FileCounterHolder.getInstance().getNextFile();

            try
            {
                if (file == null)
                    break;

                final int lReadFileCount = readFileCount(file);
                FileCounterHolder.getInstance().addCount(file, lReadFileCount);
            }
            catch (final Exception e)
            {
                log.error("Exception while getting the line numbers in the file '" + file + "'", e);
            }
        }
    }

    private static int readFileCount(
            File aNextFile)
            throws IOException
    {

        try (
                final BufferedReader lReader = getReader(aNextFile))
        {
            int    count = 0;
            String s     = null;

            while ((s = lReader.readLine()) != null)
                count++;
            return count;
        }
    }

    public static BufferedReader getReader(
            File aNextFile)
            throws IOException
    {
        final InputStream         fileIs     = new FileInputStream(aNextFile);
        final BufferedInputStream bufferedIs = new BufferedInputStream(fileIs, 65535);

        if (aNextFile.getName().endsWith(".txt") || aNextFile.getName().endsWith(".log"))
            return new BufferedReader(new InputStreamReader(bufferedIs));
        return new BufferedReader(new InputStreamReader(new GZIPInputStream(bufferedIs)));
    }

}