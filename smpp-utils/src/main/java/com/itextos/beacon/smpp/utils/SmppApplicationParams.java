package com.itextos.beacon.smpp.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

public class SmppApplicationParams
{

    private static final Log log = LogFactory.getLog(SmppApplicationParams.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final SmppApplicationParams INSTANCE = new SmppApplicationParams();

    }

    public static SmppApplicationParams getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private List<ClusterType> lClusterList;

    private SmppApplicationParams()
    {
        loadClusterTypes();
    }

    private void loadClusterTypes()
    {
    	
    	
        final String lArgClusters = System.getenv("cluster.names");

        System.out.println("lArgClusters : "+lArgClusters);
        
        if (log.isDebugEnabled())
            log.debug("Runtime Argument Cluster :" + lArgClusters);

        if ((lArgClusters == null) || lArgClusters.isBlank())
        {
            final String s = "Cluster type is not specified in the runtime";
            log.fatal(s, new Throwable(s));
            log.fatal("Exiting the application.");
          //  throw new ItextosRuntimeException(s);
            System.exit(-1);
        }
        lClusterList = getClusters(lArgClusters.split(","));
    }

    private static List<ClusterType> getClusters(
            String[] aSplit)
    {
        final List<ClusterType> returnValue = new ArrayList<>();
        if (aSplit != null)
            for (final String lCluster : aSplit)
            {
                final ClusterType lClusterType = ClusterType.getCluster(lCluster);
                if (lClusterType != null)
                    returnValue.add(lClusterType);
            }
        return returnValue;
    }

    public List<ClusterType> getClusters()
    {
        return lClusterList;
    }

}
