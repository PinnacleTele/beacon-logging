package com.itextos.beacon.inmemory.routeinfo.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class LoadMaskHeaderPool
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log   log               = LogFactory.getLog(LoadMaskHeaderPool.class);

    private List<String> mMaskedHeaderPool = new ArrayList<>();

    public LoadMaskHeaderPool(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public List<String> getMaskedHeaderPool()
    {
        return mMaskedHeaderPool;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // SELECT * FROM mask_pool_senderid

        // Table : header_mask_pool
        final List<String> lTempHeaderPools = new ArrayList<>();

        while (aResultSet.next())
        {
            final String lHeader = CommonUtility.nullCheck(aResultSet.getString("header"), true);

            lTempHeaderPools.add(lHeader.isEmpty() ? Constants.NULL_STRING : lHeader);
        }

        mMaskedHeaderPool = lTempHeaderPools;
    }

}
