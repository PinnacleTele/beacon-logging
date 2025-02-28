package com.itextos.beacon.commonlib.commondbpool.log;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class DataSourceLog {


    private static final  Logger logger = Logger.getLogger(DataSourceLog.class.getName());
    
    static {
    	 int limit = 1024 * 1024*5; // 1 MB file size limit
         int count = 1; // N

         String folderName="/opt/jboss/wildfly/logs/aux/";
         foldercreaton(folderName);
        String logFileNamePattern =folderName+ "datasource.%g.log";

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
			
		} catch (Exception e) {
		}

     

        // Set the logging level for the logger
    }

  public static void foldercreaton(String folderPath) {
        

        // Create a File object representing the directory
        File folder = new File(folderPath);

        // Check if the directory exists
        if (!folder.exists()) {
            // Attempt to create the directory
            if (folder.mkdirs()) {
                System.out.println("Directory created successfully: " + folderPath);
            } else {
                System.out.println("Failed to create directory: " + folderPath);
            }
        } else {
            System.out.println("Directory already exists: " + folderPath);
        }
    }
    
    public static void log(String string) {

    	logger.info(string);
    	
    }
}
