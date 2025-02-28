package com.itextos.beacon.inmemory.carrierhandover;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class DNReceiverInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log              log             = LogFactory.getLog(DNReceiverInfo.class);
    private ConcurrentMap<String, String> mDNReceiverInfo = new ConcurrentHashMap<>();

    public DNReceiverInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public ConcurrentMap<String, String> getDNReceiverConnInfo()
    {
        return mDNReceiverInfo;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from dn_http_receiver_info

        // Table : dn_receiver_master

        final ConcurrentMap<String, String> lTempDnReceiverIpInfo = new ConcurrentHashMap<>();

        while (aResultSet.next())
        {
            final String lDNReceiverId   = aResultSet.getString("dn_receiver_id");
            final String lDNReceiverIP   = aResultSet.getString("ip");
            final String lDNReceiverPort = aResultSet.getString("port");

            lTempDnReceiverIpInfo.put(lDNReceiverId, lDNReceiverIP + ":" + lDNReceiverPort);
        }
        mDNReceiverInfo = lTempDnReceiverIpInfo;
    }

}
