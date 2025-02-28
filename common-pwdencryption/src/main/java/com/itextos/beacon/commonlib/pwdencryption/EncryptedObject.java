package com.itextos.beacon.commonlib.pwdencryption;

public class EncryptedObject
{

    private final String mActualString;
    private final String mEncryptedWithIvAndSalt;

    EncryptedObject(
            String aActualString,
            String aEncryptedWithIvAndSalt)
    {
        super();
        mActualString           = aActualString;
        mEncryptedWithIvAndSalt = aEncryptedWithIvAndSalt;
    }

    public String getActualString()
    {
        return mActualString;
    }

    public String getEncryptedWithIvAndSalt()
    {
        return mEncryptedWithIvAndSalt;
    }

    @Override
    public String toString()
    {
        return "EncryptedObject [mActualString=" + mActualString + ", mEncryptedWithIvAndSalt=" + mEncryptedWithIvAndSalt + "]";
    }

}