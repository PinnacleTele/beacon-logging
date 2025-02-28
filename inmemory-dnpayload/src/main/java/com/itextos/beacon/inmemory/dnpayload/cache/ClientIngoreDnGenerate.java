package com.itextos.beacon.inmemory.dnpayload.cache;

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

public class ClientIngoreDnGenerate
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log     log                    = LogFactory.getLog(ClientIngoreDnGenerate.class);

    private Map<String, Boolean> mClientIgnoreDlrGenMap = new HashMap<>();

    public ClientIngoreDnGenerate(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public boolean isIgnoreAcc(
            String aClientId)
    {
        final ItextosClient lClient = new ItextosClient(aClientId);

        if (mClientIgnoreDlrGenMap != null)
        {
            if (mClientIgnoreDlrGenMap.get(lClient.getClientId()) != null)
                return mClientIgnoreDlrGenMap.get(lClient.getClientId());

            if (mClientIgnoreDlrGenMap.get(lClient.getAdmin()) != null)
                return mClientIgnoreDlrGenMap.get(lClient.getAdmin());

            if (mClientIgnoreDlrGenMap.get(lClient.getSuperAdmin()) != null)
                return mClientIgnoreDlrGenMap.get(lClient.getSuperAdmin());
        }
        return false;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, Boolean> lTempIgnoreDlrGenMap = new HashMap<>();

        while (aResultSet.next())
            lTempIgnoreDlrGenMap.put(aResultSet.getString("cli_id"), CommonUtility.isEnabled(aResultSet.getString("ignore_or_generate")));

        mClientIgnoreDlrGenMap = lTempIgnoreDlrGenMap;
    }

}
