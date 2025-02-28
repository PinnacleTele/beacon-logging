package com.itextos.beacon.inmemory.rr.cache;

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

public class LoadAlertWaitConfig
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                 log              = LogFactory.getLog(LoadAlertWaitConfig.class);

    private Map<String, Map<String, String>> mAlertWaitConfig = new HashMap<>();

    public LoadAlertWaitConfig(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, String> getAlertWaitConfig(
            String aPriority,
            String aMsgType,
            String aRetryAttempt,
            String aClientId)
    {
        final ItextosClient lClient = new ItextosClient(aClientId);

        String              lKey    = CommonUtility.combine(aPriority, aMsgType, aRetryAttempt, lClient.getClientId());

        if (log.isDebugEnabled())
            log.debug("alertConfig>>" + lKey + ">>" + mAlertWaitConfig);

        Map<String, String> lAlertWaitConfig = mAlertWaitConfig.get(lKey);

        if (lAlertWaitConfig == null)
        {
            lKey             = CommonUtility.combine(aPriority, aMsgType, aRetryAttempt, lClient.getAdmin());
            lAlertWaitConfig = mAlertWaitConfig.get(lKey);
        }

        if (lAlertWaitConfig == null)
        {
            lKey             = CommonUtility.combine(aPriority, aMsgType, aRetryAttempt, lClient.getSuperAdmin());

            lAlertWaitConfig = mAlertWaitConfig.get(lKey);
        }

        if (lAlertWaitConfig == null)
        {
            lKey             = CommonUtility.combine(aPriority, aMsgType, aRetryAttempt, "0");

            lAlertWaitConfig = mAlertWaitConfig.get(lKey);
        }
        return lAlertWaitConfig;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from alert_wait_config

        // Table Name : alert_wait_config

        final Map<String, Map<String, String>> lTempAlertWaitConfig = new HashMap<>();

        while (aResultSet.next())
        {
            final String              lKey      = CommonUtility.combine(aResultSet.getString("priority"), aResultSet.getString("msg_type"), aResultSet.getString("retry_attempt"),
                    aResultSet.getString("cli_id"));
            final Map<String, String> lValueMap = new HashMap<>();
            lValueMap.put("action", aResultSet.getString("action"));
            lValueMap.put("alert_wait_time", aResultSet.getString("alert_wait_time"));

            lTempAlertWaitConfig.put(lKey, lValueMap);
        }
        mAlertWaitConfig = lTempAlertWaitConfig;
    }

}
