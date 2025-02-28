package com.itextos.beacon.smslog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class KafkaReceiver {


	private static KafkaReceiver obj=null;


	private Map<String,Logger> objmap=new HashMap<String,Logger>();
	
	

	
	public static KafkaReceiver getInstance(String nextcomponent) {
	
		if(obj==null) {
			
			obj=new KafkaReceiver();
			
		}
		
		Logger logger =obj.objmap.get(nextcomponent);
		
		if(logger==null) {
			
			logger=Logger.getLogger(KafkaReceiver.class.getName()+":"+nextcomponent);
			
			init(logger, nextcomponent);
			
			obj.objmap.put(nextcomponent,logger );
		}
		return obj;
	}
	
	
	private KafkaReceiver() {
		
	}
	
	private static void init(Logger logger,String nextcomponent) {
		

    	
        int limit = 1024 * 1024*5; // 1 MB file size limit
        int count = 1; // N

        String logFileNamePattern = "/opt/jboss/wildfly/logs/kafkareceiver/kafkareceiver_"+nextcomponent+".%g.log";

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

    public void log(String nextcomponent,String message) {

    	objmap.get(nextcomponent).info(message);
    	
    }
}
