package com.itextos.beacon.commonlib.utility.messagetypeidentifier;

public class MessageTypeIdentifyObject
{

    private final String  mMessage;
    private final String  mClientId;
    private final short   mUcIdentifierCharLength;
    private final short   mUcIdentifierCharOccurances;
    private final boolean mRemoveUcChars;

    private boolean       mIsUcMessage;
    private String        mUpdatedMessage;

    public MessageTypeIdentifyObject(
            String aMessage,
            String aClientId,
            short aUcIdentifierCharLength,
            short aUcIdentifierCharOccurances,
            boolean aRemoveUcChars)
    {
        super();
        mMessage                    = aMessage;
        mClientId                   = aClientId;
        mUcIdentifierCharLength     = aUcIdentifierCharLength;
        mUcIdentifierCharOccurances = aUcIdentifierCharOccurances;
        mRemoveUcChars              = aRemoveUcChars;
    }

    public boolean isIsUcMessage()
    {
        return mIsUcMessage;
    }

    public void setIsUcMessage(
            boolean aIsUcMessage)
    {
        mIsUcMessage = aIsUcMessage;
    }

    public String getUpdatedMessage()
    {
        return mUpdatedMessage;
    }

    public void setUpdatedMessage(
            String aUpdatedMessage)
    {
        mUpdatedMessage = aUpdatedMessage;
    }

    public String getMessage()
    {
        return mMessage;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public short getUcIdentifierCharLength()
    {
        return mUcIdentifierCharLength;
    }

    public short getUcIdentifierCharOccurances()
    {
        return mUcIdentifierCharOccurances;
    }

    public boolean isRemoveUcChars()
    {
        return mRemoveUcChars;
    }

    public void doInitial()
    {
        mIsUcMessage    = false;
        mUpdatedMessage = mMessage;
    }

    @Override
    public String toString()
    {
        return "MessageTypeIdentifyObject [mMessage=" + mMessage + ", mClientId=" + mClientId + ", mUcIdentifierCharLength=" + mUcIdentifierCharLength + ", mUcIdentifierCharOccurances="
                + mUcIdentifierCharOccurances + ", mRemoveUcChars=" + mRemoveUcChars + ", mIsUcMessage=" + mIsUcMessage + ", mUpdatedMessage=" + mUpdatedMessage + "]";
    }

}