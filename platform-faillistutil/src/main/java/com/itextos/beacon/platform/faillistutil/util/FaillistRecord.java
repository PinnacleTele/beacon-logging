package com.itextos.beacon.platform.faillistutil.util;

public class FaillistRecord
{

    private final String mClientId;
    private final String mMobileNumber;
    private final String mActionFlag;
    private final int    mNumberSplitLength;
    private final String mRedisKeyPrefix;

    private String       mRedisOuterKey;
    private String       mRedisInnerKey;
    private boolean      mIsValid = true;

    /**
     * Constructor to represent the Record object.
     *
     * @param aClientId
     * @param aMobileNumber
     * @param aActionFlag
     * @param aNumberSplitLength
     * @param aRedisKeyPrefix
     */
    public FaillistRecord(
            String aClientId,
            String aMobileNumber,
            String aActionFlag,
            int aNumberSplitLength,
            String aRedisKeyPrefix)
    {
        super();
        mClientId          = aClientId;
        mMobileNumber      = aMobileNumber;
        mActionFlag        = aActionFlag;
        mNumberSplitLength = aNumberSplitLength;
        mRedisKeyPrefix    = aRedisKeyPrefix;

        validate();
    }

    public String getEsme()
    {
        return mClientId;
    }

    public String getNumber()
    {
        return mMobileNumber;
    }

    public String getActionFlag()
    {
        return mActionFlag;
    }

    public String getOuterKey()
    {
        return mRedisOuterKey;
    }

    public String getInnerKey()
    {
        return mRedisInnerKey;
    }

    public boolean isValid()
    {
        return mIsValid;
    }

    private void validate()
    {

        if ((mClientId == null) || (mMobileNumber == null) || (mActionFlag == null))
        {
            mIsValid = false;
            return;
        }

        if (mClientId.equals(FaillistConstants.CLIENT_ADDRESS_GLOBAL) || (mClientId.length() == 14))
        {
            // No further validation here.
        }
        else
        {
            mIsValid = false;
            return;
        }

        if (mMobileNumber.length() <= mNumberSplitLength)
        {
            mIsValid = false;
            return;
        }

        if (mActionFlag.equals(FaillistConstants.ACTION_ADD) || mActionFlag.equals(FaillistConstants.ACTION_DELETE))
        {}
        else
        {
            mIsValid = false;
            return;
        }

        final String[] keywords = FaillistUtil.getRedisKeywords(mClientId, mMobileNumber, mNumberSplitLength, mRedisKeyPrefix);

        mRedisOuterKey = keywords[0];
        mRedisInnerKey = keywords[1];
    }

    @Override
    public String toString()
    {
        return "Record [mClientId=" + mClientId + ", mMobileNumber=" + mMobileNumber + ", mActionFlag=" + mActionFlag + ", mNumberSplitLength=" + mNumberSplitLength + ", mRedisKeyPrefix="
                + mRedisKeyPrefix + ", mRedisOuterKey=" + mRedisOuterKey + ", mRedisInnerKey=" + mRedisInnerKey + ", mIsValid=" + mIsValid + "]";
    }

}