package com.itextos.beacon.platform.walletreminder.data;

import java.text.DecimalFormat;

import com.itextos.beacon.platform.walletreminder.utils.WalletReminderProperties;

public class UserInfo
{

    private static final double INVALID        = -99999d;

    private final long          mClientId;
    private final long          mParentAdminId;
    private final long          mSuperAdminId;
    private final String        mUser;
    private final String        mFirstName;
    private final String        mLastName;
    private final String        mEmailAddress;
    private final String        mCurrency;

    private double              mWalletBalance = INVALID;

    public UserInfo(
            long aClientId,
            long aParentAdminId,
            long aSuperAdminId,
            String aUser,
            String aFirstName,
            String aLastName,
            String aEmailAddress,
            String aCurrency)
    {
        super();
        mClientId      = aClientId;
        mParentAdminId = aParentAdminId;
        mSuperAdminId  = aSuperAdminId;
        mUser          = aUser;
        mFirstName     = aFirstName;
        mLastName      = aLastName;
        mEmailAddress  = aEmailAddress;
        mCurrency      = aCurrency;
    }

    public String getCurrency()
    {
        return mCurrency;
    }

    public long getClientId()
    {
        return mClientId;
    }

    public long getParentAdminId()
    {
        return mParentAdminId;
    }

    public long getSuperAdminId()
    {
        return mSuperAdminId;
    }

    public String getUser()
    {
        return mUser;
    }

    public String getFirstName()
    {
        return mFirstName;
    }

    public String getLastName()
    {
        return mLastName;
    }

    public String getEmailAddress()
    {
        return mEmailAddress;
    }

    public Double getWalletBalance()
    {
        return mWalletBalance;
    }

    public boolean isValidBalance()
    {
        return (WalletReminderProperties.getInstance().filterInvalid()) ? INVALID != mWalletBalance : true;
    }

    public void setWalletBalance(
            double aWalletBalance)
    {
        mWalletBalance = aWalletBalance;
    }

    public String getPrintableWalletBalance()
    {
        if (mWalletBalance == INVALID)
            return "Invalid Amount";

        final DecimalFormat df = new DecimalFormat("###,###,###,##0.0000");
        return df.format(mWalletBalance) + " (" + mCurrency + ")";
    }

    @Override
    public String toString()
    {
        return "UserInfo [mClientId=" + mClientId + ", mParentAdminId=" + mParentAdminId + ", mSuperAdminId=" + mSuperAdminId + ", mUser=" + mUser + ", mFirstName=" + mFirstName + ", mLastName="
                + mLastName + ", mEmailAddress=" + mEmailAddress + ", mCurrency=" + mCurrency + ", mWalletBalance=" + mWalletBalance + "]";
    }

    public String getFullName()
    {
        return mFirstName + " " + mLastName;
    }

}