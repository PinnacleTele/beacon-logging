package com.itextos.beacon.commonlib.commondbpool.tracker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;

public class SQLTracker
{

    private static final Log log = LogFactory.getLog(SQLTracker.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final SQLTracker INSTANCE = new SQLTracker();

    }

    public static SQLTracker getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final boolean logSQL = DBDataSourceFactory.isTrackerEnabled();

    private SQLTracker()
    {}

    /**
     * Log every SQL that is fired & put in a Hashtable.
     *
     * @param aSql
     */
    public void logSQL(
            String aSql)
    {

        if (logSQL)
        {
            final StackTraceCollection stackTrace = new StackTraceCollection(Thread.currentThread());

            final StringBuffer         sbSQL      = new StringBuffer();
            sbSQL.append("<SQL_INFO>").append("<SQL>").append(aSql).append("</SQL>").append("<CALLER_INFO>").append(stackTrace.getXML()).append("</CALLER_INFO>").append("</SQL_INFO>");

            if (log.isInfoEnabled())
                log.info(sbSQL.toString());
        }
    }

}