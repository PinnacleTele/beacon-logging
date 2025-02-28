package com.itextos.beacon.commonlib.scheduler.config;

public class ParamInfo
{

    private final String   mScheduleId;
    private final String   mParamName;
    private final String   mParamValue;
    private final DataType mDataType;
    private final String   mDateTimeFormat;

    public ParamInfo(
            String aScheduleId,
            String aParamName,
            String aParamValue,
            DataType aDataType,
            String aDateTimeFormat)
    {
        super();
        mScheduleId     = aScheduleId;
        mParamName      = aParamName;
        mParamValue     = aParamValue;
        mDataType       = aDataType;
        mDateTimeFormat = aDateTimeFormat;
    }

    public String getScheduleId()
    {
        return mScheduleId;
    }

    public String getParamName()
    {
        return mParamName;
    }

    public String getParamValue()
    {
        return mParamValue;
    }

    public DataType getDataType()
    {
        return mDataType;
    }

    public String getDateTimeFormat()
    {
        return mDateTimeFormat;
    }

    @Override
    public String toString()
    {
        return "ParamInfo [mScheduleId=" + mScheduleId + ", mParamName=" + mParamName + ", mParamValue=" + mParamValue + ", mDataType=" + mDataType + ", mDateTimeFormat=" + mDateTimeFormat + "]";
    }

}