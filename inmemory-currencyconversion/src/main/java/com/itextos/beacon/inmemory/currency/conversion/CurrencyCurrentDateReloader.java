package com.itextos.beacon.inmemory.currency.conversion;

import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class CurrencyCurrentDateReloader
        extends
        CurrencyReloader
{
    // configuration.currency_rates_current_date

    public CurrencyCurrentDateReloader(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

}
