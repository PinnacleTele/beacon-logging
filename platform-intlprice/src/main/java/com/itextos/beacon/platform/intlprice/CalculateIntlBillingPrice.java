package com.itextos.beacon.platform.intlprice;

import com.itextos.beacon.commonlib.constants.exception.InternationalSMSRateNotAvailableRuntimeException;
import com.itextos.beacon.inmemory.msgutil.cache.IntlSmsRates;

public class CalculateIntlBillingPrice
{

    private final String  mClientId;
    private final String  mCountry;
    private final String  mMcc;
    private final String  mMnc;

    private final String  mFromCurrency;
    private final String  mBillingCurrency;
    private final String  mRefCurrency;
    private final boolean mConvertDatewise;

    public CalculateIntlBillingPrice(
            String aClientId,
            String aCountry,
            String aMcc,
            String aMnc,
            String aFromCurrency,
            String aBillingCurrency,
            String aRefCurrency,
            boolean aConvertDatewise)
    {
        super();
        mClientId        = aClientId;
        mCountry         = aCountry;
        mFromCurrency    = aFromCurrency;
        mBillingCurrency = aBillingCurrency;
        mRefCurrency     = aRefCurrency;
        mConvertDatewise = aConvertDatewise;
        mMcc=aMcc;
        mMnc=aMnc;
    }

    public CalculateBillingPrice calculate() throws InternationalSMSRateNotAvailableRuntimeException
    {
        final IntlSmsRates          lIntlPrice             = CurrencyUtil.getIntlPrice(mClientId, mCountry,mMcc,mMnc);
        final CalculateBillingPrice lCalculateBillingPrice = new CalculateBillingPrice(mClientId, lIntlPrice.getBaseSmsRate(), lIntlPrice.getBaseAddlFixedRate(), mFromCurrency, mBillingCurrency,
                mRefCurrency, mConvertDatewise);
        CurrencyUtil.getBillingPrice(lCalculateBillingPrice);
        return lCalculateBillingPrice;
    }

}