package com.itextos.beacon.errorlog;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SMSLog {


    private static final  Logger logger = Logger.getLogger(SMSLog.class.getName());
    
    private static SMSLog obj=new SMSLog();
    
    static {
    	

    	 int limit = 1024 * 1024*5; // 1 MB file size limit
         int count = 1; // N
        String logFileNamePattern = "/opt/jboss/wildfly/logs/smslog.%g.log";

        Level loglevel=Level.INFO;
        
        String loglevelFromEnr=System.getenv("loglevel");
        
        if(loglevelFromEnr!=null) {
        
        	if(loglevelFromEnr.equals("all")) {
        		
        		loglevel=Level.ALL;
        		
        	}else if(loglevelFromEnr.equals("off")) {
        		
        		loglevel=Level.OFF;
        	}
        }
        logger.setUseParentHandlers(false);
        
        // Create a FileHandler with the specified log file name
        FileHandler fileHandler=null;
		try {

			fileHandler = new FileHandler(logFileNamePattern, limit, count, true);
			   // Set the logging level for the handler
	        fileHandler.setLevel(loglevel);

	        // Set a formatter for the handler (optional)
	        fileHandler.setFormatter(new SMSLogCustomFormatter());

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

    public static SMSLog getInstance() {
    	
    	if(obj==null) {
    		
    		 obj=new SMSLog();
    	}
    	
    	return obj;
    }
    
    public SMSLog append(String message) {
    	
    	log(message);
    	
    	return obj;
    }
    
 public SMSLog append(int message) {
    	
    	log(""+message);
    	
    	return obj;
    }
    public static void log(String string) {

    	logger.info(string);
    	
    }
}
