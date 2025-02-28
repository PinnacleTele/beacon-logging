package com.itextos.beacon.platform.dnr.process;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

public class ReceivedCounter
        extends
        TimerTask
{

    private static final Log      logger   = LogFactory.getLog(ReceivedCounter.class);
    Date                          mEOD;

    LinkedHashMap<String, String> prevList = new LinkedHashMap<>();

    Timer                         timer    = new Timer("Received Timer counter");

    private ReceivedCounter()
    {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        mEOD = cal.getTime();

        if (logger.isDebugEnabled())
            logger.debug("Reset session counter schedueld on " + mEOD);

        timer.scheduleAtFixedRate(this, mEOD, 86400000);
    }

    private AtomicLong counter = new AtomicLong();

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final ReceivedCounter INSTANCE = new ReceivedCounter();

    }

    public static ReceivedCounter getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    public void add()
    {
        counter.incrementAndGet();
    }

    public long get()
    {
        return counter.get();
    }

    private void addInfo()
    {
        final Calendar lCal = Calendar.getInstance();
        lCal.add(Calendar.DAY_OF_MONTH, -1);
        lCal.set(Calendar.HOUR_OF_DAY, 0);
        lCal.set(Calendar.MINUTE, 0);
        lCal.set(Calendar.SECOND, 0);
        final Date   lEOD        = lCal.getTime();

        final String lDateString = DateTimeUtility.getFormattedDateTime(lEOD, DateTimeFormat.DEFAULT_DATE_ONLY);

        prevList.put(lDateString, "" + counter);

        if ((prevList != null) && (prevList.size() > 3))
        {
            String lRemoveKey = null;

            for (final Object key : prevList.keySet())
            {
                lRemoveKey = (String) key;
                break;
            }
            prevList.remove(lRemoveKey);
            prevList.put(lDateString, "" + counter);
        }
    }

    public LinkedHashMap<String, String> getPrvList()
    {
        return prevList;
    }

    @Override
    public void run()
    {
        addInfo();
        logger.info("Received counter resetted.." + new Date());
        counter = new AtomicLong();
    }

}
