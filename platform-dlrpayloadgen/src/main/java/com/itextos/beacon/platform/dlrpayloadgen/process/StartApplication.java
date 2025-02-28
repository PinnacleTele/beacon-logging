package com.itextos.beacon.platform.dlrpayloadgen.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.smslog.DebugLog;

public class StartApplication
{

    private static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {
        if (log.isDebugEnabled())
            log.debug("Starting the application DLR Payload Generator");

        DebugLog.log("Starting the application DLR Payload Generator");
        
        try
        {
             String lCluster = System.getProperty("cluster");
            
            if(lCluster==null) {
            	
            	lCluster=System.getenv("dlrpayloadgen.cluster");
            }

            if (log.isDebugEnabled())
                log.debug("Cluster Type : " + lCluster);

            final String[] lClusters = lCluster.split(",");

            for (final String aCluster : lClusters)
            {
                if (log.isDebugEnabled())
                    log.debug("Cluster Value : " + aCluster);

                final ClusterType lClusterType = ClusterType.getCluster(aCluster);
                StartDnGenerator.startDlrGenerator(lClusterType);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while starting the DN Generator.", e);
            System.exit(-1);
        }
    }

}
