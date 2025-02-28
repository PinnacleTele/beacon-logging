package com.itextos.beacon.platform.walletreminder.email;

abstract class EmailAddress
{

    private final String mName;
    private final String mEmailId;

    EmailAddress(
            String aName,
            String aEmailId)
    {
        super();
        mName    = aName;
        mEmailId = aEmailId;
    }

    public String getName()
    {
        return mName;
    }

    public String getEmailId()
    {
        return mEmailId;
    }

    @Override
    public String toString()
    {
        return "EmailAddress [mName=" + mName + ", mEmailId=" + mEmailId + "]";
    }

}