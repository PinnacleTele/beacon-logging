package com.itextos.beacon.commonlib.shortcodegenerator.randomizer.util;

import java.text.DecimalFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Utility
{

    private static final Log log = LogFactory.getLog(Utility.class);

    private Utility()
    {}

    public static void printMemoryInfo()
    {
        final Runtime rt = Runtime.getRuntime();
        log.debug("MEMORY Total " + getPrintableValue(rt.totalMemory()) + " Max " + getPrintableValue(rt.maxMemory()) + " Free " + getPrintableValue(rt.freeMemory()));
    }

    public static String getPrintableValue(
            long aByteValue)
    {
        final double d = aByteValue / 1000000.0;
        return new DecimalFormat("##,##,##0.00").format(d);
    }

    // public static void callGC()
    // {
    //
    // try
    // {
    // log.debug("Trying to run GC");
    // System.gc();
    // }
    // catch (final Exception e)
    // {
    // log.error("Exception while calling the Ssytem GC", e);
    // }
    // }

    // public static void sleepForWhile(
    // long aTimeToSleepinMillies)
    // {
    //
    // try
    // {
    // Thread.sleep(aTimeToSleepinMillies);
    // }
    // catch (final InterruptedException e)
    // {
    // log.error("InterruptedException", e);
    // }
    // }

}
