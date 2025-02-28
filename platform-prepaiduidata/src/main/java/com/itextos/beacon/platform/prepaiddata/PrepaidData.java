package com.itextos.beacon.platform.prepaiddata;

import java.io.Serializable;

import com.itextos.beacon.commonlib.constants.AccountStatus;
import com.itextos.beacon.platform.prepaiddata.inmemory.CurrencyData;

public class PrepaidData
        implements
        Serializable,
        Comparable<PrepaidData>
{

    private final String        mCliId;
    private final String        mUserName;
    private final CurrencyData  mCurrencyInfo;
    private final double        mPrepaidBalance;
    private final AccountStatus mAccountStatus;

    public PrepaidData(
            String aCliId,
            String aUserName,
            CurrencyData aCurrencyInfo,
            double aPrepaidBalance,
            AccountStatus aAccountStatus)
    {
        mCliId          = aCliId;
        mUserName       = aUserName;
        mCurrencyInfo   = aCurrencyInfo;
        mPrepaidBalance = aPrepaidBalance;
        mAccountStatus  = aAccountStatus;
    }

    public String getCliId()
    {
        return mCliId;
    }

    public String getUserName()
    {
        return mUserName;
    }

    public CurrencyData getCurrencyInfo()
    {
        return mCurrencyInfo;
    }

    public double getPrepaidBalance()
    {
        return mPrepaidBalance;
    }

    public AccountStatus getAccountStatus()
    {
        return mAccountStatus;
    }

    @Override
    public String toString()
    {
        return "PrepaidData [mCliId=" + mCliId + ", mUserName=" + mUserName + ", mCurrencyInfo=" + mCurrencyInfo + ", mPrepaidBalance=" + mPrepaidBalance + ", mAccountStatus=" + mAccountStatus + "]";
    }

    @Override
    public int compareTo(
            PrepaidData aO)
    {
        return ((aO != null) && (aO.getUserName() != null) && (getUserName() != null)) ? getUserName().compareTo(aO.getUserName()) : 0;
    }

}