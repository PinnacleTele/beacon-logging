package com.itextos.beacon.platform.walletbase.data;

public class WalletUpdateInput
{

    private final String mClientId;
    private final double mAmount;

    public WalletUpdateInput(
            String aClientId,
            double aAmount)
    {
        super();
        mClientId = aClientId;
        mAmount   = aAmount;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public double getAmount()
    {
        return mAmount;
    }

    @Override
    public String toString()
    {
        return "WalletUpdateInput [mClientId=" + mClientId + ", mAmount=" + mAmount + "]";
    }

}