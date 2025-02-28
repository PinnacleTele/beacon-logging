package com.itextos.beacon.inmemory.otpconfig.cache;

public class OtpConfig
{

    private boolean isMobileNoRequired;
    private int     mOtpLength;
    private boolean isRequiredNumbers;
    private boolean isRequiredAlphabetsUpper;
    private boolean isRequiredAlphabetsLower;
    private boolean isRequiredSpecialChar;
    private String  mAllowSpecialChars;
    private int     mExpiryinMinutes;
    private boolean mGenOtpInExpiryTime;

    public OtpConfig(
            boolean aMobileNoRequired,
            int aOtpLength,
            boolean aRequiredNumbers,
            boolean aRequiredAlphabetsUpper,
            boolean aRequiredAlphabetsLower,
            boolean aRequiredSpecialChar,
            String aAllowSpecialChars,
            int aExpiryinMinutes,
            boolean aGenOtpInExpiryTime)
    {
        isMobileNoRequired       = aMobileNoRequired;
        mOtpLength               = aOtpLength;
        isRequiredNumbers        = aRequiredNumbers;
        isRequiredAlphabetsUpper = aRequiredAlphabetsUpper;
        isRequiredAlphabetsLower = aRequiredAlphabetsLower;
        isRequiredSpecialChar    = aRequiredSpecialChar;
        mAllowSpecialChars       = aAllowSpecialChars;
        mExpiryinMinutes         = aExpiryinMinutes;
        mGenOtpInExpiryTime      = aGenOtpInExpiryTime;
    }

    public boolean isMobileNoRequired()
    {
        return isMobileNoRequired;
    }

    public void setMobileNoRequired(
            boolean aIsMobileNoRequired)
    {
        isMobileNoRequired = aIsMobileNoRequired;
    }

    public int getOtpLength()
    {
        return mOtpLength;
    }

    public void setOtpLength(
            int aOtpLength)
    {
        mOtpLength = aOtpLength;
    }

    public boolean isRequiredNumbers()
    {
        return isRequiredNumbers;
    }

    public void setRequiredNumbers(
            boolean aIsRequiredNumbers)
    {
        isRequiredNumbers = aIsRequiredNumbers;
    }

    public boolean isRequiredAlphabetsUpper()
    {
        return isRequiredAlphabetsUpper;
    }

    public void setRequiredAlphabetsUpper(
            boolean aIsRequiredAlphabetsUpper)
    {
        isRequiredAlphabetsUpper = aIsRequiredAlphabetsUpper;
    }

    public boolean isRequiredAlphabetsLower()
    {
        return isRequiredAlphabetsLower;
    }

    public void setRequiredAlphabetsLower(
            boolean aIsRequiredAlphabetsLower)
    {
        isRequiredAlphabetsLower = aIsRequiredAlphabetsLower;
    }

    public boolean isRequiredSpecialChar()
    {
        return isRequiredSpecialChar;
    }

    public void setRequiredSpecialChar(
            boolean aIsRequiredSpecialChar)
    {
        isRequiredSpecialChar = aIsRequiredSpecialChar;
    }

    public String getAllowSpecialChars()
    {
        return mAllowSpecialChars;
    }

    public void setAllowSpecialChars(
            String aAllowSpecialChars)
    {
        mAllowSpecialChars = aAllowSpecialChars;
    }

    public int getExpiryinMinutes()
    {
        return mExpiryinMinutes;
    }

    public void setExpiryinMinutes(
            int aExpiryinMinutes)
    {
        mExpiryinMinutes = aExpiryinMinutes;
    }

    public boolean isGenOtpInExpiryTime()
    {
        return mGenOtpInExpiryTime;
    }

    public void setGenOtpInExpiryTime(
            boolean aGenOtpInExpiryTime)
    {
        mGenOtpInExpiryTime = aGenOtpInExpiryTime;
    }

    @Override
    public String toString()
    {
        return "OtpConfig [isMobileNoRequired=" + isMobileNoRequired + ", mOtpLength=" + mOtpLength + ", isRequiredNumbers=" + isRequiredNumbers + ", isRequiredAlphabetsUpper="
                + isRequiredAlphabetsUpper + ", isRequiredAlphabetsLower=" + isRequiredAlphabetsLower + ", isRequiredSpecialChar=" + isRequiredSpecialChar + ", mAllowSpecialChars="
                + mAllowSpecialChars + ", mExpiryinMinutes=" + mExpiryinMinutes + ", mGenOtpInExpiryTime=" + mGenOtpInExpiryTime + "]";
    }

}
