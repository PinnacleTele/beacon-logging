package com.itextos.beacon.platform.dnpcore.poller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;

public class NoPayloadRetryPollerHolder
{

    private static final Log log = LogFactory.getLog(NoPayloadRetryPollerHolder.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final NoPayloadRetryPollerHolder INSTANCE = new NoPayloadRetryPollerHolder();

    }

    public static NoPayloadRetryPollerHolder getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<ClusterType, AbstractDataPoller> allReaders = new HashMap<>();

    private NoPayloadRetryPollerHolder()
    {
        startPollars();
    }

    private void startPollars()
    {
        String lCluster = System.getProperty("cluster");

        if(lCluster==null) {
        
        	lCluster = System.getenv("cluster");
        }
        
        if(lCluster!=null) {
            
        	
       
        if (log.isDebugEnabled())
            log.debug("Cluster Type : " + lCluster);

        final String[] lClusters = lCluster.split(",");

        for (final String aCluster : lClusters)
        {
            if (log.isDebugEnabled())
                log.debug("Cluster Value : " + aCluster);

            final ClusterType lClusterType = ClusterType.getCluster(aCluster);

            allReaders.put(lClusterType, new NoPayloadRetryPoller(lClusterType));
        }
        
        
        }
    }

    public void stopMe()
    {

        for (final Map.Entry<ClusterType, AbstractDataPoller> entry : allReaders.entrySet())
        {
            final AbstractDataPoller lPoller = entry.getValue();
            lPoller.stopMe();
        }
    }

}
