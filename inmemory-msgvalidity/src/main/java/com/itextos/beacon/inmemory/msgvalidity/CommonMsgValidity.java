package com.itextos.beacon.inmemory.msgvalidity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class CommonMsgValidity
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log     log              = LogFactory.getLog(CommonMsgValidity.class);

    private Map<String, Integer> mMsgValidityInfo = new HashMap<>();

    public CommonMsgValidity(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public int getMessageValidity(
            String aMsgType,
            String aPriority)
    {
        return mMsgValidityInfo.get(CommonUtility.combine(aPriority, aMsgType));
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, Integer> lMsgValidityMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String lPriority = aResultSet.getString("priority");
            lMsgValidityMap.put(CommonUtility.combine(lPriority, "1"), aResultSet.getInt("txn_validity"));
            lMsgValidityMap.put(CommonUtility.combine(lPriority, "0"), aResultSet.getInt("promo_validity"));
        }
        mMsgValidityInfo = lMsgValidityMap;
    }

}