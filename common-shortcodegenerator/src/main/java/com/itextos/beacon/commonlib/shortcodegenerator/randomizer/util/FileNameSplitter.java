package com.itextos.beacon.commonlib.shortcodegenerator.randomizer.util;

import static java.nio.file.Path.of;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class FileNameSplitter
{

    public static void main(
            String[] args)
    {

        try
        {
            final String                     sourceFolder    = args[0];
            final List<Path>                 sourceFileNames = readSourceFileNames(sourceFolder);
            final Map<Integer, List<String>> lSplitFileNames = splitFileNames(sourceFileNames, 5000);

            generateMovingScripts(lSplitFileNames);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void generateMovingScripts(
            Map<Integer, List<String>> aSplitFileNames)
            throws IOException
    {
        final String         source      = "/home/strlen_6/";
        final String         destination = "/home/teamwork/application/strlen_6";

        final List<FileCopy> list        = new ArrayList<>();

        for (final Entry<Integer, List<String>> entry : aSplitFileNames.entrySet())
        {
            final FileCopy fileCopy = new FileCopy(source, destination, entry);
            list.add(fileCopy);
            
            /*
            final Thread t = new Thread(fileCopy);
            t.start();
            */
            Thread virtualThread = Thread.ofVirtual().start(fileCopy);

            virtualThread.setName( "fileCopy");
        }

        while (true)
        {
            boolean completed = true;

            for (final FileCopy fc : list)
            {
                completed = fc.completed;
                if (!completed)
                    break;
            }

            if (!completed)
                try
                {
                    Thread.sleep(1000);
                }
                catch (final InterruptedException e)
                {
                    e.printStackTrace();
                }
            else
                break;
        }

        int filesCount = 0;
        for (final FileCopy fc : list)
            filesCount += fc.filesCount;
        System.out.println("Process Complted. Total files copied '" + filesCount + "'");
    }

    private static Map<Integer, List<String>> splitFileNames(
            List<Path> aSourceFileNames,
            int aSize)
    {
        final int                        lSize         = aSourceFileNames.size();
        final Random                     rand          = new Random();
        final List<String>               usedFileNames = new ArrayList<>();

        int                              splitIndex    = 1;
        List<String>                     newList       = new ArrayList<>();
        final Map<Integer, List<String>> splittedMap   = new HashMap<>();
        splittedMap.put(splitIndex, newList);

        while (usedFileNames.size() != lSize)
        {
            final int    lNextInt       = rand.nextInt(lSize);
            final Path   lFileNameToUse = aSourceFileNames.get(lNextInt);
            final String fileName       = lFileNameToUse.getFileName().toString();

            if (usedFileNames.contains(fileName))
                continue;

            usedFileNames.add(fileName);
            newList.add(fileName);

            if (newList.size() == aSize)
            {
                ++splitIndex;
                newList = new ArrayList<>();
                splittedMap.put(splitIndex, newList);
            }
        }

        for (final Entry<Integer, List<String>> entry : splittedMap.entrySet())
            System.out.println(entry.getKey() + " = " + entry.getValue().size());
        return splittedMap;
    }

    private static List<Path> readSourceFileNames(
            String aSourceFolder)
            throws IOException
    {
        final List<Path> returnValue = new ArrayList<>();

        Files.list(of(aSourceFolder)).forEach(returnValue::add);

        System.out.println("Source Folder " + aSourceFolder);
        System.out.println("Total files   " + returnValue.size());
        return returnValue;
    }

}

class FileCopy
        implements
        Runnable
{

    String                       source;
    String                       destination;
    Entry<Integer, List<String>> entry;
    boolean                      completed  = false;
    int                          filesCount = 0;

    public FileCopy(
            String aSource,
            String aDestination,
            Entry<Integer, List<String>> aEntry)
    {
        source      = aSource;
        destination = aDestination;
        entry       = aEntry;
    }

    @Override
    public void run()
    {

        try
        {
            final String tempDestination    = destination + "_" + entry.getKey();
            final Path   lCreateDirectories = Files.createDirectory(of(tempDestination));
            System.out.println("Path created : '" + lCreateDirectories + "'");

            for (final String s : entry.getValue())
            {
                final String sourceFile = source + "/" + s;
                final String destFile   = destination + "_" + entry.getKey() + "/" + s;
                final Path   sourcePath = of(sourceFile);
                final Path   destPath   = of(destFile);
                final Path   lCopy      = Files.copy(sourcePath, destPath);
                final long   lCount     = Files.size(lCopy);
                filesCount++;
                System.out.println(entry.getKey() + " File count " + filesCount + +lCount + " " + s);
            }
            System.out.println("Completed for " + entry.getKey());
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        completed = true;
    }

}