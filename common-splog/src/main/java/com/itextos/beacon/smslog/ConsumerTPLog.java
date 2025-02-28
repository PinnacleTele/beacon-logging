package com.itextos.beacon.smslog;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class ConsumerTPLog {


    private  final  Logger logger = Logger.getLogger(ConsumerTPLog.class.getName());

	private static LinkedHashMap<String,ConsumerTPLog> objmap=new LinkedHashMap<String,ConsumerTPLog>();
	
	
	public static synchronized ConsumerTPLog getInstance(String topicname) {
	
		synchronized (objmap) {
			
			ConsumerTPLog obj=objmap.get(topicname);
			
			if(obj==null) {
				
				obj=new ConsumerTPLog(topicname);
				
				objmap.put(topicname, obj);
			}
			
			return obj;
		}
			
			

		
		
		
		
	}
	
	
	private ConsumerTPLog() {
		
	}
	
	private ConsumerTPLog(String username) {
		

    	
        int limit = 1024 * 5; // 1 MB file size limit
        int count = 1; // N

        String logFileNamePattern = "/opt/jboss/wildfly/logs/consumer/consumertplog_"+username+".%g.log";

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

    public void log(String string) {

    	logger.info(string);
    	
    }
}
