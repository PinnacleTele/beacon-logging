package com.itextos.beacon.inmemory.customfeatures;

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

public class InmemAccountCreditsDeductInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log     log                    = LogFactory.getLog(InmemAccountCreditsDeductInfo.class);

    private Map<String, Integer> mAccountCreditsDeducts = new HashMap<>();

    public InmemAccountCreditsDeductInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Integer getCreditValue(
            String aClientId)
    {

        try
        {
            final ItextosClient lClient = new ItextosClient(aClientId);

            if (mAccountCreditsDeducts.get(lClient.getClientId()) != null)
                return mAccountCreditsDeducts.get(lClient.getClientId());

            if (mAccountCreditsDeducts.get(lClient.getAdmin()) != null)
                return mAccountCreditsDeducts.get(lClient.getAdmin());

            if (mAccountCreditsDeducts.get(lClient.getSuperAdmin()) != null)
                return mAccountCreditsDeducts.get(lClient.getSuperAdmin());
        }
        catch (final Exception e)
        {
            log.error("Exception occur while getting Credit Value...", e);
        }
        return 1;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, Integer> loadAccCreditsDeductsInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final String lClientId     = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lCreditDeduct = CommonUtility.nullCheck(aResultSet.getString("credits_deduct_per_part"), true);

            if ("".equals(lClientId) || ("".equals(lCreditDeduct) && "0".equals(lCreditDeduct)))
                continue;

            loadAccCreditsDeductsInfo.put(lClientId, CommonUtility.getInteger(lCreditDeduct));
        }

        if (loadAccCreditsDeductsInfo.size() > 0)
            mAccountCreditsDeducts = loadAccCreditsDeductsInfo;
    }

}
