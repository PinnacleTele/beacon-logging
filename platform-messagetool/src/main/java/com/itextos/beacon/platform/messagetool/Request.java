package com.itextos.beacon.platform.messagetool;

public class Request
{

    private final String  mClientId;
    private final String  mMessage;
    private final int     mAccountLevelSplCharLength;
    private final int     mAccountLevelOccuranceCount;
    private final boolean mRemoveUcCharsInPlainMessage;

    public Request(
            String aClientId,
            String aMessage,
            int aAccountLevelSplCharLength,
            int aAccountLevelOccuranceCount,
            boolean aRemoveUcCharsInPlainMessage)
    {
        super();
        mClientId                    = aClientId;
        mMessage                     = aMessage;
        mAccountLevelSplCharLength   = aAccountLevelSplCharLength;
        mAccountLevelOccuranceCount  = aAccountLevelOccuranceCount;
        mRemoveUcCharsInPlainMessage = aRemoveUcCharsInPlainMessage;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public String getMessage()
    {
        return mMessage;
    }

    public int getAccountLevelOccuranceCount()
    {
        return mAccountLevelOccuranceCount;
    }

    public boolean isRemoveUcCharsInPlainMessage()
    {
        return mRemoveUcCharsInPlainMessage;
    }

    public int getAccountLevelSplCharLength()
    {
        return mAccountLevelSplCharLength;
    }

    @Override
    public String toString()
    {
        return "Request [mClientId=" + mClientId + ", mMessage=" + mMessage + ", mAccountLevelSplCharLength=" + mAccountLevelSplCharLength + ", mAccountLevelOccuranceCount="
                + mAccountLevelOccuranceCount + ", mRemoveUcCharsInPlainMessage=" + mRemoveUcCharsInPlainMessage + "]";
    }

}