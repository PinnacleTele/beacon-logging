package com.itextos.beacon.commonlib.utility.mobilevalidation;

import java.util.Arrays;

public class AccountMobileInfo
{

    private final String  mCountryCode;
    private final String  mCountryCurrency;
    private final int     mDomesticMobileLength;
    private final boolean mCheckForOtherSeries;
    private final int[]   mOtherDomesticMobileLengths;
    private final int     mMobileMinLength;
    private final int     mMobileMaxLength;
    private final boolean mIntlServiceEnabled;
    private final boolean mConsiderDefaultLengthAsDomestic;
    private final int     mGlobalMinMobileLength;
    private final int     mGlobalMaxMobileLength;

    public AccountMobileInfo(
            String aCountryCode,
            int aDomesticMobileLength,
            boolean aCheckForOtherSeries,
            int[] aOtherDomesticMobileLengths,
            int aMobileMinLength,
            int aMobileMaxLength,
            boolean aIntlServiceEnabled,
            boolean aConsiderDefaultLengthAsDomestic,
            int aGlobalMinMobileLength,
            int aGlobalMaxMobileLength,
            String aCountryCurrency
            )
    {
        super();
        mCountryCode                     = aCountryCode;
        mDomesticMobileLength            = aDomesticMobileLength;
        mCheckForOtherSeries             = aCheckForOtherSeries;
        mOtherDomesticMobileLengths      = aOtherDomesticMobileLengths;
        mMobileMinLength                 = aMobileMinLength;
        mMobileMaxLength                 = aMobileMaxLength;
        mIntlServiceEnabled              = aIntlServiceEnabled;
        mConsiderDefaultLengthAsDomestic = aConsiderDefaultLengthAsDomestic;
        mGlobalMinMobileLength           = aGlobalMinMobileLength;
        mGlobalMaxMobileLength           = aGlobalMaxMobileLength;
        mCountryCurrency                    = aCountryCurrency;

    }

    String getCountryCode()
    {
        return mCountryCode;
    }

    String getCountryCurrency()
    {
        return mCountryCurrency;
    }
    
    int getDomesticMobileLength()
    {
        return mDomesticMobileLength;
    }

    boolean isCheckForOtherSeries()
    {
        return mCheckForOtherSeries;
    }

    int[] getOtherDomesticMobileLengths()
    {
        return mOtherDomesticMobileLengths;
    }

    int getMobileMinLength()
    {
        return mMobileMinLength;
    }

    int getMobileMaxLength()
    {
        return mMobileMaxLength;
    }

    boolean isIntlServiceEnabled()
    {
        return mIntlServiceEnabled;
    }

    boolean isConsiderDefaultLengthAsDomestic()
    {
        return mConsiderDefaultLengthAsDomestic;
    }

    public int getGlobalMinMobileLength()
    {
        return mGlobalMinMobileLength;
    }

    public int getGlobalMaxMobileLength()
    {
        return mGlobalMaxMobileLength;
    }

    @Override
    public String toString()
    {
        return "AccountMobileInfo [mCountryCode=" + mCountryCode + ", mDomesticMobileLength=" + mDomesticMobileLength + ", mCheckForOtherSeries=" + mCheckForOtherSeries
                + ", mOtherDomesticMobileLengths=" + Arrays.toString(mOtherDomesticMobileLengths) + ", mMobileMinLength=" + mMobileMinLength + ", mMobileMaxLength=" + mMobileMaxLength
                + ", mIntlServiceEnabled=" + mIntlServiceEnabled + ", mConsiderDefaultLengthAsDomestic=" + mConsiderDefaultLengthAsDomestic + ", mGlobalMinMobileLength=" + mGlobalMinMobileLength
                + ", mGlobalMaxMobileLength=" + mGlobalMaxMobileLength + "]";
    }

}