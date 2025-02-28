package com.itextos.beacon.inmemory.otpconfig;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;
import com.itextos.beacon.inmemory.otpconfig.cache.OtpConfig;

public class InmemOtpConfig
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log       log        = LogFactory.getLog(InmemOtpConfig.class);

    private Map<String, OtpConfig> mOtpConfig = new HashMap<>();

    public InmemOtpConfig(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public OtpConfig getOtpConfig(
            String aClientId)
    {
        return mOtpConfig.get(aClientId);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException

    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, OtpConfig> lOtpConfigMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String lCliid = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);

            if (lCliid.isEmpty())
                continue;

            final boolean   isMobileNoRequired    = CommonUtility.isEnabled(aResultSet.getString("mobile_no_required"));
            final boolean   isIncludeNumbers      = CommonUtility.isEnabled(aResultSet.getString("include_numbers"));
            final boolean   isAlphabetsUpper      = CommonUtility.isEnabled(aResultSet.getString("include_alphabets_upper"));
            final boolean   isAplhabetsLower      = CommonUtility.isEnabled(aResultSet.getString("include_alphabets_lower"));
            final boolean   isAllowSpecialChar    = CommonUtility.isEnabled(aResultSet.getString("include_special_chars"));
            final int       lOtpLength            = CommonUtility.getInteger(aResultSet.getString("otp_length"));
            final String    lAllowedSpecialChars  = CommonUtility.nullCheck(aResultSet.getString("allowed_special_chars"));
            final int       lExpiryDurationMins   = CommonUtility.getInteger(aResultSet.getString("expiry_duration_mins"));
            final boolean   lGenerateOtpInExpTime = CommonUtility.isEnabled(aResultSet.getString("regenerate_otp_in_expiry_time"));

            final OtpConfig lOtpConfig            = new OtpConfig(isMobileNoRequired, lOtpLength, isIncludeNumbers, isAlphabetsUpper, isAplhabetsLower, isAllowSpecialChar, lAllowedSpecialChars,
                    lExpiryDurationMins, lGenerateOtpInExpTime);

            lOtpConfigMap.put(lCliid, lOtpConfig);
        }

        if (!lOtpConfigMap.isEmpty())
            mOtpConfig = lOtpConfigMap;
    }

}
