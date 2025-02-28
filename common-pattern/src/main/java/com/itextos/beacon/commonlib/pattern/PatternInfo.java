package com.itextos.beacon.commonlib.pattern;

class PatternInfo
{

    private final PatternCheckCategory mCategory;
    private final String               mPattern;
    private long                       mLastUsedTime;
    private int                        mUsedCount;

    public PatternInfo(
            PatternCheckCategory aPatternCheckCategory,
            String aPattern)
    {
        mCategory     = aPatternCheckCategory;
        mPattern      = aPattern;
        mLastUsedTime = System.currentTimeMillis();
        mUsedCount    = 0;
    }

    public PatternCheckCategory getCategory()
    {
        return mCategory;
    }

    public String getPattern()
    {
        return mPattern;
    }

    public long getLastUsedTime()
    {
        return mLastUsedTime;
    }

    public void setLastUsedTime(
            long aLastUsedTime)
    {
        mLastUsedTime = aLastUsedTime;
    }

    public int getUsedCount()
    {
        return mUsedCount;
    }

    public void increaseUsedCount()
    {
        mUsedCount++;
    }

    @Override
    public String toString()
    {
        return "PatternInfo [mCategory=" + mCategory + ", mPattern=" + mPattern + ", mLastUsedTime=" + mLastUsedTime + ", mUsedCount=" + mUsedCount + "]";
    }

}