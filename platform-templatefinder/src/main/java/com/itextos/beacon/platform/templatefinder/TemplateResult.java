package com.itextos.beacon.platform.templatefinder;

import java.text.DecimalFormat;

public class TemplateResult
{

    private Result     mResult;
    private String     mTemplateId;
    private final long mStartTime;
    private long       mEndTime;

    public TemplateResult()
    {
        mStartTime = System.currentTimeMillis();
    }

    public void setResult(
            Result aResult)
    {
        mResult  = aResult;
        mEndTime = System.currentTimeMillis();
    }

    public Result getResult()
    {
        return mResult;
    }

    public void setTemplateId(
            String aTemplateId)
    {
        mTemplateId = aTemplateId;
    }

    public String getTemplateId()
    {
        return mTemplateId;
    }

    public long timeTaken()
    {
        return mEndTime - mStartTime;
    }

    static DecimalFormat df = new DecimalFormat("00");

    private static String timeDiff(
            long from,
            long to)
    {
        if ((from <= 0) || (to <= 0))
            return df.format(-1);
        return df.format(to - from);
    }

    @Override
    public String toString()
    {
        return "TemplateResult [Time Taken=" + timeDiff(mStartTime, mEndTime) + ", mResult=" + mResult + ", mTemplateId=" + mTemplateId + "]";
    }

}