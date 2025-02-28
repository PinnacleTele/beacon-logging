package com.itextos.beacon.inmemory.loader.sample;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class SampleUserConfiguration
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log      log                 = LogFactory.getLog(SampleUserConfiguration.class);

    private Map<String, SampleUserInfo> mUserInfoCollection = new HashMap<>();

    public SampleUserConfiguration(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    @Override
    protected void processResultSet(
            ResultSet mResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, SampleUserInfo> localUserInfoCollection = new HashMap<>();

        while (mResultSet.next())
        {
            final SampleUserInfo ui = new SampleUserInfo(mResultSet.getString("cli_id"), mResultSet.getString("access_key"));
            localUserInfoCollection.put(ui.getClientId(), ui);
        }

        if (localUserInfoCollection.size() > 0)
            mUserInfoCollection = localUserInfoCollection;
    }

    public SampleUserInfo getUserInfo(
            String aClientId)
    {
        return mUserInfoCollection.get(aClientId);
    }

}
