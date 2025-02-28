package com.itextos.beacon.http.clouddatautil.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class CloudDataConfigInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private Map<String, CloudDataConfig> cloudDataConfig  = new HashMap<>();
    private boolean                      isLoadedFrstTime = false;

    public CloudDataConfigInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        final Map<String, CloudDataConfig> tempCloudDataConfig = new HashMap<>();

        while (aResultSet.next())
        {
            final String accessKey = aResultSet.getString(Constants.DB_AUTH_KEY);
            tempCloudDataConfig.put(accessKey,
                    new CloudDataConfig(aResultSet.getBoolean(Constants.DB_IS_WRITE_RESPONSE_FIRST), aResultSet.getString(Constants.DB_AUTH_KEY), aResultSet.getString(Constants.DB_IP_PARAMTER_KEY),
                            aResultSet.getInt(Constants.DB_TOTAL_THREADS_HIT), aResultSet.getInt(Constants.DB_REDIS_BATCH_SIZE), aResultSet.getInt(Constants.DB_PROCESS_WAIT_SECS),
                            aResultSet.getString(Constants.DB_SWAP_FROM), aResultSet.getString(Constants.DB_SWAP_TO), aResultSet.getString(Constants.DB_CLIENT_IP),
                            aResultSet.getString(Constants.DB_CLIENT_ID)));
        }

        if (tempCloudDataConfig.size() > 0)
        {
            cloudDataConfig  = tempCloudDataConfig;
            isLoadedFrstTime = true;
        }
    }

    public Map<String, CloudDataConfig> getCloudDataConfig()
    {
        return cloudDataConfig;
    }

    public CloudDataConfig getCloudDataConfigUsingAuthKey(
            String authKey)
    {
        return cloudDataConfig.get(authKey);
    }

    public boolean getLoadedFrstTime()
    {
        return isLoadedFrstTime;
    }

}
