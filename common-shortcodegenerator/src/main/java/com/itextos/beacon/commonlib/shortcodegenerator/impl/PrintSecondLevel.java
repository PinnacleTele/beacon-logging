package com.itextos.beacon.commonlib.shortcodegenerator.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrintSecondLevel
        implements
        Runnable
{

    private static final Logger log         = LogManager.getLogger(PrintSecondLevel.class);

    private final int           firstCharIndex;
    private final int           secondCharIndex;
    private final char[]        all;
    private long                count       = 0;
    private boolean             isCompleted = false;

    public PrintSecondLevel(
            int aFirstCharIndex,
            int aSecondCharIndex,
            char[] aAll)
    {
        firstCharIndex  = aFirstCharIndex;
        secondCharIndex = aSecondCharIndex;
        all             = aAll;
    }

    @Override
    public void run()
    {
        final int    totalLength = all.length;
        final char[] value       = new char[6];
        value[0] = all[firstCharIndex];
        value[1] = all[secondCharIndex];

        for (int index3 = 0; index3 < totalLength; index3++)
        {
            if ((index3 == firstCharIndex) || (index3 == secondCharIndex))
                continue;

            value[2] = all[index3];

            for (int index4 = 0; index4 < totalLength; index4++)
            {
                if ((index4 == firstCharIndex) || (index4 == secondCharIndex) || (index4 == index3))
                    continue;

                value[3] = all[index4];

                for (int index5 = 0; index5 < totalLength; index5++)
                {
                    if ((index5 == firstCharIndex) || (index5 == secondCharIndex) || (index5 == index3) || (index5 == index4))
                        continue;

                    value[4] = all[index5];

                    for (int index6 = 0; index6 < totalLength; index6++)
                    {
                        if ((index6 == firstCharIndex) || (index6 == secondCharIndex) || (index6 == index3) || (index6 == index4) || (index6 == index5))
                            continue;

                        value[5] = all[index6];
                        count++;
                        log.debug(new String(value));
                    }
                }
                // Reapter.sleepForaWhile();
            }
            // Reapter.sleepForaWhile();
        }
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

    public String getThisIterator()
    {
        return "" + all[firstCharIndex] + all[secondCharIndex];
    }

}
