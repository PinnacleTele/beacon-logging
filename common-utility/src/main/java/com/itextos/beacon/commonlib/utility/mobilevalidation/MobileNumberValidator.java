package com.itextos.beacon.commonlib.utility.mobilevalidation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public final class MobileNumberValidator
{

    private static final Log           log                     = LogFactory.getLog(MobileNumberValidator.class);
    private static final int           MIN_NUMERIC             = 48; // 0
    private static final int           MAX_NUMERIC             = 57; // 9

    private static final List<Integer> ALLOED_CHARACTTERS_LIST = new ArrayList<>();

    static
    {
        final List<Object> defaultValues = new ArrayList<>();
        defaultValues.add(Integer.toString('+'));
        defaultValues.add(Integer.toString('-'));
        defaultValues.add(Integer.toString('_'));
        defaultValues.add(Integer.toString(' '));

        final PropertiesConfiguration lProperties = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.COMMON_PROPERTIES, true);
        final List<Object>            alloedChars = lProperties.getList("mobile.allowed.special.chars", defaultValues);

        for (final Object c : alloedChars)
            try
            {
                final int i = Integer.parseInt((String) c);
                ALLOED_CHARACTTERS_LIST.add(i);
            }
            catch (final Exception e)
            {
                // ignore this.
            }
    }

    private final String            mPassedMobileNumber;
    private final AccountMobileInfo mAccountMobileInfo;
    private final boolean           mAppendCountryCode;
    private final String            mCountryCodeToAppend;
    private String                  mMobileNumber;
    private boolean                 mIsCountryCodeAppended = false;
    private boolean                 mIsValidMobileNumber   = false;
    private boolean                 mIsIntlMobileNumber    = false;
    private boolean                 mIsSpecialSeriesNumber = false;

    public MobileNumberValidator(
            String aMobileNumber,
            AccountMobileInfo aAccountMobileInfo)
    {
        this(aMobileNumber, aAccountMobileInfo, false, null);
    }

    public MobileNumberValidator(
            String aMobileNumber,
            AccountMobileInfo aAccountMobileInfo,
            boolean aAppendCountryCode,
            String aCountryCodeToAppend)
    {
        mPassedMobileNumber  = aMobileNumber;
        mAccountMobileInfo   = aAccountMobileInfo;
        mAppendCountryCode   = aAppendCountryCode;
        mCountryCodeToAppend = aCountryCodeToAppend;

        checkNumber();
    }
    
    
    

    public AccountMobileInfo getmAccountMobileInfo() {
		return mAccountMobileInfo;
	}

	public String getMobileNumber()
    {
        return mMobileNumber;
    }

    public boolean isValidMobileNumber()
    {
        return mIsValidMobileNumber;
    }

    public boolean isIntlMobileNumber()
    {
        return mIsIntlMobileNumber;
    }

    public boolean isSpecialSeriesNumber()
    {
        return mIsSpecialSeriesNumber;
    }

    private void checkNumber()
    {

        try
        {
            mMobileNumber = removeNonAllowedChars(mPassedMobileNumber);

            if (mAppendCountryCode)
            {
                final String temp = CommonUtility.nullCheck(mCountryCodeToAppend, true);

                if (!temp.isBlank())
                {
                    mMobileNumber          = temp + mMobileNumber;
                    mIsCountryCodeAppended = true;
                }
                else
                    log.error("Passed country code is not valid. Skipping Passed Country code '" + mCountryCodeToAppend + "'");
            }
            final boolean isValidMobileLength = checkForGlobalLength();
            if (isValidMobileLength)
                check();
            else
                if (log.isDebugEnabled())
                    log.debug(" Mobile Number from the Request is Not Valid " + mMobileNumber);
        }
        catch (final Exception e)
        {
            mMobileNumber        = mPassedMobileNumber;
            mIsValidMobileNumber = false;
            mIsIntlMobileNumber  = false;

            log.error("Exception while validating the mobile nuumber. Details : '" + toString() + "' " + e.getMessage(), e);
        }
    }

    private boolean checkForGlobalLength()
    {
        return ((mMobileNumber.length() >= mAccountMobileInfo.getGlobalMinMobileLength()) && (mMobileNumber.length() <= mAccountMobileInfo.getGlobalMaxMobileLength()));
    }

    /**
     * Validate the mobile numbers as follows
     * <ol>
     * <li>Allow only numeric values.
     * <li>Allow some of the characters defined in the Properties file. <b>But don't
     * include them in the validation.</b>
     * <li>Other than numeric values and allowed characters, <b>throw an exception
     * as invalid mobile number</b>.
     * </ol>
     *
     * @param aPassedMobileNumber
     *
     * @return a String equivalent to the valid mobile number.
     *
     * @throws ItextosException
     */
    private static String removeNonAllowedChars(
            String aPassedMobileNumber)
            throws ItextosException
    {
        final char[]        allchars = aPassedMobileNumber.toCharArray();
        final StringBuilder sb       = new StringBuilder();

        for (final int c : allchars)
            if ((c >= MIN_NUMERIC) && (c <= MAX_NUMERIC))
                sb.append((char) c);
            else
                if (!ALLOED_CHARACTTERS_LIST.contains(c))
                    throw new ItextosException("Invalid character '" + ((char) c) + "' in mobile number. Passed '" + aPassedMobileNumber + "'");

        final long lParseLong = Long.parseLong(sb.toString());
        return Long.toString(lParseLong);
    }

    private void check()
    {
        if (mIsCountryCodeAppended)
            validateStartsWithCountryCode();
        else
            validateWithoutCountryCode();
    }

    private void validateStartsWithCountryCode()
    {

        if (mMobileNumber.startsWith(mAccountMobileInfo.getCountryCode()))
        {
            mIsIntlMobileNumber = false;
            checkForDomesticLengths();
        }
        else
            checkForIntlServices();
    }

    private void checkForDomesticLengths()
    {
        final int mobileLength = mMobileNumber.length();

        if ((mobileLength < (mAccountMobileInfo.getMobileMinLength())) || (mobileLength > (mAccountMobileInfo.getMobileMaxLength())))
        {
            mIsValidMobileNumber = false;
            return;
        }

        if (mobileLength == (mAccountMobileInfo.getDomesticMobileLength() + mAccountMobileInfo.getCountryCode().length()))
        {
            mIsValidMobileNumber = true;
            return;
        }

        if (!mAccountMobileInfo.isCheckForOtherSeries())
        {
            mIsValidMobileNumber = false;
            return;
        }

        // final boolean isMatched =
        checkForOtherSeries();

        // if (!isMatched)
        // mIsValidMobileNumber = false;
    }

    private void validateWithoutCountryCode()
    {
        final int mobileLength = mMobileNumber.length();

        if (mobileLength == mAccountMobileInfo.getDomesticMobileLength())
            validateWithHomeCountryDefaultLength();
        else
            validateForOtherLengths();
    }

    private void validateWithHomeCountryDefaultLength()
    {

        if (mAccountMobileInfo.isConsiderDefaultLengthAsDomestic())
        {

            if (!mAccountMobileInfo.isIntlServiceEnabled())
            {
                mMobileNumber        = mAccountMobileInfo.getCountryCode() + mMobileNumber;
                mIsValidMobileNumber = true;
                mIsIntlMobileNumber  = false;
            }
            else
            {
                mIsValidMobileNumber = true;
                mIsIntlMobileNumber  = true;
            }
        }
        else
            checkForIntlServices();
    }

    private void validateForOtherLengths()
    {
        if (mMobileNumber.startsWith(mAccountMobileInfo.getCountryCode()))
            validateForHomeCountry();
        else
            checkForIntlServices();
    }

    private void validateForHomeCountry()
    {
        final int mobileLength = mMobileNumber.length();

        if ((mobileLength == (mAccountMobileInfo.getDomesticMobileLength() + mAccountMobileInfo.getCountryCode().length())))
            mIsValidMobileNumber = true;
        else
            checkForOtherSeriesAndIntl();
    }

    private void checkForOtherSeriesAndIntl()
    {

        if (mAccountMobileInfo.isCheckForOtherSeries())
        {
            final boolean needToCheckForIntl = checkForOtherSeries();
            if (needToCheckForIntl)
                checkForIntlServices();
        }
        else
            checkForIntlServices();
    }

    private void checkForIntlServices()
    {

        if (mAccountMobileInfo.isIntlServiceEnabled())
        {
            mIsValidMobileNumber = true;
            mIsIntlMobileNumber  = true;
        }
        else
            mIsValidMobileNumber = false;
    }

    private boolean checkForOtherSeries()
    {
        final int mobileLength       = mMobileNumber.length();
        boolean   needToCheckForIntl = true;

        if (mAccountMobileInfo.getOtherDomesticMobileLengths() != null)
            for (final int otherLen : mAccountMobileInfo.getOtherDomesticMobileLengths())
                if (mobileLength == otherLen)
                {
                    mIsValidMobileNumber   = true;
                    mIsSpecialSeriesNumber = true;
                    needToCheckForIntl     = false;
                    break;
                }

        return needToCheckForIntl;
    }

    public String toString() {
 
    	StringBuffer sb=new StringBuffer();
    	sb.append(" mPassedMobileNumber : "+mPassedMobileNumber).append("\t").append("mMobileNumber : "+mMobileNumber).append("\t").append(" mAppendCountryCode : "+mAppendCountryCode).append("\t")
    	.append("mCountryCodeToAppend : "+mCountryCodeToAppend).append("\t").append("mIsCountryCodeAppended : "+mIsCountryCodeAppended).append("\t")
    	.append(" mIsValidMobileNumber : "+mIsValidMobileNumber).append("\t").append("mIsIntlMobileNumber : "+mIsIntlMobileNumber).append("\t")
    	.append("mIsIntlMobileNumber : "+mIsIntlMobileNumber).append("\t");
    	return sb.toString();
    }
}
