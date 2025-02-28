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

public class MccMncRates
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    // CUSTOMER_INTL_CREDITS
    private static final Log          log                     = LogFactory.getLog(MccMncRates.class);

    private Map<String, IntlSmsRates> mCustomerIntlCreditsMap = new HashMap<>();

    public MccMncRates(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public IntlSmsRates getCustomerCredits(
            String aClientId,
            String aCountry,
            String mcc,
            String mnc)
    {
        return mCustomerIntlCreditsMap.get(CommonUtility.combine(aClientId, aCountry,mcc,mnc));
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // Table Name : client_intl_rates
        final Map<String, IntlSmsRates> lCustomerCreditMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String       lClientId          = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String       lCountry           = CommonUtility.nullCheck(aResultSet.getString("country"), true);
            final String       lMcc           = CommonUtility.nullCheck(aResultSet.getString("mcc"), true);
            final String       lMnc           = CommonUtility.nullCheck(aResultSet.getString("mnc"), true);

            final double       lBaseSmsRate       = CommonUtility.getDouble(aResultSet.getString("base_sms_rate"));
            final double       lBaseAddlFixedRate = CommonUtility.getDouble(aResultSet.getString("base_add_fixed_rate"));

            final IntlSmsRates lIntlSmsRates      = new IntlSmsRates(lBaseSmsRate, lBaseAddlFixedRate);

            lCustomerCreditMap.put(CommonUtility.combine(lClientId, lCountry.toUpperCase(),lMcc,lMnc), lIntlSmsRates);
        }

        if (!lCustomerCreditMap.isEmpty())
            mCustomerIntlCreditsMap = lCustomerCreditMap;
    }

}