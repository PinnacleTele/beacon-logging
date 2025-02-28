package com.itextos.beacon.platform.messagetool;

public class MessageSplitRequest
{

    public static final int CUTOMER_NO_SPLIT_REQUEST = -1;

    private final String    mClientId;
    private final String    mBaseMessageId;
    private final String    mMessage;
    private final String    mMessageClass;
    private final boolean   mIsHexMessage;
    private int             mClientMaxSplit          = -1;
    private String          mCountry;
    private PrefixSuffix    mClientPrefixSuffix;
    private PrefixSuffix    mDltPrefixSuffix;
    private int             mDcs;
    private int             mDestinationPort;
    private String          mDltTemplateType;
    private String          mFeatureCode;
    private String          mHeader;
    private boolean         mIs16BitUdh;
    private boolean         mIsDltEnabled;
    private int             mMessageLength;
    private int             mTotalSplitParts;
    private String          mUdh;
    private int             mUdhi;
    private int             mCharactersCount;

    public MessageSplitRequest(
            String aClientId,
            String aBaseMessageId,
            String aMessage,
            String aMessageClass,
            boolean aIsHexMessage)
    {
        super();
        mClientId      = aClientId;
        mBaseMessageId = aBaseMessageId;
        mMessage       = aMessage;
        mMessageClass  = aMessageClass;
        mIsHexMessage  = aIsHexMessage;
    }

    String getBaseMessageId()
    {
        return mBaseMessageId;
    }

    String getClientId()
    {
        return mClientId;
    }

    int getClientMaxSplit()
    {
        return mClientMaxSplit;
    }

    String getCountry()
    {
        return mCountry;
    }

    PrefixSuffix getClientPrefixSuffix()
    {
        return mClientPrefixSuffix;
    }

    int getDcs()
    {
        return mDcs;
    }

    int getDestinationPort()
    {
        return mDestinationPort;
    }

    String getDltTemplateType()
    {
        return mDltTemplateType;
    }

    public String getFeatureCode()
    {
        return mFeatureCode;
    }

    String getHeader()
    {
        return mHeader;
    }

    public String getMessage()
    {
        return mMessage;
    }

    public String getMessageClass()
    {
        return mMessageClass;
    }

    int getMessageLength()
    {
        return mMessageLength;
    }

    public int getTotalSplitParts()
    {
        return mTotalSplitParts;
    }

    String getUdh()
    {
        return mUdh;
    }

    int getUdhi()
    {
        return mUdhi;
    }

    boolean is16BitUdh()
    {
        return mIs16BitUdh;
    }

    boolean isDltEnabled()
    {
        return mIsDltEnabled;
    }

    public boolean isHexMessage()
    {
        return mIsHexMessage;
    }

    public void setClientMaxSplit(
            int aClientMaxSplit)
    {
        mClientMaxSplit = aClientMaxSplit;
    }

    public void setCountry(
            String aCountry)
    {
        mCountry = aCountry;
    }

    void setClientPrefixSuffix(
            PrefixSuffix aClientPrefixSuffix)
    {
        mClientPrefixSuffix = aClientPrefixSuffix;
    }

    public void setDcs(
            int aDcs)
    {
        mDcs = aDcs;
    }

    public void setDestinationPort(
            int aDestinationPort)
    {
        mDestinationPort = aDestinationPort;
    }

    public void setDltEnabled(
            boolean aIsDltEnabled)
    {
        mIsDltEnabled = aIsDltEnabled;
    }

    public void setDltTemplateType(
            String aDltTemplateType)
    {
        mDltTemplateType = aDltTemplateType;
    }

    void setFeatureCode(
            String aFeatureCode)
    {
        mFeatureCode = aFeatureCode;
    }

    public void setHeader(
            String aHeader)
    {
        mHeader = aHeader;
    }

    public void setIs16BitUdh(
            boolean aIs16BitUdh)
    {
        mIs16BitUdh = aIs16BitUdh;
    }

    void setMessageLength(
            int aMessageLength)
    {
        mMessageLength = aMessageLength;
    }

    void setTotalSplitParts(
            int aTotalSplitParts)
    {
        mTotalSplitParts = aTotalSplitParts;
    }

    public void setUdh(
            String aUdh)
    {
        mUdh = aUdh;
    }

    public void setUdhi(
            int aUdhi)
    {
        mUdhi = aUdhi;
    }

    @Override
    public String toString()
    {
        return "MessageSplitRequest [mBaseMessageId=" + mBaseMessageId + ", mClientId=" + mClientId + ", mClientMaxSplit=" + mClientMaxSplit + ", mCountry=" + mCountry + ", mClientPrefixSuffix="
                + mClientPrefixSuffix + ", mDcs=" + mDcs + ", mDestinationPort=" + mDestinationPort + ", mDltTemplateType=" + mDltTemplateType + ", mFeatureCode=" + mFeatureCode + ", mHeader="
                + mHeader + ", mIs16BitUdh=" + mIs16BitUdh + ", mIsDltEnabled=" + mIsDltEnabled + ", mIsHexMessage=" + mIsHexMessage + ", mMessage=" + mMessage + ", mMessageClass=" + mMessageClass
                + ", mMessageLength=" + mMessageLength + ", mTotalSplitParts=" + mTotalSplitParts + ", mUdh=" + mUdh + ", mUdhi=" + mUdhi + "]";
    }

    void setDltPrefixSuffix(
            PrefixSuffix aPrefixSuffix)
    {
        mDltPrefixSuffix = aPrefixSuffix;
    }

    PrefixSuffix getDltPrefixSuffix()
    {
        return mDltPrefixSuffix;
    }

    void setCharactersCount(
            int aLength)
    {
        mCharactersCount = aLength;
    }

    public int getCharactersCount()
    {
        return mCharactersCount;
    }

}