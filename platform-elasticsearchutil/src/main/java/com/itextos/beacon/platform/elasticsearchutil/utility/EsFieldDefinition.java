package com.itextos.beacon.platform.elasticsearchutil.utility;

import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.platform.elasticsearchutil.types.EsFieldDataType;

public class EsFieldDefinition
{

    private final MiddlewareConstant mFieldName;
    private final EsFieldDataType    mDataType;
    private final String             mDateFormat;

    public EsFieldDefinition(
            MiddlewareConstant aFieldName,
            EsFieldDataType aDataType)
    {
        this(aFieldName, aDataType, null);
    }

    public EsFieldDefinition(
            MiddlewareConstant aFieldName,
            EsFieldDataType aDataType,
            String aDateFormat)
    {
        super();
        mFieldName  = aFieldName;
        mDataType   = aDataType;
        mDateFormat = aDateFormat;
    }

    public MiddlewareConstant getFieldName()
    {
        return mFieldName;
    }

    public EsFieldDataType getDataType()
    {
        return mDataType;
    }

    public String getDateFormat()
    {
        return mDateFormat;
    }

    @Override
    public String toString()
    {
        return "EsFieldDefinition [mFieldName=" + mFieldName + ", mDataType=" + mDataType + ", mDateFormat=" + mDateFormat + "]";
    }

}