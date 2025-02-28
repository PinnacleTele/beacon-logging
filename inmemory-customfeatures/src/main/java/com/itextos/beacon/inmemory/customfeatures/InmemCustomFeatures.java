package com.itextos.beacon.inmemory.customfeatures;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;
import com.itextos.beacon.errorlog.ErrorLog;
public class InmemCustomFeatures
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log    log                = LogFactory.getLog(InmemCustomFeatures.class);
    private static final String ALL_CLIENTS        = "*";
    private Map<String, String> mAccCustomFeatures = new HashMap<>();

    public InmemCustomFeatures(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public String getValueOfCustomFeature(
            String aClientId,
            String aCustomFeature)
    {
        aCustomFeature = aCustomFeature.toLowerCase();

        if (ALL_CLIENTS.equals(aClientId))
        {
            if (log.isDebugEnabled())
                log.debug("Client Id comes as * for Custom Feature " + aCustomFeature);
            return mAccCustomFeatures.get(CommonUtility.combine(aClientId, aCustomFeature));
        }

       try {
        final ItextosClient lClient = new ItextosClient(aClientId);

        String              key     = CommonUtility.combine(lClient.getClientId(), aCustomFeature);

        if (mAccCustomFeatures.get(key) != null)
            return mAccCustomFeatures.get(key);

        key = CommonUtility.combine(lClient.getAdmin(), aCustomFeature);
        if (mAccCustomFeatures.get(key) != null)
            return mAccCustomFeatures.get(key);

        key = CommonUtility.combine(lClient.getSuperAdmin(), aCustomFeature);

        if (mAccCustomFeatures.get(key) != null)
            return mAccCustomFeatures.get(key);

        }catch(Exception e) {
        	
        	ErrorLog.log(" getValueOfCustomFeature error :  "+ErrorMessage.getStackTraceAsString(e));
        }
         
        return null;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, String> loadCustsomFeaturesInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final String lClientId     = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lFeature      = CommonUtility.nullCheck(aResultSet.getString("feature"), true);
            final String lFeatureValue = CommonUtility.nullCheck(aResultSet.getString("value"), true);

            if ("".equals(lClientId) || "".equals(lFeature) || "".equals(lFeatureValue))
                continue;

            loadCustsomFeaturesInfo.put(CommonUtility.combine(lClientId, lFeature), lFeatureValue);
        }

        if (loadCustsomFeaturesInfo.size() > 0)
            mAccCustomFeatures = loadCustsomFeaturesInfo;
    }

}