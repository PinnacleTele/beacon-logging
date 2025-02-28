package com.itextos.beacon.commonlib.urlhitterthread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;

public class Hitter
{

    private static final Log log = LogFactory.getLog(Hitter.class);

    public static void main(
            String[] args)
    {
        final int                        threadCount = PropertyFileReader.getInstance().getNoOfThreads();
        final List<Thread>               threadList  = new ArrayList<>();

        final Map<Integer, HitterThread> threads     = new HashMap<>();

        for (int index = 1; index <= threadCount; index++)
        {
            final HitterThread ht = new HitterThread(index);
            threads.put(index, ht);
            
            /*
            final Thread t = new Thread(ht, "Thread-" + index);
            */
            Thread t = Thread.ofVirtual().unstarted(ht);

            t.setName(  "Thread-" + index);
            threadList.add(t);
        }

        final CountPrinter cp  = new CountPrinter(threads);
        /*
        final Thread       tcp = new Thread(cp);
        tcp.start();

		*/
        Thread virtualThread = Thread.ofVirtual().start(cp);

        virtualThread.setName( "CountPrinter");
        
        for (final Thread t : threadList)
        {
            log.debug("Starting : '" + t.getName() + "'");
            t.start();
        }

        boolean isCompleted = false;

        while (!isCompleted)
        {

            for (final Entry<Integer, HitterThread> entry : threads.entrySet())
            {
                isCompleted = entry.getValue().isCompleted();
                if (!isCompleted)
                    break;
            }

            if (!isCompleted)
                CommonUtility.sleepForAWhile();
        }

        cp.stopMe();
        System.out.println("Process Completed.");
        log.fatal("Process Completed.");
    }

}