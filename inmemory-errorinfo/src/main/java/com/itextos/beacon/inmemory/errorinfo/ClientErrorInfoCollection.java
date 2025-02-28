package com.itextos.beacon.inmemory.errorinfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.errorinfo.data.ClientErrorInfo;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class ClientErrorInfoCollection
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log             log                        = LogFactory.getLog(ClientErrorInfoCollection.class);
    private Map<String, ClientErrorInfo> mClientErrorInfoCollection = new HashMap<>();

    public ClientErrorInfoCollection(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    protected ClientErrorInfo getErrorInfo(
            String aClientId,
            String aPlatformErrorCode)
    {
        return mClientErrorInfoCollection.get(CommonUtility.combine(aClientId, aPlatformErrorCode));
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        final Map<String, ClientErrorInfo> lTempErrorCodeMap = new HashMap<>();

        while (aResultSet.next())
        {
            final ClientErrorInfo lErrorInfo = new ClientErrorInfo(CommonUtility.nullCheck(aResultSet.getString("cli_id"), true), //
                    CommonUtility.nullCheck(aResultSet.getString("platform_error_code"), true), //
                    CommonUtility.nullCheck(aResultSet.getString("client_error_code"), true), //
                    CommonUtility.nullCheck(aResultSet.getString("client_error_desc"), true), //
                    CommonUtility.nullCheck(aResultSet.getString("dlr_status"), true));
            lTempErrorCodeMap.put(lErrorInfo.getKey(), lErrorInfo);
        }

        if (!lTempErrorCodeMap.isEmpty())
            mClientErrorInfoCollection = lTempErrorCodeMap;
    }

}