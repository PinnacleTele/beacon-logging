package com.itextos.beacon.inmemory.msgutil.cache;

public class IntlSmsRates
{

    private final double mBaseSmsRate;
    private final double mBaseAddlFixedRate;

    public IntlSmsRates(
            double aBaseSmsRate,
            double aBaseAddlFixedRate)
    {
        super();
        mBaseSmsRate       = aBaseSmsRate;
        mBaseAddlFixedRate = aBaseAddlFixedRate;
    }

    public double getBaseSmsRate()
    {
        return mBaseSmsRate;
    }

    public double getBaseAddlFixedRate()
    {
        return mBaseAddlFixedRate;
    }

    @Override
    public String toString()
    {
        return "IntlSmsRates [mBaseSmsRate=" + mBaseSmsRate + ", mBaseAddlFixedRate=" + mBaseAddlFixedRate + "]";
    }

}
