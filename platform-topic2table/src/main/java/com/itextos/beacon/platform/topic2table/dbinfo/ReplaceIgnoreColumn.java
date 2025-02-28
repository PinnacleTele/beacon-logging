package com.itextos.beacon.platform.topic2table.dbinfo;

public class ReplaceIgnoreColumn
{

    private final String  mActualColumn;
    private final String  mReplaceColumn;
    private final boolean mIgnore;

    public ReplaceIgnoreColumn(
            String aActualColumn,
            String aReplaceColumn,
            boolean aIgnore)
    {
        super();
        mActualColumn  = aActualColumn;
        mReplaceColumn = aReplaceColumn;
        mIgnore        = aIgnore;
    }

    public String getActualColumn()
    {
        return mActualColumn;
    }

    public String getReplaceColumn()
    {
        return mReplaceColumn;
    }

    public boolean isIgnore()
    {
        return mIgnore;
    }

    @Override
    public String toString()
    {
        return "ReplaceIgnoreColumn [mActualColumn=" + mActualColumn + ", mReplaceColumn=" + mReplaceColumn + ", mIgnore=" + mIgnore + "]";
    }

}