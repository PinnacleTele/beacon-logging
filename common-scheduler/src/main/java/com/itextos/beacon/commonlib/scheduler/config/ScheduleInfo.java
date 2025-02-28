package com.itextos.beacon.commonlib.scheduler.config;

import java.util.ArrayList;
import java.util.List;

public class ScheduleInfo
{

    private final String             mScheduleId;
    private final String             mScheduleGroupId;
    private final String             mScheduleName;
    private final String             mScheduleJobClassName;
    private final String             mCronExpression;
    private final ScheduleState      mScheduleState;
    private final MisfireInstruction mMisfireInstruction;
    private final List<ParamInfo>    mParamList = new ArrayList<>();

    public ScheduleInfo(
            String aScheduleId,
            String aScheduleGroupId,
            String aScheduleName,
            String aScheduleJobClassName,
            String aCronExpression,
            ScheduleState aScheduleState,
            MisfireInstruction aMisfireInstruction)
    {
        super();
        mScheduleId           = aScheduleId;
        mScheduleGroupId      = aScheduleGroupId;
        mScheduleName         = aScheduleName;
        mScheduleJobClassName = aScheduleJobClassName;
        mCronExpression       = aCronExpression;
        mScheduleState        = aScheduleState;
        mMisfireInstruction   = aMisfireInstruction;
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

    public String getScheduleJobClassName()
    {
        return mScheduleJobClassName;
    }

    public String getCronExpression()
    {
        return mCronExpression;
    }

    public ScheduleState getScheduleState()
    {
        return mScheduleState;
    }

    public MisfireInstruction getMisfireInstruction()
    {
        return mMisfireInstruction;
    }

    public void addParamInfo(
            ParamInfo aParamInfo)
    {
        if (aParamInfo != null)
            mParamList.add(aParamInfo);
    }

    public List<ParamInfo> getParamInfoList()
    {
        return mParamList;
    }

    @Override
    public String toString()
    {
        return "ScheduleInfo [mScheduleId=" + mScheduleId + ", mScheduleGroupId=" + mScheduleGroupId + ", mScheduleName=" + mScheduleName + ", mScheduleJobClassName=" + mScheduleJobClassName
                + ", mCronExpression=" + mCronExpression + ", mScheduleState=" + mScheduleState + ", mMisfireInstruction=" + mMisfireInstruction + ", mParamList=" + mParamList + "]";
    }

}