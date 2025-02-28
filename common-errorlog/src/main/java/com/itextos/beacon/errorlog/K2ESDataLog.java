package com.itextos.beacon.errorlog;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.itextos.beacon.commonlib.constants.ErrorMessage;


public class K2ESDataLog {


    private static final  Logger logger = Logger.getLogger(K2ESDataLog.class.getName());
    
    static {
    	
    	 int limit = 1024 * 1024*10; // 1 MB file size limit
         int count = 1; // N

        String logFileNamePattern = "/opt/jboss/wildfly/logs/k2esdata.%g.log";

        Level loglevel=Level.INFO;
        
        String loglevelFromEnr=System.getenv("loglevel");
        
        if(loglevelFromEnr!=null) {
        
        	if(loglevelFromEnr.equals("all")) {
        		
        		loglevel=Level.ALL;
        		
        	}else if(loglevelFromEnr.equals("off")) {
        		
        		loglevel=Level.OFF;
        	}
        }
        
        // Create a FileHandler with the specified log file name
        FileHandler fileHandler=null;
		try {

			fileHandler = new FileHandler(logFileNamePattern, limit, count, true);
			   // Set the logging level for the handler
	        fileHandler.setLevel(loglevel);

	        // Set a formatter for the handler (optional)
	        fileHandler.setFormatter(new SimpleFormatter());

	        // Add the handler to the logger
	        logger.addHandler(fileHandler);
	        
	        logger.setLevel(loglevel);

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

     

        // Set the logging level for the logger
    }

    private static K2ESDataLog obj=new K2ESDataLog();
    
    public static K2ESDataLog getInstance() {
    	
    	if(obj==null) {
    		obj=new K2ESDataLog();
    	}
    	
    	return obj;
    }
    public void log(String string) {

    	logger.info(string);
    	
    }
    
    public void info(String string) {

    	logger.info(string);
    	
    }
    
    public void error(String string,Exception e) {

    	logger.info(string+" \n "+ ErrorMessage.getStackTraceAsString(e));
    	
    }
    
    public void error(String string,Throwable e) {

    	logger.info(string+" \n "+ ErrorMessage.getStackTraceAsString(e));
    	
    }
    
    public void error(String string) {

    	logger.info(string);
    }
    
    public void debug(String string) {

    	logger.info(string);
    }
    
    public boolean isDebugEnabled() {
    	
    	return true;
    }
}
