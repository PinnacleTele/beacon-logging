package com.itextos.beacon.commonlib.datarefresher.dataobjects;

public class DataRefresherMasterData
{

    private final String mTableName;
    private final String mPrimaryColumnName;
    private final String mProcessClassName;

    public DataRefresherMasterData(
            String aTableName,
            String aPrimaryColumnName,
            String aProcessClassName)
    {
        super();
        mTableName         = aTableName;
        mPrimaryColumnName = aPrimaryColumnName;
        mProcessClassName  = aProcessClassName;
    }

    public String getTableName()
    {
        return mTableName;
    }

    public String getPrimaryColumnName()
    {
        return mPrimaryColumnName;
    }

    public String getProcessClassName()
    {
        return mProcessClassName;
    }

    @Override
    public String toString()
    {
        return "DataRefresherMasterData [mTableName=" + mTableName + ", mPrimaryColumnName=" + mPrimaryColumnName + ", mProcessClassName=" + mProcessClassName + "]";
    }

}