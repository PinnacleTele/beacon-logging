package com.itextos.beacon.platform.r3c.util;

import java.util.Date;

public class VLRepository
{

    private String  mClientId;

    private Date    mCreatedTs;

    private Date    mExpiryDate;
    private String  mFileId;
    private String  mId;
    private boolean mIsRedirectUrlForShortner;
    private String  mMid;

    private String  mMobileNumber;
    private String  mReason;

    private String  mShortCode;
    private String  mShortenUrl;
    private String  mShrinkerCode;
    private Long    mSmartLinkId;

    private String  mStatus;

    private String  mUrl;

    public String getClientId()
    {
        return mClientId;
    }

    public Date getCreatedTs()
    {
        return mCreatedTs;
    }

    public Date getExpiryDate()
    {
        return mExpiryDate;
    }

    public String getFileId()
    {
        return mFileId;
    }

    public String getId()
    {
        return mId;
    }

    public String getMid()
    {
        return mMid;
    }

    public String getMobileNumber()
    {
        return mMobileNumber;
    }

    public String getReason()
    {
        return mReason;
    }

    public String getShortCode()
    {
        return mShortCode;
    }

    public String getShortenUrl()
    {
        return mShortenUrl;
    }

    public String getShrinkerCode()
    {
        return mShrinkerCode;
    }

    public Long getSmartLinkId()
    {
        return mSmartLinkId;
    }

    public String getStatus()
    {
        return mStatus;
    }

    public String getUrl()
    {
        return mUrl;
    }

    public boolean isIsRedirectUrlForShortner()
    {
        return mIsRedirectUrlForShortner;
    }

    public void setClientId(
            String aClientId)
    {
        mClientId = aClientId;
    }

    public void setCreatedTs(
            Date aCreatedTs)
    {
        mCreatedTs = aCreatedTs;
    }

    public void setExpiryDate(
            Date aExpiryDate)
    {
        mExpiryDate = aExpiryDate;
    }

    public void setFileId(
            String aFileId)
    {
        mFileId = aFileId;
    }

    public void setId(
            String aId)
    {
        mId = aId;
    }

    public void setIsRedirectUrlForShortner(
            boolean aIsRedirectUrlForShortner)
    {
        mIsRedirectUrlForShortner = aIsRedirectUrlForShortner;
    }

    public void setMid(
            String aMid)
    {
        mMid = aMid;
    }

    public void setMobileNumber(
            String aMobileNumber)
    {
        mMobileNumber = aMobileNumber;
    }

    public void setReason(
            String aReason)
    {
        mReason = aReason;
    }

    public void setShortCode(
            String aShortCode)
    {
        mShortCode = aShortCode;
    }

    public void setShortenUrl(
            String aShortenUrl)
    {
        mShortenUrl = aShortenUrl;
    }

    public void setShrinkerCode(
            String aShrinkerCode)
    {
        mShrinkerCode = aShrinkerCode;
    }

    public void setSmartLinkId(
            Long aSmartLinkId)
    {
        mSmartLinkId = aSmartLinkId;
    }

    public void setStatus(
            String aStatus)
    {
        mStatus = aStatus;
    }

    public void setUrl(
            String aUrl)
    {
        mUrl = aUrl;
    }

    @Override
    public String toString()
    {
        return "VLRepository [mClientId=" + mClientId + ", mCreatedTs=" + mCreatedTs + ", mExpiryDate=" + mExpiryDate + ", mFileId=" + mFileId + ", mId=" + mId + ", mIsRedirectUrlForShortner="
                + mIsRedirectUrlForShortner + ", mMid=" + mMid + ", mMobileNumber=" + mMobileNumber + ", mReason=" + mReason + ", mShortCode=" + mShortCode + ", mShortenUrl=" + mShortenUrl
                + ", mShrinkerCode=" + mShrinkerCode + ", mSmartLinkId=" + mSmartLinkId + ", mStatus=" + mStatus + ", mUrl=" + mUrl + "]";
    }

}
