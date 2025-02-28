package com.itextos.beacon.commonlib.shortcodegenerator.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reapter
{

    // private static final Log log = LogFactory.getLog(Reapter.class);
    private static final Logger log        = LogManager.getLogger(Reapter.class);

    private static final String UPPERCASE  = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE  = UPPERCASE.toLowerCase();
    private static final String NUMBERS    = "0123456789";
    private static final String ALL_STRING = UPPERCASE + LOWERCASE + NUMBERS;
    private static final char[] all        = ALL_STRING.toCharArray();

    public static void start(
            int n)
    {
        final long startTime = System.currentTimeMillis();

        if (((n != 6) && (n != 5)))
            throw new RuntimeException("Invalid char length " + n);

        System.out.println("String Length  = " + n);
        System.out.println("Start time     = " + new Date(startTime));

        long               count = -1;
        final List<String> as    = new ArrayList<>();

        if (n == 6)
            count = generateChar6();
        else
            count = generateChar5();

        final long endTime = System.currentTimeMillis();
        System.out.println("File Generation completed");
        System.out.println("String Length  = " + n);
        System.out.println("Over all count = " + count);
        System.out.println("Start time     = " + new Date(startTime));
        System.out.println("End Time       = " + new Date(endTime));
        System.out.println("Time taken     = " + (endTime - startTime) + " millis " + ((endTime - startTime) / (1000.0 * 60)) + " minutes");
    }

    private static long generateChar6()
    {
        long                     count       = 0;
        // final char[] value = new char[6];
        final int                totalLength = all.length;

        final List<PrintDesends> list        = new ArrayList<>();
        final SimpleDateFormat   sdf         = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        for (int index1 = 0; index1 < totalLength; index1++)
        {
            // value[0] = all[index1];
            final PrintDesends pd = new PrintDesends(index1, all);
            list.add(pd);

            final String name = "Thread-" + all[index1];
            /*
            final Thread t    = new Thread(pd, name);
            t.start();
			*/
            Thread virtualThread = Thread.ofVirtual().start(pd);

            virtualThread.setName( name);
            System.out.println(sdf.format(new Date()) + " : First Level Thread started " + name);
        }

        a:
        while (true)
        {
            System.out.println(sdf.format(new Date()) + " : Checking for First Level completion.");
            boolean canBreak = true;

            for (final PrintDesends pd : list)
            {
                if (pd.isCompleted())
                    System.out.println(sdf.format(new Date()) + " : First Level '" + pd.getThisIterator() + "' is completed. ");

                canBreak = canBreak && pd.isCompleted();

                if (!canBreak)
                {

                    try
                    {
                        // 10 minutes
                        Thread.sleep(10 * 60 * 1000L);
                    }
                    catch (final InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    continue a;
                }
            }
            break;
        }

        for (final PrintDesends pd : list)
            count += pd.getCount();
        return count;
    }

    private static long generateChar5()
    {
        long         count       = 0;
        final char[] value       = new char[5];
        final int    totalLength = all.length;

        for (int index1 = 0; index1 < totalLength; index1++)
        {
            value[0] = all[index1];

            for (int index2 = 0; index2 < totalLength; index2++)
            {
                if (index2 == index1)
                    continue;

                value[1] = all[index2];

                for (int index3 = 0; index3 < totalLength; index3++)
                {
                    if ((index3 == index1) || (index3 == index2))
                        continue;

                    value[2] = all[index3];

                    for (int index4 = 0; index4 < totalLength; index4++)
                    {
                        if ((index4 == index1) || (index4 == index2) || (index4 == index3))
                            continue;

                        value[3] = all[index4];

                        for (int index5 = 0; index5 < totalLength; index5++)
                        {
                            if ((index5 == index1) || (index5 == index2) || (index5 == index3) || (index5 == index4))
                                continue;

                            value[4] = all[index5];
                            count++;
                            // System.out.println(new String(value));
                            log.debug(new String(value));
                        }
                        // sleepForaWhile();
                    }
                    // sleepForaWhile();
                }
                // sleepForaWhile();
            }
            // sleepForaWhile();
            System.out.println(all[index1] + " = " + count);
        }
        return count;
    }

    // public static void sleepForaWhile()
    // {
    //
    // try
    // {
    // Thread.sleep(1);
    // }
    // catch (final InterruptedException e)
    // {
    // e.printStackTrace();
    // }
    // }

    private static void doForLoop(
            int aN)
    {
        System.out.println(aN);
        if (aN == 0)
            return;
        doForLoop(aN - 1);
    }

    public static void main(
            String[] args)
    {
        start(5);
    }

}
