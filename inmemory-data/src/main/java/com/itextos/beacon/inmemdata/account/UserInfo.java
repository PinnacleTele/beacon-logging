package com.itextos.beacon.inmemdata.account;

import com.itextos.beacon.commonlib.constants.AccountStatus;

public class UserInfo
{

    private final String  mClientId;
    private final String  mUserName;
    private final String  mApiPassword;
    private final String  mSmppPassword;
    private int           mStatus;
    private AccountStatus mAccountStatus;

    private String        accountDetails;

    public UserInfo(
            String aClientId,
            String aUserName,
            String aApiPassword,
            String aSmppPassword,
            int aStatus)
    {
        super();
        mClientId      = aClientId;
        mUserName      = aUserName;
        mApiPassword   = aApiPassword;
        mSmppPassword  = aSmppPassword;
        mStatus        = aStatus;
        mAccountStatus = AccountStatus.getAccountStatus(mStatus);
    }

    public String getClientId()
    {
        return mClientId;
    }

    public String getUserName()
    {
        return mUserName;
    }

    public String getApiPassword()
    {
        return mApiPassword;
    }

    public String getSmppPassword()
    {
        return mSmppPassword;
    }

    public int getStatus()
    {
        return mStatus;
    }

    public AccountStatus getAccountStatus()
    {
        return mAccountStatus;
    }

    public void setStatus(
            int aStutus)
    {
        mStatus        = aStutus;
        mAccountStatus = AccountStatus.getAccountStatus(mStatus);
    }

    public String getAccountDetails()
    {
        return accountDetails;
    }

    public void setAccountDetails(
            String aAccountDetails)
    {
        accountDetails = aAccountDetails;
    }

    @Override
    public String toString()
    {
        return "UserInfo [mClientId=" + mClientId + ", mUserName=" + mUserName + ", mApiPassword=" + mApiPassword + ", mSmppPassword=" + mSmppPassword + ", mStatus=" + mStatus + ", mAccountStatus="
                + mAccountStatus + ", accountDetails=" + accountDetails + "]";
    }

}