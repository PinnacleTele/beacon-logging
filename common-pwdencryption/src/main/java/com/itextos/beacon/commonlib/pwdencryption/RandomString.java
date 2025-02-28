package com.itextos.beacon.commonlib.pwdencryption;

import org.apache.commons.text.RandomStringGenerator;

class RandomString
{

    private static RandomStringGenerator mGenerator = new RandomStringGenerator.Builder().selectFrom(PasswordConstants.ALL_CHARS).build();

    private RandomString()
    {}

    static String getSaltString()
    {
        return getRandomString(PasswordConstants.SALT_LEGNTH);
    }

    static String getGuiPassword()
    {
        return getRandomString(PasswordConstants.GUI_PASSWORD_LEGNTH);
    }

    static String getApiPassword()
    {
        return getRandomString(PasswordConstants.API_PASSWORD_LEGNTH);
    }

    static String getSmppPassword()
    {
        return getRandomString(PasswordConstants.SMPP_PASSWORD_LEGNTH);
    }

    private static String getRandomString(
            int aLength)
    {
        return mGenerator.generate(aLength);
    }

}