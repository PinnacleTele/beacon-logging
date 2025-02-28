package com.itextos.beacon.platform.walletbase.data;

public class WalletResult
{

    private final boolean status;
    private final double  smsRate;
    private final double  dltRate;
    private final int     noOfParts;

    private WalletResult(
            boolean aResult)
    {
        this(aResult, -1, -1, -1);
    }

    private WalletResult(
            boolean aStatus,
            double aSmsRate,
            double aDltRate,
            int aNoOfParts)
    {
        super();
        status    = aStatus;
        smsRate   = aSmsRate;
        dltRate   = aDltRate;
        noOfParts = aNoOfParts;
    }

    public boolean isSuccess()
    {
        return status;
    }

    public double getSmsRate()
    {
        return smsRate;
    }

    public double getDltRate()
    {
        return dltRate;
    }

    public int getNoOfParts()
    {
        return noOfParts;
    }

    @Override
    public String toString()
    {
        return "WalletResult [status=" + status + ", smsRate=" + smsRate + ", dltRate=" + dltRate + ", noOfParts=" + noOfParts + "]";
    }

    public static WalletResult getSuccessWalletResult(
            double aSmsRate,
            double aDltRate,
            int aNoOfParts)
    {
        return new WalletResult(true, aSmsRate, aDltRate, aNoOfParts);
    }

    public static WalletResult getFailWalletResult()
    {
        return new WalletResult(false);
    }

}