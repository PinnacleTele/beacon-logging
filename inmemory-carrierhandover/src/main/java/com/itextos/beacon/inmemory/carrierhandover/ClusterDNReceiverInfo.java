package com.itextos.beacon.inmemory.carrierhandover;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.RoundRobin;
import com.itextos.beacon.inmemory.carrierhandover.util.ICHUtil;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class ClusterDNReceiverInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                        log                   = LogFactory.getLog(ClusterDNReceiverInfo.class);

    private ConcurrentHashMap<String, List<String>> mClusterDnReceiverMap = new ConcurrentHashMap<>();

    public ClusterDNReceiverInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public String getDlrUrlInfo(
            String aCluster)
    {

        try
        {
            String dnReceiverId = null;

            // get receiverid from cluster
            if (aCluster != null)
            {
            	
            	String profile=System.getenv("profile");
            	
                final List<String> dnReceiverIds = mClusterDnReceiverMap.get(profile.toLowerCase()+"~"+aCluster.toLowerCase());

                if (!dnReceiverIds.isEmpty())
                {
                    final int index = RoundRobin.getInstance().getCurrentIndex(aCluster, dnReceiverIds.size());
                    dnReceiverId = dnReceiverIds.get(index - 1);
                    if (log.isInfoEnabled())
                        log.info("clusterwise===>" + aCluster + " dnReceiverId===>" + dnReceiverId);
                }
            }
            if (log.isInfoEnabled())
                log.info("dnReceiverId==>" + dnReceiverId);

            final String actualDnReceiverID = dnReceiverId;
            String       ipColonPort        = null;

            if (dnReceiverId != null)
            {
                ipColonPort = ICHUtil.getDNReceiverConnInfo(actualDnReceiverID);
                log.info("ipColonPort==>" + ipColonPort);
                return ipColonPort;
            }
        }
        catch (final Exception exp)
        {
            log.error("problem framing dlr url getDlrUrl()....", exp);
        }
        return null;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from cluster_dn_receiver_http_info

        // Table : dn_receiver_info

        final ConcurrentHashMap<String, List<String>> lTempClusterDNReceiverIdMap = new ConcurrentHashMap<>();

        while (aResultSet.next())
        {
            final String       lCluster        = aResultSet.getString("cluster").toLowerCase();
            final String       lProfile        = aResultSet.getString("profile").toLowerCase();
            final String       lDnReceiverId   = aResultSet.getString("dn_receiver_id").toLowerCase();

            final List<String> lDnReceiverIdLs = lTempClusterDNReceiverIdMap.computeIfAbsent(lProfile+"~"+lCluster, k -> new ArrayList<>());

            lDnReceiverIdLs.add(lDnReceiverId);
        }
        mClusterDnReceiverMap = lTempClusterDNReceiverIdMap;
    }

}
