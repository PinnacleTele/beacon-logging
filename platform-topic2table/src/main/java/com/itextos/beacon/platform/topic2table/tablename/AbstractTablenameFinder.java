package com.itextos.beacon.platform.topic2table.tablename;

import java.util.Map;

import com.itextos.beacon.commonlib.commondbpool.JndiInfo;

public abstract class AbstractTablenameFinder
        implements
        ITablenameFinder
{

    protected JndiInfo            mDefaultJndiInfo;
    protected String              mDefaultTableName;
    protected Map<String, String> mInputValues;
    protected JndiInfo            mJndiInfo;
    protected String              mDbName;
    protected String              mTableName;

    @Override
    public void setInputValues(
            JndiInfo aJndiInfo,
            String aDatabaseName,
            String aTableName,
            Map<String, String> aKeyValuesFromMap)
    {
        mDefaultJndiInfo  = aJndiInfo;
        mJndiInfo         = aJndiInfo;
        mDbName           = aDatabaseName;
        mDefaultTableName = aTableName;
        mTableName        = aTableName;
        mInputValues      = aKeyValuesFromMap;
    }

    @Override
    public JndiInfo getJndiInfo()
    {
        return mJndiInfo;
    }

    @Override
    public String getTableName()
    {
        return mTableName;
    }

}