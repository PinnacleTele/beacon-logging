package com.itextos.beacon.commonlib.shortcodegenerator.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrintDesends
        implements
        Runnable
{

    private static final Logger log         = LogManager.getLogger(PrintDesends.class);

    private final int           firstCharIndex;
    private final char[]        all;
    private final char          thisIterator;
    private boolean             isCompleted = false;
    private long                count       = 0;

    public PrintDesends(
            int afirstCharIndex,
            char[] aAll)
    {
        firstCharIndex = afirstCharIndex;
        all            = aAll;
        thisIterator   = all[firstCharIndex];
    }

    public char getThisIterator()
    {
        return thisIterator;
    }

    @Override
    public void run()
    {
        final int    totalLength = all.length;
        final char[] value       = new char[6];
        value[0] = all[firstCharIndex];

        final List<PrintSecondLevel> secondLevelList = new ArrayList<>();
        final SimpleDateFormat       sdf             = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        for (int index2 = 0; index2 < totalLength; index2++)
        {
            if (index2 == firstCharIndex)
                continue;

            final PrintSecondLevel psl = new PrintSecondLevel(firstCharIndex, index2, all);
            secondLevelList.add(psl);

            final String name = "Thread-" + all[firstCharIndex] + all[index2];
            /*
            final Thread t    = new Thread(psl, name);
            t.start();
*/
            Thread virtualThread = Thread.ofVirtual().start(psl);

            virtualThread.setName( name);
            System.out.println(sdf.format(new Date()) + " : Second Level Thread started " + name);
        }

        int index = 0;

        a:
        while (true)
        {
            System.out.println(sdf.format(new Date()) + " : Checking for Second completion.");
            boolean canBreak = true;

            for (final PrintSecondLevel psl : secondLevelList)
            {
                index++;

                if (psl.isCompleted())
                    System.out.println(sdf.format(new Date()) + " : Second Level thread '" + psl.getThisIterator() + "' is completed. ");

                canBreak = canBreak && psl.isCompleted();

                if (!canBreak)
                {

                    try
                    {
                        // 5 minutes
                        Thread.sleep(5 * 60 * 1000L);
                    }
                    catch (final InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    if (index == 1000)
                        break;

                    continue a;
                }
            }

            if (!canBreak)
            {
                for (final PrintSecondLevel psl : secondLevelList)
                    System.out.println(sdf.format(new Date()) + " : Second Level thread '" + psl.getThisIterator() + "' = " + psl.getCount());
                index = 0;
                continue;
            }
            break;
        }

        for (final PrintSecondLevel psl : secondLevelList)
            count += psl.getCount();

        System.out.println(all[firstCharIndex] + " = " + count);
        isCompleted = true;
    }

    public long getCount()
    {
        return count;
    }

    public boolean isCompleted()
    {
        return isCompleted;
    }

}
