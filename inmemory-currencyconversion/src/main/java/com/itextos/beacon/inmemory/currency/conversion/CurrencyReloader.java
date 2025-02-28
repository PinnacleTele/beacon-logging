package com.itextos.beacon.inmemory.currency.conversion;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public abstract class CurrencyReloader
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                 log                      = LogFactory.getLog(CurrencyReloader.class);

    public static final double               INVALID_CONVERSION_VALUE = -999999999D;

    private Map<String, Map<String, Double>> mCurrentDateConversion   = new HashMap<>();

    CurrencyReloader(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public double getConversionRate(
            String aFromCurrency,
            String aToCurrency)
    {
        Double                    returnValue = INVALID_CONVERSION_VALUE;

        final Map<String, Double> lMap        = mCurrentDateConversion.get(aFromCurrency);

        if (lMap != null)
        {
            returnValue = lMap.get(aToCurrency);

            if (returnValue == null)
            {
                log.error("No currency conversion rate available from '" + aFromCurrency + "' to '" + aToCurrency + "'");
                returnValue = INVALID_CONVERSION_VALUE;
            }
        }
        else
            log.error("Currency '" + aFromCurrency + "' does not have any conversion rate..");

        return returnValue;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        final Map<String, Map<String, Double>> tempCurrentDateConversion = new HashMap<>();

        while (aResultSet.next())
        {
            final String fromCur        = aResultSet.getString(1);
            final String toCur          = aResultSet.getString(2);
            final double conversionRate = aResultSet.getDouble(3);

            if (conversionRate <= 0)
            {
                log.error("Conversion is not valid for '" + fromCur + "' to '" + toCur + "'");
                continue;
            }

            Map<String, Double> toCurrencyMap = tempCurrentDateConversion.computeIfAbsent(fromCur, k -> new HashMap<>());
            toCurrencyMap.put(toCur, conversionRate);

            toCurrencyMap = tempCurrentDateConversion.computeIfAbsent(toCur, k -> new HashMap<>());
            toCurrencyMap.put(fromCur, 1.0 / conversionRate);
        }

        if (!tempCurrentDateConversion.isEmpty())
            mCurrentDateConversion = tempCurrentDateConversion;
    }

}