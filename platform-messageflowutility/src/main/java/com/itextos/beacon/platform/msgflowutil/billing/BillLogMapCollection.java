package com.itextos.beacon.platform.msgflowutil.billing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class BillLogMapCollection
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log        log          = LogFactory.getLog(BillLogMapCollection.class);

    private Map<String, BillLogMap> mMappingInfo = new HashMap<>();

    public BillLogMapCollection(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public BillLogMap getBillLogInfo(
            String aClientId)
    {
        final ItextosClient lClient     = new ItextosClient(aClientId);
        BillLogMap          lBillLogMap = mMappingInfo.get(lClient.getClientId());

        if (lBillLogMap == null)
            lBillLogMap = mMappingInfo.get(lClient.getAdmin());

        if (lBillLogMap == null)
            lBillLogMap = mMappingInfo.get(lClient.getSuperAdmin());

        return lBillLogMap;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // configuration.bill_log_map

        final Map<String, BillLogMap> lTempMappingInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final String lClientId   = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lJndiInfoId = CommonUtility.nullCheck(aResultSet.getString("jndi_info_id"), true);
            final String lSuffix     = CommonUtility.nullCheck(aResultSet.getString("suffix"), true);

            if (lClientId.isEmpty() || lJndiInfoId.isEmpty() || lSuffix.isEmpty())
                continue;

            lTempMappingInfo.put(lClientId, new BillLogMap(lClientId, lJndiInfoId, lSuffix));
        }

        if (!lTempMappingInfo.isEmpty())
            mMappingInfo = lTempMappingInfo;
    }

}
