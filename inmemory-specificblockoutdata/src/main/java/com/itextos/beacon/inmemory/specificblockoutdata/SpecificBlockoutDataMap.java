package com.itextos.beacon.inmemory.specificblockoutdata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class SpecificBlockoutDataMap
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                        log                   = LogFactory.getLog(SpecificBlockoutDataMap.class);
    private static final String                     CONSTANT_ONE          = "1";

    private Map<String, List<SpecificBlockoutData>> mSpecificblockoutData = new HashMap<>();

    public SpecificBlockoutDataMap(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public List<SpecificBlockoutData> getSpecificBlockoutData(
            String aClientId,
            String aMobilenumber)
    {
        final String               key   = CommonUtility.combine(aClientId, aMobilenumber);
        List<SpecificBlockoutData> lList = mSpecificblockoutData.get(key);
        if (lList == null)
            lList = mSpecificblockoutData.get(aClientId);
        return lList == null ? new ArrayList<>() : lList;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, List<SpecificBlockoutData>> tempSpecificblockoutData = new HashMap<>();

        while (aResultSet.next())
        {
            // If dest is null, take care of the case.
            final String               lClientId      = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String               lMobileNo      = CommonUtility.nullCheck(aResultSet.getString("dest"), true);
            final String               lDropMessage   = CommonUtility.nullCheck(aResultSet.getString("drop_msg"), true);
            final String               lMsgPattern    = CommonUtility.nullCheck(aResultSet.getString("msg_pattern"), true);
            final String               lBlockOutStart = CommonUtility.nullCheck(aResultSet.getString("blockout_start"), true);
            final String               lBlockOutStop  = CommonUtility.nullCheck(aResultSet.getString("blockout_end"), true);

            final SpecificBlockoutData lBlockoutData  = new SpecificBlockoutData(lClientId, lMobileNo, lMsgPattern, lBlockOutStart, lBlockOutStop, CONSTANT_ONE.equals(lDropMessage));

            if (!lBlockoutData.isValid())
            {
                if (log.isDebugEnabled())
                    log.debug("Cannot use the Specific blockout data " + lBlockoutData);
                continue;
            }
            final List<SpecificBlockoutData> tempList = tempSpecificblockoutData.computeIfAbsent(lBlockoutData.getKey(), k -> new ArrayList<>());
            tempList.add(lBlockoutData);
        }
        if (!tempSpecificblockoutData.isEmpty())
            mSpecificblockoutData = tempSpecificblockoutData;
    }

}