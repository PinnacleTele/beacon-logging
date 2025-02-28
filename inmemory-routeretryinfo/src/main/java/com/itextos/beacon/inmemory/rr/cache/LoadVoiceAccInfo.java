package com.itextos.beacon.inmemory.rr.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class LoadVoiceAccInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log    log          = LogFactory.getLog(LoadVoiceAccInfo.class);

    private Map<String, Object> mVoiceAccMap = new HashMap<>();

    public LoadVoiceAccInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, Object> getVoiceAccountInfo()
    {
        return mVoiceAccMap;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from OTP_SMS_VOICE_ACCOUNT_MAPPING

        // Table Name : otp_voice_connect_map
        final Map<String, Object> lTempVoiceAccInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final String lClientId       = aResultSet.getString("cli_id");
            final String lVoiceCfgId     = aResultSet.getString("voice_config_id");

            final String lKey            = CommonUtility.combine(lClientId, lVoiceCfgId);

            final Object lColumnValueMap = CommonUtility.getMapFromResultSet(aResultSet, aResultSet.getMetaData());
            lTempVoiceAccInfo.put(lKey, lColumnValueMap);
        }

        mVoiceAccMap = lTempVoiceAccInfo;
    }

}
