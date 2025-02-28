package com.itextos.beacon.commonlib.scheduler.logging;

import java.util.Date;

public class JobData
{

    private final String mScheduleId;
    private final String mScheduleGroupId;
    private final String mScheduleName;
    private final Date   mScheduledFireTime;
    private final Date   mFiredTime;

    public JobData(
            String aScheduleId,
            String aScheduleGroupId,
            String aScheduleName,
            Date aScheduledFireTime,
            Date aFireTime)
    {
        mScheduleId        = aScheduleId;
        mScheduleGroupId   = aScheduleGroupId;
        mScheduleName      = aScheduleName;
        mScheduledFireTime = aScheduledFireTime;
        mFiredTime         = aFireTime;
    }

    public String getScheduleId()
    {
        return mScheduleId;
    }

    public String getScheduleGroupId()
    {
        return mScheduleGroupId;
    }

    public String getScheduleName()
    {
        return mScheduleName;
    }

    public Date getScheduledFireTime()
    {
        return mScheduledFireTime;
    }

    public Date getFiredTime()
    {
        return mFiredTime;
    }

    public boolean isLogRequired()
    {
        return !("Mandatory".equalsIgnoreCase(mScheduleGroupId));
    }

    @Override
    public String toString()
    {
        return "JobData [mScheduleId=" + mScheduleId + ", mScheduleGroupId=" + mScheduleGroupId + ", mScheduleName=" + mScheduleName + ", mScheduledFireTime=" + mScheduledFireTime + ", mFiredTime="
                + mFiredTime + "]";
    }

}