package com.itextos.beacon.r3r.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler2;

public class DataProcessor
{

    private static final Log log = LogFactory.getLog(DataProcessor.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final DataProcessor INSTANCE = new DataProcessor();

    }

    public static DataProcessor getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final DBProcessorThread          dbProcessorThread          = new DBProcessorThread();
    private final UrlRequestDetailsProcessor urlRequestDetailsProcessor = new UrlRequestDetailsProcessor();

    private DataProcessor()
    {
        initDBProcessor();
        initRequestDataProcessor();
    }

    private void initDBProcessor()
    {
    	
    	
    	ExecutorSheduler2.getInstance().addTask(dbProcessorThread, "DB Process Thread");

    	if (log.isDebugEnabled())
            log.debug("DB Processor Thread Started ");
    }

    private void initRequestDataProcessor()
    {
    
    	
    	ExecutorSheduler2.getInstance().addTask(urlRequestDetailsProcessor, "URL Request Processor");
         
        if (log.isDebugEnabled())
            log.debug("URL Request Details Processor Started ");
    }

    public void stopMe()
    {

        try
        {
            dbProcessorThread.stopMe();
            urlRequestDetailsProcessor.stopMe();
        }
        catch (final Exception e)
        {
            log.error("Exception while stopping the dbProcessorThread or urlRequestDetailsProcessor", e);
        }
    }

}