package com.itextos.beacon.platform.dnrfallbackpoller.process;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;

public class DlrFallbackPollerHolder
{

    private static final Log log = LogFactory.getLog(DlrFallbackPollerHolder.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final DlrFallbackPollerHolder INSTANCE = new DlrFallbackPollerHolder();

    }

    public static DlrFallbackPollerHolder getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<ClusterType, AbstractDataPoller> allReaders = new HashMap<>();

    private DlrFallbackPollerHolder()
    {
        startPollars();
    }

    private void startPollars()
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

            allReaders.put(lClusterType, new DlrFallbackPoller(lClusterType));
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
