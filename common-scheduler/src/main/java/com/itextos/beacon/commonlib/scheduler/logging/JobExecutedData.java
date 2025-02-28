package com.itextos.beacon.commonlib.scheduler.logging;

import java.util.Date;

public class JobExecutedData
        extends
        JobData
{

    private final long    mDuration;
    private final Date    mNextFireTime;
    private final String  mErrorDesc;
    private final boolean mMisFired;

    public JobExecutedData(
            String aScheduleId,
            String aScheduleGroupId,
            String aScheduleName,
            Date aScheduledFireTime,
            Date aFireTime,
            long aDuration,
            Date aNextFireTime,
            String aErrorDesc,
            boolean aIsMisfired)
    {
        super(aScheduleId, aScheduleGroupId, aScheduleName, aScheduledFireTime, aFireTime);

        mDuration     = aDuration;
        mNextFireTime = aNextFireTime;
        mErrorDesc    = aErrorDesc;
        mMisFired     = aIsMisfired;
    }

    public long getDuration()
    {
        return mDuration;
    }

    public Date getNextFireTime()
    {
        return mNextFireTime;
    }

    public String getErrorDesc()
    {
        return mErrorDesc;
    }

    public boolean isMisFired()
    {
        return mMisFired;
    }

    @Override
    public String toString()
    {
        return "JobExecutedData [mDuration=" + mDuration + ", mNextFireTime=" + mNextFireTime + ", mErrorDesc=" + mErrorDesc + ", mMisFired=" + mMisFired + ", getScheduleId()=" + getScheduleId()
                + ", getScheduleGroupId()=" + getScheduleGroupId() + ", getScheduleName()=" + getScheduleName() + ", getScheduledFireTime()=" + getScheduledFireTime() + ", getFiredTime()="
                + getFiredTime() + ", toString()=" + super.toString() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + "]";
    }

}