package com.itextos.beacon.http.interfaceutil.log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemdata.account.ClientAccountDetails;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.itextos.beacon.smslog.LogCustomFormatter;
import com.itextos.beacon.smslog.util.FeatureData;


public class HttpInterfaceLog {


	private static HttpInterfaceLog obj=null;


	private Map<String,Logger> objmap=new HashMap<String,Logger>();
	
	

	
	public static HttpInterfaceLog getInstance(String clientId) {
	
		if(obj==null) {
			
			obj=new HttpInterfaceLog();
			
		}
		
		 final boolean userDebugEnable = CommonUtility.isEnabled(FeatureData.getCutomFeatureValue(clientId, CustomFeatures.USER_DEBUG_LOG));
	
		 if(userDebugEnable) {
		
			 Logger logger =obj.objmap.get(clientId);
		
			 if(logger==null) {
			
			      logger=Logger.getLogger(HttpInterfaceLog.class.getName()+":"+clientId);
			
			      init(logger, clientId);
			
		          obj.objmap.put(clientId,logger );
		    }
		 }
		return obj;
	}
	
	
	private HttpInterfaceLog() {
		
	}
	
	private static void init(Logger logger,String aClientId) {
		

    	
        final UserInfo lUserInfo = ClientAccountDetails.getUserDetailsByClientId(aClientId);

        int limit = 1024 * 1024*5; // 1 MB file size limit
        int count = 1; // N

        String logFileNamePattern = "/opt/jboss/wildfly/logs/http/http_"+lUserInfo.getUserName()+".%g.log";

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
	        fileHandler.setFormatter(new LogCustomFormatter());

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

    public void log(String clientId,String message) {

    	if(objmap.get(clientId)!=null) {
    		
    		objmap.get(clientId).info(message);
    	
    	}
    }
}
