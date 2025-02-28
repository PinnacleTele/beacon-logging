package com.itextos.beacon.inmemory.spamcheck.util;

public class SpamWords
{

    private final String mSpamWord;
    private final int    mThresholdCount;
    private final int    mAction;

    public SpamWords(
            String aSpamWord,
            int aThresholdCount,
            int aAction)
    {
        mSpamWord       = aSpamWord;
        mThresholdCount = aThresholdCount;
        mAction         = aAction;
    }

    public String getSpamWord()
    {
        return mSpamWord;
    }

    public int getThresholdCount()
    {
        return mThresholdCount;
    }

    public int getAction()
    {
        return mAction;
    }

    @Override
    public String toString()
    {
        return "SpamWords [mSpamWord=" + mSpamWord + ", mThresholdCount=" + mThresholdCount + ", mAction=" + mAction + "]";
    }

}
