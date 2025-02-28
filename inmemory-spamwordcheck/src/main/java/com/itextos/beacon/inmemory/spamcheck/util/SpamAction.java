package com.itextos.beacon.inmemory.spamcheck.util;

public class SpamAction
{

    private final int mAction;
    private final int mSpamWordCount;

    public SpamAction(
            int aAction,
            int aSpamWordCount)
    {
        mAction        = aAction;
        mSpamWordCount = aSpamWordCount;
    }

    public int getAction()
    {
        return mAction;
    }

    public int getSpamWordCount()
    {
        return mSpamWordCount;
    }

    @Override
    public String toString()
    {
        return "ActionBean [mAction=" + mAction + ", mSpamWordCount=" + mSpamWordCount + "]";
    }

}