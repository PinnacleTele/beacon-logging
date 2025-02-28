package com.itextos.beacon.inmemory.templates.pojo;

public class DLTMsgPrefixSuffixObj
{

    private final String mPrefix;
    private final String mSuffix;

    public DLTMsgPrefixSuffixObj(
            String aPrefix,
            String aSuffix)
    {
        super();
        mPrefix = aPrefix;
        mSuffix = aSuffix;
    }

    public String getPrefix()
    {
        return mPrefix;
    }

    public String getSuffix()
    {
        return mSuffix;
    }

    @Override
    public String toString()
    {
        return "DLTMsgPrefixSufixObj [mPrefix=" + mPrefix + ", mSuffix=" + mSuffix + "]";
    }

}