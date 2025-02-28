package com.itextos.beacon.commonlib.dnddataloader.csv;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.dnddataloader.common.InMemoryDataHolder;
import com.itextos.beacon.commonlib.dnddataloader.util.DndPropertyProvider;

public class Csv2RedisThreadBased
{

    private static final Log log = LogFactory.getLog(Csv2RedisThreadBased.class);

    private Csv2RedisThreadBased()
    {}

    public static boolean process()
    {

        try
        {
            final File[] allCSVFiles = getCSVFiles();

            if (log.isInfoEnabled())
                log.info("CSV files count " + ((allCSVFiles == null) ? "0" : "" + allCSVFiles.length));

            if ((allCSVFiles == null) || (allCSVFiles.length == 0))
            {
                if (log.isInfoEnabled())
                    log.info("No CSV files found to process.");
                return false;
            }

            startFileReaderThread(allCSVFiles);
            return true;
        }
        catch (final Exception e)
        {
            log.error("Exception while processing CSV files.", e);
            return false;
        }
    }

    private static void startFileReaderThread(
            File[] aAllCSVFiles)
    {
        final Thread th = new Thread((Runnable) () -> {
            if (log.isDebugEnabled())
                log.debug("Processing individual csv files");

            for (final File curFile : aAllCSVFiles)
            {
                if (log.isDebugEnabled())
                    log.debug("Adding csv file : '" + curFile.getAbsolutePath() + "'");

                try
                {
                    InMemoryDataHolder.getInstance().addFilesToRead(curFile);
                }
                catch (final InterruptedException e)
                {
                    log.error("Exception while adding the file to read " + curFile.getAbsolutePath(), e);
                }
            }
        }, "AddingFileToReadList");
        th.start();

        for (int index = 0; index < DndPropertyProvider.getInstance().getFileReaderThreadCount(); index++)
        {
            final CsvFileReader fileReader = new CsvFileReader();
            final Thread        t          = new Thread(fileReader, "Filreader-" + (index + 1));
            t.start();
        }
    }

    private static File[] getCSVFiles()
    {
        if (log.isDebugEnabled())
            log.debug("Looking for CSV files in '" + DndPropertyProvider.getInstance().getCsvSourceFilePath() + "'");

        final File src = new File(DndPropertyProvider.getInstance().getCsvSourceFilePath());

        if (!src.exists())
            throw new RuntimeException("Source Folder does not exists. Source Path '" + src.getAbsolutePath() + "'");

        return src.listFiles(new CsvFileFilter());
    }

}

class CsvFileFilter
        implements
        FileFilter
{

    @Override
    public boolean accept(
            File aFileName)
    {
        return (aFileName.isFile() && aFileName.getName().toLowerCase().endsWith(".csv"));
    }

}