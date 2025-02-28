package com.itextos.beacon.inmemory.blocklistnumbers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class BlockListNumberCheck
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log         log                 = LogFactory.getLog(BlockListNumberCheck.class);

    private Map<String, Set<String>> mMobileBlockListMap = new HashMap<>();

    public BlockListNumberCheck(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public boolean isMobileListedInGlobalBlock(
            String aDest)
    {
        // Assuming always the mobile number length is more than 5 characters, as the
        // minimum length is 5.
        final Set<String> lSet = mMobileBlockListMap.get(aDest.substring(0, 5));

        if (lSet != null)
            return lSet.contains(aDest);
        return false;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        // select SUBSTR(mnumber, 1, 5) as prefix, mnumber from
        // listing.block_list_numbers where is_active =1;

        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, Set<String>> tempMobileBlockListMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String      prefix = aResultSet.getString("prefix");
            final Set<String> set    = tempMobileBlockListMap.computeIfAbsent(prefix, k -> new HashSet<>());
            set.add(aResultSet.getString("mnumber"));
        }

        if (!tempMobileBlockListMap.isEmpty())
            mMobileBlockListMap = tempMobileBlockListMap;
    }

}