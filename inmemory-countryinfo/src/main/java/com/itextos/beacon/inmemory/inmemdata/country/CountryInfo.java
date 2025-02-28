package com.itextos.beacon.inmemory.inmemdata.country;

import java.util.Arrays;

public class CountryInfo
{

    private final String mCountryCodeIso3;
    private final String mCountry;
    private final String mCountryShortName;
    private final String mCountryCodeIso2;
    private final int    mCountryCodeIsoNumeric;
    private final int    mDialInCode;
    private final String mDialInCodeFull;
    private final int    mDefaultMobileLength;
    private final int[]  mOtherMobileLength;
    private final int    mMinMobileLength;
    private final int    mMaxMobileLength;
    private final String mCountryCurrency;

    public CountryInfo(
            String aCountryCodeIso3,
            String aCountry,
            String aCountryShortName,
            String aCountryCodeIso2,
            int aCountryCodeIsoNumeric,
            int aDialInCode,
            String aDialInCodeFull,
            int aDefaultMobileLength,
            int[] aOtherMobileLength,
            int aMinMobileLength,
            int aMaxMobileLength,
            String aCountryCurrency)
    {
        super();
        mCountryCodeIso3       = aCountryCodeIso3;
        mCountry               = aCountry;
        mCountryShortName      = aCountryShortName;
        mCountryCodeIso2       = aCountryCodeIso2;
        mCountryCodeIsoNumeric = aCountryCodeIsoNumeric;
        mDialInCode            = aDialInCode;
        mDialInCodeFull        = aDialInCodeFull;
        mDefaultMobileLength   = aDefaultMobileLength;
        mOtherMobileLength     = aOtherMobileLength;
        mMinMobileLength       = aMinMobileLength;
        mMaxMobileLength       = aMaxMobileLength;
        mCountryCurrency=aCountryCurrency;
    }

    public String getCountryCode()
    {
        return mCountryCodeIso3;
    }

    public boolean isOtherMobileSeriesCheckReq()
    {
        if (mOtherMobileLength == null)
            return false;
        if (mOtherMobileLength.length > 0)
            return true;
        return false;
    }

    public String getCountryCodeIso3()
    {
        return mCountryCodeIso3;
    }

    public String getCountry()
    {
        return mCountry;
    }

    public String getCountryShortName()
    {
        return mCountryShortName;
    }

    public String getCountryCodeIso2()
    {
        return mCountryCodeIso2;
    }

    public int getCountryCodeIsoNumeric()
    {
        return mCountryCodeIsoNumeric;
    }

    public int getDialInCode()
    {
        return mDialInCode;
    }

    public String getDialInCodeFull()
    {
        return mDialInCodeFull;
    }

    public int getDefaultMobileLength()
    {
        return mDefaultMobileLength;
    }

    public int[] getOtherMobileLength()
    {
        return mOtherMobileLength;
    }

    public int getMinMobileLength()
    {
        return mMinMobileLength;
    }

    public int getMaxMobileLength()
    {
        return mMaxMobileLength;
    }
    
    
    public String getCountryCurrency() {
		return mCountryCurrency;
	}

	@Override
    public String toString()
    {
        return "CountryInfo [mCountryCodeIso3=" + mCountryCodeIso3 + ", mCountry=" + mCountry + ", mCountryShortName=" + mCountryShortName + ", mCountryCodeIso2=" + mCountryCodeIso2
                + ", mCountryCodeIsoNumeric=" + mCountryCodeIsoNumeric + ", mDialInCode=" + mDialInCode + ", mDialInCodeFull=" + mDialInCodeFull + ", mDefaultMobileLength=" + mDefaultMobileLength
                + ", mOtherMobileLength=" + Arrays.toString(mOtherMobileLength) + ", mMinMobileLength=" + mMinMobileLength + ", mMaxMobileLength=" + mMaxMobileLength + " , mCountryCurrency="+mCountryCurrency+"]";
    }

}