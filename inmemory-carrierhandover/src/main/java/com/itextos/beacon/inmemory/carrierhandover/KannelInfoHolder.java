package com.itextos.beacon.inmemory.carrierhandover;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.carrierhandover.bean.KannelInfo;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class KannelInfoHolder
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log log                          = LogFactory.getLog(KannelInfoHolder.class);

    private static final int COL_INDEX_ROUTE_ID           = 1;
    private static final int COL_INDEX_KANNEL_IP          = 2;
    private static final int COL_INDEX_KANNEL_PORT        = 3;
    private static final int COL_INDEX_KANNEL_STATUS_PORT = 4;
    private static final int COL_INDEX_STORE_SIZE         = 5;

    Map<String, KannelInfo>  kannelConfigInfo             = new HashMap<>();

    public KannelInfoHolder(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Set<String> getRoutes()
    {
        return kannelConfigInfo.keySet();
    }

    public KannelInfo getRouteConfig(
            String aRoute)
    {
        return kannelConfigInfo.get(CommonUtility.nullCheck(aRoute, true).toLowerCase());
    }

    public Map<String, KannelInfo> getAllRouteConfigs()
    {
        return kannelConfigInfo;
    }

    @Override
    public void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        // select route_id, kannel_ip, kannel_port, kannel_status_port,
        // kannel_store_size_max_limit from route_configuration

        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, KannelInfo> tempKannelInfo = new HashMap<>();

        try
        {

            while (aResultSet.next())
            {
                final KannelInfo ki = new KannelInfo(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_ROUTE_ID), true).toLowerCase(),
                        CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_KANNEL_IP), true), CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_KANNEL_PORT), true),
                        CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_KANNEL_STATUS_PORT), true), CommonUtility.getInteger(aResultSet.getString(COL_INDEX_STORE_SIZE), 0));
                tempKannelInfo.put(ki.getRouteId(), ki);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the route config information.", e);
        }

        if (!tempKannelInfo.isEmpty())
            kannelConfigInfo = tempKannelInfo;
    }

}