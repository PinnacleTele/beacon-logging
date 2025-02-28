package com.itextos.beacon.commonlib.shortcodegenerator.filecounter;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileLineCounter
{

    private static final Logger log = LogManager.getLogger(FileLineCounter.class);

    public static void main(
            String[] args)
    {
        final String folderPath = System.getProperty("folder.tolook");

        loadFiles(folderPath);

        startCounters();

        while (!FileCounterHolder.getInstance().isCompleted())
        {
            log.debug("Waiting for he process to complete");

            try
            {
                Thread.sleep(10000);
            }
            catch (final InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        FileCounterHolder.getInstance().printCounts();
    }

    private static void startCounters()
    {

        for (int index = 0; index < 50; index++)
        {
            final FileLengthCheck flc    = new FileLengthCheck();
            
            /*
            final Thread          thread = new Thread(flc, "FileLengthCheck-" + index);
            thread.start();
            */
            Thread virtualThread = Thread.ofVirtual().start(flc);

            virtualThread.setName( "FileLengthCheck-" + index);
            
            
        }
    }

    private static void loadFiles(
            String aFolderPath)
    {
        final File parent = new File(aFolderPath);

        if (!parent.exists())
        {
            log.error("Not a valid path '" + parent.getAbsolutePath() + "'");
            return;
        }

        if (!parent.isDirectory())
        {
            log.error("Path is not a vaid directory '" + parent.getAbsolutePath() + "'");
            return;
        }

        try
        {
            final File[] lList = parent.listFiles();

            for (final File file : lList)
            {

                if (file.isDirectory())
                {
                    log.error("Path is a directory. Leaving this path.");
                    continue;
                }
                FileCounterHolder.getInstance().addFilesToProcess(file);
            }

            log.debug("Total files in the directory " + FileCounterHolder.getInstance().getTotalFiles());
        }
        catch (final Exception e1)
        {
            log.error("Exception while getting the filenames.", e1);
        }
    }

}