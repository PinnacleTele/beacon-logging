package com.itextos.beacon.commonlib.shortcodegenerator.filecounter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileCounterHolder
{

    private static final Logger log = LogManager.getLogger(FileCounterHolder.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final FileCounterHolder INSTANCE = new FileCounterHolder();

    }

    public static FileCounterHolder getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final BlockingQueue<File> fileLineCountList = new LinkedBlockingQueue<>();
    private final Map<String, Long>   lineCounts        = new HashMap<>();
    private long                      totalCounts       = 0;
    private int                       totalFiles        = 0;

    public int getTotalFiles()
    {
        return totalFiles;
    }

    public void addFilesToProcess(
            File aFile)
    {
        fileLineCountList.add(aFile);
        totalFiles++;
    }

    public File getNextFile()
    {
        return fileLineCountList.poll();
    }

    public synchronized void addCount(
            File aFileName,
            long aCount)
    {
        lineCounts.put(aFileName.getName(), aCount);
        totalCounts += aCount;

        log.debug("Total files " + totalFiles + " counted files " + lineCounts.size() + " total records " + totalCounts);
    }

    public boolean isCompleted()
    {
        return totalFiles == lineCounts.size();
    }

    public void printCounts()
    {
        final Set<String>  lKeySet = lineCounts.keySet();
        final List<String> list    = new ArrayList<>(lKeySet);
        Collections.sort(list, new FilenameComparator());

        long temp = 0;

        for (final String p : list)
        {
            final long t = lineCounts.get(p);
            temp += t;
            log.debug(p + " : " + t + " " + temp);
        }
        log.debug("Total count : " + totalCounts + " " + (totalCounts == temp));
    }

}