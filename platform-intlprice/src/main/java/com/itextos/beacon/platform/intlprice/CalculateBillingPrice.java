package com.itextos.beacon.platform.intlprice;

public class CalculateBillingPrice
{

    private final String  mClientId;
    private final double  mBaseSmsRate;
    private final double  mBaseAdditionalFixedRate;

    private final String  mFromCurrency;
    private final String  mBillingCurrency;
    private final String  mRefCurrency;
    private final boolean mConvertDatewise;

    private double        mBillingConversionRate;
    private double        mBillingSmsRate;
    private double        mBillingAdditionalFixedRate;

    private double        mRefConversionRate;
    private double        mRefSmsRate;
    private double        mRefAdditionalFixedRate;

    public CalculateBillingPrice(
            String aClientId,
            double aBaseSmsRate,
            double aBaseAdditionalFixedRate,
            String aFromCurrency,
            String aBillingCurrency,
            String aRefCurrency,
            boolean aConvertDatewise)
    {
        super();
        mClientId                = aClientId;
        mBaseSmsRate             = aBaseSmsRate;
        mBaseAdditionalFixedRate = aBaseAdditionalFixedRate;
        mFromCurrency            = aFromCurrency;
        mBillingCurrency         = aBillingCurrency;
        mRefCurrency             = aRefCurrency;
        mConvertDatewise         = aConvertDatewise;
    }

    public double getBillingConversionRate()
    {
        return mBillingConversionRate;
    }

    public void setBillingConversionRate(
            double aBillingConversionRate)
    {
        mBillingConversionRate = aBillingConversionRate;
    }

    public double getBillingSmsRate()
    {
        return mBillingSmsRate;
    }

    public void setBillingSmsRate(
            double aBillingSmsRate)
    {
        mBillingSmsRate = aBillingSmsRate;
    }

    public double getBillingAdditionalFixedRate()
    {
        return mBillingAdditionalFixedRate;
    }

    public void setBillingAdditionalFixedRate(
            double aBillingAdditionalFixedRate)
    {
        mBillingAdditionalFixedRate = aBillingAdditionalFixedRate;
    }

    public double getRefConversionRate()
    {
        return mRefConversionRate;
    }

    public void setRefConversionRate(
            double aRefConversionRate)
    {
        mRefConversionRate = aRefConversionRate;
    }

    public double getRefSmsRate()
    {
        return mRefSmsRate;
    }

    public void setRefSmsRate(
            double aRefSmsRate)
    {
        mRefSmsRate = aRefSmsRate;
    }

    public double getRefAdditionalFixedRate()
    {
        return mRefAdditionalFixedRate;
    }

    public void setRefAdditionalFixedRate(
            double aRefAdditionalFixedRate)
    {
        mRefAdditionalFixedRate = aRefAdditionalFixedRate;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public double getBaseSmsRate()
    {
        return mBaseSmsRate;
    }

    public double getBaseAdditionalFixedRate()
    {
        return mBaseAdditionalFixedRate;
    }

    public String getFromCurrency()
    {
        return mFromCurrency;
    }

    public String getBillingCurrency()
    {
        return mBillingCurrency;
    }

    public String getRefCurrency()
    {
        return mRefCurrency;
    }

    public boolean isConvertDatewise()
    {
        return mConvertDatewise;
    }

}