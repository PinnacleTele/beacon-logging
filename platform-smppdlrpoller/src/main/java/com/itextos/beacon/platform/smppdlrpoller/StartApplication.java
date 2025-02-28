package com.itextos.beacon.platform.smppdlrpoller;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.errorlog.ErrorLog;
import com.itextos.beacon.platform.smppdlr.fbp.SmppDlrFallbackPollerHolder;
import com.itextos.beacon.platform.smppdlrutil.util.SmppDlrRedis;
import com.itextos.beacon.smslog.StartupFlowLog;

public class StartApplication
{

    private static final Log       log            = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {
    
        try
        {
        	final String cluster1=System.getProperty("cluster");
            
            if(cluster1==null) {

            	String cl=System.getenv("cluster");
            	
            	if(cl!=null) {
            	System.setProperty("cluster", cl);
            	}
            }

            final String modvalue=System.getProperty("modvalue");
            
            if(modvalue==null) {
            	
            	System.setProperty("modvalue", System.getenv("modvalue"));

            } 
    
            // ClientWiseCounter.getInstance().init(null, 0);

             String   lClusters    = System.getProperty("cluster");
            
            if(lClusters==null) {
            
            	System.out.println("System.getenv(\"cluster\")"+" : "+System.getenv("cluster"));
          
            	lClusters=System.getenv("cluster");
            }
            
            
    
            final int lRedisPoolCnt = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.SMPP_SESSION);

            for (int redisIndex = 0; redisIndex < lRedisPoolCnt; redisIndex++)
            {
                if (log.isDebugEnabled())
                    log.debug("RedisIndex:'" + redisIndex + "', Component:'" + Component.SMPP_SESSION + "'");

                final SmppDlrRedis lSmppDlrRedis = new SmppDlrRedis((redisIndex + 1));
            }

            SmppDlrFallbackPollerHolder.getInstance();
            
            StartupFlowLog.log(" SmppDlrFallbackPollerHolder.getInstance() finished");

        }
        catch (final Exception e)
        {
            ErrorLog.log("Exception while starting component "+ ErrorMessage.getStackTraceAsString(e));
            System.exit(-1);
            
        }
    }

}
