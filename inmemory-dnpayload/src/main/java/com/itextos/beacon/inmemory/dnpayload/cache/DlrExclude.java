package com.itextos.beacon.inmemory.dnpayload.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class DlrExclude
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log log             = LogFactory.getLog(DlrExclude.class);
    private List<String>     mDlrExcludeList = new ArrayList<>();

    public DlrExclude(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public boolean isClientDlrExclude(
            String aClientId)
    {
        final ItextosClient lClient = new ItextosClient(aClientId);

        if (log.isDebugEnabled())
            log.debug("Client Id : " + lClient);

        if (mDlrExcludeList.contains(lClient.getClientId()))
            return true;

        if (mDlrExcludeList.contains(lClient.getAdmin()))
            return true;

        return mDlrExcludeList.contains(lClient.getSuperAdmin());
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from dn_percentage_exempt
        // Table Naame : dn_gen_percentage_exempt

        final List<String> lTempDlrExcludesList = new ArrayList<>();

        while (aResultSet.next())
        {
            final String lClientId = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);

            if (lClientId.isEmpty())
                continue;

            lTempDlrExcludesList.add(lClientId);
        }

        if (!lTempDlrExcludesList.isEmpty())
            mDlrExcludeList = lTempDlrExcludesList;
    }

}
