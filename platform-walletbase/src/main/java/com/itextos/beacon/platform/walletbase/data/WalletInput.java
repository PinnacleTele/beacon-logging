package com.itextos.beacon.platform.walletbase.data;

import java.io.Serializable;

import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

public abstract class WalletInput
        implements
        Serializable
{

    private static final long       serialVersionUID = -5720229871874460892L;

    private final String            mClientId;
    private final String            mFileId;
    private final String            mBaseMessageId;
    private final String            mMessageId;
    private final int               mNoOfParts;
    private final double            mSmsRate;
    private final double            mDltRate;
    private String                  mReason;
    private final long              mRequestedTime;
    private long                    mProcessedTime;
    private final WalletProcessType mType;

    private final boolean           mIsIntl;

    WalletInput(
            WalletProcessType aType,
            String aClientId,
            String aFileId,
            String aBaseMessageId,
            String aMessageId,
            int aNoOfParts,
            double aSmsRate,
            double aDltRate,
            String aReason,
            boolean aIsIntl)
    {
        super();
        mType          = aType;
        mClientId      = aClientId;
        mFileId        = aFileId;
        mBaseMessageId = aBaseMessageId;
        mMessageId     = aMessageId;
        mNoOfParts     = aNoOfParts;
        mSmsRate       = aSmsRate;
        mDltRate       = aDltRate;
        mReason        = aReason;
        mRequestedTime = System.currentTimeMillis();
        mIsIntl        = aIsIntl;
    }

    public static final WalletRefundInput getRefundInput(
            String aClientId,
            String aFileId,
            String aBaseMessageId,
            String aMessageId,
            int aNoOfParts,
            double aSmsRate,
            double aDltRate,
            String aReason,
            boolean isIntl)
    {
        return new WalletRefundInput(aClientId, aFileId, aBaseMessageId, aMessageId, aNoOfParts, aSmsRate, aDltRate, aReason, isIntl);
    }

    public static final WalletDeductInput getDeductInput(
            String aClientId,
            String aFileId,
            String aBaseMessageId,
            String aMessageId,
            int aNoOfParts,
            double aSmsRate,
            double aDltRate,
            String aReason,
            boolean isIntl)
    {
        return new WalletDeductInput(aClientId, aFileId, aBaseMessageId, aMessageId, aNoOfParts, aSmsRate, aDltRate, aReason, isIntl);
    }

    public String getClientId()
    {
        return mClientId;
    }

    public String getFileId()
    {
        return mFileId;
    }

    public String getBaseMessageId()
    {
        return mBaseMessageId;
    }

    public String getMessageId()
    {
        return mMessageId;
    }

    public int getNoOfParts()
    {
        return mNoOfParts;
    }

    public double getSmsRate()
    {
        return mSmsRate;
    }

    public double getDltRate()
    {
        return mDltRate;
    }

    public String getReason()
    {
        return mReason;
    }

    public void setProcessed()
    {
        mProcessedTime = System.currentTimeMillis();
    }

    public long getRequestedTime()
    {
        return mRequestedTime;
    }

    public long getProcessedTime()
    {
        return mProcessedTime;
    }

    public void updateReason(
            String aReason)
    {
        mReason = aReason;
    }

    public String getProcessType()
    {
        return mType.name();
    }

    public boolean isIntl()
    {
        return mIsIntl;
    }

    @Override
    public WalletInput clone()
            throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    public WalletInput getRefundObject()
    {
    	/*
        if (mType == WalletProcessType.REFUND)
            throw new ItextosRuntimeException("Invalid object type to get the Refund Object.");
        return getRefundInput(mClientId, mFileId, mBaseMessageId, mBaseMessageId, mNoOfParts, mSmsRate, mDltRate, mReason, mIsIntl);
    
    	*/
    	if (mType == WalletProcessType.REFUND) {
    		
    		return null;
    	}else {
            return getRefundInput(mClientId, mFileId, mBaseMessageId, mBaseMessageId, mNoOfParts, mSmsRate, mDltRate, mReason, mIsIntl);

    	}
    }

    @Override
    public String toString()
    {
        return "WalletDeductRefundInput [mClientId=" + mClientId + ", mFileId=" + mFileId + ", mBaseMessageId=" + mBaseMessageId + ", mMessageId=" + mMessageId + ", mNoOfParts=" + mNoOfParts
                + ", mSmsRate=" + mSmsRate + ", mDltRate=" + mDltRate + ", mReason=" + mReason + ", mRequestedTime=" + mRequestedTime + ", mProcessedTime=" + mProcessedTime + "]";
    }

}