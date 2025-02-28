package com.itextos.beacon.inmemory.configvalues;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class ApplicationConfiguration
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    public ApplicationConfiguration(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    private static Log          log           = LogFactory.getLog(ApplicationConfiguration.class);

    private Map<String, String> mConfigValues = new HashMap<>();

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, String> loadConfigValues = new HashMap<>();

        while (aResultSet.next())
        {
            final String lParamKey = CommonUtility.nullCheck(aResultSet.getString("param_key"), true);
            final String lKeyvalue = CommonUtility.nullCheck(aResultSet.getString("key_value"), true);

            if ("".equals(lParamKey) || "".equals(lKeyvalue))
                continue;

            loadConfigValues.put(lParamKey, lKeyvalue);
        }

        if (loadConfigValues.size() > 0)
            mConfigValues = loadConfigValues;
    }

    /**
     * Use {@link #getConfigValue(ConfigParamConstants)} method.
     *
     * @param aKey
     *
     * @return
     *
     * @deprecated
     */
    @Deprecated
    public String getConfigValue(
            String aKey)
    {
        final String lKey = CommonUtility.nullCheck(aKey, true).toLowerCase();
        return mConfigValues.get(lKey);
    }

    public String getConfigValue(
            ConfigParamConstants aConfigParamConstants)
    {
        final String lKey = aConfigParamConstants != null ? aConfigParamConstants.getKey().toLowerCase() : "";
        return mConfigValues.get(lKey);
    }

}