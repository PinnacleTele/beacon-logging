package com.itextos.beacon.platform.topic2table.dbinfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.commondbpool.JndiInfo;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class TableInserterInfo
{

    private static final String                    IGNORE_COLUMNS = "-1";
    private final Table2DBInserterId               mTableInserterId;
    private final JndiInfo                         mJndiInfo;
    private final String                           mDatabaseName;
    private final String                           mTableName;
    private final String                           mTableNameFinderClass;
    private final String[]                         mTableNameFinderKeys;
    private final Map<String, ReplaceIgnoreColumn> mReplaceorIgnoreColumns;
    private final int                              mSleepSecs;
    private final int                              mBatchSize;

    public TableInserterInfo(
            Table2DBInserterId aTableInserterId,
            int aJndiInfoName,
            String aDatabaseName,
            String aTableName,
            String aTableNameFinderClass,
            String aTableNameFinderKeys,
            String aReplaceorIgnoreColumns,
            int aSleepSec,
            int aBatchSize)
    {
        super();
        mTableInserterId        = aTableInserterId;
        mJndiInfo               = JndiInfoHolder.getInstance().getJndiInfo(aJndiInfoName);
        mDatabaseName           = aDatabaseName;
        mTableName              = aTableName;
        mTableNameFinderClass   = aTableNameFinderClass;
        mTableNameFinderKeys    = CommonUtility.split(aTableNameFinderKeys, ',');
        mReplaceorIgnoreColumns = getReplaceIgnoreColumns(aReplaceorIgnoreColumns);
        mSleepSecs              = aSleepSec;
        mBatchSize              = aBatchSize;
    }

    public Table2DBInserterId getTableInserterId()
    {
        return mTableInserterId;
    }

    public JndiInfo getJndiInfo()
    {
        return mJndiInfo;
    }

    public String getDatabaseName()
    {
        return mDatabaseName;
    }

    public String getTableName()
    {
        return mTableName;
    }

    public String getTableNameFinderClass()
    {
        return mTableNameFinderClass;
    }

    public boolean isStaticTableInserter()
    {
        return mTableNameFinderClass.isBlank();
    }

    public String[] getTableNameFinderKeys()
    {
        return mTableNameFinderKeys;
    }

    public Map<String, ReplaceIgnoreColumn> getReplaceorIgnoreColumns()
    {
        return mReplaceorIgnoreColumns;
    }

    public int getSleepSecs()
    {
        return mSleepSecs;
    }

    public int getBatchSize()
    {
        return mBatchSize;
    }

    @Override
    public String toString()
    {
        return "TableInserterInfo [mTableInserterId=" + mTableInserterId + ", mJndiInfo=" + mJndiInfo + ", mDatabaseName=" + mDatabaseName + ", mTableName=" + mTableName + ", mTableNameFinderClass="
                + mTableNameFinderClass + ", mTableNameFinderKeys=" + Arrays.toString(mTableNameFinderKeys) + ", mReplaceorIgnoreColumns=" + mReplaceorIgnoreColumns + ", mSleepSecs=" + mSleepSecs
                + ", mBatchSize=" + mBatchSize + "]";
    }

    private static Map<String, ReplaceIgnoreColumn> getReplaceIgnoreColumns(
            String aReplaceorIgnoreColumns)
    {
        final Map<String, ReplaceIgnoreColumn> returnValue = new HashMap<>();
        final String                           temp        = CommonUtility.nullCheck(aReplaceorIgnoreColumns, true);

        if (!temp.isEmpty())
        {
            final String[] colSplit = CommonUtility.split(temp, ':');

            for (final String s : colSplit)
            {
                final String[] valueSplit    = CommonUtility.split(s, '=');
                final String   actualColumn  = valueSplit[0];
                final String   replaceColumn = valueSplit[1];
                final boolean  isIgnore      = IGNORE_COLUMNS.contentEquals(replaceColumn);
                returnValue.put(actualColumn, new ReplaceIgnoreColumn(actualColumn, isIgnore ? actualColumn : replaceColumn, isIgnore));
            }
        }
        return returnValue;
    }

}
