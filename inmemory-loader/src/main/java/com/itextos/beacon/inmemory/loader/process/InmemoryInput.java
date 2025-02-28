package com.itextos.beacon.inmemory.loader.process;

import com.itextos.beacon.commonlib.commondbpool.JndiInfo;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;

public class InmemoryInput
{

    private final InmemoryId mInmemoryId;
    private final String     mDescription;
    private final JndiInfo   mJNDIInfo;
    private final String     mSQL;
    private final boolean    isAutoRefreshRequired;
    private final int        mSleepSec;
    private final String     mInmemoryProcessClassName;

    public InmemoryInput(
            String aInmemoryId,
            String aDescription,
            int aJNDIInfoId,
            String aSQL,
            boolean aIsAutoRefreshRequired,
            int aSleepSec,
            String aInmemoryProcessClassName)
    {
        mInmemoryId               = InmemoryId.getInmemoryId(aInmemoryId);
        mDescription              = aDescription;
        mJNDIInfo                 = JndiInfoHolder.getInstance().getJndiInfo(aJNDIInfoId);
        mSQL                      = aSQL;
        isAutoRefreshRequired     = aIsAutoRefreshRequired;
        mSleepSec                 = aSleepSec;
        mInmemoryProcessClassName = aInmemoryProcessClassName;
    }

    public InmemoryId getInmemoryId()
    {
        return mInmemoryId;
    }

    public String getDescription()
    {
        return mDescription;
    }

    public JndiInfo getJNDIInfo()
    {
        return mJNDIInfo;
    }

    public String getSQL()
    {
        return mSQL;
    }

    public int getSleepSec()
    {
        return mSleepSec;
    }

    public String getInmemoryProcessClassName()
    {
        return mInmemoryProcessClassName;
    }

    public boolean isAutoRefreshRequired()
    {
        return isAutoRefreshRequired;
    }

    @Override
    public String toString()
    {
        return "InmemoryInput [mInmemoryId=" + mInmemoryId + ", mDescription=" + mDescription + ", mJNDIInfo=" + mJNDIInfo + ", mSQL=" + mSQL + ", isAutoRefreshRequired=" + isAutoRefreshRequired
                + ", mSleepSec=" + mSleepSec + ", mInmemoryProcessClassName=" + mInmemoryProcessClassName + "]";
    }

}