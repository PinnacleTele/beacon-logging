package com.itextos.beacon.inmemory.msgutil.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class IntlCredits
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private final Log                 log                    = LogFactory.getLog(IntlCredits.class);

    private Map<String, IntlSmsRates> mIntlCountryCreditsMap = new HashMap<>();

    public IntlCredits(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    // public boolean isCountryHavingCredits(
    // String aCountry)
    // {
    // return mIntlCountryCreditsMap.containsKey(aCountry);
    // }

    public IntlSmsRates getCountryCredits(
            String aCountry)
    {
        return mIntlCountryCreditsMap.get(aCountry);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // Table Name : intl_rates

        final Map<String, IntlSmsRates> lIntlCountryCreditsMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String       lCountry           = CommonUtility.nullCheck(aResultSet.getString("country"), true);
            final double       lBaseSmsRate       = CommonUtility.getDouble(aResultSet.getString("base_sms_rate"));
            final double       lBaseAddlFixedRate = CommonUtility.getDouble(aResultSet.getString("base_add_fixed_rate"));

            final IntlSmsRates lIntlSmsRates      = new IntlSmsRates(lBaseSmsRate, lBaseAddlFixedRate);

            lIntlCountryCreditsMap.put(lCountry.toUpperCase(), lIntlSmsRates);
        }

        if (!lIntlCountryCreditsMap.isEmpty())
            mIntlCountryCreditsMap = lIntlCountryCreditsMap;
    }

}
