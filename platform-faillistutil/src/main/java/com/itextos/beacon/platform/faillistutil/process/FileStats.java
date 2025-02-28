package com.itextos.beacon.platform.faillistutil.process;

public class FileStats
{

    private final String mFileName;
    private long         mTotalRecords          = 0;
    private long         mValidRecords          = 0;
    private long         mInvalidRecords        = 0;
    private long         mInsertCount           = 0;
    private long         mUpdateCount           = 0;
    private long         mDeleteCount           = 0;
    private long         mDeleteFailCount       = 0;
    private long         mInvalidOperationCount = 0;

    private long         mProcessStartTime      = 0;
    private long         mProcessEndTime        = 0;

    public FileStats(
            String aFileName)
    {
        super();
        mFileName = aFileName;
    }

    public String getFileName()
    {
        return mFileName;
    }

    public long getTotalRecords()
    {
        return mTotalRecords;
    }

    public long getValidRecords()
    {
        return mValidRecords;
    }

    public long getInvalidRecords()
    {
        return mInvalidRecords;
    }

    public long getInsertCount()
    {
        return mInsertCount;
    }

    public long getUpdateCount()
    {
        return mUpdateCount;
    }

    public long getDeleteCount()
    {
        return mDeleteCount;
    }

    public long getDeleteFailCount()
    {
        return mDeleteFailCount;
    }

    public long getInvalidOperationCount()
    {
        return mInvalidOperationCount;
    }

    public void addTotalRecords(
            long aIncrementCount)
    {
        mTotalRecords += aIncrementCount;
    }

    public void addValidRecords(
            long aIncrementCount)
    {
        mValidRecords += aIncrementCount;
    }

    public void addInvalidRecords(
            long aIncrementCount)
    {
        mInvalidRecords += aIncrementCount;
    }

    public void addInsertCount(
            long aIncrementCount)
    {
        mInsertCount += aIncrementCount;
    }

    public void addUpdateCount(
            long aIncrementCount)
    {
        mUpdateCount += aIncrementCount;
    }

    public void addDeleteCount(
            long aIncrementCount)
    {
        mDeleteCount += aIncrementCount;
    }

    public void addDeleteFailCount(
            long aIncrementCount)
    {
        mDeleteFailCount += aIncrementCount;
    }

    public void addInvalidOperationCount(
            long aIncrementCount)
    {
        mInvalidOperationCount += aIncrementCount;
    }

    public long getProcessStartTime()
    {
        return mProcessStartTime;
    }

    public void setProcessStartTime(
            long aProcessStartTime)
    {
        mProcessStartTime = aProcessStartTime;
    }

    public long getProcessEndTime()
    {
        return mProcessEndTime;
    }

    public void setProcessEndTime(
            long aProcessEndTime)
    {
        mProcessEndTime = aProcessEndTime;
    }

    @Override
    public String toString()
    {
        return "FileStatistics [mFileName=" + mFileName + ", mTotalRecords=" + mTotalRecords + ", mValidRecords=" + mValidRecords + ", mInvalidRecords=" + mInvalidRecords + ", mInsertCount="
                + mInsertCount + ", mUpdateCount=" + mUpdateCount + ", mDeleteCount=" + mDeleteCount + ", mDeleteFailCount=" + mDeleteFailCount + ", mInvalidOperationCount=" + mInvalidOperationCount
                + ", mProcessStartTime=" + mProcessStartTime + ", mProcessEndTime=" + mProcessEndTime + "]";
    }

}