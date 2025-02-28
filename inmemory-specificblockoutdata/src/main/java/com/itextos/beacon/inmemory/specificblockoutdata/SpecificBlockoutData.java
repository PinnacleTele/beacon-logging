package com.itextos.beacon.inmemory.specificblockoutdata;

import com.itextos.beacon.commonlib.utility.CommonUtility;

public class SpecificBlockoutData
{

    private final String  mClientId;
    private final String  mMobileNumber;
    private final String  mMessagePattern;
    private final String  mBlockoutStartTime;
    private final String  mBlockoutEndTime;
    private final boolean mIsDropMessage;
    private final String  key;

    public SpecificBlockoutData(
            String aClientId,
            String aMobileNumber,
            String aMessagePattern,
            String aBlockoutStartTime,
            String aBlockoutEndTime,
            boolean aIsDropMessage)
    {
        super();
        mClientId          = aClientId;
        mMobileNumber      = aMobileNumber;
        mMessagePattern    = aMessagePattern;
        mBlockoutStartTime = aBlockoutStartTime;
        mBlockoutEndTime   = aBlockoutEndTime;
        mIsDropMessage     = aIsDropMessage;
        key                = "".equals(aMobileNumber) ? mClientId : CommonUtility.combine(mClientId, mMobileNumber);
    }

    public String getKey()
    {
        return key;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public String getMobileNumber()
    {
        return mMobileNumber;
    }

    public String getMessagePattern()
    {
        return mMessagePattern;
    }

    public String getBlockoutStartTime()
    {
        return mBlockoutStartTime;
    }

    public String getBlockoutEndTime()
    {
        return mBlockoutEndTime;
    }

    public boolean IsDropMessage()
    {
        return mIsDropMessage;
    }

    @Override
    public String toString()
    {
        return "SpecificBlockoutData [mClientId=" + mClientId + ", mMobileNumber=" + mMobileNumber + ", mMessagePattern=" + mMessagePattern + ", mBlockoutStartTime=" + mBlockoutStartTime
                + ", mBlockoutEndTime=" + mBlockoutEndTime + ", mIsDropMessage=" + mIsDropMessage + ", key=" + key + "]";
    }

    public boolean isValid()
    {
        return (!mClientId.isEmpty() && !mMessagePattern.isEmpty() && !mBlockoutStartTime.isEmpty() && !mBlockoutEndTime.isEmpty());
    }

}